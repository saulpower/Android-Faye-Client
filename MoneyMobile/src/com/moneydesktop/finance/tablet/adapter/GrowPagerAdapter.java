package com.moneydesktop.finance.tablet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.moneydesktop.finance.tablet.fragment.SummaryTabletFragment;
import com.moneydesktop.finance.views.GrowViewPager.OnScrollChangedListener;

import java.util.ArrayList;
import java.util.List;

public class GrowPagerAdapter extends FragmentPagerAdapter implements OnPageChangeListener, OnScrollChangedListener {

    public final String TAG = this.getClass().getSimpleName();
    
    private final int COUNT = 4;
    
    public static final float BASE_SIZE = 0.8f;
    public static final float BASE_ALPHA = 0.8f;
    
    private int mCurrentPage = 0;
    private boolean mScrollingLeft;
    
    private List<SummaryTabletFragment> mFragments;
    
    public int getCurrentPage() {
        return mCurrentPage;
    }
    
    public void addFragment(SummaryTabletFragment fragment) {
        mFragments.add(fragment.getPosition(), fragment);
  }
    
    public GrowPagerAdapter(FragmentManager fm) {
        super(fm);

        mFragments = new ArrayList<SummaryTabletFragment>();
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return SummaryTabletFragment.newInstance(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        adjustSize(position, positionOffset);
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
    }
    
    /**
     * Used to adjust the size of each view in the viewpager as the user
     * scrolls.  This provides the effect of children scaling down as they
     * are moved out and back to full size as they come into focus.
     * 
     * @param position
     * @param percent
     */
    private void adjustSize(int position, float percent) {
        
        position += (mScrollingLeft ? 1 : 0);
        int secondary = position + (mScrollingLeft ? -1 : 1);
        int tertiary = position + (mScrollingLeft ? 1 : -1);
        
        float scaleUp = mScrollingLeft ? percent : 1.0f - percent;
        float scaleDown = mScrollingLeft ? 1.0f - percent : percent;

        float percentOut = scaleUp > BASE_ALPHA ? BASE_ALPHA : scaleUp;
        float percentIn = scaleDown > BASE_ALPHA ? BASE_ALPHA : scaleDown;
        
        if (scaleUp < BASE_SIZE)
            scaleUp = BASE_SIZE;

        if (scaleDown < BASE_SIZE)
            scaleDown = BASE_SIZE;
        
        // Adjust the fragments that are, or will be, on screen
        SummaryTabletFragment current = (position < mFragments.size()) ? mFragments.get(position) : null;
        SummaryTabletFragment next = (secondary < mFragments.size() && secondary > -1) ? mFragments.get(secondary) : null;
        SummaryTabletFragment afterNext = (tertiary < mFragments.size() && tertiary > -1) ? mFragments.get(tertiary) : null;
        
        if (current != null && next != null) {
            
            // Apply the adjustments to each fragment
            current.transitionFragment(percentIn, scaleUp);
            next.transitionFragment(percentOut, scaleDown);
            
            if (afterNext != null) {
                afterNext.transitionFragment(BASE_ALPHA, BASE_SIZE);
            }
        }
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {

        // Keep track of which direction we are scrolling
        mScrollingLeft = (oldl - l) < 0;
    }
}
