package com.moneydesktop.finance.shared;

import android.os.AsyncTask;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DemoData;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.DialogUtils;

public abstract class LoginBaseActivity extends BaseActivity {

	protected boolean mFailed = false;
	
	protected void demoMode() {
		
		User.registerDemoUser();
		
		DialogUtils.showProgress(this, getString(R.string.demo_message));
		
		new AsyncTask<Void, Void, Boolean>() {
    		
			@Override
			protected Boolean doInBackground(Void... params) {

				DemoData.createDemoData();

				return true;
			}
    		
    		@Override
    		protected void onPostExecute(Boolean result) {

    			toDashboard();
    		}
			
		}.execute();
	}
	
	protected void login() {
		
		if (mFailed) {
			
			resetLogin();
			
			return;
		}
        
		if (loginCheck()) {
	        
			authenticate();
	        
		} else {
			
			DialogUtils.alertDialog(getString(R.string.error_title), getString(R.string.error_login_incomplete), this, null);
		}
	}
	
	protected void resetLogin() {

		mFailed = false;
	}
	
	protected abstract void authenticate();
	protected abstract boolean loginCheck();
	protected abstract void toDashboard();
	
	@Override
	public String getActivityTitle() {
		return null;
	}

}
