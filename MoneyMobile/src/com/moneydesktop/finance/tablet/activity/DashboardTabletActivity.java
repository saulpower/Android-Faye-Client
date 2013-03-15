package com.moneydesktop.finance.tablet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.activity.DashboardBaseActivity;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.adapter.TabletGrowPagerAdapter;
import com.moneydesktop.finance.tablet.fragment.AccountTypesTabletFragment;
import com.moneydesktop.finance.tablet.fragment.SettingsTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsTabletFragment;
import com.moneydesktop.finance.util.EmailUtils;
import com.moneydesktop.finance.util.FileIO;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.FixedSpeedScroller;
import com.moneydesktop.finance.views.GrowViewPager;
import com.moneydesktop.finance.views.navigation.NavBarButtons;
import com.moneydesktop.finance.views.navigation.NavWheelView;
import com.moneydesktop.finance.views.navigation.NavWheelView.onNavigationChangeListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DashboardTabletActivity extends DashboardBaseActivity implements onNavigationChangeListener, ViewFactory {
    
    public final String TAG = this.getClass().getSimpleName();

	private ViewFlipper mFlipper;
	private NavWheelView mNavigation;
	private ImageView mHomeButton;
	private TextSwitcher mNavTitle;
    private RelativeLayout mNavBar;
	
//	private Handler mHandler = new Handler();
//	private Runnable mTask = new Runnable() {
//
//        @Override
//        public void run() {
//            viewDidAppear();
//        }
//    };
//
//	private Animation mIn, mOut, mInHome;
    
    @Override
    public void onFragmentAttached(BaseFragment fragment) {
    	super.onFragmentAttached(fragment);

        if (mOnHome && mCurrentFragmentType != FragmentType.DASHBOARD) showFragment(FragmentType.DASHBOARD, true);
    }
	
	@Override
	public void onBackPressed() {
		
		if (mNavigation.isShowing()) {
			
			toggleNavigation();
			return;
			
		} else if (mFragment != null && mFragment.onBackPressed()) {
		    
            return;
            
        } else if (!mOnHome) {
			
        	showFragment(FragmentType.DASHBOARD, true);
			return;
		}
		
		super.onBackPressed();
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_dashboard_view);

        loadFragments();
        
        setupView();
        setupNavigation();
//        loadAnimations();

        setupDashboard();
        
        if (savedInstanceState != null) {
        	
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGER));
            mOnHome = savedInstanceState.getBoolean(KEY_ON_HOME);
            mCurrentFragmentType = (FragmentType) savedInstanceState.getSerializable(KEY_NAVIGATION);
            
            mNavigation.post(new Runnable() {
				
				@Override
				public void run() {

		            mNavigation.setCurrentIndex(mCurrentFragmentType.index());
				}
			});
        }
    }

    private void setupDashboard() {

        mAdapter = new TabletGrowPagerAdapter(mFm);

        mPager.setOnPageChangeListener(mAdapter);
        mPager.setOnScrollChangedListener(mAdapter);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(5);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (mOnHome) {
            
            updateNavBar(getActivityTitle(), false);
            
            if (getActivityTitle().toLowerCase().contains(getResources().getString(R.string.title_activity_dashboard).toLowerCase())) {
                setupTitleBar();
            }
        }
    }
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(KEY_PAGER, mPager.getCurrentItem());
		outState.putBoolean(KEY_ON_HOME, mOnHome);
		outState.putSerializable(KEY_NAVIGATION, mCurrentFragmentType);
	}
    
