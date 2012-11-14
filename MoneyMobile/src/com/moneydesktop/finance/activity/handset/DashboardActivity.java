package com.moneydesktop.finance.activity.handset;

import android.app.Activity;
import android.os.Bundle;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.DialogUtils;

import de.greenrobot.event.EventBus;

public class DashboardActivity extends Activity {
	
	public static final String TAG = "DashboardActivity";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dashboard_view);
        
        EventBus.getDefault().register(this);
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, "Syncing Data...");
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
	
}
