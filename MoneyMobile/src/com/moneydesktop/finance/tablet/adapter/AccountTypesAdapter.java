package com.moneydesktop.finance.tablet.adapter;

import java.util.List;

import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.views.AccountTypeChildView;
import com.moneydesktop.finance.views.AccountTypeGroupView;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;


public class AccountTypesAdapter extends BaseExpandableListAdapter {

	private List<AccountType> mAccountTypes;
	private Context mContext;
	private ExpandableListView mExpandableListView;

	public AccountTypesAdapter (final List<AccountType> accountTypes,
								final Activity context,
								final ExpandableListView expandableListView) {
		mAccountTypes = accountTypes;
		mContext = context;
		mExpandableListView = expandableListView;
  	}



	public Object getGroup(int groupPosition) {
		return mAccountTypes.get(groupPosition);
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		return mAccountTypes.get(groupPosition).getBankAccounts().get(childPosition);
	}

	public int getGroupCount() {
		return mAccountTypes.size();
	}
	
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}


	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		AccountTypeGroupView view = new AccountTypeGroupView(mContext, mAccountTypes.get(groupPosition), mExpandableListView.isGroupExpanded(groupPosition));
		return view;
	}
	
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
      AccountTypeChildView accountTypeChildView = new AccountTypeChildView(mContext, mAccountTypes.get(groupPosition).getBankAccounts());
      return accountTypeChildView;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
