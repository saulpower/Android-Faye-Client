package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;

public class SpendingSummaryFragment extends BaseFragment {

    private static SpendingSummaryFragment sFragment;
    
	public static SpendingSummaryFragment getInstance(FragmentType type) {
		
	    if (sFragment != null) {
	        return sFragment;
	    }
	    
	    sFragment = new SpendingSummaryFragment();
	    sFragment.setType(type);
        sFragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_spending_summary_view, null);
		
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
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
