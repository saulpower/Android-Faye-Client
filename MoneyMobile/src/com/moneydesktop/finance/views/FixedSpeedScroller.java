package com.moneydesktop.finance.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

@TargetApi(11)
public class FixedSpeedScroller extends Scroller {
	
	public final String TAG = this.getClass().getSimpleName();

    private int mDuration = 480;
	
	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}
	};

    public FixedSpeedScroller(Context context) {
        super(context);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator) {
        super(context, sInterpolator);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, sInterpolator, flywheel);
    }


    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        
        if (duration <= 200)
        	duration = mDuration;
        
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
