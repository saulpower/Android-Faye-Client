package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

public class BudgetSummaryHandsetFragment extends BaseFragment {

    private static BudgetSummaryHandsetFragment sFragment;
    
	public static BudgetSummaryHandsetFragment getInstance(int position) {
		
	    if (sFragment != null) {
	        return sFragment;
	    }
	    
	    sFragment = new BudgetSummaryHandsetFragment();
	    sFragment.setPosition(position);
        sFragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_budget_summary_view, null);
		
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
