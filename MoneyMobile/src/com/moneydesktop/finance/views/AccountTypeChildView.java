package com.moneydesktop.finance.views;

import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.BankAccount;


public class AccountTypeChildView extends FrameLayout {

	private NumberFormat formatter = NumberFormat.getCurrencyInstance();
    private View mChildView;
    private Context mContext;
    private List<BankAccount> mBankAccounts;
    private LinearLayout mBankAccountContainer;
    
    public AccountTypeChildView (Context context, List<BankAccount> bankAccounts) {
        super(context);
        mContext = context;
        mBankAccounts = bankAccounts;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChildView = inflater.inflate(R.layout.account_type_child, this, true);
        mBankAccountContainer = (LinearLayout) mChildView.findViewById(R.id.account_type_bank_container);
        populateView();
    }

    private void populateView () {
        for (final BankAccount account : mBankAccounts) {        	
        	View view = createChildView();
        	final TextView accountName = (TextView)view.findViewById(R.id.tablet_account_type_bank_name);
        	final TextView accountSum = (TextView)view.findViewById(R.id.tablet_account_type_bank_sum);
        		
        	accountName.setText(account.getAccountName() == null ? "" : account.getAccountName());
        	accountSum.setText(account.getBalance() == null ? "" : formatter.format(account.getBalance()));
        	
        	
        	accountName.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(mContext, "Account " + accountName.getText() + " was clicked!", Toast.LENGTH_SHORT).show();					
				}
			});

            mBankAccountContainer.addView(view);
        }
    }
    
    private View createChildView () {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.account_type_child_bank, null);
    }
}
