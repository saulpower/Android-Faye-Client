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
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.handset.fragment.DashboardHandsetFragmentFactory;
import com.moneydesktop.finance.handset.fragment.SettingsHandsetFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsHandsetFragment;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.shared.LockFragment;

import de.greenrobot.event.EventBus;

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
        mPager.setOffscreenPageLimit(5);

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
	public void onFragmentAttached(BaseFragment fragment) {
		super.onFragmentAttached(fragment);
		
		if (mFragmentCount == 1)
			configureView(false);
	}
  	
	@Override
    public void showFragment(FragmentType type) {
    	
    	mOnFragment = true;
    	
    	BaseFragment fragment = getFragment(type);
    	
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

	        EventBus.getDefault().post(new EventMessage().new ParentAnimationEvent(false, false));
	        AnimationFactory.flipTransition(mFlipper, null, finish, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
    	}
    }
	
	private void setupView() {
		
		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (ViewPager) findViewById(R.id.pager);
	}
    
	private BaseFragment getFragment(FragmentType type) {

    	BaseFragment frag = null;
    	
        switch (type) {
            case SETTINGS:
            	frag = SettingsHandsetFragment.getInstance(type);
            	break;
            case LOCK_SCREEN:
            	frag = LockFragment.newInstance(false);
            	break;
        	default:
        	    break;
        }
        
        return frag;
    }
	
	public class FragmentAdapter extends FragmentStatePagerAdapter {
        
        private final int COUNT = 6;
        
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            
            return DashboardHandsetFragmentFactory.getInstance(position);
        }
    }
}
