package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.handset.activity.DashboardActivity;

public abstract class BaseFragment extends Fragment {

	private int position = -1;
	
	protected DashboardActivity activity;
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
        
        this.activity = (DashboardActivity) activity;
	}
	
	public abstract String getFragmentTitle();
}
