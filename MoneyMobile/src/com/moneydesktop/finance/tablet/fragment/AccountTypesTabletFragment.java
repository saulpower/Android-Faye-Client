package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.PopupWindowAtLocation;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;
import com.moneydesktop.finance.views.AnimatedListView.SlideExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class AccountTypesTabletFragment extends BaseFragment implements FragmentVisibilityListener{
    private ListView mListView;
    private static SlidingDrawerRightSide sRightDrawer;
    private View mFooter;
    private PopupWindowAtLocation mPopup;
	
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
		
		return mRoot;
	}
	
	private void setupView() {
	    setupTitleBar(mActivity);
	    mActivity.updateNavBar(getResources().getString(R.string.title_activity_accounts));
	    
		final LinearLayout panelLayoutHolder = (LinearLayout)mRoot.findViewById(R.id.panel_layout_holder);
        
		AccountTypeDao accountDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
		
        List<AccountType> accountTypes = accountDAO.loadAll();
        List<AccountType> accountTypesFiltered = new ArrayList<AccountType>();
        
        
        for (AccountType type : accountTypes) {  //This  could possibly be optimized by throwing a "where" in the query builder
        	if (!type.getBankAccounts().isEmpty()) {
        		accountTypesFiltered.add(type);
        	}
        }

        
        if (!accountTypesFiltered.isEmpty()) {        	
            mListView.addFooterView(mFooter);
            
            //This sets the GroupView
            ListAdapter adapter = new AccountsExpandableListAdapter(getActivity(),  
                    R.layout.account_type_group, 
                    R.id.account_type_group_name, 
                    accountTypesFiltered);
         
            
            //this animates and sets the ChildView
            mListView.setAdapter(
                    new SlideExpandableListAdapter(
                            adapter, 
                            R.id.account_type_group_container, 
                            R.id.expandable,
                            getActivity(),
                            accountTypesFiltered));
            
        } else {
        	Toast.makeText(mActivity, "No Accounts types that have bank accounts...show empty state", Toast.LENGTH_SHORT).show();
        }

        
        //This allows you to grab the panel and close it by touching and dragging on any part of the panel instead of just the handle
        panelLayoutHolder.setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View view, MotionEvent event) {
				return true;
			}
		});

        
        final ViewGroup.LayoutParams layoutParams = panelLayoutHolder.getLayoutParams();
        layoutParams.width = UiUtils.getMinimumPanalWidth(mActivity);
        panelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams, mActivity);
        initializeDrawer(panelLayoutHolder);
	}
	
	
	private void setupTitleBar(final Activity activity) {
	    
	    String[] icons = activity.getResources().getStringArray(R.array.account_types_title_bar_icons);
	    
	    ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
	    
	    onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "add", Toast.LENGTH_LONG).show();
            }
        });
	    
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "refresh", Toast.LENGTH_LONG).show();
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
	private void initializeDrawer (LinearLayout panelLayoutHolder) {
		List<Bank> banksList = ApplicationContext.getDaoSession().getBankDao().loadAll();
    	
    	panelLayoutHolder.addView(getPanelHeader());

        //For every bank that is attached, add it to the Drawer
        for (Bank bank : banksList) {
            //create the view to be attached to Drawer
        	panelLayoutHolder.addView(populateDrawerView(bank, panelLayoutHolder));
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
     * Creates a View of a bank represented on the right panel.
     * @param bank -the bank to be added
     * @param panelLayoutHolder 
     * @return bank view 
     */
	private View populateDrawerView (final Bank bank, final LinearLayout panelLayoutHolder) {
        LayoutInflater layoutInflater = mActivity.getLayoutInflater();
        final View bankTypeAccountView = layoutInflater.inflate(R.layout.bank_account, null);
        ImageView bankImage = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_image);  
        final ImageView booklet = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_bankbook);
        
        BankLogoManager.getBankImage(bankImage, bank.getBankId());
        
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
						Toast.makeText(getActivity(), "REFRESH DATA", Toast.LENGTH_SHORT).show();
					}
				});
				
				onClickListeners.add(new OnClickListener() { 	
					@Override
					public void onClick(View v) {
						deleteAccount(bankAccountView, panelLayoutHolder, bank);
					}

				});
				
				onClickListeners.add(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getActivity(), "UPDATE USERNAME AND PASSWORD", Toast.LENGTH_SHORT).show();
					}
				});
				
		        
				mPopup = new PopupWindowAtLocation(getActivity(), parentView, sRightDrawer.getLeft(), (int)bankTypeAccountView.getTop(), getActivity().getResources().getStringArray(R.array.bank_selection_popup), onClickListeners, booklet);
			}
		});

        return bankTypeAccountView;
    }
	
	
	
    private void deleteAccount(final View v, final LinearLayout panelView, final Bank bank) {
        Toast.makeText(getActivity(), "REMOVE", Toast.LENGTH_SHORT).show();
            
//        DialogUtils.alertDialog(String.format(getString(R.string.delete_bank_title), bank.getBankName()), 
//                getString(R.string.delete_bank_message), 
//                getString(R.string.label_yes).toUpperCase(), 
//                getString(R.string.label_no).toUpperCase(), 
//                getActivity(), 
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {   
//                        DialogUtils.dismissAlert();
//                        //set the bank for deletion
//                        bank.setDeleted(true);
//                        mPopup.fadeOutTransparency();
//                        
//                        //start the sync
//                        Intent intent = new Intent(getActivity(), SyncService.class);
//                        getActivity().startService(intent);
//                        
//                        //remove bank from view
//                      //  panelView.removeView(v);
//                    }
//        }, new DialogInterface.OnClickListener() {
//            
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                DialogUtils.dismissAlert();
//                mPopup.fadeOutTransparency();                
//            }
//        });
                
    }

	/**
	 * Drawer's width is set to a percentage of screen.
	 * @param layoutParams
	 * @param activity
	 * @return the drawer
	 */
    public static SlidingDrawerRightSide setupDrawer (final ViewGroup.LayoutParams layoutParams, Activity activity) {
        final ViewGroup.LayoutParams drawerLayoutParams = sRightDrawer.getLayoutParams();

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        int width = bitmap.getWidth();
        bitmap.recycle();

        drawerLayoutParams.width = layoutParams.width + width;
        drawerLayoutParams.height = UiUtils.getScreenHeight(activity) ;
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