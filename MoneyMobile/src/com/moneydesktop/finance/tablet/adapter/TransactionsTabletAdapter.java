package com.moneydesktop.finance.tablet.adapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.CaretView;
import com.moneydesktop.finance.views.VerticalTextView;

public class TransactionsTabletAdapter extends TransactionsAdapter {
    
    public final String TAG = this.getClass().getSimpleName();
    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("M/d/yy");

	public TransactionsTabletAdapter(Activity activity, AmazingListView listView) {
		super(activity, listView);
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
	    TransactionViewHolder viewHolder;
		View res = convertView;
		
		if (res == null) {
		    
			res = mActivity.getLayoutInflater().inflate(R.layout.tablet_transaction_item, parent, false);

			viewHolder = new TransactionViewHolder();
			
	        viewHolder.newText = (VerticalTextView) res.findViewById(R.id.text_new);
	        viewHolder.date = (TextView) res.findViewById(R.id.date);
	        viewHolder.payee = (TextView) res.findViewById(R.id.payee);
	        viewHolder.category = (TextView) res.findViewById(R.id.category);
	        viewHolder.amount = (TextView) res.findViewById(R.id.amount);
	        viewHolder.type = (ImageView) res.findViewById(R.id.type);
            viewHolder.flag = (ImageView) res.findViewById(R.id.flag);
	        viewHolder.caret = (CaretView) res.findViewById(R.id.caret); 
	        
	        res.setTag(viewHolder);
	        
	        applyFonts(viewHolder);
	        
		} else {
		    
		    viewHolder = (TransactionViewHolder) res.getTag();
		}
		
		final Transactions transactions = getItem(position);
		
		if (transactions != null) {
		
			viewHolder.date.setText(mDateFormatter.format(transactions.getDate()));
			viewHolder.payee.setText(transactions.getCapitalizedTitle());
			viewHolder.caret.setVisibility(transactions.isIncome() ? View.VISIBLE : View.INVISIBLE);
			
			viewHolder.newText.setText(!transactions.getIsProcessed() ? "NEW" : "");
			viewHolder.newText.setBackgroundResource(!transactions.getIsProcessed() ? R.drawable.primary_to_white : R.color.gray1);
			
			viewHolder.amount.setText(mFormatter.format(transactions.normalizedAmount()));
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.amount.getLayoutParams();
			int[] rules = params.getRules();
			rules[RelativeLayout.ALIGN_PARENT_LEFT] = 0;
            rules[RelativeLayout.ALIGN_PARENT_RIGHT] = -1;
            
            if (transactions.getTransactionType() == 1) {
                rules[RelativeLayout.ALIGN_PARENT_LEFT] = -1;
                rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 0;
            }

			viewHolder.type.setImageResource(transactions.getIsBusiness() ? R.drawable.ipad_txndetail_icon_business_color : R.drawable.ipad_txndetail_icon_personal_grey);
			viewHolder.flag.setVisibility(transactions.getIsFlagged() ? View.VISIBLE : View.INVISIBLE);
			viewHolder.category.setText(transactions.getCategory() != null ? transactions.getCategory().getCategoryName() : "");
		}

		return res;
	}
	
	private void applyFonts(TransactionViewHolder viewHolder) {

        Fonts.applyPrimaryBoldFont(viewHolder.newText, 8);
        Fonts.applyPrimarySemiBoldFont(viewHolder.date, 10);
        Fonts.applyPrimarySemiBoldFont(viewHolder.payee, 10);
        Fonts.applyPrimarySemiBoldFont(viewHolder.category, 10);
        Fonts.applyPrimaryBoldFont(viewHolder.amount, 10);
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

    @Override
    protected boolean isSectionVisible(int position) {
        return true;
    }

    @Override
    protected boolean isPositionVisible(int position) {
        return true;
    }
}
