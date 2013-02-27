package com.moneydesktop.finance.handset.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.moneydesktop.finance.handset.fragment.DashboardFragmentFactory;
import com.moneydesktop.finance.tablet.adapter.GrowPagerAdapter;

public class HandsetGrowPagerAdapter extends GrowPagerAdapter {
    
    private final int COUNT = 3;

	public HandsetGrowPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		return COUNT;
	}

    @Override
    public Fragment getItem(int position) {
        return DashboardFragmentFactory.getHandsetInstance(position);
    }

}
