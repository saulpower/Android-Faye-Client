package com.moneydesktop.finance.views;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.util.UiUtils;


public class AccountTypeChildView extends FrameLayout {

	private NumberFormat formatter = NumberFormat.getCurrencyInstance();
    private View mChildView;
    private Context mContext;
    private List<BankAccount> mBankAccounts;
    private LinearLayout mBankAccountContainer;
    private ExpandableListView mExpandableListView;
    private int mGroupPosition;
    
    public AccountTypeChildView (Context context, List<BankAccount> bankAccounts, ExpandableListView expandableListView, int groupPosition) {
        super(context);
        mContext = context;
        mBankAccounts = bankAccounts;
        mExpandableListView = expandableListView;
        mGroupPosition = groupPosition;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChildView = inflater.inflate(R.layout.account_type_child, this, true);
        mBankAccountContainer = (LinearLayout) mChildView.findViewById(R.id.account_type_bank_container);
        populateView();
    }

    private void populateView () {
        for (final BankAccount account : mBankAccounts) {        	
        	final View view = createChildView();
        	final TextView accountName = (TextView)view.findViewById(R.id.tablet_account_type_bank_name);
        	final TextView accountSum = (TextView)view.findViewById(R.id.tablet_account_type_bank_sum);
        	ImageView bankLogo = (ImageView)view.findViewById(R.id.tablet_account_type_bank_logo);
        		
        	accountName.setText(account.getAccountName() == null ? "" : account.getAccountName());
        	accountSum.setText(account.getBalance() == null ? "" : formatter.format(account.getBalance()));
        	BankLogoManager.getBankImage(bankLogo, account.getInstitutionId());
        	
        	
        	view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					
					List<OnClickListener> onClickListeners = new ArrayList<View.OnClickListener>();
					
					onClickListeners.add(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "VIEW ALL TXNS", Toast.LENGTH_SHORT).show();
						}
					});
					
					onClickListeners.add(new OnClickListener() { 	
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "VIEW UNCLEARED TXNS", Toast.LENGTH_SHORT).show();
						}
					});
					
					onClickListeners.add(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "EDIT SETTINGS", Toast.LENGTH_SHORT).show();
						}
					});
					
					onClickListeners.add(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "SHOW/HIDE DATA", Toast.LENGTH_SHORT).show();
						}
					});
					
					onClickListeners.add(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "DELETE THIS ACCOUNT", Toast.LENGTH_SHORT).show();
						}
					});
					
					RelativeLayout parentView = (RelativeLayout)((Activity)mContext).findViewById(R.id.account_types_container);
					
					new PopupWindowAtLocation(mContext, parentView, (int)view.getLeft() + view.getWidth(), (int)mChildView.getTop() + 10, 
							mContext.getResources().getStringArray(R.array.account_selection_popup), onClickListeners);
					
					
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
