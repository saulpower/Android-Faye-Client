package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

public class AccountSummaryTabletFragment extends BaseFragment {

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
        
        this.mActivity.onFragmentAttached(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_account_summary, null);
		mLaunchNav = (Button)mRoot.findViewById(R.id.view_nav_button);
		
		return mRoot;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_accounts);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
