package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

public class BudgetSummaryFragment extends BaseFragment {

	public static BudgetSummaryFragment newInstance(int position) {
		
		BudgetSummaryFragment frag = new BudgetSummaryFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.handset_budget_summary_view, null);
		
		return root;
	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
