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
import com.moneydesktop.finance.util.Enums.SlideFrom;
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
   // View mRoot;
   // LinearLayout mContainer;
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
        infaltedView.setLayoutParams(params);
        
        mScreenHeight = UiUtils.getScreenHeight((Activity)mContext);
        mScreenWidth = UiUtils.getScreenWidth((Activity)mContext);
         
        mParentView.addView(mInflatedView);
        
        //animate the container to the desired XY position
        animateView();
    }

    private void animateView() {     
        if (mFrom == SlideFrom.BOTTOM) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(mInflatedView, "translationY", mScreenHeight, mY);
            animation.setDuration(300); 
            animation.start();
        } else if (mFrom == SlideFrom.RIGHT) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(mInflatedView, "translationX", mScreenWidth, mX);
            animation.setDuration(300); 
            animation.start();
        } else if (mFrom == SlideFrom.TOP) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(mInflatedView, "translationY", 0, mY);
            animation.setDuration(300); 
            animation.start();
        } else if (mFrom == SlideFrom.LEFT) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(mInflatedView, "translationX", 0, mX);
            animation.setDuration(300); 
            animation.start();
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
        if (mFrom == SlideFrom.BOTTOM) {
            animation = new TranslateAnimation(mX, 0, 0, mScreenHeight);
        } else if (mFrom == SlideFrom.RIGHT) {
            animation = new TranslateAnimation(mScreenWidth, mX, 0, 0);
        } else if (mFrom == SlideFrom.TOP) {
            animation = new TranslateAnimation(0, 0, mScreenHeight, mY);
        } else if (mFrom == SlideFrom.LEFT) {
            animation = new TranslateAnimation(0, mX, 0, 0);
        }
        return animation;
    }
    
}