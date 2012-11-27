package com.moneydesktop.finance.tablet.activity;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.DialogUtils;

import de.greenrobot.event.EventBus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DashboardTabletActivity extends BaseActivity {

	Button mTempButton; 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dashboard_view);
        
        mTempButton = (Button) findViewById(R.id.view_accounts_button);
        
        mTempButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(DashboardTabletActivity.this, AccountTypesTabletActivity.class);
				
				startActivity(intent);
				finish();
			}
		});
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, "Syncing Data...");
        }
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		EventBus.getDefault().register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
        EventBus.getDefault().unregister(this);
	}
	
	public void onEvent(SyncEvent event) {
		
		if (event.isFinished()) {
			DialogUtils.hideProgress();
		}
	}

	@Override
	public String getActivityTitle() {
		return null;
	}
	
}