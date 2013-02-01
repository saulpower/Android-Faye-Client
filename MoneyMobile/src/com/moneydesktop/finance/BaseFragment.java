package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.DashboardBaseActivity;

public abstract class BaseFragment extends Fragment {

	private FragmentType mType;
	
	protected DashboardBaseActivity mActivity;
	protected View mRoot;
    
	public FragmentType getType() {
		return mType;
	}

	public void setType(FragmentType type) {
		this.mType = type;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof DashboardBaseActivity){
            this.mActivity = (DashboardBaseActivity) activity;
        }
	}
	
	public abstract String getFragmentTitle();
	public abstract boolean onBackPressed();
}
