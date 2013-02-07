package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.moneydesktop.finance.tablet.adapter.GrowPagerAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.GrowViewPager;

import de.greenrobot.event.EventBus;

public class DashboardHandsetActivity extends DashboardBaseActivity {
	
	public static final String TAG = "DashboardActivity";
	private ViewFlipper mFlipper;

	private GrowPagerAdapter mAdapter;
	private GrowViewPager mPager;
	
    private RelativeLayout mNavBar;
    private TextView mTitle,mLeft, mRight;
    
    @Override
    public void onFragmentAttached(BaseFragment fragment) {
    	super.onFragmentAttached(fragment);

        if (mFragmentCount == 1 && mOnHome) configureView(false);
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.handset_dashboard_view);
        
        setupView();
        applyFonts();
        
        mPager.setPageMargin(20);
        mAdapter = new GrowPagerAdapter(mFm);
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
	
	private void setupView() {
		
		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mPager = (GrowViewPager) findViewById(R.id.pager);
		
		mNavBar = (RelativeLayout) findViewById(R.id.nav_bar);
		mTitle = (TextView) mNavBar.findViewById(R.id.title);
		mLeft = (TextView) mNavBar.findViewById(R.id.left_button);
		mRight = (TextView) mNavBar.findViewById(R.id.right_button);
	}
	
	private void applyFonts() {
		
		Fonts.applyPrimaryFont(mTitle, 14);
		Fonts.applyNavIconFont(mLeft, 20);
		Fonts.applyNavIconFont(mRight, 20);
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
	
	/**
	 * Update the navigation bar with the passed in title.  Configure the
	 * back button if necessary.
	 * 
	 * @param titleString the title for the navigation bar
	 * @param navButtons 
	 * @return 
	 */
	public void updateNavBar(String titleString) {
			
		if (titleString != null) mTitle.setText(titleString);
	}
    
    public void configureView(final boolean home) {
		
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
    
	private BaseFragment getFragment(FragmentType type) {

    	BaseFragment frag = null;
    	
        switch (type) {
            case SETTINGS:
            	frag = SettingsHandsetFragment.getInstance(type);
            	break;
            case LOCK_SCREEN:
            	frag = LockFragment.newInstance(false);
            	break;
            case TRANSACTIONS:
                frag = TransactionsHandsetFragment.newInstance("0");
                break;
        	default:
        	    break;
        }
        
        return frag;
    }
	
	public class FragmentAdapter extends FragmentStatePagerAdapter {
        
        private final int COUNT = 3;
        
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
