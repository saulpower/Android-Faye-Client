package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.devsmart.android.ui.HorizontalListView;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.SlideFrom;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.handset.adapter.AccountBankListAdapter;
import com.moneydesktop.finance.handset.adapter.AccountTypesHandsetAdapter;
import com.moneydesktop.finance.handset.adapter.BankOptionsAdapter;
import com.moneydesktop.finance.model.EventMessage.*;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.shared.fragment.AccountTypesFragment;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.SlidingView;
import com.moneydesktop.finance.views.UltimateListView;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountTypesHandsetFragment extends AccountTypesFragment{
    
    private HorizontalListView mBanksHorizontalList;
    private UltimateListView mAccountsListView;
    private View mBankOptionsView;
    private SlidingView mSliderView;
    private String[] mOptions;
    private ListView mBankOptionsList;
    private AccountTypesHandsetAdapter mAdapter;
    private BankOptionsAdapter mBankOptionsAdapter;
    private int mAccountCounter = 0;
    private Handler mHandler;    
	private List<AccountType> mAccountTypesFiltered;
	private AccountBankListAdapter mBankListAdapter;

    @Override
    public void isShowing() {
        super.isShowing();

		setupMenuItems();
    }
	
    public static AccountTypesHandsetFragment newInstance() {

        AccountTypesHandsetFragment fragment = new AccountTypesHandsetFragment();
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.account_types_title).toUpperCase();
    }

    @Override
    public boolean onBackPressed() {
        if (mSliderView != null) {
            mSliderView.dismiss();
            return true;
        }
        return false;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.handset_account_types, null);
        setupView();
        
        return mRoot;
    }
     
    private void setupMenuItems() {

		List<Pair<Integer, List<int[]>>> data = new ArrayList<Pair<Integer, List<int[]>>>();

    	List<int[]> items = new ArrayList<int[]>();
    	items.add(new int[] {R.string.nav_icon_add, R.string.label_add_account});
    	items.add(new int[] {R.string.nav_icon_refresh, R.string.label_refresh_all_accounts});
    	
    	data.add(new Pair<Integer, List<int[]>>(R.string.label_account_menu, items));
    	
    	mActivity.configureRightMenu(data, getType());
    }
    
	public void onEvent(MenuEvent event) {
		if (!event.getFragmentType().equals(FragmentType.ACCOUNT_TYPES)) {
			return;
		}
	    switch (event.getChildPosition()) {
		    case 0:
		    	//add an account
		        ((DashboardHandsetActivity)mActivity).getMenuDrawer().closeMenu();
		    	
				AddAccountHandsetFragment frag = AddAccountHandsetFragment.newInstance();
                mActivity.pushFragment(R.id.accounts_fragment, frag);
		    	break;
		    case 1:
		    	//refresh all accounts
		    	for (Bank bank : mBankList) {
		    		refreshAccount(bank);
		    	}
		    	break;
	    }
	}
    
	private void setupView() {
		mBanksHorizontalList = (HorizontalListView) mRoot.findViewById(R.id.account_types_bank_list_handset);
        mAccountsListView = (UltimateListView) mRoot.findViewById(R.id.handset_account_types_list);
        mAccountTypesFiltered = new ArrayList<AccountType>();
                     
        getNewData();        
        loadBank();
     	
        mAccountsListView.setDividerHeight(0);
        mAccountsListView.setDivider(null);
        mAccountsListView.setChildDivider(null);
                
        mBankListAdapter = new AccountBankListAdapter(mActivity, mBankList);
        mAdapter = new AccountTypesHandsetAdapter(mActivity, mAccountTypesFiltered, mAccountsListView);
        
    	mAccountsListView.setAdapter(mAdapter);
    	mBanksHorizontalList.setAdapter(mBankListAdapter);

        setAllBanksToUpdate();
        updateAllBankStatus();
    	
    	mAccountsListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				showBankDetails(groupPosition, childPosition);
				return true;
			}
		});
    	
    	mBanksHorizontalList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				addBankOrShowBankOptions(view, position);
			}
		});

        mHandler = new Handler();
    	mAccountsListView.expandAll();
        
        mAccountsListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });        
    }   
  
    private void loadBank() {
    	ApplicationContext.getDaoSession().clear();
    	getAllBanks();
    	
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
    }

	private void bankImageListener(final Bank bank, final View v) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBankOptionsView = inflater.inflate(R.layout.account_types_handset_bank_options, null);
        
        mBankOptionsList = (ListView)mBankOptionsView.findViewById(R.id.account_type_handset_options);
        
        updateBankOptions(bank);
        
        mBankOptionsAdapter = new BankOptionsAdapter(mActivity,R.layout.handset_account_types_bank_options_item, bank, mOptions);
        
        addBankOptionsHeader(inflater, mBankOptionsList, bank);
        
        mBankOptionsList.setAdapter(mBankOptionsAdapter);
        
        mBankOptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				TextView txt = (TextView)view.findViewById(R.id.handset_bank_options_name_list_item);
				
				
				if (txt.getText().equals(getString(R.string.fix_bank_more_info_needed)) || txt.getText().equals(getString(R.string.fix_bank_somethings_wrong))) {
					showFixBankFragment(bank);
					return;
				}
				
				if (txt.getText().equals(getString(R.string.credentials))) {					
					showCredentialsFragment(bank);
					return;
				}
				
				if (txt.getText().equals(getString(R.string.delete_institution))) {
					deleteMemberAccount(v, bank);
					return;
				}
			}
		});
        
        if (mSliderView != null) {
            
            //this will make it so we don't end up in a loop of sliding views
            if (mSliderView.getSelectedView() != v) {
                AnimationListener listener = new AnimationListener() {
                    
                    @Override
                    public void onAnimationStart(Animation animation) {
                        
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSliderView = new SlidingView(mActivity, 0, v.getBottom() + (int)UiUtils.convertDpToPixel(20, mActivity), (ViewGroup)mRoot, mBankOptionsView, SlideFrom.BOTTOM, v);                                    
                    }
                };
                mSliderView.dismiss(listener);
                mSliderView = null;                
            } else {
                mSliderView.dismiss();
                mSliderView = null;
            }
        } else {
            mSliderView = new SlidingView(mActivity, 0, v.getBottom() + (int)UiUtils.convertDpToPixel(20, mActivity), (ViewGroup)mRoot, mBankOptionsView, SlideFrom.BOTTOM, v);
        }
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mSliderView != null){
			mSliderView.dismiss();
		}
        mSliderView = null;
	}

	private void updateBankOptions(final Bank bank) {
		if (bank.getProcessStatus() <= BankRefreshStatus.STATUS_SUCCEEDED.index()) {
        	mOptions = mActivity.getResources().getStringArray(R.array.bank_options);
        } else if (bank.getProcessStatus() == BankRefreshStatus.STATUS_MFA.index() ||
        		bank.getProcessStatus() == BankRefreshStatus.STATUS_LOGIN_FAILED.index()) {
        	mOptions = mActivity.getResources().getStringArray(R.array.fix_bank_options);
        } else if (bank.getProcessStatus() == BankRefreshStatus.STATUS_EXCEPTION.index()) {
        	mOptions = mActivity.getResources().getStringArray(R.array.fix_bank_exception_options);
        }
	}
    
	private void addBankOptionsHeader(LayoutInflater inflater, ListView bankOptionsList, Bank bank) {
		View header = inflater.inflate(R.layout.account_types_handset_bank_options_header, null);
        
        TextView bankName = (TextView)header.findViewById(R.id.handset_bank_options_account_name_header);
        TextView numberOfAccounts = (TextView)header.findViewById(R.id.handset_bank_options_number_of_accounts);
        
        bankName.setText(bank.getBankName());
        numberOfAccounts.setText(String.valueOf(bank.getBankAccounts().size()) + " " + getString(R.string.accounts));
        
        Fonts.applySecondaryBoldFont(bankName, 12);
        Fonts.applySecondaryItalicFont(numberOfAccounts, 10);
        
        bankOptionsList.addHeaderView(header);
	}
	
    public void onEvent(final DatabaseSaveEvent event) {
    	//only update screen if Bank or BankAccount Objects have been updated.
    	if (event.getChangedClassesList().contains(Bank.class) || event.getChangedClassesList().contains(BankAccount.class) ) {
	
    		mHandler.post(new Runnable() {
        	    public void run()
        	    {
        	    	getNewData();
        	    	redrawScreen();
        	    	loadBank();
        	    }
        	});
    	}
    }

    private void redrawScreen() {
		mAdapter.notifyDataSetChanged();
		mBankListAdapter.notifyDataSetChanged();
    	mAccountsListView.expandAll();
		if (mBankOptionsAdapter != null) {
			mBankOptionsAdapter.notifyDataSetChanged();
		}
    
	}
    
    private void deleteMemberAccount(final View v, final Bank bank) {
        
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
                                mSliderView.dismiss();
                                removeBank(v, bank);                                
                                break;                                
                            case DialogInterface.BUTTON_NEGATIVE:
                                DialogUtils.dismissAlert();           
                                break;
                        }
                }
        });                
    }
    
	private void removeBank(final View v, final Bank bank) {        
		mBankListAdapter.notifyDataSetChanged();
		
		for (BankAccount account : bank.getBankAccounts()) {
			account.softDeleteBatch();
		}
		
		bank.softDeleteSingle();
	
		
		//start the sync
		Intent intent = new Intent(getActivity(), SyncService.class);
		getActivity().startService(intent);
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
                View bankView = mBanksHorizontalList.getChildAt(i); 
                if (bankView != null) {
                	
                	loadBank();
                	
                	mHandler.post(new Runnable() {
						@Override
						public void run() {
							mBankListAdapter.notifyDataSetChanged();
						}
					});
                	
	                if (bank.getProcessStatus() < 3) {
	                	mHandler.post(new Runnable() {
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
    
    public void onEvent(final UpdateSpecificBankStatus event) {
    	mHandler.post(new Runnable() {
    	    public void run()
    	    {
    	    	setBankToUpdate(event.getBank());
    	    }
    	});
    }
        
    public void setAllBanksToUpdate() {
        
        int i = 0;
        for (Bank bank : mBankList) {

            if (!bank.isDeleted()) {

                i++;
                View bankView = mBanksHorizontalList.getChildAt(i); 
            
                if (bankView != null) {
                	mBankListAdapter.setAllToUpdate(true);
                	mHandler.post(new Runnable() {
						@Override
						public void run() {
							mBankListAdapter.notifyDataSetChanged();
						}
					});
                }
            }
        }
        
    }
    
    public void setBankToUpdate(Bank bank) {
                   	
		mBankListAdapter.setBankToUpdate(bank);
		
    	mHandler.post(new Runnable() {
			@Override
			public void run() {
				mBankListAdapter.notifyDataSetChanged();
			}
		});

    }
     	
    public void onEvent(CheckRemoveBankEvent event) {
    	Bank bankForRemoval = event.getBank();
    	int i = 0;
        for (Bank bank : mBankList) {
        	i++;
        	View bankView = mBanksHorizontalList.getChildAt(i); 
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
        		SyncEngine.sharedInstance().beginSync();
          	}
        	mAccountCounter = 0;
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
	    		if (bank.getProcessStatus() == BankRefreshStatus.STATUS_SUCCEEDED.index() && bank.getBankAccounts().size() == 0) {
		    		SyncEngine.sharedInstance().beginSync();
	    		}
                
                for (Bank bankIterator : mBankList) {
                    i++;
                    View bankView = mBanksHorizontalList.getChildAt(i);
                    if (bankView != null) {
	                    if (bankIterator.getBankName().equals(bank.getBankName())) {

	                    	mBankListAdapter.setAllToUpdate(false);
	                    	
	                    	mHandler.post(new Runnable() {
	    						@Override
	    						public void run() {
	    							mBankListAdapter.notifyDataSetChanged();
	    						}
	    					});
	                    }
                    }
                }                
            }
        });
    }
	
	@Override
	public FragmentType getType() {
		return FragmentType.ACCOUNT_TYPES;
	}
    
	private void deleteBankWithNoAccountsConfirmation(final Bank bank, final View bankView) {
		
		if (bank.getBankName().toLowerCase().equals("manual institution")) {
			
    		bank.softDeleteSingle();
  
    		SyncEngine.sharedInstance().beginSync();
    		
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
	                               
	                        		bank.softDeleteSingle();
	                            		
	                        		//start the sync
	                                SyncEngine.sharedInstance().beginSync();
	                        		
	                        		//remove bank from view
	                        		mBankListAdapter.notifyDataSetChanged();
	                                
	                                break;                                
	                            case DialogInterface.BUTTON_NEGATIVE:
	                                DialogUtils.dismissAlert();
	                                
	                        		//start the sync
	                                SyncEngine.sharedInstance().beginSync();
	                                break;
	                        }
	                }
	        });
		}
	}
    
	public void getNewData() {
		
        AccountTypeDao accountTypeDAO = ApplicationContext.getDaoSession().getAccountTypeDao();        
        
        List<AccountType> allAccountTypes = new ArrayList<AccountType>();
        mAccountTypesFiltered.clear();
       
	    //Get all account types in alphabetical order. Removing the "Unknown" Type. 
		PowerQuery query = new PowerQuery(accountTypeDAO);	
		query.where(mAccountTypeWhere, "Unknown")
		.orderBy(mOrderBy, false);
		
		allAccountTypes = accountTypeDAO.queryRaw(query.toString(), query.getSelectionArgs());
	    
        //Create a new list of AccountTypes that have bank accounts
        for (AccountType accountType : allAccountTypes) {
        	accountType.resetBankAccounts(); //pulls fresh from the DB
        	if (!accountType.getBankAccounts().isEmpty() && !accountType.isDeleted()) {
        		mAccountTypesFiltered.add(accountType);
        	}
        }
        
        
        //the next 3 lines and for loop can be removed if we make a PowerQuery that handles it all
        List<AccountType> tempList = new ArrayList<AccountType>();
        tempList = Arrays.asList(new AccountType[mAccountTypesFiltered.size()]);  
        Collections.copy(tempList, mAccountTypesFiltered);
        
        for (AccountType accountTypes : tempList) {
        	if (accountTypes.getBankAccounts().size() == 1) {
        		if (accountTypes.getBankAccounts().get(0).getBank() == null){
        			mAccountTypesFiltered.remove(accountTypes);
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
//        			bankAccount.softDeleteSingle();
        		}
        	}
        	if (counter == accountType.getBankAccounts().size()) {
        		mAccountTypesFiltered.remove(accountType);
        	}
        	
        }
	}
	
    private void refreshAccount(Bank bank) {
        setBankToUpdate(bank);
        updateBankStatus(bank); 
    }

	private void addBankOrShowBankOptions(View view, int position) {

        //this is the add bank button
		if (position == 0) {

			AddAccountHandsetFragment frag = AddAccountHandsetFragment.newInstance();
            mActivity.pushFragment(R.id.accounts_fragment, frag);

            return;
        }

        // for everything else, open the bank options menu
        bankImageListener(mBankList.get(position - 1), view);
	}

	private void showFixBankFragment(final Bank bank) {
		AccountOptionFixBankHandsetFragment frag = AccountOptionFixBankHandsetFragment.newInstance(bank);
        mActivity.pushFragment(R.id.accounts_fragment, frag);
	}
	
	private void showCredentialsFragment(final Bank bank) {
		AccountOptionsCredentialsHandsetFragment frag = AccountOptionsCredentialsHandsetFragment.newInstance(bank);
        mActivity.pushFragment(R.id.accounts_fragment, frag);
	}
	
	private void showBankDetails(int groupPosition, int childPosition) {
		BankAccount bankAccount = mAccountTypesFiltered.get(groupPosition).getBankAccounts().get(childPosition);
		AccountBankDetailsHandsetFragment frag = AccountBankDetailsHandsetFragment.newInstance(bankAccount);
        mActivity.pushFragment(R.id.accounts_fragment, frag);
	}

}