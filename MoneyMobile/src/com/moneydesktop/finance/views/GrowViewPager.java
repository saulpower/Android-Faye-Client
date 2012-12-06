package com.moneydesktop.finance.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.moneydesktop.finance.adapters.FragmentAdapter;
import com.moneydesktop.finance.tablet.fragment.SummaryTabletFragment;

@TargetApi(11)
public class GrowViewPager extends ViewPager {
	
	public final String TAG = this.getClass().getSimpleName();
    
	public static final float BASE_SIZE = 0.8f;
	public static final float BASE_ALPHA = 0.8f;
	private final float MARGIN_SIZE = -0.25f;
	
    private boolean scrollingLeft;
    
	public GrowViewPager(Context context) {
		super(context);

        init();
	}

    public GrowViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }
    
    private void init() {
    	
    	// Get screen size to set margin width accordingly
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        
        int margin = (int) (MARGIN_SIZE * metrics.widthPixels);
        setPageMargin(margin);
        
        // Keep 3 pages loaded up at all times
        setOffscreenPageLimit(3);
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    	
    	adjustSize(position, positionOffset);
    }
    
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
    	super.onScrollChanged(l, t, oldl, oldt);
    	
    	// Keep track of which direction we are scrolling
    	scrollingLeft = (oldl - l) < 0;
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
    	
    	position += (scrollingLeft ? 1 : 0);
    	int secondary = position + (scrollingLeft ? -1 : 1);
    	int tertiary = position + (scrollingLeft ? 1 : -1);
    	
    	float scaleUp = scrollingLeft ? percent : 1.0f - percent;
    	float scaleDown = scrollingLeft ? 1.0f - percent : percent;

    	float percentOut = scaleUp > BASE_ALPHA ? BASE_ALPHA : scaleUp;
    	float percentIn = scaleDown > BASE_ALPHA ? BASE_ALPHA : scaleDown;
    	
    	if (scaleUp < BASE_SIZE)
    		scaleUp = BASE_SIZE;

    	if (scaleDown < BASE_SIZE)
    		scaleDown = BASE_SIZE;
    	
    	// Adjust the fragments that are, or will be, on screen
    	SummaryTabletFragment current = (SummaryTabletFragment) ((position < getAdapter().getCount()) ? ((FragmentAdapter) getAdapter()).getItem(position) : null);
    	SummaryTabletFragment next = (SummaryTabletFragment) ((secondary < getAdapter().getCount() && secondary > -1) ? ((FragmentAdapter) getAdapter()).getItem(secondary) : null);
    	SummaryTabletFragment afterNext = (SummaryTabletFragment) ((tertiary < getAdapter().getCount() && tertiary > -1) ? ((FragmentAdapter) getAdapter()).getItem(tertiary) : null);
    	
    	if (current != null && next != null) {
    		
    		// Apply the adjustments to each fragment
	    	current.transitionFragment(percentIn, scaleUp);
	    	next.transitionFragment(percentOut, scaleDown);
	    	
	    	if (afterNext != null)
	    		afterNext.transitionFragment(BASE_ALPHA, BASE_SIZE);
    	}
    }
}
