package com.moneydesktop.finance.adapters;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.util.DialogUtils;

public class TransactionsAdapter extends AmazingAdapter {
	
	public final String TAG = "TransactionsAdapter";
	
	private List<Pair<String, List<Transactions>>> sections;
	private List<Transactions> allTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Pair<String, List<Transactions>>>>> backgroundTask;
	private NumberFormat formatter = NumberFormat.getCurrencyInstance();
	private ColorStateList greenColor;
	private ColorStateList grayColor;

	private Activity activity;
	
	public TransactionsAdapter(Activity activity, List<Pair<String, List<Transactions>>> sections) {
		this.activity = activity;
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
		
		Log.i(TAG, "Got onNextPageRequested page=" + page);
		
		DialogUtils.showProgress(activity, activity.getString(R.string.loading));
		
		if (backgroundTask != null) {
			backgroundTask.cancel(false);
		}
		
		backgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Pair<String, List<Transactions>>>>>() {
			
			@Override
			protected Pair<Boolean, List<Pair<String, List<Transactions>>>> doInBackground(Integer... params) {
				
				int page = params[0];
				
				Pair<Boolean, List<Transactions>> rows = Transactions.getRows(page);
				allTransactions.addAll(rows.second);
				
				List<Pair<String, List<Transactions>>> grouped = Transactions.groupTransactions(allTransactions);
				
				return new Pair<Boolean, List<Pair<String,List<Transactions>>>>(rows.first, grouped);
			}
			
			@Override
			protected void onPostExecute(Pair<Boolean, List<Pair<String, List<Transactions>>>> result) {
				
				if (isCancelled()) return; 
				
				sections.clear();
				sections.addAll(result.second);
				
				nextPage();
				notifyDataSetChanged();
				
				if (result.first) {
					notifyMayHaveMorePages();
				} else {
					notifyNoMorePages();
				}
				
				DialogUtils.hideProgress();
			};
			
		}.execute(page);
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
		
		if (displaySectionHeader) {
			
			view.findViewById(R.id.header).setVisibility(View.VISIBLE);
			TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
			lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
			
		} else {
			
			view.findViewById(R.id.header).setVisibility(View.GONE);
		}
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
		View res = convertView;
		
		if (res == null)
			res = activity.getLayoutInflater().inflate(R.layout.item_transaction, null);
		
		TextView title = (TextView) res.findViewById(R.id.title);
		TextView amount = (TextView) res.findViewById(R.id.amount);
		TextView category = (TextView) res.findViewById(R.id.category);
		
		Transactions transactions = getItem(position);
		title.setText(WordUtils.capitalize(transactions.getTitle().toLowerCase()));
		amount.setText(formatter.format(transactions.getAmount()));
		amount.setTextColor(transactions.getRawAmount() < 0 ? greenColor : grayColor);
		category.setText(transactions.getCategory().getCategoryName());
		
		return res;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		
		TextView sectionHeader = (TextView) header;
		sectionHeader.setText(getSections()[getSectionForPosition(position)]);
	}

	@Override
	public int getPositionForSection(int section) {
		
		if (section < 0) section = 0;
		if (section >= sections.size()) section = sections.size() - 1;
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
