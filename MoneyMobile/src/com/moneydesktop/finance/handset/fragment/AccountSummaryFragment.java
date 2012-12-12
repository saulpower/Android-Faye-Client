package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

public class AccountSummaryFragment extends BaseFragment {

    private static AccountSummaryFragment sFragment;
    
	public static AccountSummaryFragment getInstance(int position) {
		
	    if (sFragment != null) {
	        return sFragment;
	    }
	    
	    sFragment = new AccountSummaryFragment();
	    sFragment.setPosition(position);
	    sFragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_account_summary_view, null);
		setupView();
		
		return mRoot;
	}
    
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)  {
        super.onSaveInstanceState(outState);
    }
	
	private void setupView() {

	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
