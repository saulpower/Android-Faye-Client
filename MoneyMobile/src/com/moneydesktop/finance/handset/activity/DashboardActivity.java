package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.handset.fragment.AccountSummaryFragment;
import com.moneydesktop.finance.handset.fragment.BudgetSummaryFragment;
import com.moneydesktop.finance.handset.fragment.SettingsFragment;
import com.moneydesktop.finance.handset.fragment.SpendingSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsFragment;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.DialogUtils;

import de.greenrobot.event.EventBus;

public class DashboardActivity extends BaseActivity {
	
	public static final String TAG = "DashboardActivity";
	
	FragmentManager fm;
	
	ViewFlipper flipper;

    MyAdapter mAdapter;
    ViewPager mPager;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        setContentView(R.layout.handset_dashboard_view);
        
        super.onCreate(savedInstanceState);
        
        setupView();
        mPager.setPageMargin(20);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pager", mPager.getCurrentItem());
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		EventBus.getDefault().register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
        EventBus.getDefault().unregister(this);
	}
	
	@Override
	public void onBackPressed() {
		
		if (flipper.indexOfChild(flipper.getCurrentView()) == 1) {
			
			if (fragmentCount == 1) {
				configureView(true);
			} else {
				navigateBack();
			}
			
		} else {
			
			super.onBackPressed();
		}
	}
	
	/**
	 * Sync has completed and if database defaults are
	 * loaded we can dismiss the progress dialog
	 * 
	 * @param event
	 */
	public void onEvent(SyncEvent event) {
		
		if (event.isFinished())
			DialogUtils.hideProgress();
	}
	
	private void setupView() {
		
		flipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (ViewPager) findViewById(R.id.pager);
	}
	
	@Override
	public void onFragmentAttached(AppearanceListener fragment) {
		super.onFragmentAttached(fragment);
		
		if (fragmentCount == 1)
			configureView(false);
	}
  	
    public void showFragment(int position) {
    	
    	onFragment = true;
    	
    	BaseFragment fragment = getFragment(position);
    	
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment, fragment);
		ft.addToBackStack(null);
        ft.commit();
    }
    
    private BaseFragment getFragment(int position) {

        switch (position) {
        case 0:
        	return SettingsFragment.newInstance(position);
        case 1:
        	return SettingsFragment.newInstance(position);
        case 2:
        	return SettingsFragment.newInstance(position);
        case 3:
        	return TransactionsFragment.newInstance();
        case 4:
        	return SettingsFragment.newInstance(position);
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

	@Override
	public String getActivityTitle() {
		return getString(R.string.title_activity_dashboard);
	}
	
	public static class MyAdapter extends FragmentPagerAdapter {
		
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Fragment getItem(int position) {
        	
            switch (position) {
            case 0:
            	return AccountSummaryFragment.newInstance(position);
            case 1:
            	return SpendingSummaryFragment.newInstance(position);
            case 2:
            	return BudgetSummaryFragment.newInstance(position);
            case 3:
            	return TransactionSummaryFragment.newInstance(position);
            case 4:
            	return SettingsFragment.newInstance(position);
            }
            
            return null;
        }
    }
}
