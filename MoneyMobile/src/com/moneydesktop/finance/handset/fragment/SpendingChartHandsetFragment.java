package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.fragment.GrowFragment;

public class SpendingChartHandsetFragment extends GrowFragment {
    
	public static SpendingChartHandsetFragment getInstance(int position) {
	    
	    SpendingChartHandsetFragment fragment = new SpendingChartHandsetFragment();
	    fragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        
        return fragment;
	}

	@Override
	public FragmentType getType() {
		return null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_spending_summary_view, null);
		
		return mRoot;
	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}
}
