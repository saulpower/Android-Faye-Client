package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;

import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.tablet.activity.PopupTabletActivity;

import de.greenrobot.event.EventBus;

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
	    } else {
	    	EventBus.getDefault().post(new EventMessage().new BackEvent());
	    }
	}
    
    public void popupVisible() {}
}
