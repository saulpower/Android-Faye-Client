package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.*;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.shared.adapter.AccountsExpandableListAdapter;
import com.moneydesktop.finance.shared.fragment.AccountTypesFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.AnimatedListView.SlideExpandableListAdapter;
import com.moneydesktop.finance.views.PopupWindowAtLocation;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;
import com.moneydesktop.finance.views.navigation.NavBarButtons;
import de.greenrobot.event.EventBus;

import java.util.*;

public class AccountTypesTabletFragment extends AccountTypesFragment {
	
    private ListView mListView;
    private static SlidingDrawerRightSide sRightDrawer;
    private View mFooter;
    private PopupWindowAtLocation mPopup;
    private List<Bank> mBanksForDeletion;
    LinearLayout mPanelLayoutHolder;
    private List<AccountType> mAccountTypesFiltered;
    private Handler mHandler;    
    private SlideExpandableListAdapter mAdapter;
    private AccountsExpandableListAdapter mAdapter1;
    private int mAccountCounter = 0;
    private HashMap<Integer, Boolean> mOpenState;
    private Boolean mIsInitialization;
	
	public static AccountTypesTabletFragment newInstance() {	
		AccountTypesTabletFragment frag = new AccountTypesTabletFragment();
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mIsInitialization = true;
		
		mRoot = inflater.inflate(R.layout.tablet_account_types, null);
		mFooter = inflater.inflate(R.layout.account_type_list_footer, null);
		mListView = (ListView) mRoot.findViewById(R.id.accounts_expandable_list_view);
		sRightDrawer = (SlidingDrawerRightSide) mRoot.findViewById(R.id.account_slider);
		sRightDrawer.open();
		mOpenState = new HashMap<Integer, Boolean>();


		setupView();
		
		mHandler = new Handler();
		
		return mRoot;
	}

    @Override
    public void isShowing() {

        setupTitleBar();
    }
	
    private void setupView() {
    	
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
	    getAllBanks(); 
		
        mAccountTypesFiltered = new ArrayList<AccountType>();
        
        if (mBankList.size() != 0) {
        
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
	        
	        //remove any account types where all children have a bank == null
	        //this will also delete any bank accounts that don't have a BANK. Without a bank, many issues (NPE) come up. 
	        //This issue can happen when a user deletes a bank that in being sync'ed for the first time
	        for (AccountType accountType : tempList) {
	        	int counter = 0;
	        	for (BankAccount bankAccount : accountType.getBankAccounts()) {	        		
	        		if (bankAccount.getBank() == null) {
	        			counter++;
//	        			bankAccount.softDeleteSingle();
	        		}
	        	}
	        	if (counter == accountType.getBankAccounts().size()) {
	        		mAccountTypesFiltered.remove(accountType);
	        	}
	        	
	        }
        } else {
        	mAccountTypesFiltered.clear();
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
        
    	prepPanel();
	}

	private void prepPanel() {
		mPanelLayoutHolder.removeAllViews();
		final ViewGroup.LayoutParams layoutParams = mPanelLayoutHolder.getLayoutParams();
        layoutParams.width = UiUtils.getMinimumPanalWidth(mActivity);
        mPanelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams);
        initializeDrawer();
	}
		
