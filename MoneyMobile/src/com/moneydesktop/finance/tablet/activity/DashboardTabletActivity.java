package com.moneydesktop.finance.tablet.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.FragmentAdapter;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.shared.Dashboard;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.CircleNavView;
import com.moneydesktop.finance.views.GrowViewPager;

public class DashboardTabletActivity extends Dashboard {

	Button mLaunchNav;
	CircleNavView mCircleNav;

	private FragmentAdapter mAdapter;
	private GrowViewPager mPager;
	
	@Override
	public void onBackPressed() {
		if (mCircleNav.getVisibility() == View.VISIBLE) {
			mCircleNav.setVisibility(View.INVISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        setContentView(R.layout.dashboard_view);
        
        super.onCreate(savedInstanceState);
        
        UiUtils.setupTitleBar(this, getResources().getString(R.string.dashboard_title), "useremail@email.com", false, false, 0, 0, 0, 0);
        
        mLaunchNav = (Button) findViewById(R.id.view_nav_button);
        mCircleNav = (CircleNavView) findViewById(R.id.tablet_nav);
        
        mLaunchNav.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {  
                mCircleNav.setVisibility(View.VISIBLE);
			}
		});
        
        if (SyncEngine.sharedInstance().isSyncing()) {
        	DialogUtils.showProgress(this, "Syncing Data...");
        }
    }
	
	private void setupView() {
		
        mPager = (GrowViewPager) findViewById(R.id.pager);
        
        mPager.setPageMargin(20);
        mAdapter = new FragmentAdapter(fm);
        mPager.setAdapter(mAdapter);
	}
}