package com.moneydesktop.finance.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.views.AccountTypeGroupView;

import java.util.List;

public class AccountsExpandableListAdapter extends ArrayAdapter<AccountType>{

    List<AccountType> mAccountTypes;
    Context mContext;
    
    public AccountsExpandableListAdapter(Context context, int resource, int textViewResourceId, List<AccountType> objects) {
        super(context, resource, textViewResourceId, objects);
        mAccountTypes = objects;
        mContext = context;
        
    }
   

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AccountTypeGroupView view = new AccountTypeGroupView(mContext, mAccountTypes.get(position));
        return view;
    }

}
