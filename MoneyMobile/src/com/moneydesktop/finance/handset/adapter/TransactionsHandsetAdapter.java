package com.moneydesktop.finance.handset.adapter;

import java.text.DecimalFormat;

import org.apache.commons.lang.WordUtils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;

public class TransactionsHandsetAdapter extends TransactionsAdapter {

	public final String TAG = "TransactionsAdapter";
	
	private DecimalFormat mFormatter = new DecimalFormat("###,##0.00");
	private ColorStateList mGreenColor;
	private ColorStateList mGrayColor;

	public TransactionsHandsetAdapter(Activity activity, AmazingListView listView) {
		super(activity, listView, true);

		mGreenColor = activity.getResources().getColorStateList(R.drawable.green_to_white);
		mGrayColor = activity.getResources().getColorStateList(R.drawable.gray7_to_white);
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {

		if (displaySectionHeader) {

			view.findViewById(R.id.header).setVisibility(View.VISIBLE);
			TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
			Fonts.applyPrimaryBoldFont(lSectionTitle, 8);
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
		    
			res = mActivity.getLayoutInflater().inflate(R.layout.handset_transaction_item, null);
			
			viewHolder = new TransactionViewHolder();

			viewHolder.root = (RelativeLayout) res.findViewById(R.id.item_root);
			viewHolder.cell = (LinearLayout) res.findViewById(R.id.cell);
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
		
			viewHolder.root.setBackgroundResource(transactions.getIsProcessed() ? R.color.gray2 : R.drawable.primary_to_white);
			viewHolder.cell.setBackgroundResource(transactions.getIsProcessed() ? R.drawable.gray1_to_gray3 : R.drawable.transaction_item);
			
			boolean income = transactions.getTransactionType() == 1;
		    viewHolder.title.setText(WordUtils.capitalize(transactions.getTitle().toLowerCase()));
		    viewHolder.amount.setText((income ? "(" : "") + mFormatter.format(transactions.normalizedAmount()) + (income ? ")" : ""));
		    viewHolder.amount.setTextColor(income ? mGreenColor : mGrayColor);
	
			if (transactions.getCategory() != null) {
			    viewHolder.category.setText(transactions.getCategory().getCategoryName());
			}
		}

		return res;
	}
	
	private void applyFonts(TransactionViewHolder viewHolder) {

        Fonts.applyPrimaryBoldFont(viewHolder.amount, 12);
        Fonts.applyPrimaryFont(viewHolder.title, 12);
        Fonts.applyPrimaryFont(viewHolder.category, 8);
        Fonts.applySecondaryItalicFont(viewHolder.dollar, 8);
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {

		TextView sectionHeader = (TextView) header;
		if (getSections().length > 0) {
			sectionHeader.setText(getSections()[getSectionForPosition(position)]);
		}
		Fonts.applyPrimaryBoldFont(sectionHeader, 8);
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

		return 0;
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
