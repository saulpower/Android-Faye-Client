package com.moneydesktop.finance.views;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.model.EventMessage.BankStatusUpdateEvent;
import com.moneydesktop.finance.model.EventMessage.RefreshAccountEvent;
import com.moneydesktop.finance.model.EventMessage.ReloadBannersEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.Enums.BankRefreshStatus;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class AccountTypeChildView extends FrameLayout {

	private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
    private View mChildView;
    private Context mContext;
    private List<BankAccount> mBankAccounts;
    private LinearLayout mBankAccountContainer;
    private View mParent;
    private Handler mHandler; 
    private ImageView mStatus;
    
    public AccountTypeChildView (Context context, List<BankAccount> bankAccounts, View parent) {
        super(context);
        mContext = context;
        mBankAccounts = bankAccounts;
        mParent = parent;
        
        mHandler = new Handler();
        EventBus.getDefault().register(this);

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChildView = inflater.inflate(R.layout.account_type_child, this, true);
        mBankAccountContainer = (LinearLayout) mChildView.findViewById(R.id.account_type_bank_container);
        populateView();
    }

    private void populateView () {
        for (final BankAccount account : mBankAccounts) {
        	final View view = createChildView();
        	
        	mStatus = (ImageView)view.findViewById(R.id.account_type_child_banner);
        	final TextView accountName = (TextView)view.findViewById(R.id.tablet_account_type_bank_name);
        	final TextView accountSum = (TextView)view.findViewById(R.id.tablet_account_type_bank_sum);
        	ImageView bankLogo = (ImageView)view.findViewById(R.id.tablet_account_type_bank_logo);
        	
        	accountName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        	accountName.setText(account.getAccountName() == null ? "" : account.getAccountName());
        	accountSum.setText(account.getBalance() == null ? "" : mFormatter.format(account.getBalance()));
        	BankLogoManager.getBankImage(bankLogo, account.getInstitutionId());

        	view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					
					List<OnClickListener> onClickListeners = new ArrayList<View.OnClickListener>();
					
					onClickListeners.add(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "VIEW ALL TXNS", Toast.LENGTH_SHORT).show();
//						    Intent i = new Intent(mActivity, PopupTabletActivity.class);
//					        i.putExtra("fragment", 1);
//					        i.putExtra("accountNumber", account.getAccountId());
//					        i.putExtra("txnType", TxFilter.ALL);
//					        mContext.startActivity(i);
//					        mActivity.overridePendingTransition(R.anim.in_down, R.anim.none);
						}
					});
					
					onClickListeners.add(new OnClickListener() { 	
						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "VIEW UNCLEARED TXNS", Toast.LENGTH_SHORT).show();
//	                        Intent i = new Intent(mActivity, PopupTabletActivity.class);
//	                        i.putExtra("fragment", 1);
//	                        i.putExtra("accountNumber", account.getAccountId());
//	                        i.putExtra("txnType", TxFilter.UNCLEARED);
//	                        mContext.startActivity(i);
//	                        mActivity.overridePendingTransition(R.anim.in_down, R.anim.none);
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
					
				    new PopupWindowAtLocation(mContext, parentView, (int)view.getLeft() + view.getWidth(), (int)mParent.getTop() + (int)UiUtils.convertDpToPixel(62, mContext), 
							mContext.getResources().getStringArray(R.array.account_selection_popup), onClickListeners, view);
				}
			});

            mBankAccountContainer.addView(view);
        }
    }
    
    private View createChildView () {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.account_type_child_bank, null);
    }
    
    public void onEvent(BankStatusUpdateEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {                
                if (mStatus != null) {
                    updateChildBanners(false);
                }
            }
        });
    }
    
    public void onEvent(final SyncEvent event) {
        
        mHandler.post(new Runnable() {
            @Override
            public void run() {                
                if (event.isFinished()) {
                    updateChildBanners(false);
                } else {
                    updateChildBanners(true);
                }
            }
        });        
    }
    
    public void onEvent(final RefreshAccountEvent event) {
        
        mHandler.post(new Runnable() {
            @Override
            public void run() {         
                updateSpecificChildBanner(event.getRefreshedBank());
            }
        });        
    }
    
    public void onEvent(final ReloadBannersEvent event) {
        
        mHandler.post(new Runnable() {
            @Override
            public void run() {         
                updateChildBanners(false);
            }
        });        
    }
    
    private void updateChildBanners(Boolean forceBanner) {
        int iterator = 0;
        for (final BankAccount account : mBankAccounts) {
            if (mContext != null){
                
                View view = mBankAccountContainer.getChildAt(iterator);
                mStatus = (ImageView)view.findViewById(R.id.account_type_child_banner);
                
                if (forceBanner){
                    mStatus.setVisibility(View.VISIBLE);
                    mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
                } else {
                    mStatus.setVisibility(View.VISIBLE);
                    if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_SUCCEEDED.index()) { //3
                        mStatus.setVisibility(View.GONE);
                        
                    } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_PENDING.index()) { //1
                        mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
                        
                    } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_LOGIN_FAILED.index()) { //5
                        mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
                        
                    } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_EXCEPTION.index()) { //4
                        mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
                        
                    } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_PROCESSING.index()) { //2
                        mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
                        
                    } else {
                        mStatus.setVisibility(View.GONE);
                    }             
                }
            }   
            iterator++;
        }
    }
    
    private void updateSpecificChildBanner(Bank bank) {
        int iterator = 0;
        for (final BankAccount account : mBankAccounts) {
            if (bank.getBankName().equals(account.getBank().getBankName())){
                
                View view = mBankAccountContainer.getChildAt(iterator);
                mStatus = (ImageView)view.findViewById(R.id.account_type_child_banner);
                
                mStatus.setVisibility(View.VISIBLE);
                mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));         
                
            }   
            iterator++;
        }
    }
   
}