	private void setupTitleBar() {
	    
	    String[] icons = mActivity.getResources().getStringArray(R.array.account_types_title_bar_icons);
	    
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
	    
        onClickListeners.add(new OnClickListener() { //refresh
            @Override
            public void onClick(View v) {
                if (User.getCurrentUser().getCanSync()){
                    //start the sync
                    SyncEngine.sharedInstance().beginSync();
                    
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
                Toast.makeText(mActivity, "help", Toast.LENGTH_LONG).show();
            }
        });
	    
	    new NavBarButtons(mActivity, icons, onClickListeners);
    }

    /**
	 * Setup the Panel/Drawer to show all banks attached.
	 */
	private void initializeDrawer () {
	    mBanksForDeletion = new ArrayList<Bank>();

		mPanelLayoutHolder.addView(getPanelHeader());

		List<Bank> bankList = new ArrayList<Bank>(mBankList);
		for (Bank bank : bankList) {
			if (bank.isDeleted()) {
				mBankList.remove(bank);
			}
		}
		
		
        //the next 3 lines and for loop can be removed if we make a PowerQuery that handles it all
        List<Bank> tempList = new ArrayList<Bank>();
        tempList = Arrays.asList(new Bank[mBankList.size()]);  
        Collections.copy(tempList, mBankList);
		
        //remove any manual institutions from the panel...not doing so messes up the banks status.
        for (Bank bank : tempList) {
        	if (bank.getBankName().toLowerCase().equals("manual institution")) {
        		mBankList.remove(bank);
        	}
        }

        //For every bank that is attached, add it to the Drawer
        for (Bank bank : mBankList) {       	
    		View v = populateDrawerView(bank);     		
    		mPanelLayoutHolder.addView(v);	    		        	
        }
        
        if (User.getCurrentUser().getCanSync() && mIsInitialization) {
            updateAllBankStatus();
            mIsInitialization = false;
        }
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
            for (final Bank bank : mBankList) {
                i++;
                View bankView = mPanelLayoutHolder.getChildAt(i); 
                if (bankView != null) {
                	ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
	                setBanner(bank, status);
	                if (bank.getProcessStatus() < 3) {
	                	Handler updateStatus = new Handler(Looper.getMainLooper());
	                	updateStatus.post(new Runnable() {
	                	    public void run()
	                	    {
	                	    	updateBankStatus(bank);
	                	    }
	                	});
	                }
                }
            }
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
	    		if (bank.getProcessStatus() == BankRefreshStatus.STATUS_SUCCEEDED.index()) {
		    		Intent intent = new Intent(mActivity, SyncService.class);
		    		mActivity.startService(intent);
	    		}
                
                for (Bank bankIterator : mBankList) {
                    i++;
                    View bankView = mPanelLayoutHolder.getChildAt(i);
                    if (bankView != null) {
	                    if (bankIterator.getBankName().equals(bank.getBankName())) {
	                        ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
	                        
	                        if (status != null) {
	                            setBanner(bank, status);
	                        }
	                    }
                    }
                }                
            }
        });
    }
    
    public void setAllBanksToUpdate() {
         
        int i = 0;
        for (Bank bank : mBankList) {

            if (!bank.isDeleted()) {

                i++;
                View bankView = mPanelLayoutHolder.getChildAt(i); 
            
                if (bankView != null) {
	                ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
	                
	                status.setVisibility(View.VISIBLE);
	                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
                }
            }
        }
        
    }
    
    public void setBankToUpdate(Bank bank) {
        
        int i = 0;
        for (Bank bankIterator : mBankList) {

            i++;
            if (bankIterator.getBankName().equals(bank.getBankName())) {

                View bankView = mPanelLayoutHolder.getChildAt(i); 
            
                if (bankView != null) {
	                ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
	                
	                status.setVisibility(View.VISIBLE);
	                status.setImageDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
                }
            }
        }
    }
    
    /**
     * Creates a View of a bank represented on the right panel.
     * @param bank -the bank to be added
     *
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
				bankPanelClickEvent(bank, bankTypeAccountView, booklet, bankAccountView);
			}
		});

        return bankTypeAccountView;
    }
	
	private void bankPanelClickEvent(final Bank bank, final View bankTypeAccountView, final ImageView booklet, final View bankAccountView) {
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
                        sRightDrawer.getLeft() + mPanelLayoutHolder.getWidth(), 
                        location[1] - topOffset, 
                        titles, 
                        onClickListeners, 
                        booklet);
		    }
		}
	}

    private void setBanner(final Bank bank, final ImageView status) {
    	status.setVisibility(View.VISIBLE);
    	Handler refreshBanner = new Handler(Looper.getMainLooper());
    	refreshBanner.post(new Runnable() {
    	    public void run()
    	    {
		        if (mActivity != null) {
		        	if (bank.getProcessStatus() == null) {
		        		status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
		        		return;
		        	}
		        	
		        	BankRefreshStatus type = BankRefreshStatus.fromInteger(bank.getProcessStatus().intValue());

                    if (type == null) return;

		        	switch (type) {
					case STATUS_SUCCEEDED:
						status.setVisibility(View.GONE);
						break;
						
					case STATUS_PENDING:
						status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
						break;
						
					case STATUS_MFA:
						status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_more_info_banner));
						break;
						
					case STATUS_LOGIN_FAILED:
						status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
						break;
						
					case STATUS_UPDATE_REQUIRED:
						status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_more_info_banner));
						break;
						
					case STATUS_EXCEPTION:
						status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_error_banner));
						break;
						
					case STATUS_PROCESSING:
						status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.tablet_accounts_bank_book_updating_banner));
						break;

					default:
						break;
					}
		        }
    	    }
    	});
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

        Animation outRight = AnimationUtils.loadAnimation(mActivity, R.anim.out_right);
        outRight.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				//remove bank from view
				 new Handler().post(new Runnable() {
				        public void run() {
							panelView.removeView(v);
							updateChildAccountsList(bank);
				        }
				 });
			}
		});
        v.startAnimation(outRight);
		
		
		for (BankAccount account : bank.getBankAccounts()) {
			account.softDeleteBatch();
		}
		
		bank.softDeleteSingle();
		
		//start the sync
		Intent intent = new Intent(getActivity(), SyncService.class);
		getActivity().startService(intent);
		
		

		
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
        		deleteBankWithNoAccountsConfirmation(bankForRemoval, bankView);
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
	    	    		setupView();
	    	    	}
	    	    }
	    	});
    	}
    }
    
    public void onEvent(final UpdateSpecificBankStatus event) {
    	Handler refreshBankStatus = new Handler(Looper.getMainLooper());
    	refreshBankStatus.post(new Runnable() {
    	    public void run()
    	    {
    	    	setBankToUpdate(event.getBank());
    	    }
    	});
    }

	private void deleteBankWithNoAccountsConfirmation(final Bank bank, final View bankView) {
	
		if (bank.getBankName().toLowerCase().equals("manual institution")) {
			mBanksForDeletion.add(bank);
    		if (mBanksForDeletion.contains(bank)) {
    		    mBankList.remove(bank);
    		}
    		
    		bank.softDeleteSingle();
        		
    		//start the sync
    		Intent intent = new Intent(getActivity(), SyncService.class);
    		getActivity().startService(intent);
		}else {
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
	                                
	                                removeBank(bank, bankView);
	                                
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
	}
    
	protected void updateChildAccountsList(Bank bank) {
	    EventBus.getDefault().post(new EventMessage().new BankDeletedEvent(bank));
    }

    /**
	 * Drawer's width is set to a percentage of screen.
	 * @param layoutParams
     *
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
		return mActivity.getString(R.string.title_activity_accounts).toUpperCase();
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public void isShowing(boolean fromBackstack) {

        setupTitleBar();
    }

	private void removeBank(final Bank bank, final View bankView) {
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
	}   
}