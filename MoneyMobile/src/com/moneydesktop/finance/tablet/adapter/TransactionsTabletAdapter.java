package com.moneydesktop.finance.tablet.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AmazingAdapter;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.VerticalTextView;

import org.apache.commons.lang.WordUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionsTabletAdapter extends AmazingAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

	private List<Transactions> mAllTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>> mBackgroundTask;
    private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("M/d/yy");
	private AmazingListView mListView;

	private Activity mActivity;

	public TransactionsTabletAdapter(Activity activity, AmazingListView listView, List<Transactions> transactions) {
		this.mActivity = activity;
		this.mListView = listView;

		mAllTransactions = transactions;
	}

	public int getCount() {

		return mAllTransactions.size();
	}

	public Transactions getItem(int position) {
		
		return mAllTransactions.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {

		DialogUtils.showProgress(mActivity, mActivity.getString(R.string.loading));

		if (mBackgroundTask != null) {
			mBackgroundTask.cancel(false);
		}

		mBackgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>>() {

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

				mAllTransactions.addAll(rows.second);

				nextPage();

				if (rows.first) {
					notifyMayHaveMorePages();
				} else {
					notifyNoMorePages();
				}
				
				mListView.requestLayout();
				DialogUtils.hideProgress();
			}

		}.execute(page);
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
		View res = convertView;

		if (res == null) {
			res = mActivity.getLayoutInflater().inflate(R.layout.tablet_transaction_item, null);
			fixDottedLine(res);
		}

		VerticalTextView newText = (VerticalTextView) res.findViewById(R.id.text_new);
		TextView date = (TextView) res.findViewById(R.id.date);
        TextView payee = (TextView) res.findViewById(R.id.payee);
        TextView category = (TextView) res.findViewById(R.id.category);
		TextView amount = (TextView) res.findViewById(R.id.amount);
		
		Fonts.applyPrimaryBoldFont(newText, 10);
		Fonts.applyPrimarySemiBoldFont(date, 12);
        Fonts.applyPrimarySemiBoldFont(payee, 12);
		Fonts.applyPrimarySemiBoldFont(category, 12);
        Fonts.applyPrimaryBoldFont(amount, 12);

		Transactions transactions = getItem(position);
		
		if (transactions != null) {
		
			date.setText(mDateFormatter.format(transactions.getDate()));
			payee.setText(WordUtils.capitalize(transactions.getTitle()));
			amount.setText(mFormatter.format(transactions.getAmount()));
	
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
            res.findViewById(R.id.dotted4).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
	}

	@Override
	public int getPositionForSection(int section) {

		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {

		return 0;
	}

	@Override
	public String[] getSections() {

		return null;
	}
}
