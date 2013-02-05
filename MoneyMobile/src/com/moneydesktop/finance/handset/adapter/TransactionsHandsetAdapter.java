package com.moneydesktop.finance.handset.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AmazingAdapter;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;

import org.apache.commons.lang.WordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionsHandsetAdapter extends AmazingAdapter {

	public final String TAG = "TransactionsAdapter";

	private List<Pair<String, List<Transactions>>> mSections;
	private List<Transactions> mAllTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>> mBackgroundTask;
	private DecimalFormat mFormatter = new DecimalFormat("###,##0.00");
	private ColorStateList mGreenColor;
	private ColorStateList mGrayColor;
	private AmazingListView mListView;
    private String mOrderBy = "DATE", mDirection = "DESC";

	private Activity mActivity;

	public TransactionsHandsetAdapter(Activity activity, AmazingListView listView, List<Pair<String, List<Transactions>>> sections) {
		this.mActivity = activity;
		this.mListView = listView;
		this.mSections = sections;

		for (Pair<String, List<Transactions>> pairs : sections)
			this.mAllTransactions.addAll(pairs.second);

		mGreenColor = activity.getResources().getColorStateList(R.drawable.green_to_white);
		mGrayColor = activity.getResources().getColorStateList(R.drawable.gray7_to_white);
	}

	public int getCount() {

		int res = 0;

		for (int i = 0; i < mSections.size(); i++)
			res += mSections.get(i).second.size();

		return res;
	}

	public Transactions getItem(int position) {
		
		return mAllTransactions.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {

		if (mBackgroundTask != null) {
			mBackgroundTask.cancel(false);
		}

		mBackgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>>() {

			@Override
			protected Pair<Boolean, List<Transactions>> doInBackground(Integer... params) {
				
				int page = params[0];

				Pair<Boolean, List<Transactions>> rows = Transactions.getRows(page, mOrderBy, mDirection);

				return rows;
			}

			@Override
			protected void onPostExecute(Pair<Boolean, List<Transactions>> rows) {

				if (isCancelled())
					return;

				mAllTransactions.addAll(rows.second);
				List<Pair<String, List<Transactions>>> grouped = Transactions.groupTransactions(mAllTransactions);
				
				mSections.clear();
				mSections.addAll(grouped);

				nextPage();

				if (rows.first) {
					notifyMayHaveMorePages();
				} else {
					notifyNoMorePages();
				}
				
				mListView.requestLayout();
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
		
	    TransactionViewHolder viewHolder;
		View res = convertView;

		if (res == null) {
		    
			res = mActivity.getLayoutInflater().inflate(R.layout.handset_item_transaction, null);
			fixDottedLine(res);
			
			viewHolder = new TransactionViewHolder();

			viewHolder.title = (TextView) res.findViewById(R.id.title);
			viewHolder.amount = (TextView) res.findViewById(R.id.amount);
			viewHolder.category = (TextView) res.findViewById(R.id.category);
			viewHolder.dollar = (TextView) res.findViewById(R.id.dollar_sign);
			
			applyFonts(viewHolder);
			
			res.setTag(viewHolder);
			
		} else {
		    
		    viewHolder = (TransactionViewHolder) res.getTag();
		}

		Transactions transactions = getItem(position);
		
		if (transactions != null) {
		
		    viewHolder.title.setText(WordUtils.capitalize(transactions.getTitle().toLowerCase()));
		    viewHolder.amount.setText(mFormatter.format(transactions.normalizedAmount()));
		    viewHolder.amount.setTextColor(transactions.getTransactionType() == 1 ? mGreenColor : mGrayColor);
	
			if (transactions.getCategory() != null) {
			    viewHolder.category.setText(transactions.getCategory().getCategoryName());
			}
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
	
	private void applyFonts(TransactionViewHolder viewHolder) {

        Fonts.applyPrimaryBoldFont(viewHolder.amount, 18);
        Fonts.applyPrimaryFont(viewHolder.title, 18);
        Fonts.applyPrimaryFont(viewHolder.category, 12);
        Fonts.applySecondaryItalicFont(viewHolder.dollar, 10);
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
		if (section >= mSections.size())
			section = mSections.size() - 1;
		int c = 0;

		for (int i = 0; i < mSections.size(); i++) {

			if (section == i)
				return c;

			c += mSections.get(i).second.size();
		}

		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {

		int c = 0;

		for (int i = 0; i < mSections.size(); i++) {

			if (position >= c && position < c + mSections.get(i).second.size())
				return i;

			c += mSections.get(i).second.size();
		}

		return -1;
	}

	@Override
	public String[] getSections() {

		String[] res = new String[mSections.size()];

		for (int i = 0; i < mSections.size(); i++)
			res[i] = mSections.get(i).first;

		return res;
	}

    @Override
    protected boolean isSectionVisible(int position) {
        return true;
    }

    @Override
    protected boolean isPositionVisible(int position) {
        return true;
    }
}
