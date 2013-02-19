package com.moneydesktop.finance.tablet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.moneydesktop.finance.handset.fragment.DashboardFragmentFactory;

public class TabletGrowPagerAdapter extends GrowPagerAdapter {
    
    private final int COUNT = 2;

	public TabletGrowPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		return COUNT;
	}

    @Override
    public Fragment getItem(int position) {
        return DashboardFragmentFactory.getTabletInstance(position);
    }

}
