package com.moneydesktop.finance.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.moneydesktop.finance.handset.fragment.AccountSummaryFragment;
import com.moneydesktop.finance.handset.fragment.BudgetSummaryFragment;
import com.moneydesktop.finance.handset.fragment.SettingsFragment;
import com.moneydesktop.finance.handset.fragment.SpendingSummaryFragment;
import com.moneydesktop.finance.handset.fragment.TransactionSummaryFragment;

public class FragmentAdapter extends FragmentPagerAdapter {
	
    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Fragment getItem(int position) {
    	
        switch (position) {
        case 0:
        	return AccountSummaryFragment.newInstance(position);
        case 1:
        	return SpendingSummaryFragment.newInstance(position);
        case 2:
        	return BudgetSummaryFragment.newInstance(position);
        case 3:
        	return TransactionSummaryFragment.newInstance(position);
        case 4:
        	return SettingsFragment.newInstance(position);
        }
        
        return null;
    }
}