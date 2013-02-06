package com.moneydesktop.finance.views;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.SlideFrom;
import com.moneydesktop.finance.util.UiUtils;

import java.util.List;

public class SlidingView extends FrameLayout{

    final Context mContext;
    final SlideFrom mFrom;
    final int mX;
    final int mY;
    View mInflatedView; 
    int mScreenHeight;
    int mScreenWidth;
    ViewGroup mParentView;
    View mSelectedView;
    
    
    
    public SlidingView(Context context, int toX, int toY, ViewGroup parentView, View infaltedView, SlideFrom from, View selectedView) {
        super(context);
        
        mContext = context;
        mFrom = from;
        mX = toX;
        mY = toY;
        mInflatedView = infaltedView;
        mParentView = parentView;
        mSelectedView = selectedView;
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        mInflatedView.setLayoutParams(params);
        
        mScreenHeight = UiUtils.getScreenHeight((Activity)mContext);
        mScreenWidth = UiUtils.getScreenWidth((Activity)mContext);
         
        mParentView.addView(mInflatedView);
        
        //animate the container to the desired XY position
        animateView();
    }

    private void animateView() {     
    	   	
        switch (mFrom) {
	        case BOTTOM:
	            ObjectAnimator animationBottom = ObjectAnimator.ofFloat(mInflatedView, "translationY", mScreenHeight, mY);
	            animationBottom.setDuration(300); 
	            animationBottom.start();
                break;
	        case RIGHT:
	        	ObjectAnimator animationRight = ObjectAnimator.ofFloat(mInflatedView, "translationX", mScreenWidth, mX);
	        	animationRight.setDuration(300); 
	        	animationRight.start();
                break;
	        case TOP:
	            ObjectAnimator animationTop = ObjectAnimator.ofFloat(mInflatedView, "translationY", 0, mY);
	            animationTop.setDuration(300); 
	            animationTop.start();
                break;
	        case LEFT:
	            ObjectAnimator animationLeft = ObjectAnimator.ofFloat(mInflatedView, "translationX", 0, mX);
	            animationLeft.setDuration(300); 
	            animationLeft.start();
                break;
        }
          
    }

    public void dismiss() {
        TranslateAnimation animation = translate();
        
        animation.setDuration(300); 
        animation.setFillAfter(true); 
        mInflatedView.startAnimation(animation);
        mParentView.removeView(mInflatedView);
    }

    public void dismiss(AnimationListener listener) {
        TranslateAnimation animation = translate();
        
        animation.setDuration(300); 
        animation.setFillAfter(true); 
        animation.setAnimationListener(listener);
        mInflatedView.startAnimation(animation);
        mParentView.removeView(mInflatedView);
    }
    
    public View getSelectedView () {
        return mSelectedView;
    }
    
    
    private TranslateAnimation translate() {
        TranslateAnimation animation = null;
        
        switch (mFrom) {
        case BOTTOM:
        	animation = new TranslateAnimation(mX, 0, 0, mScreenHeight);
            break;
        case RIGHT:
        	animation = new TranslateAnimation(mParentView.getX(), mParentView.getWidth(), 0, 0);
            break;
        case TOP:
        	animation = new TranslateAnimation(0, 0, mScreenHeight, mY);
            break;
        case LEFT:
        	animation = new TranslateAnimation(0, mX, 0, 0);
            break;
        }
        
        return animation;
    }
        
}