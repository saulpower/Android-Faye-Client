package com.moneydesktop.finance.tablet.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher.ViewFactory;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.FragmentVisibilityListener;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.tablet.adapter.GrowPagerAdapter;
import com.moneydesktop.finance.tablet.fragment.AccountTypesTabletFragment;
import com.moneydesktop.finance.tablet.fragment.SettingsTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsTabletFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.FixedSpeedScroller;
import com.moneydesktop.finance.views.GrowViewPager;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.NavWheelView;
import com.moneydesktop.finance.views.NavWheelView.onNavigationChangeListener;

import de.greenrobot.event.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DashboardTabletActivity extends DashboardBaseActivity implements onNavigationChangeListener, ViewFactory {
    
    public final String TAG = this.getClass().getSimpleName();

	private ViewFlipper mFlipper;
	private GrowViewPager mPager;
	private GrowPagerAdapter mAdapter;
	private NavWheelView mNavigation;
	private ImageView mHomeButton;
	private TextSwitcher mNavTitle;
	
	private Handler mHandler = new Handler();
	private Runnable mTask = new Runnable() {
        
        @Override
        public void run() {
            viewDidAppear();
        }
    };
	
	private Animation mIn, mOut;
	
	private FragmentType mCurrentIndex = FragmentType.DASHBOARD;

	public GrowPagerAdapter getPagerAdapter() {
	    return mAdapter;
	}
	
	@Override
	public void onBackPressed() {
		
		if (mNavigation.isShowing()) {
			
			toggleNavigation();
			
		} else if (mFragment != null && mFragment.onBackPressed()) {
		    
            return;
            
        } else if (mFlipper.indexOfChild(mFlipper.getCurrentView()) == 1) {
			
			if (mFragmentCount == 1 && !mOnHome) {
				configureView(true);
			} else {
				navigateBack();
			}
			
		} else {
			super.onBackPressed();
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_dashboard_view);
        
        setupView();
        loadAnimations();
        
        mAdapter = new GrowPagerAdapter(mFm);
        
        mPager.setOnPageChangeListener(mAdapter);
        mPager.setOnScrollChangedListener(mAdapter);
        mPager.setAdapter(mAdapter);
        
        if (mPager != null && savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt("pager"));
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Fragment frag = mFm.findFragmentById(R.id.fragment);
        
        if (frag != null) {
            frag.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Only run if there are no fragments currently showing
        if (mFragmentCount == 0) {
            
            updateNavBar(getActivityTitle());
            
            if (getActivityTitle().equals(getResources().getString(R.string.title_activity_dashboard))) {
                setupTitleBar();
            }
        }
    }
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("pager", mPager.getCurrentItem());
	}
    
    @Override
    public void configureView(final boolean home) {
		super.configureView(home);

        mOnHome = home;
        setupTitleBar();
        
    	if (home) {
    		
    		mCurrentIndex = FragmentType.DASHBOARD;
    		mNavigation.setCurrentIndex(0);
	        
			mFlipper.setInAnimation(this, R.anim.in_down);
			mFlipper.setOutAnimation(mOut);
			mFlipper.setDisplayedChild(getNextIndex());

            mNavTitle.setInAnimation(this, R.anim.in_down_fade);
            mNavTitle.setOutAnimation(this, R.anim.out_down_fade);
			
    	} else {
	    	
			EventBus.getDefault().post(new EventMessage().new ParentAnimationEvent(false, false));
            
            mFlipper.setInAnimation(mIn);
            mFlipper.setOutAnimation(this, R.anim.out_up);
            mFlipper.setDisplayedChild(getNextIndex());

            mNavTitle.setInAnimation(this, R.anim.in_up_fade);
            mNavTitle.setOutAnimation(this, R.anim.out_up_fade);
    	}
    }
    
    private int getNextIndex() {
        
        return (mFlipper.getDisplayedChild() + 1) % mFlipper.getChildCount();
    }
   
    
    @Override
    public void updateNavBar(String titleString) {
        
        TextView tv = (TextView) mNavTitle.getCurrentView();
        
        if (mNavBar != null && titleString != null && !tv.getText().toString().equalsIgnoreCase(titleString)) {
            
            mNavTitle.setText(titleString.toUpperCase());
        }
    }
    
	
	public void onEvent(NavigationEvent event) {
		
		if (event.isShowing() == null && event.getDirection() == null) {
			toggleNavigation();
		}
	}
	
	private void loadAnimations() {

        AnimationListener finish = new AnimationListener() {
            
            public void onAnimationStart(Animation animation) {}
            
            public void onAnimationRepeat(Animation animation) {}
            
            public void onAnimationEnd(Animation animation) {
                viewDidAppear();
            }
        };
        
        mIn = AnimationUtils.loadAnimation(this, R.anim.in_up);
        mIn.setAnimationListener(finish);
        
        finish = new AnimationListener() {
            
            public void onAnimationStart(Animation animation) {
                navigateBack();
            }
            
            public void onAnimationRepeat(Animation animation) {}
            
            public void onAnimationEnd(Animation animation) {}
        };
        
        mOut = AnimationUtils.loadAnimation(this, R.anim.out_down);
        mOut.setAnimationListener(finish);
	}

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
                
                if (mFragmentCount == 1) {
                    configureView(true);
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
        
        String[] icons = getResources().getStringArray(R.array.account_summary_title_bar_icons);
        
        ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
        
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardTabletActivity.this, "print", Toast.LENGTH_LONG).show();
            }
        });
        
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Tag> tags = ApplicationContext.getDaoSession().getTagDao().loadAll();
                for (Tag tag : tags) {
                    if (tag.getTagInstances().size() == 0) {
                        Tag.deleteTag(tag);
                    }
                }
                Toast.makeText(DashboardTabletActivity.this, "email", Toast.LENGTH_LONG).show();
            }
        });
       
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardTabletActivity.this, "help", Toast.LENGTH_LONG).show();
                
                SyncEngine.sharedInstance().beginSync();
            }
        });
        
        new NavBarButtons(DashboardTabletActivity.this, icons, onClickListeners);
     }
	
	@Override
    public void showFragment(FragmentType fragmentType) {
    	
    	mOnFragment = true;
    	
    	BaseFragment fragment = getFragment(fragmentType);
    	
    	if (fragment != null) {
    	    
    		mCurrentIndex = fragmentType;
    		
	        FragmentTransaction ft = mFm.beginTransaction();
	        
	        if (!mOnHome) {
	            ft.setCustomAnimations(R.anim.in_up, R.anim.out_up);
	            
	            mHandler.postDelayed(mTask, 850);
	        }
	        
	        ft.replace(R.id.fragment, fragment);
	        ft.commit();
	        
            if (fragment instanceof FragmentVisibilityListener) {
                ((FragmentVisibilityListener) fragment).onShow(DashboardTabletActivity.this);
            }
    	}
    }

    private BaseFragment getFragment(FragmentType type) {

        switch (type) {
            case DASHBOARD:
    			configureView(true);
            	return null;
            case ACCOUNT_TYPES:
            	return AccountTypesTabletFragment.newInstance(type);
            case TRANSACTIONS:
                return TransactionsTabletFragment.newInstance();
            case SETTINGS:
                return SettingsTabletFragment.newInstance(type);
            default:
                break;
        }
        
        return null;
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
		
		if (mCurrentIndex.index() == index)
			return;
		
		showFragment(FragmentType.fromInteger(index));
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
}