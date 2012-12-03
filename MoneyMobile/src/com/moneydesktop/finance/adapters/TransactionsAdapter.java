package com.moneydesktop.finance.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;

public class TransactionsAdapter extends AmazingAdapter {

	public final String TAG = "TransactionsAdapter";

	private List<Pair<String, List<Transactions>>> sections;
	private List<Transactions> allTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>> backgroundTask;
	private DecimalFormat formatter = new DecimalFormat("###,##0.00");
	private ColorStateList greenColor;
	private ColorStateList grayColor;
	private AmazingListView listView;

	private Activity activity;

	public TransactionsAdapter(Activity activity, AmazingListView listView, List<Pair<String, List<Transactions>>> sections) {
		this.activity = activity;
		this.listView = listView;
		this.sections = sections;

		for (Pair<String, List<Transactions>> pairs : sections)
			this.allTransactions.addAll(pairs.second);

		greenColor = activity.getResources().getColorStateList(R.drawable.green_to_white);
		grayColor = activity.getResources().getColorStateList(R.drawable.gray7_to_white);
	}

	public int getCount() {

		int res = 0;

		for (int i = 0; i < sections.size(); i++)
			res += sections.get(i).second.size();

		return res;
	}

	public Transactions getItem(int position) {
		
		return allTransactions.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {

		Log.i(TAG, "Got onNextPageRequested page = " + page);

		DialogUtils.showProgress(activity, activity.getString(R.string.loading));

		if (backgroundTask != null) {
			backgroundTask.cancel(false);
		}

		backgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>>() {

			@Override
			protected Pair<Boolean, List<Transactions>> doInBackground(Integer... params) {
				
				int page = params[0];

				Pair<Boolean, List<Transactions>> rows = Transactions.getRows(page);

				return rows;
			}

			@Override
			protected void onPostExecute(Pair<Boolean, List<Transactions>> rows) {

				if (isCancelled())
					return;

				allTransactions.addAll(rows.second);
				List<Pair<String, List<Transactions>>> grouped = Transactions.groupTransactions(allTransactions);
				
				sections.clear();
				sections.addAll(grouped);

				nextPage();

				if (rows.first) {
					notifyMayHaveMorePages();
				} else {
					notifyNoMorePages();
				}
				
				listView.requestLayout();
				DialogUtils.hideProgress();
			}

		}.execute(page);
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {

		if (displaySectionHeader) {

			view.findViewById(R.id.header).setVisibility(View.VISIBLE);
			TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
			Fonts.applyPrimaryBoldFont(lSectionTitle, 12);
			lSectionTitle.setText(getSections()[getSectionForPosition(position)]);

		} else {

			view.findViewById(R.id.header).setVisibility(View.GONE);
		}
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
		View res = convertView;

		if (res == null) {
			res = activity.getLayoutInflater().inflate(R.layout.item_transaction, null);
			fixDottedLine(res);
		}

		TextView title = (TextView) res.findViewById(R.id.title);
		TextView amount = (TextView) res.findViewById(R.id.amount);
		TextView category = (TextView) res.findViewById(R.id.category);
		TextView dollar = (TextView) res.findViewById(R.id.dollar_sign);

		Fonts.applyPrimaryBoldFont(amount, 18);
		Fonts.applyPrimaryFont(title, 18);
		Fonts.applyPrimaryFont(category, 12);
		Fonts.applySecondaryItalicFont(dollar, 10);

		Transactions transactions = getItem(position);
		
		if (transactions != null) {
		
			title.setText(WordUtils.capitalize(transactions.getTitle().toLowerCase()));
			amount.setText(formatter.format(transactions.getAmount()));
			amount.setTextColor(transactions.getRawAmount() < 0 ? greenColor : grayColor);
	
			if (transactions.getCategory() != null)
				category.setText(transactions.getCategory().getCategoryName());
		}

		return res;
	}
	
	@TargetApi(11)
	private void fixDottedLine(View res) {
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			
			res.findViewById(R.id.dotted1).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			res.findViewById(R.id.dotted2).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			res.findViewById(R.id.dotted3).setLayerType(View.LAYER_TYPE_SOFTWARE, null);	
		}
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {

		TextView sectionHeader = (TextView) header;
		sectionHeader.setText(getSections()[getSectionForPosition(position)]);
		Fonts.applyPrimaryBoldFont(sectionHeader, 12);
	}

	@Override
	public int getPositionForSection(int section) {

		if (section < 0)
			section = 0;
		if (section >= sections.size())
			section = sections.size() - 1;
		int c = 0;

		for (int i = 0; i < sections.size(); i++) {

			if (section == i)
				return c;

			c += sections.get(i).second.size();
		}

		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {

		int c = 0;

		for (int i = 0; i < sections.size(); i++) {

			if (position >= c && position < c + sections.get(i).second.size())
				return i;

			c += sections.get(i).second.size();
		}

		return -1;
	}

	@Override
	public String[] getSections() {

		String[] res = new String[sections.size()];

		for (int i = 0; i < sections.size(); i++)
			res[i] = sections.get(i).first;

		return res;
	}
}
