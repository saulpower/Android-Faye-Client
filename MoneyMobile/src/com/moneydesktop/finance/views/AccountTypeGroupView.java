package com.moneydesktop.finance.views;

import java.text.NumberFormat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.BankAccount;

public class AccountTypeGroupView extends FrameLayout {
    
    private TextView mAccountTypeName;
    private Context mContext;
    private AccountType mAccountType;
    private TextView mAccountTypeSum;

    public AccountTypeGroupView (Context context, AccountType accountType) {
        super(context);
        mContext = context;
        mAccountType = accountType;
    
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);    
        View view = inflater.inflate(R.layout.account_type_group, this, true);

        mAccountTypeName = (TextView)view.findViewById(R.id.account_type_group_name);
        mAccountTypeSum = (TextView)view.findViewById(R.id.account_type_group_sum);
        
        populateView();
    }

    private void populateView () {

        mAccountTypeName.setText(mAccountType.getAccountTypeName()); //get the account name (Checking, savings, etc)
        double accountTypeSum = 0;
        
        for (BankAccount bankAccount : mAccountType.getBankAccounts()) {
        	accountTypeSum = accountTypeSum + bankAccount.getBalance();
        }
        
        String formatedSum = NumberFormat.getCurrencyInstance().format(accountTypeSum);
        
        mAccountTypeSum.setText(formatedSum);
    }
    
    public AccountTypeGroupView (Context context) {
        super(context);
    }
}