//    public void configureView(final boolean home) {
//
//    	if (mFlipper == null) return;
//
//
//    	if (home && mFlipper.getDisplayedChild() == 1) {
//
//    		setupTitleBar();
//
//    		mNavigation.setCurrentIndex(0);
//
//    		mInHome.setAnimationListener(new AnimationListener() {
//
//    			public void onAnimationStart(Animation animation) {}
//
//	            public void onAnimationRepeat(Animation animation) {}
//
//	            public void onAnimationEnd(Animation animation) {
//
//	                mOnHome = home;
//
//	            	NavigationEvent navEvent = new EventMessage().new NavigationEvent();
//	                navEvent.setMovingHome(mOnHome);
//	                EventBus.getDefault().post(navEvent);
//	        	}
//			});
//
//			mFlipper.setInAnimation(mInHome);
//			mFlipper.setOutAnimation(mOut);
//			mFlipper.setDisplayedChild(0);
//
//            mNavTitle.setInAnimation(this, R.anim.in_down_fade);
//            mNavTitle.setOutAnimation(this, R.anim.out_down_fade);
//
//            updateNavBar(getActivityTitle(), false);
//
//    	} else if (!home && mFlipper.getDisplayedChild() == 0) {
//
//            mOnHome = home;
//
//    		NavigationEvent navEvent = new EventMessage().new NavigationEvent();
//            navEvent.setMovingHome(home);
//
//    		EventBus.getDefault().post(navEvent);
//			EventBus.getDefault().post(new EventMessage().new ParentAnimationEvent(false, false));
//
//            mFlipper.setInAnimation(mIn);
//            mFlipper.setOutAnimation(this, R.anim.out_up);
//            mFlipper.setDisplayedChild(1);
//
//            mNavTitle.setInAnimation(this, R.anim.in_up_fade);
//            mNavTitle.setOutAnimation(this, R.anim.out_up_fade);
//    	}
//    }
    
    @Override
    public void updateNavBar(String titleString, boolean fragmentTitle) {

        if (fragmentTitle && mFragmentCount == 0) return;
        
        TextView tv = (TextView) mNavTitle.getCurrentView();
        
        if (mNavBar != null && titleString != null && !tv.getText().toString().equalsIgnoreCase(titleString)) {
            
            mNavTitle.setText(titleString);
        }
    }
    
	public void onEvent(NavigationEvent event) {
		
		if (event.getToggleNavigation() != null) {
			toggleNavigation();
		}
	}
	
