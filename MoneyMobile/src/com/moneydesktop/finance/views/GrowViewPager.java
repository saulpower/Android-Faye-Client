package com.moneydesktop.finance.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.moneydesktop.finance.util.UiUtils;

public class GrowViewPager extends ViewPager {
	
	public final String TAG = this.getClass().getSimpleName();
    
	private final float MARGIN_SIZE = -0.25f;
	
    private OnScrollChangedListener mListener;
    
    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mListener = listener;
    }
    
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
        final DisplayMetrics metrics = UiUtils.getDisplayMetrics(getContext());
        
        float margin = MARGIN_SIZE * metrics.widthPixels;
        		
        if (android.os.Build.VERSION.SDK_INT < 11) {
        	margin = UiUtils.getDynamicPixels(getContext(), -45);
        }
        
        setPageMargin((int) margin);
        
        // Keep 3 pages loaded up at all times
        setOffscreenPageLimit(3);
    }
    
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
    	super.onScrollChanged(l, t, oldl, oldt);
    	
    	if (mListener != null) {
    	    mListener.onScrollChanged(l, t, oldl, oldt);
    	}
    }
    
    public interface OnScrollChangedListener {
        public void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
