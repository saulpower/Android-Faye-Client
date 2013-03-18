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
            mActivity.onFragmentAttached(this);
        }
	}

    @Override
    public void onDetach() {
        super.onDetach();

        if (mActivity != null) {
            mActivity.onFragmentDetached(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        isShowing();
    }
    
    public void isShowing() {

        if (mActivity != null) {
            mActivity.updateNavBar(getFragmentTitle());
        }
    }
	
	public abstract String getFragmentTitle();
	public abstract boolean onBackPressed();
}
