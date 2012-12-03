package com.moneydesktop.finance.tablet.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.shared.Dashboard;
import com.moneydesktop.finance.tablet.fragment.AccountSummaryTabletFragment;
import com.moneydesktop.finance.tablet.fragment.AccountTypesTabletFragment;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.views.CircleNavView;

public class DashboardTabletActivity extends Dashboard {
	public static final String TAG = "DashboardTabletActivity";

	LinearLayout mContainer;
	public static CircleNavView mCircleNav;
    FragmentManager fm;
	ViewFlipper flipper;
    MyAdapter mAdapter;
    ViewPager mPager;

	@Override
	public void onBackPressed() {
		if (mCircleNav.getVisibility() == View.VISIBLE) {
			mCircleNav.setVisibility(View.INVISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tablet_dashboard_view);       
        
        setupView();
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);	
        
        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt("pager"));
        }
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, "Syncing Data...");
        }

    	fm = getSupportFragmentManager();
    }

	@Override
	public void onFragmentAttached(AppearanceListener fragment) {
		super.onFragmentAttached(fragment);

		if (fragmentCount == 1)
			configureView(false);
	}

	private void setupView() {		
        mContainer = (LinearLayout) findViewById(R.id.dashboard_container);
        mCircleNav = (CircleNavView) findViewById(R.id.circle_tablet_nav);
		flipper = (ViewFlipper) findViewById(R.id.dashboard_tablet_flipper);
        mPager = (ViewPager) findViewById(R.id.tablet_pager);
             
	}

    public void showFragment(int position) {
    	
    	onFragment = true;
    	
    	BaseTabletFragment fragment = getFragment(position);
    	
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment, fragment);
		ft.addToBackStack(null);
        ft.commit();
    }

    private BaseTabletFragment getFragment(int position) {

        switch (position) {
        case 0:
        	return AccountSummaryTabletFragment.newInstance(position);
        case 1:
        	return AccountTypesTabletFragment.newInstance(position);
        }
        
        return null;
    }

    
    @Override
    public void configureView(final boolean home) {
		super.configureView(home);
		Log.i(TAG, "flip");

    	if (home) {
    		
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

	public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
        	
            switch (position) {
            case 0:
            	return AccountSummaryTabletFragment.newInstance(position);
            
            case 1:
            	return AccountTypesTabletFragment.newInstance(position);
            }
            
            return null;
        }
    }


}