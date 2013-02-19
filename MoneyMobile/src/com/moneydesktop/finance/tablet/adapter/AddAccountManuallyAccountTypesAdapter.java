package com.moneydesktop.finance.tablet.adapter;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.tablet.adapter.AddNewInstitutionAdapter.AddInstitutionListHolder;
import com.moneydesktop.finance.util.Fonts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AddAccountManuallyAccountTypesAdapter extends ArrayAdapter<AccountType> {

	private Context mContext;
	private int mLayoutId;
	private List<AccountType> mAccountTypesList;
	
	public AddAccountManuallyAccountTypesAdapter(Context context, int layoutResourceId, List<AccountType> accountTypes) {
		super(context, layoutResourceId, accountTypes);
	
		mContext = context;
		mLayoutId = layoutResourceId;
		mAccountTypesList = accountTypes;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AccountTypesHolder holder = new AccountTypesHolder();
		
		//if (convertView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(mLayoutId, parent, false);
						
			holder.accountTypeImg = (ImageView)convertView.findViewById(R.id.image);
			holder.txtTitle = (TextView)convertView.findViewById(R.id.tablet_add_bank_manually_account_type_name_list_item);			
			
			if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("CASH")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_cash));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("CHECKING")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_checking));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("CREDIT CARD")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_credit));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("INVESTMENTS")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_investments));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("LINE OF CREDIT")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_credit));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("LOANS")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_loans));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("MORTGAGE")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_mortgage));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("PROPERTY")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_property));
			} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("SAVINGS")) {
				holder.accountTypeImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tablet_accounts_icon_accounttype_savings));
			}
			
			
			holder.txtTitle.setText(mAccountTypesList.get(position).getAccountTypeName());
			Fonts.applyPrimaryBoldFont(holder.txtTitle, 14);
			
	//	} 
		
		return convertView;
	}
	
	
    static class AccountTypesHolder
    {
    	ImageView accountTypeImg;
        TextView txtTitle;
    }

	
	
}