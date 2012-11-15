package com.moneydesktop.finance.handset.activity;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.layout;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.DialogUtils;

import de.greenrobot.event.EventBus;

import android.app.Activity;
import android.os.Bundle;

public class DashboardActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dashboard_view);
        
        EventBus.getDefault().register(this);
        
        if (SyncEngine.sharedInstance().isRunning()) {
        	DialogUtils.showProgress(this, "Syncing Data...");
        }
    }
	
	public void onEvent(SyncEvent event) {
		
		if (event.isFinished())
			DialogUtils.hideProgress();
	}
	
}