//	private void loadAnimations() {
//
//        AnimationListener finish = new AnimationListener() {
//
//            public void onAnimationStart(Animation animation) {}
//
//            public void onAnimationRepeat(Animation animation) {}
//
//            public void onAnimationEnd(Animation animation) {
//                viewDidAppear();
//            }
//        };
//
//        mIn = AnimationUtils.loadAnimation(this, R.anim.in_up);
//        mIn.setAnimationListener(finish);
//
//        finish = new AnimationListener() {
//
//            public void onAnimationStart(Animation animation) {}
//
//            public void onAnimationRepeat(Animation animation) {}
//
//            public void onAnimationEnd(Animation animation) {
//        		clearBackStack();
//        	}
//        };
//
//        mOut = AnimationUtils.loadAnimation(this, R.anim.out_down);
//        mOut.setAnimationListener(finish);
//
//        mInHome = AnimationUtils.loadAnimation(this, R.anim.in_down);
//	}

	private void setupView() {
		
        mNavigation = (NavWheelView) findViewById(R.id.nav_wheel);
		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (GrowViewPager) findViewById(R.id.tablet_pager);
        mNavBar = (RelativeLayout) findViewById(R.id.navigation);
        
        mNavTitle = (TextSwitcher) findViewById(R.id.title_bar_name);
        mHomeButton = (ImageView) findViewById(R.id.home);
        
        // Hack fix to adjust scroller velocity on view pager
        try {
            
        	Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true); 
            FixedSpeedScroller scroller = new FixedSpeedScroller(mPager.getContext(), null);
            mScroller.set(mPager, scroller);
            
        } catch (Exception e) {}

        findViewById(R.id.root).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mPager.setPagingEnabled(true);
                        break;
                }

                if (mPager.isPagingEnabled()) {
                    mPager.onTouchEvent(event);
                }

                return true;
            }
        });
	}

    private void setupNavigation() {

        List<Integer> items = new ArrayList<Integer>();
        items.add(R.drawable.tablet_newnav_dashboard_white);
        items.add(R.drawable.tablet_newnav_accounts_white);
        items.add(R.drawable.tablet_newnav_txns_white);
//        items.add(R.drawable.tablet_newnav_budgets_white);
//        items.add(R.drawable.tablet_newnav_reports_white);
        items.add(R.drawable.tablet_newnav_settings_white);

        mNavigation.setItems(items);
        mNavigation.setOnNavigationChangeListener(this);

        mNavTitle.setFactory(this);
        mNavTitle.setInAnimation(this, R.anim.in_up_fade);
        mNavTitle.setOutAnimation(this, R.anim.out_up_fade);

        mHomeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mOnHome) {
                    showFragment(FragmentType.DASHBOARD, true);
                }
            }
        });
    }
	
	public void toggleNavigation() {
		
		if (!mNavigation.isShowing()) {
			mNavigation.showNav();
		} else {
			mNavigation.hideNav();
		}
	}
	
	public void showDropdownFragment(FragmentType fragment) {

	    Intent i = new Intent(this, DropDownTabletActivity.class);
	    i.putExtra(Constant.EXTRA_FRAGMENT, fragment);
	    startActivity(i);
	}

	private void setupTitleBar() {

		String[] icons = getResources().getStringArray(R.array.dashboard_title_bar_icons);

		ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();

		onClickListeners.add(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});

		onClickListeners.add(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(DashboardTabletActivity.this, "help", Toast.LENGTH_LONG).show();
			}
		});

		onClickListeners.add(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SyncEngine.sharedInstance().beginSync();
			}
		});

		new NavBarButtons(DashboardTabletActivity.this, icons, onClickListeners);
	}
	
	private void sendEmail() {

        Bitmap image = UiUtils.convertViewToBitmap(mPager.getChildAt(mPager.getCurrentItem()));
        String path = FileIO.saveBitmap(this, image, mPager.getCurrentItem() + "");
        
        EmailUtils.sendEmail(this, getString(R.string.email_dashboard_subject), "", path);
	}

    private void loadFragments() {

        loadFragment(R.id.accounts_fragment, AccountTypesTabletFragment.newInstance());
        loadFragment(R.id.transactions_fragment, TransactionsTabletFragment.newInstance());
        loadFragment(R.id.settings_fragment, SettingsTabletFragment.newInstance());
    }

    private void loadFragment(int containerViewId, BaseFragment fragment) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(containerViewId, fragment);
        ft.commit();
    }
	
	@Override
    public void showFragment(FragmentType fragmentType, boolean moveUp) {

        if (mCurrentFragmentType == fragmentType) return;

        mCurrentFragmentType = fragmentType;

        mOnHome = (FragmentType.DASHBOARD == fragmentType);

        String title = getActivityTitle();

        if (mOnHome) {

            SyncEngine.sharedInstance().syncCheck();

        } else {

            title = mFragments.get(mCurrentFragmentType).getFragmentTitle();
        }

        updateNavBar(title, false);

        AnimationFactory.slideTransition(mFlipper, fragmentType.index(), mStart, mFinish, moveUp ? AnimationFactory.FlipDirection.BOTTOM_TOP : AnimationFactory.FlipDirection.TOP_BOTTOM, TRANSITION_DURATION);
    }
    
    public void showNextPage() {
    	int item = mPager.getCurrentItem() + 1;
    	mPager.setCurrentItem(item, true);
    }
    
    public void showPrevPage() {
    	int item = mPager.getCurrentItem() - 1;
    	mPager.setCurrentItem(item, true);
    }

	@Override
	public void onNavigationChanged(int index) {
		
		if (mCurrentFragmentType.index() == index) return;

        boolean moveUp = (mCurrentFragmentType.index() - index) < 0;

        // Update navigation title animation direction
        mNavTitle.setInAnimation(this, moveUp ? R.anim.in_up_fade : R.anim.in_down_fade);
        mNavTitle.setOutAnimation(this, moveUp ? R.anim.out_up_fade : R.anim.out_down_fade);
		
		showFragment(FragmentType.fromInteger(index), moveUp);
	}

    @Override
    public View makeView() {
        
        TextView t = new TextView(this);
        FrameLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        t.setLayoutParams(params);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.WHITE);
        t.setBackgroundColor(Color.TRANSPARENT);
        t.setShadowLayer(2.0f, 1.0f, 1.0f, getResources().getColor(R.color.gray8));

        Fonts.applyPrimaryBoldFont(t, 14);
        
        return t;
    }

	@Override
	public String getActivityTitle() {
		return getString(R.string.title_activity_dashboard).toUpperCase() + ": " + User.getCurrentUser().getUserName();
	}
}