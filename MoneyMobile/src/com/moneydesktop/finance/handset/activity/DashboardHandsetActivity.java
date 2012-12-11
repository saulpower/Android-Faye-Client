package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.handset.fragment.DashboardFragmentFactory;
import com.moneydesktop.finance.handset.fragment.LockFragment;
import com.moneydesktop.finance.handset.fragment.SettingsFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsFragment;
import com.moneydesktop.finance.shared.DashboardBaseActivity;

public class DashboardHandsetActivity extends DashboardBaseActivity {
	
	public static final String TAG = "DashboardActivity";
	private ViewFlipper mFlipper;

	private FragmentAdapter mAdapter;
	private ViewPager mPager;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        setContentView(R.layout.handset_dashboard_view);
        
        super.onCreate(savedInstanceState);
        
        setupView();
        mPager.setPageMargin(20);
        mAdapter = new FragmentAdapter(mFm);
        mPager.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGER));
        }
    }
	
	@Override
	public void onBackPressed() {
		
		if (mFlipper.indexOfChild(mFlipper.getCurrentView()) == 1) {
			
			if (mFragmentCount == 1) {
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
	public void onFragmentAttached() {
		super.onFragmentAttached();
		
		if (mFragmentCount == 1)
			configureView(false);
	}
  	
    public void showFragment(int position) {
    	
    	mOnFragment = true;
    	
    	BaseFragment fragment = getFragment(position);
    	
        FragmentTransaction ft = mFm.beginTransaction();
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
    		
			AnimationFactory.flipTransition(mFlipper, finish, null, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
			
    	} else {

    		AnimationListener finish = new AnimationListener() {
				
				public void onAnimationStart(Animation animation) {}
				
				public void onAnimationRepeat(Animation animation) {}
				
				public void onAnimationEnd(Animation animation) {
					viewDidAppear();
				}
			};
	    	
	        AnimationFactory.flipTransition(mFlipper, null, finish, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
    	}
    }
	
	private void setupView() {
		
		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (ViewPager) findViewById(R.id.pager);
	}
    
	private BaseFragment getFragment(int position) {

    	BaseFragment frag = null;
    	
        switch (position) {
        case 0:
        	frag = SettingsFragment.getInstance(position);
        	break;
        case 1:
        	frag = SettingsFragment.getInstance(position);
        	break;
        case 2:
        	frag = SettingsFragment.getInstance(position);
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
	
	public class FragmentAdapter extends FragmentStatePagerAdapter {
        
        private final int COUNT = 5;
        
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            
            return DashboardFragmentFactory.getInstance(position);
        }
    }
}
