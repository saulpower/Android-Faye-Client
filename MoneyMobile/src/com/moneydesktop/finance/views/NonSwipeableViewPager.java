package com.moneydesktop.finance.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

public class NonSwipeableViewPager extends ViewPager {

	static boolean canSroll = false;
	
    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	
        Log.d("Testing" , " onPageScrolled");
    }

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (canSroll) {
			setScrollX(0);
		}
	}

    public static void setCanScroll(boolean shouldScroll) {
    	canSroll = shouldScroll;
    }

	
    
}
