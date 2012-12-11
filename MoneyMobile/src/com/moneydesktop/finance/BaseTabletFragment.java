package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;

public abstract class BaseTabletFragment extends Fragment {
    
    public final String TAG = this.getClass().getSimpleName();

	private int mPosition = -1;
	
	protected DashboardTabletActivity mActivity;
	protected View mRoot;
    
	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		this.mPosition = position;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.mActivity = (DashboardTabletActivity) activity;
	}
	
	public abstract String getFragmentTitle();
}
