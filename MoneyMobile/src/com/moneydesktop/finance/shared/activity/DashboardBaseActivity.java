package com.moneydesktop.finance.shared.activity;

import java.util.List;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.handset.activity.LoginHandsetActivity;
import com.moneydesktop.finance.model.EventMessage.LogoutEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.activity.LoginTabletActivity;
import com.moneydesktop.finance.tablet.adapter.GrowPagerAdapter;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsTabletFragment;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.views.GrowViewPager;

@TargetApi(11)
public abstract class DashboardBaseActivity extends BaseActivity {

	protected final String KEY_PAGER = "pager";
	
	protected boolean mLoggingOut = false;
	protected boolean mOnHome = true;
    
    protected BaseFragment mFragment;
	protected GrowViewPager mPager;
	protected GrowPagerAdapter mAdapter;

	public GrowPagerAdapter getPagerAdapter() {
	    return mAdapter;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, getString(R.string.text_syncing));
        }
    }
    
    @Override
    public void onFragmentAttached(BaseFragment fragment) {
    	super.onFragmentAttached(fragment);
    	
        setCurrentFragment(fragment);
    }
    
    public void setCurrentFragment(BaseFragment fragment) {
    	mFragment = fragment;
    }
    
    public void setDetailFragment(TransactionsDetailTabletFragment fragment) {
        
        if (mFragment instanceof TransactionsTabletFragment) {
            ((TransactionsTabletFragment) mFragment).setDetailFragment(fragment);
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
			
			if (mLoggingOut) {
				
				logout(ApplicationContext.isTablet());
			}
		}
	}
	
	public void onEvent(LogoutEvent event) {
		
	    DialogUtils.alertDialog(getString(R.string.unlink_title).toUpperCase(), getString(R.string.unlink_message), getString(R.string.label_yes).toUpperCase(), getString(R.string.label_no).toUpperCase(), this, new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                switch (which) {
                    case -1:
                        processLogout();
                        break;
                }
                
                DialogUtils.dismissAlert();
            }
        });
	}
	
	private void processLogout() {

        if (User.getCurrentUser().getCanSync())
            SyncEngine.sharedInstance().syncIfNeeded();
        
        if (SyncEngine.sharedInstance().isSyncing()) {
            
            mLoggingOut = true;
            DialogUtils.alertDialog(getString(R.string.logout_title), getString(R.string.logout_message), getString(R.string.logout_cancel), this, new OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DialogUtils.dismissAlert();
                }
            });
            
        } else {
            
            logout(ApplicationContext.isTablet());
        }
	}
	
	private void logout(final boolean isTablet) {
		
		mLoggingOut = false;
		
		DialogUtils.showProgress(this, getString(R.string.logging_out));
		
		new AsyncTask<Void, Void, Boolean>() {
    		
			@Override
			protected Boolean doInBackground(Void... params) {

				SyncEngine.sharedInstance().endBankStatusTimer();
				DataController.deleteAllLocalData();
				User.clear();
	            Preferences.saveString(Preferences.KEY_LOCK_CODE, "");
			
				Preferences.saveBoolean(Preferences.KEY_IS_DEMO_MODE, false);
				DataController.clearCache();

				return true;
			}
    		
    		@Override
    		protected void onPostExecute(Boolean result) {

    			DialogUtils.hideProgress();
    			
    	    	Intent i = new Intent(getApplicationContext(), isTablet ? LoginTabletActivity.class : LoginHandsetActivity.class);
    	    	startActivity(i);
                overridePendingTransition(R.anim.in_up, R.anim.none);
    	    	finish();
    		}
			
		}.execute();
	}
	
	public abstract void showFragment(FragmentType fragment, boolean moveUp);
	
	public void showFragment(FragmentType fragment) {
		showFragment(fragment, false);
	}
	
	public void addMenuItems(List<Pair<Integer, List<int[]>>> data) {}
	public void pushMenuView(View view) {}
	public void popMenuView() {}
	public View getmenuParent() {
		return null;
	}
}
