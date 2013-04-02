package com.moneydesktop.finance.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.BankDeletedEvent;
import com.moneydesktop.finance.model.EventMessage.BankStatusUpdateEvent;
import com.moneydesktop.finance.model.EventMessage.RefreshAccountEvent;
import com.moneydesktop.finance.model.EventMessage.ReloadBannersEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
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
    private Activity mActivity;
    private PopupWindowAtLocation mPopup;
    
    private QueryProperty mWhereId = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.Id, "= ?");
    
    public AccountTypeChildView (Context context, List<BankAccount> bankAccounts, View parent) {
        super(context);
        mContext = context;
        mBankAccounts = bankAccounts;
        mParent = parent;
        
        mActivity = (Activity)mContext;
        
        mHandler = new Handler();
        EventBus.getDefault().register(this);

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChildView = inflater.inflate(R.layout.account_type_child, this, true);
        mBankAccountContainer = (LinearLayout) mChildView.findViewById(R.id.account_type_bank_container);
        
        populateView();
    }

    private void populateView () {
    	int accountCounter = 0;
    	AccountType accountType = new AccountType();
        for (final BankAccount account : mBankAccounts) {
        	accountType = AccountType.getAccountType(account.getAccountTypeId().toString());
        	if (account.getBank() != null && account.getBank().getBusinessObjectBase() != null) {
	        	if (account.getBank().getBusinessObjectBase().getDataState() != 3) {
		        	final View view = createChildView();
		        	
		        	mStatus = (ImageView)view.findViewById(R.id.account_type_child_banner);
		        	final TextView accountName = (TextView)view.findViewById(R.id.tablet_account_type_bank_name);
		        	final TextView accountSum = (TextView)view.findViewById(R.id.tablet_account_type_bank_sum);
		        	ImageView bankLogo = (ImageView)view.findViewById(R.id.tablet_account_type_bank_logo);
		        	final TextView propertyType = (TextView)view.findViewById(R.id.tablet_account_type_property_description);
		        	
		        	accountName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
		        	accountName.setText(account.getAccountName() == null ? "" : account.getAccountName());
		        	accountSum.setText(account.getBalance() == null ? "" : mFormatter.format(account.getBalance()));


                    //If we have the image in memory cache, get it from there. Don't bother looking at the SD card.
                    Bitmap bitmap = BankLogoManager.getBitmapFromMemCache(account.getInstitutionId());

                    if (bitmap == null) {
                        BankLogoManager.getBankImage(bankLogo, account.getInstitutionId());
                    } else {
                        bankLogo.setImageBitmap(bitmap);
                    }

		        	Fonts.applyPrimaryFont(accountName, 12);
		        	Fonts.applyPrimaryBoldFont(accountSum, 16);
		        	Fonts.applyPrimaryFont(propertyType, 12);
		        	
		        	if (account.getSubAccountTypeId() != null) {
		        		propertyType.setText(getPropertyTypeName(account.getSubAccountTypeId()));
		        	}
		        	
		        	view.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							
							List<OnClickListener> onClickListeners = new ArrayList<View.OnClickListener>();
							
							onClickListeners.add(new OnClickListener() {
								@Override
								public void onClick(View v) {  //View All transactions
									mPopup.fadeOutTransparency();
								    Intent i = new Intent(mActivity, DropDownTabletActivity.class);
							        i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.TRANSACTIONS_PAGE);
							        i.putExtra(Constant.EXTRA_ACCOUNT_ID, Long.toString(account.getId()));
							        i.putExtra(Constant.EXTRA_TXN_TYPE, TxFilter.ALL);
							        mContext.startActivity(i);
								}
							});
							
							onClickListeners.add(new OnClickListener() {  //View uncleared transactions
								@Override
								public void onClick(View v) {
									mPopup.fadeOutTransparency();
			                        Intent i = new Intent(mActivity, DropDownTabletActivity.class);
		                            i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.TRANSACTIONS_PAGE);
		                            i.putExtra(Constant.EXTRA_ACCOUNT_ID, Long.toString(account.getId()));
		                            i.putExtra(Constant.EXTRA_TXN_TYPE, TxFilter.UNCLEARED);
			                        mContext.startActivity(i);
								}
							});
							
							onClickListeners.add(new OnClickListener() { //Account Settings
								@Override
								public void onClick(View v) {
									mPopup.fadeOutTransparency();
									Intent intent = new Intent(mActivity, DropDownTabletActivity.class);
									intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.ACCOUNT_SETTINGS);
									intent.putExtra(Constant.KEY_ACCOUNT_TYPE, account.getAccountType().getAccountTypeName());
									intent.putExtra(Constant.KEY_ACCOUNT_NAME, account.getAccountName());
									intent.putExtra(Constant.KEY_BANK_ACCOUNT_ID, account.getId());
							        mActivity.startActivity(intent);
								}
							});
							
							onClickListeners.add(new OnClickListener() { // Show and Hide data
								@Override
								public void onClick(View v) {
									mPopup.fadeOutTransparency();
			                        Intent i = new Intent(mActivity, DropDownTabletActivity.class);
		                            i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.SHOW_HIDE_DATA);
		                            i.putExtra(Constant.KEY_BANK_ACCOUNT_ID, account.getAccountId());
			                        mContext.startActivity(i);
								}
							});
							
							onClickListeners.add(new OnClickListener() { //Delete Account
								@Override
								public void onClick(View v) {
									mPopup.fadeOutTransparency();									
									deleteAccountConfirmation(account, view);  									
								}

							});
							
							RelativeLayout parentView = (RelativeLayout)((Activity)mContext).findViewById(R.id.account_types_container);

                            DisplayMetrics dm = new DisplayMetrics();
                            ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                            int topOffset = dm.heightPixels - mParent.getMeasuredHeight();


                            int[] location = new int[2];
                            view.getLocationOnScreen(location);

						    mPopup = new PopupWindowAtLocation(mContext, parentView, view.getLeft() + view.getWidth(), location[1] - topOffset - (int)UiUtils.convertDpToPixel(5, mContext),
									mContext.getResources().getStringArray(R.array.account_selection_popup), onClickListeners, view);
						}
					});
		
		            mBankAccountContainer.addView(view);
		        } else {
		        	accountCounter++;
		        }
        	}
        }
    	if (accountCounter > 0) {
    		EventBus.getDefault().post(new EventMessage(). new RemoveAccountTypeEvent(accountType));
    	}
    }
    
    private String getPropertyTypeName (Long id) {
    	
		AccountTypeDao accountTypeDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		PowerQuery query = new PowerQuery(accountTypeDao);
	    
	    query.where(mWhereId, String.valueOf(id));
	    	 
	    List<AccountType> propertyTypeList = new ArrayList<AccountType>();
	    propertyTypeList = accountTypeDao.queryRaw(query.toString(), query.getSelectionArgs());
	    
	    return propertyTypeList.get(0).getAccountTypeName();
    	
    }

	private void deleteAccount(BankAccount account, View view) {
		mBankAccountContainer.removeView(view);
		removeInstancesOfAccount(account);
		account.softDeleteSingle();
        mPopup.fadeOutTransparency();
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
    
    public void onEvent(final BankDeletedEvent event) {
        
        mHandler.post(new Runnable() {
            @Override
            public void run() {         
                removeInstancesOfBank(event.getDeletedBank());
            }
        });        
    }
    
    protected void removeInstancesOfBank(Bank deletedBank) {
        int iterator = 0;
        boolean updateListDataSet = false;
        AccountType accountToBeRemoved = new AccountType();
        for (final BankAccount account : mBankAccounts) {
        	if (account.getBank() != null) {
	            if (account.getBank().getBankName().equals(deletedBank.getBankName())) { //null pointer here sometimes when deleting multiple banks quickly
	                View view = mBankAccountContainer.getChildAt(iterator);
	                mBankAccountContainer.removeView(view);      
	                
	                if (mBankAccountContainer.getChildCount() == 0) {
	                    updateListDataSet = true;
	                    accountToBeRemoved = AccountType.getAccountType(account.getAccountTypeId().toString());
	                }
	            }
        	}
        }
        if (updateListDataSet) {
            EventBus.getDefault().post(new EventMessage(). new RemoveAccountTypeEvent(accountToBeRemoved));
        }
    }
  
    protected void removeInstancesOfAccount(BankAccount account) {        
        if (mBankAccountContainer.getChildCount() == 0) {
        	AccountType accountToBeRemoved = new AccountType();
        	accountToBeRemoved = AccountType.getAccountType(account.getAccountTypeId().toString());
            EventBus.getDefault().post(new EventMessage(). new RemoveAccountTypeEvent(accountToBeRemoved));
            EventBus.getDefault().post(new EventMessage(). new CheckRemoveBankEvent(account.getBank()));
        } else {
    		//start the sync
            Intent intent = new Intent(mActivity, SyncService.class);
            mActivity.startService(intent);
        }
    }
    
    private void updateChildBanners(Boolean forceBanner) {
        int iterator = 0;
        for (final BankAccount account : mBankAccounts) {
            if (mContext != null){
                
                View view = mBankAccountContainer.getChildAt(iterator);
                
                if (view != null && account.getBank() != null) {
                
                    mStatus = (ImageView)view.findViewById(R.id.account_type_child_banner);
                    
                    if (forceBanner){
                        mStatus.setVisibility(View.VISIBLE);
                        mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_updating_banner));
                    } else {
                        mStatus.setVisibility(View.VISIBLE);

                        if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_SUCCEEDED.index()) { //3
                            mStatus.setVisibility(View.GONE);
                            
                        } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_PENDING.index()) { //1
                            mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_updating_banner));
                            
                        } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_LOGIN_FAILED.index()) { //5
                            mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
                            
                        } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_EXCEPTION.index()) { //4
                            mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
                            
                        } else if (account.getBank().getProcessStatus().intValue() == BankRefreshStatus.STATUS_PROCESSING.index()) { //2
                            mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_updating_banner));
                            
                        } else {
                            mStatus.setVisibility(View.GONE);
                        }             
                    }
                }
            }   
            iterator++;
        }
    }
    
    private void updateSpecificChildBanner(Bank bank) {
        int iterator = 0;
        for (final BankAccount account : mBankAccounts) {
        	if (account.getBank() != null) {
        		if (bank.getBankName().equals(account.getBank().getBankName())){
                
	                View view = mBankAccountContainer.getChildAt(iterator);
	                mStatus = (ImageView)view.findViewById(R.id.account_type_child_banner);
	                
	                mStatus.setVisibility(View.VISIBLE);
	                mStatus.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_updating_banner));         
	                
	            }   
            iterator++;
        	}
        }
    }

	private void deleteAccountConfirmation(final BankAccount account, final View view) {
		DialogUtils.alertDialog(String.format(mActivity.getString(R.string.delete_bank_title), account.getAccountName()),
				mActivity.getString(R.string.delete_account_message), 
				mActivity.getString(R.string.label_yes).toUpperCase(), 
				mActivity.getString(R.string.label_no).toUpperCase(),
				mActivity, 
		        new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {  
		                
		                switch (which) {
		                    case DialogInterface.BUTTON_POSITIVE:
		                        DialogUtils.dismissAlert();
		                        deleteAccount(account, view);
		                        break;                                
		                    case DialogInterface.BUTTON_NEGATIVE:
		                        DialogUtils.dismissAlert();
		                        mPopup.fadeOutTransparency();            
		                        break;
		                }
		        }
		});
	}
   
}