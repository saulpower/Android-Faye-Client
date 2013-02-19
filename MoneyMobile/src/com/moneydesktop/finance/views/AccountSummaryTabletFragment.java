package com.moneydesktop.finance.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.tablet.fragment.SummaryTabletFragment;

public class AccountSummaryTabletFragment extends SummaryTabletFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_summary_view, null);
        
        setupViews();
        configureView();

        return mRoot;
    }

	@Override
	public FragmentType getType() {
		return FragmentType.ACCOUNT_SUMMARY;
	}

    public static SummaryTabletFragment newInstance(int position) {
        
        AccountSummaryTabletFragment fragment = new AccountSummaryTabletFragment();
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        
        return fragment;
    }

    @Override
    public String getTitleText() {
        // TODO Auto-generated method stub
        return "Account Summary";
    }
}
