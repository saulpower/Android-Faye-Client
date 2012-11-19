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
import android.widget.TextView;
import android.widget.Toast;


public class AccountTypeChildView extends FrameLayout {

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
        	final TextView bankName = (TextView)view.findViewById(R.id.account_bank_name);
        	
        	bankName.setText(account.getInstitutionId());
        
        	bankName.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(mContext, "Bank Account " + ((TextView)v.findViewById(R.id.account_bank_name)).getText() + " was clicked!", Toast.LENGTH_SHORT).show();					
				}
			});

            mBankAccountContainer.addView(view);
        }
    }
    
    private View createChildView () {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.bank_account, null);
    }
}
