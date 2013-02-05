package com.moneydesktop.finance.animation;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class WidthAnimation extends Animation {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private View mAnimatedLayout;
	private int mWidthChange, mStartWidth;
	private LayoutParams mLayoutParams;

	public WidthAnimation(View layout, int duration, int newWidth) {

		setDuration(duration);
		
		mAnimatedLayout = layout;
        mLayoutParams = layout.getLayoutParams();
		mStartWidth = mLayoutParams.width;
		mWidthChange = newWidth - mStartWidth;
	}

    @Override
    public void initialize(int width, int height, int parentWidth,
            int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		
		mLayoutParams.width = mStartWidth + (int) (mWidthChange * interpolatedTime);
		mAnimatedLayout.requestLayout();
	}
}
