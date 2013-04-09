package main.java.com.moneydesktop.finance.tablet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import main.java.com.moneydesktop.finance.handset.fragment.DashboardFragmentFactory;
import main.java.com.moneydesktop.finance.shared.adapter.GrowPagerAdapter;

public class TabletGrowPagerAdapter extends GrowPagerAdapter {

    private final int COUNT = 3;

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
