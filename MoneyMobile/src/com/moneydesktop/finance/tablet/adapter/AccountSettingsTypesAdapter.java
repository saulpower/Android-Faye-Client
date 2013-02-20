package com.moneydesktop.finance.tablet.adapter;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AccountSettingsTypesAdapter extends ArrayAdapter<AccountType> {

	private Context mContext;
	private int mLayoutId;
	private List<AccountType> mAccountTypesList;
	
	public AccountSettingsTypesAdapter(Context context, int layoutResourceId, List<AccountType> accountTypes) {
		super(context, layoutResourceId, accountTypes);
	
		mContext = context;
		mLayoutId = layoutResourceId;
		mAccountTypesList = accountTypes;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		View view = inflater.inflate(mLayoutId, parent, false);
		
		AccountTypesHolder holder = new AccountTypesHolder();
		holder.txtTitle = (TextView)view.findViewById(R.id.account_type_settings_item);
		
		holder.txtTitle.setText(mAccountTypesList.get(position).getAccountTypeName());
		
		return view;
	}
	
	
    static class AccountTypesHolder
    {
        TextView txtTitle;
    }

	
	
}