package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AccountsExpandableListAdapter;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.BankDao;
import com.moneydesktop.finance.database.BusinessObjectBaseDao;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.CheckRemoveBankEvent;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.model.EventMessage.RemoveAccountTypeEvent;
import com.moneydesktop.finance.model.FragmentVisibilityListener;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.model.EventMessage.BankStatusUpdateEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.PopupWindowAtLocation;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;
import com.moneydesktop.finance.views.AnimatedListView.SlideExpandableListAdapter;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountTypesTabletFragment extends BaseFragment implements FragmentVisibilityListener{
	
	private QueryProperty mBusinessObjectBaseTable = new QueryProperty(BusinessObjectBaseDao.TABLENAME, BankDao.Properties.BusinessObjectId, BusinessObjectBaseDao.Properties.Id);
	private QueryProperty mBankAccountTable = new QueryProperty(BankAccountDao.TABLENAME, AccountTypeDao.Properties.BusinessObjectId, BankAccountDao.Properties.BusinessObjectId);
	private QueryProperty mWhereDataState = new QueryProperty(BusinessObjectBaseDao.TABLENAME, BusinessObjectBaseDao.Properties.DataState, "!= ?");
	private QueryProperty mAccountTypeWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
	private QueryProperty mOrderBy = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);
	
    private ListView mListView;
    private static SlidingDrawerRightSide sRightDrawer;
    private View mFooter;
    private PopupWindowAtLocation mPopup;
    private List<Bank> mBankList;
    private List<Bank> mBanksForDeletion;
    LinearLayout mPanelLayoutHolder;
    List<AccountType> mAccountTypesFiltered;
    private Handler mHandler;    
    private SlideExpandableListAdapter mAdapter;
    private AccountsExpandableListAdapter mAdapter1;
    private int mAccountCounter = 0;
    private HashMap<Integer, Boolean> mOpenState;
	
	public static AccountTypesTabletFragment newInstance(FragmentType type) {	
		AccountTypesTabletFragment frag = new AccountTypesTabletFragment();
		frag.setType(type);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);        
        EventBus.getDefault().register(this);
        this.mActivity.onFragmentAttached(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_account_types, null);
		mFooter = inflater.inflate(R.layout.account_type_list_footer, null);
		mListView = (ListView) mRoot.findViewById(R.id.accounts_expandable_list_view);
		sRightDrawer = (SlidingDrawerRightSide) mRoot.findViewById(R.id.account_slider);
		sRightDrawer.open();
		mOpenState = new HashMap<Integer, Boolean>();


		setupView(false);
		
		mHandler = new Handler();
		
		return mRoot;
	}
	
    private void setupView(boolean updateListOnly) {
		setupTitleBar((getActivity() != null) ? getActivity() : mActivity);
		mActivity.updateNavBar(mActivity.getString(R.string.title_activity_accounts));
    	
    	//clears out any previous adapter it had
    	mListView.setAdapter(null);
    	
    	if (mListView.getFooterViewsCount() > 0) {
    		mListView.removeFooterView(mFooter);
    	}
    	
    	ApplicationContext.getDaoSession().clear();
    	
	    mPanelLayoutHolder = (LinearLayout)mRoot.findViewById(R.id.panel_layout_holder);
        	    
	    AccountTypeDao accountTypeDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
			    
	    List<AccountType> allAccountTypes = new ArrayList<AccountType>();
	    		
	    //Get all account types in alphabetical order. Removing the "Unknown" Type. 
		PowerQuery query = new PowerQuery(accountTypeDAO);	
		query.where(mAccountTypeWhere, "Unknown")
		.orderBy(mOrderBy, false);
		
		allAccountTypes = accountTypeDAO.queryRaw(query.toString(), query.getSelectionArgs());
		
		
        mAccountTypesFiltered = new ArrayList<AccountType>();
        
        //Create a new list of AccountTypes that have bank accounts
        for (AccountType accountType : allAccountTypes) {
        	if (!accountType.getBankAccounts().isEmpty() && !accountType.isDeleted()) {
        		mAccountTypesFiltered.add(accountType);
        	}
        }
        
        
        //the next 3 lines and for loop can be removed if we make a PowerQuery that handles it all
        List<AccountType> tempList = new ArrayList<AccountType>();
        tempList = Arrays.asList(new AccountType[mAccountTypesFiltered.size()]);  
        Collections.copy(tempList, mAccountTypesFiltered);
        
        for (AccountType accountType : tempList) {
        	if (accountType.getBankAccounts().size() == 1) {
        		if (accountType.getBankAccounts().get(0).getBank() == null){
        			mAccountTypesFiltered.remove(accountType);
        		}
        	}
        }
        
        
        
        
        if (!mAccountTypesFiltered.isEmpty() && mAccountTypesFiltered != null) {        	
            mListView.addFooterView(mFooter);
            
            //This sets the GroupView
            mAdapter1 = new AccountsExpandableListAdapter((getActivity() != null) ? getActivity() : mActivity,  
                    R.layout.account_type_group, 
                    R.id.account_type_group_name, 
                    mAccountTypesFiltered);
            
            mAdapter = new SlideExpandableListAdapter(
                    mAdapter1, 
                    R.id.account_type_group_container, 
                    R.id.expandable,
                    (getActivity() != null) ? getActivity() : mActivity,
                    mAccountTypesFiltered); 
            
            //this animates and sets the ChildView
            mListView.setAdapter(mAdapter);
            mAdapter1.notifyDataSetChanged();
            
            mOpenState = mAdapter.getOpenStateList();
            
            if (mOpenState.isEmpty()) {
            	for (int i = 0; i < mAccountTypesFiltered.size(); i++) {
        			mOpenState.put(i, true);
        		}
            	mAdapter.setOpenStateList(mOpenState);
            }
            
        } else {
        	Toast.makeText(mActivity, "No Accounts types that have bank accounts...show empty state", Toast.LENGTH_SHORT).show();
        }
           
        
        //This allows you to grab the panel and close it by touching and dragging on any part of the panel instead of just the handle
        mPanelLayoutHolder.setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View view, MotionEvent event) {
				return true;
			}
		});
        
        //don't update the panel if we are only trying to update the account types list
        if (!updateListOnly){
        	prepPanel();
        }
	}

	private void prepPanel() {
		mPanelLayoutHolder.removeAllViews();
		final ViewGroup.LayoutParams layoutParams = mPanelLayoutHolder.getLayoutParams();
        layoutParams.width = UiUtils.getMinimumPanalWidth(mActivity);
        mPanelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams);
        initializeDrawer();
	}
		
	private void setupTitleBar(final Activity activity) {
	    
	    String[] icons = activity.getResources().getStringArray(R.array.account_types_title_bar_icons);
	    
	    ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
	    
	    onClickListeners.add(new OnClickListener() { //add Bank
            @Override
            public void onClick(View v) {
                if (User.getCurrentUser().getCanSync()){ 
					Intent intent = new Intent(mActivity, DropDownTabletActivity.class);
					intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.ADD_BANK);
			        mActivity.startActivity(intent);
                } else {
                   //Dialog....can't update data 
                    DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
                }
            }
        });
	    
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getCurrentUser().getCanSync()){
                    //start the sync
                    Intent intent = new Intent((getActivity() != null) ? getActivity() : mActivity, SyncService.class);
                    ((getActivity() != null) ? getActivity() : mActivity).startService(intent);
                    
                    setAllBanksToUpdate();
                } else {
                   //Dialog....can't update data 
                    DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
                }
            }

        });
       
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "help", Toast.LENGTH_LONG).show();
            }
        });
	    
	    new NavBarButtons(activity, icons, onClickListeners);
    }

    /**
	 * Setup the Panel/Drawer to show all banks attached.
	 * @param panelLayoutHolder -- the panel container
	 */
	private void initializeDrawer () {
	    
	    getAllBanks(); 
	    
	    mBanksForDeletion = new ArrayList<Bank>();

		mPanelLayoutHolder.addView(getPanelHeader());

		List<Bank> bankList = new ArrayList<Bank>(mBankList);
		for (Bank bank : bankList) {
			if (bank.isDeleted()) {
				mBankList.remove(bank);
			}
		}

        //For every bank that is attached, add it to the Drawer
        for (Bank bank : mBankList) {
    		mPanelLayoutHolder.addView(populateDrawerView(bank));
        }
        if (User.getCurrentUser().getCanSync()) {
            updateAllBankStatus();
        }
    }

	private void getAllBanks() {
		BankDao bankDao = ApplicationContext.getDaoSession().getBankDao();
		PowerQuery query = new PowerQuery(bankDao);
	    
	    query.join(mBusinessObjectBaseTable).where(mWhereDataState, "3");
	    	    
	    mBankList = bankDao.queryRaw(query.toString(), query.getSelectionArgs());
	}

	/**
	 * Adds the header Text to the panel
	 * @return headerView
	 */
    private View getPanelHeader() {
    	LayoutInflater layoutInflater = mActivity.getLayoutInflater();
    	final View headerView = layoutInflater.inflate(R.layout.tablet_panel_header, null); 
		return headerView;
	}

    /**
     * Sync has completed.
     * 
     * @param event
     */
    public void onEvent(SyncEvent event) {

        if (event.isFinished()) {
            
            int i = 0;
            for (Bank bank : mBankList) {
                i++;
                View bankView = mPanelLayoutHolder.getChildAt(i); 
                ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
                setBanner(bank, status);
            }
            //updateAllBankStatus();
        }
    }
    
    /**
     * Bank has been updated in background.
     * 
     * @param event
     */
    public void onEvent(BankStatusUpdateEvent event) {
    
        final Bank bank = event.getUpdatedBank();
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
                int i = 0;
                for (Bank bankIterator : mBankList) {
                    i++;
                    View bankView = mPanelLayoutHolder.getChildAt(i);
                    if (bankIterator.getBankName().equals(bank.getBankName())) {
                        ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
                        
                        if (status != null) {
                            setBanner(bank, status);
                        }
                    }
                }                
            }
        });
    }
    

    
    /**
     * Updates status for all banks.
     */
    private void updateAllBankStatus() {
        for (Bank bank : mBankList) {
    		SyncEngine.sharedInstance().beginBankStatusUpdate(bank);
        }        
    }
    
    /**
     * Updates status for given bank.
     */
    private void updateBankStatus(Bank bank) {
        SyncEngine.sharedInstance().beginBankStatusUpdate(bank);  
    }
    
    @Override
    public void onResume() {
        super.onResume();
        setupTitleBar(getActivity());
    }
    
    public void setAllBanksToUpdate() {
         
        int i = 0;
        for (Bank bank : mBankList) {

            if (!bank.isDeleted()) {

                i++;
                View bankView = mPanelLayoutHolder.getChildAt(i); 
            
            
                ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
                
                status.setVisibility(View.VISIBLE);
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
            }
        }
        
    }
    
    public void setBankToUpdate(Bank bank) {
        
        int i = 0;
        for (Bank bankIterator : mBankList) {

            i++;
            if (bankIterator.getBankName().equals(bank.getBankName())) {

                View bankView = mPanelLayoutHolder.getChildAt(i); 
            
                ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
                
                status.setVisibility(View.VISIBLE);
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
            }
        }
    }
    
    
    /**
     * Creates a View of a bank represented on the right panel.
     * @param bank -the bank to be added
     * @param panelLayoutHolder 
     * @return bank view 
     */
	private View populateDrawerView (final Bank bank) {
        LayoutInflater layoutInflater = ((getActivity() != null) ? getActivity() : mActivity).getLayoutInflater();
        final View bankTypeAccountView = layoutInflater.inflate(R.layout.bank_account, null);
        ImageView bankImage = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_image);  
        final ImageView booklet = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_bankbook);
        
        String logoId = bank.getBankId();
        
        ImageView status = (ImageView) bankTypeAccountView.findViewById(R.id.bank_status);
        if (bank.getInstitution() != null) {
            logoId = bank.getInstitution().getInstitutionId();
        }
        
        status.setVisibility(View.VISIBLE);
        setBanner(bank, status);
        BankLogoManager.getBankImage(bankImage, logoId);
        
        TextView bankName = (TextView)bankTypeAccountView.findViewById(R.id.account_bank_name);
        
        bankName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        bankName.setText(bank.getBankName());
        
        bankTypeAccountView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View bankAccountView) {
				
				RelativeLayout parentView = (RelativeLayout)getActivity().findViewById(R.id.account_types_container);
				
				List<OnClickListener> onClickListeners = new ArrayList<View.OnClickListener>();
				String[] titles = getActivity().getResources().getStringArray(R.array.bank_selection_popup);
				
				if (bank.getProcessStatus().equals(BankRefreshStatus.STATUS_MFA.index())
						|| bank.getProcessStatus().equals(BankRefreshStatus.STATUS_UPDATE_REQUIRED.index())
						|| bank.getProcessStatus().equals(BankRefreshStatus.STATUS_LOGIN_FAILED.index())) {
					
					titles = getActivity().getResources().getStringArray(R.array.fix_bank_selection_popup);
					onClickListeners.add(new OnClickListener() {
						@Override
						public void onClick(View v) {
						    if (User.getCurrentUser().getCanSync()){
						        fixBank(bank);
						    } else {
						        DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
						    }
					        mPopup.fadeOutTransparency();
						}

					});
				}
				
				onClickListeners.add(new OnClickListener() {
					@Override
					public void onClick(View v) {
					    if (User.getCurrentUser().getCanSync()){
					        refreshAccount(bank);
					    } else {
					        DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
					    }
				        mPopup.fadeOutTransparency();
					}

				});
				
				onClickListeners.add(new OnClickListener() { 	

                    @Override
					public void onClick(View v) {
                        if (User.getCurrentUser().getCanSync()){
                        	deleteMemberAccount(bankAccountView, mPanelLayoutHolder, bank);
                        } else {
                            //Popup dialog saying you can't delete demo data
                            DialogUtils.alertDialog(String.format(getString(R.string.feature_not_available_delete_title), bank.getBankName()), getResources().getString(R.string.feature_not_available_delete_message), getActivity());
                        }
                        mPopup.fadeOutTransparency();
					}

				});
				
				onClickListeners.add(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (User.getCurrentUser().getCanSync()) {
	                        Intent i = new Intent(mActivity, DropDownTabletActivity.class);
                            i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.UPDATE_USERNAME_PASSWORD);
                            i.putExtra(Constant.KEY_BANK_ACCOUNT_ID, bank.getBankId());
	                        mActivity.startActivity(i);
						} else {
						    DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
						}
                        mPopup.fadeOutTransparency();
					}
				});
				
				
				//Display popup with offset when bank is clicked
				for (Bank bankIterator : mBankList) {
				    if (bankIterator.getBankName().equals(bank.getBankName())) {
				        
				        DisplayMetrics dm = new DisplayMetrics();
				        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				        int topOffset = dm.heightPixels - mRoot.getMeasuredHeight();
				        
				        int[] location = new int[2];
                        bankTypeAccountView.getLocationOnScreen(location);
				        
				        mPopup = new PopupWindowAtLocation(getActivity(), 
		                        parentView, 
		                        sRightDrawer.getLeft(), 
		                        location[1] - topOffset, 
		                        titles, 
		                        onClickListeners, 
		                        booklet);
				    }
				}
			}
		});

        return bankTypeAccountView;
    }

    private void setBanner(final Bank bank, ImageView status) {
        
        status.setVisibility(View.VISIBLE);
        if (getActivity() != null) {
        	if (bank.getProcessStatus() == null) {
        		status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
        		return;
        	}
            if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_SUCCEEDED.index()) {
                status.setVisibility(View.GONE);
                
            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_PENDING.index()) {
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
                
            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_MFA.index()) {
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_more_info_banner));
                
            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_LOGIN_FAILED.index()) {
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
                
            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_UPDATE_REQUIRED.index()) {
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_more_info_banner));
                
            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_EXCEPTION.index()) {
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
                
            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_PROCESSING.index()) {
                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
            }
        }
    }
		
    private void refreshAccount(Bank bank) {
        setBankToUpdate(bank);
        updateBankStatus(bank); 
        EventBus.getDefault().post(new EventMessage().new RefreshAccountEvent(bank));
    }
    
	private void fixBank(Bank bank) {
		
		Context context = (getActivity() != null) ? getActivity() : mActivity;
		
		Intent intent = new Intent(mActivity, DropDownTabletActivity.class);
		intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.FIX_BANK);
		intent.putExtra(Constant.KEY_BANK_ACCOUNT_ID, bank.getBankId());
		intent.putExtra(Constant.KEY_ACCOUNT_NAME, bank.getBankName());
		context.startActivity(intent);
	}
    
    private void deleteMemberAccount(final View v, final LinearLayout panelView, final Bank bank) {
            
        DialogUtils.alertDialog(String.format(getString(R.string.delete_bank_title), bank.getBankName()), 
                getString(R.string.delete_bank_message), 
                getString(R.string.label_yes).toUpperCase(), 
                getString(R.string.label_no).toUpperCase(), 
                getActivity(), 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {  
                        
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                DialogUtils.dismissAlert();
                                //set the bank for deletion
                                removeBank(v, panelView, bank);                                
                                break;                                
                            case DialogInterface.BUTTON_NEGATIVE:
                                DialogUtils.dismissAlert();
                                mPopup.fadeOutTransparency();            
                                break;
                        }
                }
        });                
    }
    
	private void removeBank(final View v, final LinearLayout panelView, final Bank bank) {
		mBanksForDeletion.add(bank);
		mPopup.fadeOutTransparency();
		
		//so we don't try to update UI elements that are no longer there.
		if (mBanksForDeletion.contains(bank)) {
		    mBankList.remove(bank);
		}
		
		bank.softDeleteSingle();
		
		//start the sync
		Intent intent = new Intent(getActivity(), SyncService.class);
		getActivity().startService(intent);
		
		//remove bank from view
		panelView.removeView(v);
		
		updateChildAccountsList(bank);
	}
   
    public void onEvent(RemoveAccountTypeEvent event) {        
        final AccountType accountType = event.getAccountType();
        if (!mAccountTypesFiltered.isEmpty()){
	        mAccountTypesFiltered.remove(accountType);
	        mAdapter1.notifyDataSetChanged();
        }
    }
    
    public void onEvent(CheckRemoveBankEvent event) {        
    	Bank bankForRemoval = event.getBank();
    	int i = 0;
        for (Bank bank : mBankList) {
        	i++;
        	View bankView = mPanelLayoutHolder.getChildAt(i); 
        	
        	
        	for (AccountType accountType : mAccountTypesFiltered) {
        		for(BankAccount bankAccount : bank.getBankAccounts()) {
        			if (bank.getBankName().equals(bankForRemoval.getBankName()) && (bankAccount.getAccountType().equals(accountType))) {
        				mAccountCounter++;
        			}
        		}
        	}
        	
        	if (mAccountCounter == 0) {
        		deleteBankWithNoAccountsConfirmation(bank, bankView);
          	} else {
        		//start the sync
        		Intent intent = new Intent(getActivity(), SyncService.class);
        		getActivity().startService(intent);
          	}
        	mAccountCounter = 0;
        	
        }
    }
    
    public void onEvent(DatabaseSaveEvent event) {
    	//only update screen if Bank or BankAccount Objects have been updated.
    	if (event.getChangedClassesList().contains(Bank.class) || event.getChangedClassesList().contains(BankAccount.class) ) {
	    	Handler refresh = new Handler(Looper.getMainLooper());
	    	refresh.post(new Runnable() {
	    	    public void run()
	    	    {
	    	    	if (mActivity != null) {
	    	    		setupView(false);
	    	    	}
	    	    }
	    	});
    	}
    }

	private void deleteBankWithNoAccountsConfirmation(final Bank bank, final View bankView) {
	
        DialogUtils.alertDialog(String.format(getString(R.string.delete_bank_title), bank.getBankName()), 
                getString(R.string.delete_bank_with_no_accounts_message), 
                getString(R.string.label_yes).toUpperCase(), 
                getString(R.string.label_no).toUpperCase(), 
                getActivity(), 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {  
                        
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                DialogUtils.dismissAlert();
                                
                        		mBanksForDeletion.add(bank);
                        		if (mBanksForDeletion.contains(bank)) {
                        		    mBankList.remove(bank);
                        		}
                        		
                        		bank.softDeleteSingle();
                            		
                        		//start the sync
                        		Intent intent = new Intent(getActivity(), SyncService.class);
                        		getActivity().startService(intent);
                        		
                        		//remove bank from view
                        		mPanelLayoutHolder.removeView(bankView);
                                
                                break;                                
                            case DialogInterface.BUTTON_NEGATIVE:
                                DialogUtils.dismissAlert();
                                mPopup.fadeOutTransparency();
                        		//start the sync
                                Intent i = new Intent(mActivity, SyncService.class);
                                mActivity.startService(i);
                                break;
                        }
                }
        }); 

	}
    
	protected void updateChildAccountsList(Bank bank) {
	    EventBus.getDefault().post(new EventMessage().new BankDeletedEvent(bank));
    }

    /**
	 * Drawer's width is set to a percentage of screen.
	 * @param layoutParams
	 * @param activity
	 * @return the drawer
	 */
    public SlidingDrawerRightSide setupDrawer (final ViewGroup.LayoutParams layoutParams) {
        final ViewGroup.LayoutParams drawerLayoutParams = sRightDrawer.getLayoutParams();

        //this is here so we can adjust for the handle on the panel...without it, sizing is a little off.
        drawerLayoutParams.width = (int) (layoutParams.width + UiUtils.convertDpToPixel(7, (getActivity() != null) ? getActivity() : mActivity));
        drawerLayoutParams.height = UiUtils.getScreenHeight((getActivity() != null) ? getActivity() : mActivity) ;
        sRightDrawer.setLayoutParams(drawerLayoutParams);

        return sRightDrawer;
    }
	
	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_accounts).toUpperCase();
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }
    
    @Override
    public void onShow(Activity activity) {
        if (activity != null) {
            setupTitleBar(activity);
        } 
    }
   
}