package com.moneydesktop.finance.tablet.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AmazingAdapter;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.CaretView;
import com.moneydesktop.finance.views.VerticalTextView;

import org.apache.commons.lang.WordUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsTabletAdapter extends AmazingAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

	private List<Transactions> mAllTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>> mBackgroundTask;
    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("M/d/yy");
	private AmazingListView mListView;
	
	private Date mStart, mEnd;
	
	private String mOrderBy = Constant.FIELD_DATE, mDirection = Constant.ORDER_DESC;
	private String mSearch = "%";

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
	
	public void setDateRange(Date start, Date end) {
	    mStart = start;
	    mEnd = end;
	}
	
	public void setOrder(String orderBy, String direction) {
	    mOrderBy = orderBy;
	    mDirection = direction;
	}

	public void setSearch(String search) {
        this.mSearch = search;
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

				Pair<Boolean, List<Transactions>> rows;
				
				if (mStart == null || mEnd == null) {
				    rows = Transactions.getRows(page, mOrderBy, mDirection);
				} else {
				    rows = Transactions.getRows(page, mSearch, mStart, mEnd, mOrderBy, mDirection);
				}
				
				return rows;
			}

			@Override
			protected void onPostExecute(Pair<Boolean, List<Transactions>> rows) {

				if (isCancelled()) {
					return;
				}

				mAllTransactions.addAll(rows.second);

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
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
	    TransactionViewHolder viewHolder;
		View res = convertView;
		
		if (res == null) {
		    
			res = mActivity.getLayoutInflater().inflate(R.layout.tablet_transaction_item, null);
			fixDottedLine(res);

			viewHolder = new TransactionViewHolder();
			
	        viewHolder.newText = (VerticalTextView) res.findViewById(R.id.text_new);
	        viewHolder.date = (TextView) res.findViewById(R.id.date);
	        viewHolder.payee = (TextView) res.findViewById(R.id.payee);
	        viewHolder.category = (TextView) res.findViewById(R.id.category);
	        viewHolder.amount = (TextView) res.findViewById(R.id.amount);
	        viewHolder.type = (ImageView) res.findViewById(R.id.type);
	        viewHolder.caret = (CaretView) res.findViewById(R.id.caret);
	        
	        res.setTag(viewHolder);
	        
	        applyFonts(viewHolder);
	        
		} else {
		    
		    viewHolder = (TransactionViewHolder) res.getTag();
		}

		Transactions transactions = getItem(position);
		
		if (transactions != null) {
		
			viewHolder.date.setText(mDateFormatter.format(transactions.getDate()));
			viewHolder.payee.setText(WordUtils.capitalize(transactions.getTitle()));
			viewHolder.caret.setVisibility(transactions.isIncome() ? View.VISIBLE : View.GONE);
			
			int gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
			double value = transactions.getAmount();
			
			if (transactions.isIncome()) {
			    value = Math.abs(value);
			    gravity = Gravity.CENTER_VERTICAL|Gravity.LEFT;
			}
			
			viewHolder.amount.setText(mFormatter.format(value));
			viewHolder.amount.setGravity(gravity);
			
			if (transactions.getIsBusiness()) {
			    viewHolder.type.setImageResource(R.drawable.ipad_txndetail_icon_business_grey);
			}
			
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
            res.findViewById(R.id.dotted4).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}
	
	private void applyFonts(TransactionViewHolder viewHolder) {

        Fonts.applyPrimaryBoldFont(viewHolder.newText, 10);
        Fonts.applyPrimarySemiBoldFont(viewHolder.date, 12);
        Fonts.applyPrimarySemiBoldFont(viewHolder.payee, 12);
        Fonts.applyPrimarySemiBoldFont(viewHolder.category, 12);
        Fonts.applyPrimaryBoldFont(viewHolder.amount, 12);
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
