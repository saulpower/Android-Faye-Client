package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.activity.DashboardBaseActivity;

public abstract class BaseFragment extends Fragment {
	
	protected DashboardBaseActivity mActivity;
	protected View mRoot;
    
	public abstract FragmentType getType();
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof DashboardBaseActivity){
            mActivity = (DashboardBaseActivity) activity;
        	mActivity.setFragmentCount(mActivity.getFragmentCount() + 1);
            mActivity.onFragmentAttached(this);
        }
	}
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (mActivity != null) {
            mActivity.setCurrentFragment(this);
            mActivity.updateNavBar(getFragmentTitle(), true);
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (mActivity != null) {
    		mActivity.setFragmentCount(mActivity.getFragmentCount() - 1);
    	}
    }
    
    public void isShowing(boolean fromBackstack) {
    	
    	if (mActivity != null) {
    		mActivity.updateNavBar(getFragmentTitle(), true);
    	}
    }
	
	public abstract String getFragmentTitle();
	public abstract boolean onBackPressed();
}
