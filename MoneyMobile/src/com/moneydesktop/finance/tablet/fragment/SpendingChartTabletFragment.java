package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;

public class SpendingChartTabletFragment extends SummaryTabletFragment {
    
    public final String TAG = this.getClass().getSimpleName();
	
	public static SpendingChartTabletFragment newInstance(int position) {
		
		SpendingChartTabletFragment frag = new SpendingChartTabletFragment();
		
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        
        return frag;
	}

	@Override
	public FragmentType getType() {
		return FragmentType.ACCOUNT_SUMMARY;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	Debug.startMethodTracing("pieChart");
    	
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_spending_summary_view, null);
		
		return mRoot;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Debug.stopMethodTracing();
	}

    @Override
    public String getTitleText() {
        return "Account Balances";
    }
}
