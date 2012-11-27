package com.moneydesktop.finance;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.View;

import com.moneydesktop.finance.BaseActivity.AppearanceListener;

public abstract class BaseListFragment extends ListFragment implements AppearanceListener {
	
	protected BaseActivity activity;
	protected View root;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.activity = (BaseActivity) activity;
        this.activity.onFragmentAttached(this);
        this.activity.updateNavBar(getFragmentTitle());
	}
	
	public abstract String getFragmentTitle();
}
