package com.moneydesktop.finance.views;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


public class AccountTypeGroupView extends FrameLayout {
    
    private TextView mAccountTypeName;
    private TextView mIndicator;
    private Context mContext;
    private AccountType mAccountType;

    
    public AccountTypeGroupView (Context context, AccountType accountType, final boolean isGroupExpanded) {
        super(context);
        mContext = context;
        mAccountType = accountType;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.account_type_group, this, true);
        mAccountTypeName = (TextView)view.findViewById(R.id.account_type_group_name);
        mIndicator = (TextView)view.findViewById(R.id.account_type_group_indicator);


        populateView(isGroupExpanded);
    }

    private void populateView (boolean isGroupExpanded) {

        mAccountTypeName.setText(mAccountType.getAccountTypeName()); //get the account name (Checking, savings, etc)

        if (isGroupExpanded) {
            mIndicator.setText(mContext.getString(R.string.account_types_indicator_hide));
        } else {
            mIndicator.setText(mContext.getString(R.string.account_types_indicator_show));
        }

    }

    public AccountTypeGroupView (Context context) {
        super(context);
    }
}
