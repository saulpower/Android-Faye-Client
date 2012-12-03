package com.moneydesktop.finance.views;

import java.util.ArrayList;

import com.moneydesktop.finance.views.DirectionalViewPager.ItemInfo;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class GrowViewPager extends ViewPager {
	
	public final String TAG = this.getClass().getSimpleName();
    private static final boolean DEBUG = true;

    static class ItemInfo {
        Object object;
        int position;
        boolean scrolling;
    }

    private final ArrayList<ItemInfo> mItems = new ArrayList<ItemInfo>();
    
    private Scroller mScroller;
    
    private boolean mPopulatePending;
    private boolean mScrolling;

    private int mTouchSlop;
    
    private OnPageChangeListener mOnPageChangeListener;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    
	public GrowViewPager(Context context) {
		super(context);
		
	}

    public GrowViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }
    
    void initViewPager() {

        setWillNotDraw(false);
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public void computeScroll() {
        if (DEBUG) Log.i(TAG, "computeScroll: finished=" + mScroller.isFinished());
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                if (DEBUG) Log.i(TAG, "computeScroll: still scrolling");
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();

                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                }

                if (mOnPageChangeListener != null) {
                    int size = getWidth();
                    int value = x;

                    final int position = value / size;
                    final int offsetPixels = value % size;
                    final float offset = (float) offsetPixels / size;
                    mOnPageChangeListener.onPageScrolled(position, offset, offsetPixels);
                }

                // Keep on drawing until the animation has finished.
                invalidate();
                return;
            }
        }

        // Done with scroll, clean up state.
        completeScroll();
    }

    private void completeScroll() {
        boolean needPopulate;
        if ((needPopulate = mScrolling)) {
            // Done with scroll, no longer want to cache view drawing.
//            setScrollingCacheEnabled(false);
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
//            setScrollState(SCROLL_STATE_IDLE);
        }
        mPopulatePending = false;
        mScrolling = false;
        for (int i=0; i<mItems.size(); i++) {
            ItemInfo ii = mItems.get(i);
            if (ii.scrolling) {
                needPopulate = true;
                ii.scrolling = false;
            }
        }
        if (needPopulate) {
//            populate();
        }
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    	
    	Log.i(TAG, "Scrolling: ");
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

}
