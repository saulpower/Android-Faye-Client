package com.moneydesktop.finance.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;

@SuppressLint("NewApi")
public class HeaderView extends TextView {
    
    public final String TAG = this.getClass().getSimpleName();

    private boolean mIsShowing = false;
    private boolean mIsAscending = false;
    private CaretDrawable mCaret;
    private int mDefaultColor;
    private FilterChangeListener mListener;
    
    public boolean isShowing() {
        return mIsShowing;
    }

    public void setIsShowing(boolean isShowing) {
        this.mIsShowing = isShowing;
        
        invalidate();
    }

    public boolean isAscending() {
        return mIsAscending;
    }

    public void setIsAscending(boolean isAscending, boolean animate) {
        this.mIsAscending = isAscending;
        configureCaret(animate);
    }
    
    public void setOnFilterChangeListener(FilterChangeListener listener) {
        mListener = listener;
    }
    
    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mDefaultColor = getContext().getResources().getColor(R.color.primaryColor);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        createCaret();
    }
    
    public void createCaret() {
        
        float size = UiUtils.getDynamicPixels(getContext(), 10.0f);
        
        PointF position = new PointF(getWidth() - (size * 1.5f), (getHeight() / 2.0f) - (size / 2.0f));
        
        mCaret = new CaretDrawable(position, size, size);
        mCaret.setColor(mDefaultColor);
        mCaret.setCallback(this);
    }
    
    private void configureCaret(boolean animate) {
        
        if (mIsShowing) {
            rotateCaret(!mIsAscending ? 180 : 0, mIsAscending ? 180 : 0, animate);
        }
    }
    
    private void rotateCaret(float fromDegree, float toDegree, boolean animate) {
        
        if (mCaret != null && animate) {
            
            ObjectAnimator rotate = ObjectAnimator.ofFloat(mCaret, "caretRotation", fromDegree, toDegree);
            rotate.setDuration(300);
            rotate.addListener(new AnimatorListener() {
                
                @Override
                public void onAnimationStart(Animator animation) {}
                
                @Override
                public void onAnimationRepeat(Animator animation) {}
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mListener != null) {
                        mListener.filterChanged(mIsAscending ? 1 : 0);
                    }
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {}
            });
            rotate.start();
            
        } else if (mCaret != null) {
            
            mCaret.setCaretRotation(mIsAscending ? 180 : 0);
            
            if (mListener != null) {
                mListener.filterChanged(mIsAscending ? 1 : 0);
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        
        final int action = ev.getAction();
        
        switch (action) {
        
            case MotionEvent.ACTION_DOWN: {
                mCaret.setColor(Color.WHITE);
                break;
            }
            
            case MotionEvent.ACTION_UP: {
                mCaret.setColor(mDefaultColor);
                break;
            }
        }
        
        return super.onTouchEvent(ev);
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        super.invalidateDrawable(who);
        
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mIsShowing) mCaret.draw(canvas);
    }
}
