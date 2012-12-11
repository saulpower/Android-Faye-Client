package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;

import de.greenrobot.event.EventBus;

public abstract class BaseFragment extends Fragment {

	private int mPosition = -1;
	
	protected DashboardHandsetActivity mActivity;
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
        
        this.mActivity = (DashboardHandsetActivity) activity;
	}
    
    @Override
    public void onResume() {
        super.onResume();
        
        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        EventBus.getDefault().unregister(this);
    }
	
	public abstract String getFragmentTitle();
}
