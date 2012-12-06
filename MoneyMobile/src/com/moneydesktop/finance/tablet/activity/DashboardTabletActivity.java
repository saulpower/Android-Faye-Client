package com.moneydesktop.finance.tablet.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.FragmentAdapter;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.shared.Dashboard;
import com.moneydesktop.finance.tablet.fragment.AccountSummaryTabletFragment;
import com.moneydesktop.finance.tablet.fragment.AccountTypesTabletFragment;
import com.moneydesktop.finance.tablet.fragment.SummaryTabletFragment;
import com.moneydesktop.finance.views.FixedSpeedScroller;
import com.moneydesktop.finance.views.GrowViewPager;
import com.moneydesktop.finance.views.NavWheelView;
import com.moneydesktop.finance.views.NavWheelView.onNavigationChangeListener;

public class DashboardTabletActivity extends Dashboard implements onNavigationChangeListener {
	
	public static final String TAG = "DashboardTabletActivity";

	private ViewFlipper flipper;
	private FragmentAdapter mAdapter;
	private GrowViewPager mPager;
	private NavWheelView navigation;
	
	private int currentIndex = 0;

	@Override
	public void onBackPressed() {
		
		if (navigation.getVisibility() == View.VISIBLE) {
			
			toggleNavigation();
			
		} else if (flipper.indexOfChild(flipper.getCurrentView()) == 1) {
			
			if (fragmentCount == 1) {
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
		
        setContentView(R.layout.tablet_dashboard_view);
        
        super.onCreate(savedInstanceState);    
        
        setupView();
        
        mAdapter = new FragmentAdapter(fm, getFragments());
        mPager.setAdapter(mAdapter);
        
        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt("pager"));
        }
    }
	
	@Override
	public void onFragmentAttached(AppearanceListener fragment) {
		super.onFragmentAttached(fragment);
		
		if (fragmentCount >= 1)
			configureView(false);
	}
    
    @Override
    public void configureView(final boolean home) {
		super.configureView(home);
		
    	if (home) {
    		
    		currentIndex = 0;
    		navigation.setCurrentIndex(0);
    		
    		AnimationListener finish = new AnimationListener() {
				
				public void onAnimationStart(Animation animation) {}
				
				public void onAnimationRepeat(Animation animation) {}
				
				public void onAnimationEnd(Animation animation) {
					navigateBack();
				}
			};
    		
			AnimationFactory.flipTransition(flipper, finish, null, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
			
    	} else {

    		AnimationListener finish = new AnimationListener() {
				
				public void onAnimationStart(Animation animation) {}
				
				public void onAnimationRepeat(Animation animation) {}
				
				public void onAnimationEnd(Animation animation) {
					viewDidAppear();
				}
			};
	    	
	        AnimationFactory.flipTransition(flipper, null, finish, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
    	}
    }
	
	public void onEvent(NavigationEvent event) {
		
		if (event.isShowing() == null && event.getDirection() == null)
			toggleNavigation();
	}

	private void setupView() {
		
        navigation = (NavWheelView) findViewById(R.id.nav_wheel);
		flipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (GrowViewPager) findViewById(R.id.tablet_pager);
        
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
        items.add(R.drawable.tablet_newnav_budgets_white);
        items.add(R.drawable.tablet_newnav_reports_white);
        items.add(R.drawable.tablet_newnav_settings_white);
        
        navigation.setItems(items);
        navigation.setOnNavigationChangeListener(this);
	}
	
	public void toggleNavigation() {
		
		if (navigation.getVisibility() == View.GONE) {
			navigation.showNav();
		} else {
			navigation.hideNav();
		}
	}

    public void showFragment(int index) {
    	
    	onFragment = true;
    	
    	BaseTabletFragment fragment = getFragment(index);
    	
    	if (fragment != null) {
    		
    		currentIndex = index;
    		
	        FragmentTransaction ft = fm.beginTransaction();
	        ft.replace(R.id.fragment, fragment);
	        ft.addToBackStack(null);
	        ft.commit();
    	}
    }

    private BaseTabletFragment getFragment(int index) {

        switch (index) {
        case 0:
			configureView(true);
			
        	return null;
        case 1:
        	return AccountTypesTabletFragment.newInstance(index);
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
	
	private List<Fragment> getFragments() {

    	List<Fragment> fragments = new ArrayList<Fragment>();
    	
    	// Dummy fragments currently for testing
    	fragments.add(SummaryTabletFragment.newInstance(0));
    	fragments.add(SummaryTabletFragment.newInstance(1));
    	fragments.add(SummaryTabletFragment.newInstance(2));
    	fragments.add(SummaryTabletFragment.newInstance(3));
    	
    	return fragments;
	}

	@Override
	public void onNavigationChanged(int index) {
		
		if (currentIndex == index)
			return;
		
		showFragment(index);
	}

}