package com.moneydesktop.finance.views.piechart;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class ThreadAnimator {

	private AnimationListener mAnimationListener;

	private float mStart, mEnd, mChange;
	private long mStartTime;
	private long mDuration;
	private Interpolator mInterpolator;
	
	private boolean mRunning = false;
	
	public void setAnimationListener(AnimationListener mAnimationListener) {
		this.mAnimationListener = mAnimationListener;
	}
	
	public boolean isRunning()  {
		return mRunning;
	}
	
	public void setDuration(long duration) {
		mDuration = duration;
	}
	
	public long getDuration() {
		return mDuration;
	}
	
	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}
	
	public static ThreadAnimator ofFloat(float start, float end) {
		
		ThreadAnimator animator = new ThreadAnimator();
		animator.mStart = start;
		animator.mEnd = end;
		animator.mChange = end - start;
		
		return animator;
	}
	
	public static ThreadAnimator ofInt(int start, int end) {
		
		return ofFloat(start, end);
	}
	
	public ThreadAnimator() {
		mInterpolator = new AccelerateDecelerateInterpolator();
		mDuration = 1000;
	}
	
	public float floatUpdate() {
		
		if (!mRunning) return mEnd;
		
		float duration = AnimationUtils.currentAnimationTimeMillis() - mStartTime;
		
		if (duration >= mDuration) {
			
			mRunning = false;
			
			if (mAnimationListener != null) {
				mAnimationListener.onAnimationEnded();
			}
			
			return mEnd;
		}
		
		float progress = mInterpolator.getInterpolation(duration/mDuration);
		
		return mStart + mChange * progress;
	}
	
	public int intUpdate() {
		return (int) floatUpdate();
	}
	
	public void start() {
		mRunning = true;
		mStartTime = AnimationUtils.currentAnimationTimeMillis();
	}
	
	public interface AnimationListener {
		public void onAnimationEnded();
	}
}
