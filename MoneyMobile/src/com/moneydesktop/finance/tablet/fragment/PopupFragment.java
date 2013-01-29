package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.tablet.activity.PopupTabletActivity;

public abstract class PopupFragment extends BaseFragment {
	
	protected PopupTabletActivity mActivity;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        if (activity instanceof PopupTabletActivity){
            this.mActivity = (PopupTabletActivity) activity;
        }
	}
	
	protected void dismissPopup() {
	    if (mActivity != null) {
	        mActivity.dismissPopup();
	    }
	}
    
    protected void dismissPopup(int resultCode, Intent resultIntent) {
        if (mActivity != null) {
            mActivity.dismissPopup(resultCode, resultIntent);
        }
    }
}
