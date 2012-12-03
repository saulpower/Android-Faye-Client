package com.moneydesktop.finance.handset.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.handset.fragment.AccountSummaryFragment;
import com.moneydesktop.finance.handset.fragment.BudgetSummaryFragment;
import com.moneydesktop.finance.handset.fragment.LockFragment;
import com.moneydesktop.finance.handset.fragment.SettingsFragment;
import com.moneydesktop.finance.handset.fragment.SpendingSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsFragment;
import com.moneydesktop.finance.model.EventMessage.LogoutEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.DialogUtils;

public class DashboardActivity extends BaseActivity {
	
	public static final String TAG = "DashboardActivity";
	
	private final String KEY_PAGER = "pager";
	
	private FragmentManager fm;
	
	private ViewFlipper flipper;

	private MyAdapter mAdapter;
	private ViewPager mPager;
	
	private boolean loggingOut = false;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        setContentView(R.layout.handset_dashboard_view);
        
        super.onCreate(savedInstanceState);
        
        setupView();
        mPager.setPageMargin(20);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGER));
        }
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, getString(R.string.text_syncing));
        }

    	fm = getSupportFragmentManager();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PAGER, mPager.getCurrentItem());
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
		
		if (event.isFinished()) {
			
			DialogUtils.hideProgress();
			
			if (loggingOut) {
				
				logout();
			}
		}
	}
	
	public void onEvent(LogoutEvent event) {
		
		if (User.getCurrentUser().getCanSync())
			SyncEngine.sharedInstance().syncIfNeeded();
		
		if (SyncEngine.sharedInstance().isSyncing()) {
			
			loggingOut = true;
			DialogUtils.alertDialog(getString(R.string.logout_title), getString(R.string.logout_message), getString(R.string.logout_cancel), this, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DialogUtils.dismissAlert();
				}
			});
			
		} else {
			
			logout();
		}
	}
	
	private void logout() {
		
		loggingOut = false;
		
		DialogUtils.showProgress(this, getString(R.string.logging_out));
		
		new AsyncTask<Void, Void, Boolean>() {
    		
			@Override
			protected Boolean doInBackground(Void... params) {

				SyncEngine.sharedInstance().endBankStatusTimer();
				DataController.deleteAllLocalData();
				User.clear();
			
				Preferences.saveBoolean(Preferences.KEY_IS_DEMO_MODE, false);

				return true;
			}
    		
    		@Override
    		protected void onPostExecute(Boolean result) {

    			DialogUtils.hideProgress();
    			
    	    	Intent i = new Intent(getApplicationContext(), LoginActivity.class);
    	    	startActivity(i);
    	    	finish();
    		}
			
		}.execute();
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
