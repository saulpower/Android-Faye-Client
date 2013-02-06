package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;

public class AccountSummaryTabletFragment extends BaseFragment {

	Button mLaunchNav;
	
	public static AccountSummaryTabletFragment newInstance(FragmentType type) {
		
		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();
		frag.setType(type);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
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
