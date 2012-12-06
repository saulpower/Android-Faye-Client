package com.moneydesktop.finance.handset.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.FragmentAdapter;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.handset.fragment.AccountSummaryFragment;
import com.moneydesktop.finance.handset.fragment.BudgetSummaryFragment;
import com.moneydesktop.finance.handset.fragment.LockFragment;
import com.moneydesktop.finance.handset.fragment.SettingsFragment;
import com.moneydesktop.finance.handset.fragment.SpendingSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsFragment;
import com.moneydesktop.finance.shared.Dashboard;

public class DashboardActivity extends Dashboard {
	
	public static final String TAG = "DashboardActivity";
	private ViewFlipper flipper;

	private FragmentAdapter mAdapter;
	private ViewPager mPager;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        setContentView(R.layout.handset_dashboard_view);
        
        super.onCreate(savedInstanceState);
        
        setupView();
        mPager.setPageMargin(20);
        mAdapter = new FragmentAdapter(fm, getFragments());
        mPager.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGER));
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PAGER, mPager.getCurrentItem());
    }
	
	@Override
	public void onFragmentAttached(AppearanceListener fragment) {
		super.onFragmentAttached(fragment);
		
		if (fragmentCount == 1)
			configureView(false);
	}
	
	private List<Fragment> getFragments() {

    	List<Fragment> fragments = new ArrayList<Fragment>();
    	
    	fragments.add(AccountSummaryFragment.newInstance(0));
    	fragments.add(SpendingSummaryFragment.newInstance(1));
    	fragments.add(BudgetSummaryFragment.newInstance(2));
    	fragments.add(TransactionSummaryFragment.newInstance(3));
    	fragments.add(SettingsFragment.newInstance(4));
    	
    	return fragments;
	}
  	
    public void showFragment(int position) {
    	
    	onFragment = true;
    	
    	BaseFragment fragment = getFragment(position);
    	
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment, fragment);
		ft.addToBackStack(null);
        ft.commit();
    }
    
    @Override
    public void configureView(final boolean home) {
		super.configureView(home);
		
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
	
	private void setupView() {
		
		flipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (ViewPager) findViewById(R.id.pager);
	}
    
	private BaseFragment getFragment(int position) {

    	BaseFragment frag = null;
    	
        switch (position) {
        case 0:
        	frag = SettingsFragment.newInstance(position);
        	break;
        case 1:
        	frag = SettingsFragment.newInstance(position);
        	break;
        case 2:
        	frag = SettingsFragment.newInstance(position);
        	break;
        case 3:
        	frag = TransactionsFragment.newInstance();
        	break;
        case 4:
        	frag = LockFragment.newInstance();
        	break;
        }
        
        return frag;
    }
}
