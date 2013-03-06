package com.moneydesktop.finance.handset.adapter;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.util.Fonts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AccountExclusionsAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private int mLayoutId;
	private String[] mExclusions;
	private List<AccountExclusionFlags> mExclusionFlags;
	private BankAccount mBankAccount;
	
	public AccountExclusionsAdapter(Context context, int layoutResourceId, String[] exclusions, List<AccountExclusionFlags> exclusionFlags, BankAccount bankAccount) {
		super(context, layoutResourceId, exclusions);
	
		mContext = context;
		mLayoutId = layoutResourceId;
		mExclusions = exclusions;
		mExclusionFlags = exclusionFlags;
		mBankAccount = bankAccount;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder = new ViewHolder();
		
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		convertView = inflater.inflate(mLayoutId, parent, false);

		holder.exclusionCheckbox = (CheckBox)convertView.findViewById(R.id.handset_exclude_checkbox);
		holder.txtTitle = (TextView)convertView.findViewById(R.id.handset_exclude_txt);			
		
		for (AccountExclusionFlags flag : mExclusionFlags) {
			if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST)) {
				if (mExclusions[position].equals(mContext.getString(R.string.handset_account_exclusion_from_transaction_lists))) {
					holder.exclusionCheckbox.setChecked(true);
				}
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS)) {
				if (mExclusions[position].equals(mContext.getString(R.string.handset_account_exclusion_from_reports_charts))) {
					holder.exclusionCheckbox.setChecked(true);
				}
			}  else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST)) {
				if (mExclusions[position].equals(mContext.getString(R.string.handset_account_exclusion_from_account_listings))) {
					holder.exclusionCheckbox.setChecked(true);
				}
			}  else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS)) {
				if (mExclusions[position].equals(mContext.getString(R.string.handset_account_exclusion_from_budgets))) {
					holder.exclusionCheckbox.setChecked(true);
				}
			}  else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME)) {
				if (mExclusions[position].equals(mContext.getString(R.string.handset_account_exclusion_from_transfers_in))) {
					holder.exclusionCheckbox.setChecked(true);
				}
			}  else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES)) {
				if (mExclusions[position].equals(mContext.getString(R.string.handset_account_exclusion_from_transfers_out))) {
					holder.exclusionCheckbox.setChecked(true);
				}
			}
		}
		
		holder.txtTitle.setText(mExclusions[position]);
	
		Fonts.applyPrimaryBoldFont(holder.txtTitle, 14);
					
		return convertView;
	}
	
    static class ViewHolder
    {
    	CheckBox exclusionCheckbox;
        TextView txtTitle;
    }
    
}