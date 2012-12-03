package com.moneydesktop.finance.shared;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.handset.activity.LoginActivity;
import com.moneydesktop.finance.model.EventMessage.LogoutEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.DialogUtils;

public abstract class Dashboard extends BaseActivity {

	protected final String KEY_PAGER = "pager";
	
	protected boolean loggingOut = false;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, getString(R.string.text_syncing));
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

	@Override
	public String getActivityTitle() {
		return getString(R.string.title_activity_dashboard);
	}
}
