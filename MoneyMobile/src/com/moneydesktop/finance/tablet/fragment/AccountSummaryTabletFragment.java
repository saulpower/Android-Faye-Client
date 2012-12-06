package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.moneydesktop.finance.BaseActivity.AppearanceListener;
import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;

public class AccountSummaryTabletFragment extends BaseTabletFragment implements AppearanceListener {

	Button mLaunchNav;
	
	public static AccountSummaryTabletFragment newInstance(int position) {
		
		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.activity.onFragmentAttached(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.tablet_account_summary, null);
		mLaunchNav = (Button)root.findViewById(R.id.view_nav_button);
		
		return root;
	}

	@Override
	public String getFragmentTitle() {
		return null;
	}

	@Override
	public void onViewDidAppear() {
		// TODO Auto-generated method stub
		
	}

}
