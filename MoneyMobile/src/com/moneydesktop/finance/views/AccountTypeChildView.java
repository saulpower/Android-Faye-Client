package com.moneydesktop.finance.views;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.BankAccount;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class AccountTypeChildView extends FrameLayout {

    private View mView;
    private RelativeLayout mBankAccountLayout;
    private Context mContext;
    private List<BankAccount> mBankAccounts;
    private LinearLayout mBankAccountContainer;


    public AccountTypeChildView (Context context, List<BankAccount> bankAccounts) {
        super(context);
        mContext = context;
        mBankAccounts = bankAccounts;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.account_type_child, this, true);
        mBankAccountLayout = (RelativeLayout) findViewById(R.layout.bank_account);
        mBankAccountContainer = (LinearLayout) findViewById(R.id.account_type_bank_container);
        populateView();
    }

    private void populateView () {
        for (BankAccount account : mBankAccounts) {
            //mBankAccount.setImage();
        	mBankAccountLayout.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Toast.makeText(mContext, "Bank Account " + mBankAccounts.get(0) + " was clicked!", Toast.LENGTH_SHORT).show();					
				}
			});

            mBankAccountContainer.addView(mBankAccountLayout);
        }
    }

    public AccountTypeChildView (Context context) {
        super(context);
    }
}
