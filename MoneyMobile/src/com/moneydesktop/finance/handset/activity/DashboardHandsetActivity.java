package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.handset.adapter.HandsetGrowPagerAdapter;
import com.moneydesktop.finance.handset.adapter.MenuHandsetAdapter;
import com.moneydesktop.finance.handset.adapter.MenuRightHandsetAdapter;
import com.moneydesktop.finance.handset.fragment.AccountTypesHandsetFragment;
import com.moneydesktop.finance.handset.fragment.SettingsHandsetFragment;
import com.moneydesktop.finance.handset.fragment.SpendingChartHandsetFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsHandsetFragment;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.shared.activity.DashboardBaseActivity;
import com.moneydesktop.finance.shared.adapter.GrowPagerAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.GrowViewPager;
import com.moneydesktop.finance.views.UltimateListView;
import com.moneydesktop.finance.views.ViewAnimator;
import com.moneydesktop.finance.views.navigation.NavBarView;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.OnDrawerStateChangeListener;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardHandsetActivity extends DashboardBaseActivity implements OnItemClickListener {
	
	public static final String TAG = "DashboardActivity";
	
	private static final String STATE_MENUDRAWER = "menuDrawer";

	private static final int DEFAULT_FRAGMENTS = 8;
	
	private ViewFlipper mMenuFlipper;
	private ViewAnimator mFlipper;
	private TextView mUpdateLabel, mUpdate;
	private NavBarView mRefresh;
	private UltimateListView mRightMenuList;
	
	private MenuRightHandsetAdapter mRightMenuAdapter;

	private GrowPagerAdapter mAdapter;
	private GrowViewPager mPager;
	
    private RelativeLayout mNavBar;
    private TextView mTitle, mLeft, mRight;
    
    private MenuHandsetAdapter mMenuAdapter;
    private MenuDrawer mMenuDrawerLeft, mMenuDrawerRight;
    
    private Animation mLeftIn, mLeftOut, mRightIn, mRightOut;
    
    private OnMenuChangeListener mOnMenuChangeListener;

    protected SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM.dd.yyyy '@' h:mma", Locale.US);
	
	public FragmentType getCurrentFragmentType() {
		return mCurrentFragmentType;
	}

	public GrowPagerAdapter getPagerAdapter() {
	    return mAdapter;
	}
    
    public void setOnMenuChangeListener(OnMenuChangeListener mOnMenuChangeListener) {
		this.mOnMenuChangeListener = mOnMenuChangeListener;
	}

	@Override
    public void onFragmentAttached(BaseFragment fragment) {
    	super.onFragmentAttached(fragment);
    	
    	resetRightMenu();
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DateFormatSymbols sym = mDateFormatter.getDateFormatSymbols();
        sym.setAmPmStrings(new String[] { "am", "pm" });
        mDateFormatter.setDateFormatSymbols(sym);

        loadFragments();
        
        setupMenus();
        
        loadAnimations();
        
        setupView();
        applyFonts();
        setupListeners();
        
        mAdapter = new HandsetGrowPagerAdapter(mFragmentManager);
        
        mPager.setOnPageChangeListener(mAdapter);
        mPager.setOnScrollChangedListener(mAdapter);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(5);

        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGER, 0));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    	
        // Only run if there are no fragments currently showing
        if (mFlipper.getDisplayedChild() == 0) {
            updateNavBar(getActivityTitle());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");
    }
	
	@Override
	public void onBackPressed() {

        final int drawerStateLeft = mMenuDrawerLeft.getDrawerState();
        final int drawerStateRight = mMenuDrawerRight.getDrawerState();
        
		if (drawerStateLeft == MenuDrawer.STATE_OPEN || drawerStateLeft == MenuDrawer.STATE_OPENING) {
			
            mMenuDrawerLeft.closeMenu();
            return;
            
        } else if (drawerStateRight == MenuDrawer.STATE_OPEN || drawerStateRight == MenuDrawer.STATE_OPENING) {
			
        	mMenuDrawerRight.closeMenu();
            return;
            
        } else if (mFragment != null && mFragment.onBackPressed()) {
		    
            return;
            
        } else if (getStackCount() > 0) {

			popFragment();
			
			return;
		}

        super.onBackPressed();
	}

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        
        mMenuDrawerLeft.restoreState(inState.getParcelable(STATE_MENUDRAWER));
        mPager.setCurrentItem(inState.getInt(KEY_PAGER, 0));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(KEY_PAGER, mPager.getCurrentItem());
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawerLeft.saveState());
    }

    private void loadFragments() {

        loadFragment(R.id.accounts_fragment, AccountTypesHandsetFragment.newInstance());
        loadFragment(R.id.transactions_fragment, TransactionsHandsetFragment.newInstance());
        loadFragment(R.id.spending_fragment, SpendingChartHandsetFragment.newInstance());
        loadFragment(R.id.settings_fragment, SettingsHandsetFragment.newInstance());
    }

    private void setupView() {

        mFlipper = (ViewAnimator) findViewById(R.id.flipper);
        mPager = (GrowViewPager) findViewById(R.id.pager);

        mNavBar = (RelativeLayout) findViewById(R.id.nav_bar);
        mTitle = (TextView) mNavBar.findViewById(R.id.title);
        mLeft = (TextView) mNavBar.findViewById(R.id.left_button);
        mRight = (TextView) mNavBar.findViewById(R.id.right_button);
    }

    private void setupListeners() {

        mLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (getStackCount() <= 0) {

                    mMenuDrawerRight.closeMenu();
                    mMenuDrawerLeft.toggleMenu();

                } else {

                    onBackPressed();
                }
            }
        });

        mRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMenuDrawerRight.toggleMenu();
            }
        });
    }

    private void applyFonts() {

        Fonts.applyPrimarySemiBoldFont(mTitle, 10);
        Fonts.applyNavIconFont(mLeft, 24);
        Fonts.applyNavIconFont(mRight, 24);
        Fonts.applyNavIconFont(mRefresh, 24);
        Fonts.applyPrimaryFont(mUpdateLabel, 8);
        Fonts.applySecondaryItalicFont(mUpdate, 8);
    }
    
    @Override
    public void addMenuItems(List<Pair<Integer, List<int[]>>> data) {
    	mRightMenuAdapter.setData(data);
    }
    
    @Override
    public void setMenuFragment(FragmentType fragmentType) {
    	mRightMenuAdapter.setCurrentFragmentType(fragmentType);
    }

    @Override
	public void pushMenuView(View view) {
    	
    	mMenuFlipper.setInAnimation(mRightIn);
    	mMenuFlipper.setOutAnimation(mLeftOut);
    	
    	mMenuFlipper.addView(view);
    	mMenuFlipper.showNext();
    }

    @Override
	public void popMenuView() {
    	
    	if (mMenuFlipper.getChildCount() == 1) return;
    	
    	mMenuFlipper.setInAnimation(mLeftIn);
    	mMenuFlipper.setOutAnimation(mRightOut);
    	
    	mMenuFlipper.showPrevious();
    	mMenuFlipper.removeViewAt(mMenuFlipper.getChildCount() - 1);
    }
    
    @Override
    public View getMenuParent() {
		return mMenuFlipper;
	}
    
    public MenuDrawer getMenuDrawer () {
    	return mMenuDrawerRight;
    }
	
	private void resetRightMenu() {
		
    	if (mMenuFlipper == null) return;

    	mRightMenuAdapter.resetMenu();
    	
    	if (mMenuFlipper.getDisplayedChild() != 0) {
    		mMenuFlipper.setDisplayedChild(0);
    	}
    	
    	final int count = mMenuFlipper.getChildCount();
    	if (count > 1) {
    		mMenuFlipper.removeViews(1, count - 1);
    	}
	}
    
    private void setupMenus() {

        mMenuDrawerLeft = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawerLeft.setContentView(R.layout.handset_dashboard_view);
        mMenuDrawerLeft.setMenuView(R.layout.handset_menu_left);
        mMenuDrawerLeft.setMenuSize((int) UiUtils.getDynamicPixels(this, 125));
        mMenuDrawerLeft.setOnDrawerStateChangeListener(new OnDrawerStateChangeListener() {
			
			@Override
			public void onDrawerStateChange(int oldState, int newState) {
				
				if (mOnMenuChangeListener != null) {
					mOnMenuChangeListener.onLeftMenuStateChanged(oldState, newState);
				}
			}
		});
        
        mMenuAdapter = new MenuHandsetAdapter(this, R.layout.handset_menu_item, Constant.MENU_ITEMS);
        ListView list = (ListView) findViewById(R.id.menu_list);
        list.setAdapter(mMenuAdapter);
        list.setOnItemClickListener(this);
        
    	mMenuAdapter.setSelectedIndex(0);
    	mMenuAdapter.notifyDataSetChanged();
    	
    	setupMenuRight();
    }
    
    private void setupMenuRight() {

        mMenuDrawerRight = (MenuDrawer) findViewById(R.id.drawer);
        mMenuDrawerRight.setMenuSize((int) (UiUtils.getScreenWidth(this) * 0.8f));
        mMenuDrawerRight.setTouchMode(MenuDrawer.TOUCH_MODE_BEZEL);
        mMenuDrawerRight.setOnDrawerStateChangeListener(new OnDrawerStateChangeListener() {
			
			@Override
			public void onDrawerStateChange(int oldState, int newState) {
				
				if (mOnMenuChangeListener != null) {
					mOnMenuChangeListener.onRightMenuStateChanged(oldState, newState);
				}
				
				if (newState == MenuDrawer.STATE_CLOSED || newState == MenuDrawer.STATE_OPEN) {
					
					boolean opened = newState == MenuDrawer.STATE_OPEN;
					mMenuDrawerLeft.setTouchMode(opened
	                        ? MenuDrawer.TOUCH_MODE_NONE
	                        : MenuDrawer.TOUCH_MODE_BEZEL);
				}
			}
		});
        
        mMenuFlipper = (ViewFlipper) findViewById(R.id.mdMenu);
    	mUpdateLabel = (TextView) findViewById(R.id.updated_label);
    	mUpdate = (TextView) findViewById(R.id.updated_status);
    	mRefresh = (NavBarView) findViewById(R.id.refresh);
    	mRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SyncEngine.sharedInstance().beginSync();
			}
		});
    	
    	setLastUpdated();
    	
    	mRightMenuList = (UltimateListView) findViewById(R.id.menu_right_list);
    	mRightMenuList.setDividerHeight(0);
    	mRightMenuList.setDivider(null);
    	mRightMenuList.setChildDivider(null);
    	
        mRightMenuAdapter = new MenuRightHandsetAdapter(this, mRightMenuList);
        
        mRightMenuList.setAdapter(mRightMenuAdapter);
        mRightMenuList.expandAll();
    }
    
    private void setLastUpdated() {

    	long updateDate = Preferences.getLong(Preferences.KEY_LAST_SYNC, -1l);
    	
    	if (updateDate != -1l && mUpdate != null) {
    		mUpdate.setText(mDateFormatter.format((new Date(updateDate))));
    	}
    }
    
    private void loadAnimations() {
    	mLeftIn = AnimationUtils.loadAnimation(this, R.anim.in_left);
    	mLeftOut = AnimationUtils.loadAnimation(this, R.anim.out_left);
    	mRightIn = AnimationUtils.loadAnimation(this, R.anim.in_right);
    	mRightOut = AnimationUtils.loadAnimation(this, R.anim.out_right);
    }
	
	public void onEvent(SyncEvent event) {
		
		if (event.isFinished()) {
	    	setLastUpdated();
		}
	}
  	
	@Override
    public void showFragment(FragmentType type, boolean moveUp) {
    	
		if (mCurrentFragmentType == type) return;

    	resetRightMenu();
    	mCurrentFragmentType = type;
    	
    	int index = mCurrentFragmentType.index();
    	
    	// Adjustment for ordering issues the must remain so
    	// things work properly on the tablet version
    	if (index == 3) {
    		index = 4;
    	} else if (index == 4) {
    		index = 3;
    	}
    	
    	mMenuAdapter.setSelectedIndex(index);
    	
    	if (FragmentType.DASHBOARD == mCurrentFragmentType) {

        	SyncEngine.sharedInstance().syncCheck();
            updateNavBar(getActivityTitle());
    	}

        AnimationFactory.slideTransition(mFlipper, type.index(), mStart, mFinish, moveUp ? FlipDirection.BOTTOM_TOP : FlipDirection.TOP_BOTTOM, TRANSITION_DURATION);
    }
	
	/**
	 * Update the navigation bar with the passed in title.  Configure the
	 * back button if necessary.
	 * 
	 * @param titleString the title for the navigation bar
	 * @return 
	 */
	public void updateNavBar(String titleString) {
		
		if (titleString != null) {
			mTitle.setText(titleString);
		}
	}

    @Override
    public void stackChange() {
        configureBackButton();
    }
	
	private void configureBackButton() {

		mLeft.setText(getStackCount() > 0 ? R.string
                .nav_icon_back : R.string.nav_icon_menu_left);
	}

	@Override
	public String getActivityTitle() {
		return getString(R.string.title_activity_handset_dashboard).toUpperCase();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		
		if (mMenuAdapter.getSelectedIndex() == position) return;
		
		boolean moveUp = (mMenuAdapter.getSelectedIndex() - position) < 0;
    	
        mMenuDrawerLeft.closeMenu();

        // Adjustment for ordering issues the must remain so
        // things work properly on the tablet version
        if (position == 3) {
            position = 4;
        } else if (position == 4) {
            position = 3;
        }

        clearBackStack();
        showFragment(FragmentType.fromInteger(position), moveUp);
	}
	
	public interface OnMenuChangeListener {
		public void onLeftMenuStateChanged(int oldState, int newState);
		public void onRightMenuStateChanged(int oldState, int newState);
	}
}
