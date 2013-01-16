package com.moneydesktop.finance.tablet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.moneydesktop.finance.tablet.fragment.TestFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class SlidePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };

    private int mCount = CONTENT.length;

    public SlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return SlidePagerAdapter.CONTENT[position % CONTENT.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getIconResId(int index) {
        // TODO Auto-generated method stub
        return 0;
    }
}