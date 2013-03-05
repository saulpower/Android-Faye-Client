package com.moneydesktop.finance.shared.adapter;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.util.Fonts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectPropertyTypesAdapter extends ArrayAdapter<AccountType> {

	private Context mContext;
	private int mLayoutId;
	private List<AccountType> mAccountTypesList;
	
	public SelectPropertyTypesAdapter(Context context, int layoutResourceId, List<AccountType> accountTypes) {
		super(context, layoutResourceId, accountTypes);
	
		mContext = context;
		mLayoutId = layoutResourceId;
		mAccountTypesList = accountTypes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AccountTypesHolder holder = new AccountTypesHolder();
		
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		convertView = inflater.inflate(mLayoutId, parent, false);
					
		holder.accountTypeImg = (TextView)convertView.findViewById(R.id.txt_image);
		holder.txtTitle = (TextView)convertView.findViewById(R.id.select_property_type_name_list_item);			
		
		if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("REAL ESTATE")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_real_estate));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("VEHICLE")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_vehicle));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("ART")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_art));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("JEWELRY")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_jewelry));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("FURNITURE")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_furniture));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("APPLIANCES")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_appliances));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("COMPUTER")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_computer));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("ELECTRONICS")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_electronics));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("SPORTS EQUIPMENT")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_sports_equipment));
		} else if (mAccountTypesList.get(position).getAccountTypeName().toUpperCase().equals("MISCELLANEOUS")) {
			holder.accountTypeImg.setText(mContext.getString(R.string.icon_miscellaneous));
		}
		
		
		holder.txtTitle.setText(mAccountTypesList.get(position).getAccountTypeName());
		Fonts.applyGlyphFont(holder.accountTypeImg, 22);
		Fonts.applyPrimaryBoldFont(holder.txtTitle, 14);
			
		
		return convertView;
	}
	
	
    static class AccountTypesHolder
    {
    	TextView accountTypeImg;
        TextView txtTitle;
    }

	
	
}