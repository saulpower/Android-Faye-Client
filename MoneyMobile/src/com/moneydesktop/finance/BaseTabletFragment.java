package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;

public abstract class BaseTabletFragment extends Fragment {

	private int position = -1;
	
	protected DashboardTabletActivity activity;
	protected View root;
    
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.activity = (DashboardTabletActivity) activity;
	}
	
	public abstract String getFragmentTitle();
}
