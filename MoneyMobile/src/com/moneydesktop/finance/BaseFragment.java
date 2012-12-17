package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.shared.DashboardBaseActivity;

public abstract class BaseFragment extends Fragment {

	private int mPosition = -1;
	
	protected DashboardBaseActivity mActivity;
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
        if(activity instanceof DashboardBaseActivity){
        this.mActivity = (DashboardBaseActivity) activity;
        }
	}
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
	
	public abstract String getFragmentTitle();
	public abstract boolean onBackPressed();
}
