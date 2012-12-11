package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.View;

import de.greenrobot.event.EventBus;

public abstract class BaseListFragment extends ListFragment {
	
	protected BaseActivity mActivity;
	protected View mRoot;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.mActivity = (BaseActivity) activity;
        this.mActivity.onFragmentAttached();
        this.mActivity.updateNavBar(getFragmentTitle());
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
