package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;

public class AccountSummaryTabletFragment extends SummaryTabletFragment {

	public static AccountSummaryTabletFragment newInstance(int position) {
		
		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();
		
        Bundle args = new Bundle();
        args.putInt("position",position);
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
		
		mRoot = inflater.inflate(R.layout.tablet_summary_view, null);
		
		setupViews();
        configureView();
        
		return mRoot;
	}

    @Override
    public String getTitleText() {
        return "Account Balances";
    }
}
