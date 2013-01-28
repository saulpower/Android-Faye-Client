package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AccountsExpandableListAdapter;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.model.EventMessage.BankStatusUpdateEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Enums.BankRefreshStatus;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.PopupWindowAtLocation;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;
import com.moneydesktop.finance.views.AnimatedListView.SlideExpandableListAdapter;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AccountTypesTabletFragment extends BaseFragment implements FragmentVisibilityListener{
    private ListView mListView;
    private static SlidingDrawerRightSide sRightDrawer;
    private View mFooter;
    private PopupWindowAtLocation mPopup;
    private List<Bank> mBankList;
    private List<Bank> mBanksForDeletion;
    LinearLayout mPanelLayoutHolder;
    List<AccountType> mAccountTypesFiltered;
    private Handler mHandler;    
	
	public static AccountTypesTabletFragment newInstance(int position) {	
		AccountTypesTabletFragment frag = new AccountTypesTabletFragment();
		frag.setPosition(position);
		
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
		setupView();
		
		mHandler = new Handler();
		
		return mRoot;
	}

    private void setupView() {
	    setupTitleBar(mActivity);
	    mActivity.updateNavBar(getResources().getString(R.string.title_activity_accounts));
	    
	    mPanelLayoutHolder = (LinearLayout)mRoot.findViewById(R.id.panel_layout_holder);
        
		AccountTypeDao accountDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
		
        List<AccountType> accountTypes = accountDAO.loadAll();
        mAccountTypesFiltered = new ArrayList<AccountType>();
        
        
        for (AccountType type : accountTypes) {  //This  could be optimized by throwing a "where" in the query builder
        	if (!type.getBankAccounts().isEmpty() && !type.getAccountTypeName().equals("Unknown")) {
        	    mAccountTypesFiltered.add(type);
        	}
        }

        
        if (!mAccountTypesFiltered.isEmpty()) {        	
            mListView.addFooterView(mFooter);
            
            //This sets the GroupView
            ListAdapter adapter = new AccountsExpandableListAdapter(getActivity(),  
                    R.layout.account_type_group, 
                    R.id.account_type_group_name, 
                    mAccountTypesFiltered);
         
            
            //this animates and sets the ChildView
            mListView.setAdapter(
                    new SlideExpandableListAdapter(
                            adapter, 
                            R.id.account_type_group_container, 
                            R.id.expandable,
                            getActivity(),
                            mAccountTypesFiltered));
            
        } else {
        	Toast.makeText(mActivity, "No Accounts types that have bank accounts...show empty state", Toast.LENGTH_SHORT).show();
        }

        
        //This allows you to grab the panel and close it by touching and dragging on any part of the panel instead of just the handle
        mPanelLayoutHolder.setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View view, MotionEvent event) {
				return true;
			}
		});

        
        final ViewGroup.LayoutParams layoutParams = mPanelLayoutHolder.getLayoutParams();
        layoutParams.width = UiUtils.getMinimumPanalWidth(mActivity);
        mPanelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams);
        initializeDrawer();
	}
		
	private void setupTitleBar(final Activity activity) {
	    
	    String[] icons = activity.getResources().getStringArray(R.array.account_types_title_bar_icons);
	    
	    ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
	    
	    onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getCurrentUser().getCanSync()){
                    Toast.makeText(activity, "add", Toast.LENGTH_LONG).show();
                } else {
                   //Dialog....can't update data 
                    DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
                }
            }
        });
	    
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (User.getCurrentUser().getCanSync()){
                        //start the sync
                        Intent intent = new Intent(getActivity(), SyncService.class);
                        getActivity().startService(intent);
                        
                        setAllBanksToUpdate();
                    } else {
                       //Dialog....can't update data 
                        DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
                    }
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
	    mBankList = ApplicationContext.getDaoSession().getBankDao().loadAll();
	    mBanksForDeletion = new ArrayList<Bank>();
	    
		mPanelLayoutHolder.addView(getPanelHeader());

        //For every bank that is attached, add it to the Drawer
        for (Bank bank : mBankList) {
            //create the view to be attached to Drawer
            mPanelLayoutHolder.addView(populateDrawerView(bank));
        }
        if (User.getCurrentUser().getCanSync()) {
            updateAllBankStatus();
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
        
        for (Bank bank : mBanksForDeletion) {
            bank.setDeleted(true);
        }
        
        if (event.isFinished()) {
            
            int i = 0;
            for (Bank bank : mBankList) {

                if (!bank.isDeleted()) {

                    i++;
                    View bankView = mPanelLayoutHolder.getChildAt(i); 
                
                    ImageView status = (ImageView) bankView.findViewById(R.id.bank_status);
                    
                    setBanner(bank, status);
             //       setAccountsBannerUpdate(false);
                    Log.d("Bank sync done", "Just set the Banners for " + bank.getBankName());
                }
            }
            
            Log.d("Bank Status", "NOW UPDATING");
            updateAllBankStatus();
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
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View bankTypeAccountView = layoutInflater.inflate(R.layout.bank_account, null);
        ImageView bankImage = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_image);  
        final ImageView booklet = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_bankbook);
        
        BankLogoManager.getBankImage(bankImage, bank.getBankAccounts().get(0).getInstitutionId());
        
        ImageView status = (ImageView) bankTypeAccountView.findViewById(R.id.bank_status);
        
        status.setVisibility(View.VISIBLE);
        setBanner(bank, status);
        
        TextView bankName = (TextView)bankTypeAccountView.findViewById(R.id.account_bank_name);
        
        bankName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        bankName.setText(bank.getBankName());
        
        bankTypeAccountView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View bankAccountView) {
				RelativeLayout parentView = (RelativeLayout)getActivity().findViewById(R.id.account_types_container);
				
				List<OnClickListener> onClickListeners = new ArrayList<View.OnClickListener>();
				
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
                            deleteAccount(bankAccountView, mPanelLayoutHolder, bank);
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
						    Toast.makeText(getActivity(), "UPDATE USERNAME AND PASSWORD", Toast.LENGTH_SHORT).show();
						} else {
						    DialogUtils.alertDialog(getResources().getString(R.string.feature_not_available), getResources().getString(R.string.feature_not_available_message), getActivity());
						}
                        mPopup.fadeOutTransparency();
					}
				});
				
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
		                        getActivity().getResources().getStringArray(R.array.bank_selection_popup), 
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
    
    private void deleteAccount(final View v, final LinearLayout panelView, final Bank bank) {
            
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
                                mBanksForDeletion.add(bank);
                                mPopup.fadeOutTransparency();
                                
                                //start the sync
                                Intent intent = new Intent(getActivity(), SyncService.class);
                                getActivity().startService(intent);
                                
                                //remove bank from view
                                panelView.removeView(v);
                                break;
                                
                            case DialogInterface.BUTTON_NEGATIVE:
                                DialogUtils.dismissAlert();
                                mPopup.fadeOutTransparency();            
                                break;
                        }
                }
        });
                
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
        drawerLayoutParams.width = (int) (layoutParams.width + UiUtils.convertDpToPixel(7, getActivity()));
        drawerLayoutParams.height = UiUtils.getScreenHeight(getActivity()) ;
        sRightDrawer.setLayoutParams(drawerLayoutParams);

        return sRightDrawer;
    }
	
	@Override
	public String getFragmentTitle() {
		return null;
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