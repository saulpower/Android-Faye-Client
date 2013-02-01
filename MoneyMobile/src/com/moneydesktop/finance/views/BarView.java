
package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.color;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;

public class BarView extends View {
    private String mLabelText;
    private float mTextSize;
    private Paint mPaint;
    private Paint mBPaint;
    private int mRed;
    private int mBlue;
    private int mGreen;
    private int mAlpha;
    private double mAmount;
    private double mScaleAmount;
    private int mSelectedColor;
    private Rect mRect;
    private boolean mShowLabel;
    public BarView(Context context){
        super(context);
        mTextSize = UiUtils.getDynamicPixels(getContext(), 11);
        makePaint();
        mLabelText = "text";
        mShowLabel = false;
        int defColor = getResources().getColor(color.gray3);
        mRed = Color.red(defColor);
        mGreen = Color.green(defColor);
        mBlue = Color.blue(defColor);
        mAlpha = Color.alpha(defColor);
        mSelectedColor = getResources().getColor(R.color.primaryColor);
        mAmount = 50;
        mScaleAmount = 100;
    }
    public BarView(Context context, String day, double amount, double scale_amount) {
        super(context);
        mTextSize = UiUtils.getDynamicPixels(getContext(), 11);
        makePaint();
        mLabelText = day;
        mShowLabel = true;
        int defColor = getResources().getColor(color.gray3);
        mRed = Color.red(defColor);
        mGreen = Color.green(defColor);
        mBlue = Color.blue(defColor);
        mAlpha = Color.alpha(defColor);
        mAmount = amount;
        mScaleAmount = scale_amount;
    }

    public BarView(Context context, double amount, double scale_amount){
        super(context);     
        makePaint();
        mShowLabel = false;
        int defColor = getResources().getColor(color.gray3);
        mRed = Color.red(defColor);
        mGreen = Color.green(defColor);
        mBlue = Color.blue(defColor);
        mAlpha = Color.alpha(defColor);
        mAmount = amount;
        mScaleAmount = scale_amount;
    }
    public void setTextSize(float s){
        mTextSize = s;
        mPaint.setTextSize(mTextSize);
        invalidate();
    }
    public void setLabelText(String s){
        mLabelText = s;
    }
    public void showLabel(boolean s){
        mShowLabel = s;
        invalidate();
    }
    public double getScaleAmount(){
        return mScaleAmount;
    }
    public double getAmount(){
        return (float)mAmount;
    }
    public int getSelectedColor() {
        return mSelectedColor;
    }
    public void setSelectedColor(int mSelectedColor) {
        this.mSelectedColor = mSelectedColor;
    }
    public void setScaleAmount(float a){
        mScaleAmount = a;
        invalidate();
    }
    public void setAmount(float a){
        mAmount = a;
        invalidate();
    }
    private void setRed(int r){
        mRed = r;
    }
    private void setBlue(int b){
        mBlue = b;
    }
    private void setGreen(int g){
        mGreen = g;
    }
    private void setAlpha(int a){
        mAlpha = a;
    }
    public void setAmountAnimated(float a){
        final float start = (float) mAmount;
        ObjectAnimator v = ObjectAnimator.ofFloat(this, "amount", start,(float)a);
        v.setDuration(100);
        v.start();
        invalidate();
    }
    public void setColorAnimated(int color){
        final int startR = mRed;
        final int startG = mGreen;
        final int startB = mBlue;
        final int startA = mAlpha;
        ObjectAnimator vR = ObjectAnimator.ofInt(this, "red", startR,Color.red(color));
        ObjectAnimator vG = ObjectAnimator.ofInt(this, "green", startG,Color.green(color));
        ObjectAnimator vB = ObjectAnimator.ofInt(this, "blue", startB,Color.blue(color));
        ObjectAnimator vA = ObjectAnimator.ofInt(this, "alpha", startA,Color.alpha(color));
        vR.setDuration(200);
        vG.setDuration(200);
        vB.setDuration(200);
        vA.setDuration(200);
        vR.start();
        vG.start();
        vB.start();
        vA.start();
        mRed = Color.red(color);
        mGreen = Color.green(color);
        mBlue = Color.blue(color);
        mAlpha = Color.alpha(color);
        invalidate();

    }

    public void setBarColor(int color){
        if(mBPaint == null){
            makePaint();
        }
        mRed = Color.red(color);
        mGreen = Color.green(color);
        mBlue = Color.blue(color);
        mAlpha = Color.alpha(color);
        invalidate();
    }
    public void setLabelColor(int color){
        if(mPaint == null){
            makePaint();
        }
        mPaint.setColor(color);
        invalidate();
    }
    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if(mShowLabel){
            Rect bounds = new Rect();
            mPaint.getTextBounds(mLabelText,0,mLabelText.length(),bounds);
            
            c.drawText(mLabelText, (getWidth() / 2)-(bounds.width()/2), (float) (getHeight()*.95)+(bounds.height()/2), mPaint);
        }
        double scalePercentage = mAmount / mScaleAmount;
            if(mRect == null){    
                   mRect = new Rect();   
            }
            
                if(mShowLabel){
                    mRect.set(1,
                        (int) ((getHeight() - (.90 * (getHeight() * scalePercentage)) - (getHeight() * .10))),
                        getWidth() - 1, (int) (getHeight() * .90));
                }
                else{
                    mRect.set(1,
                        (int) ((getHeight() - ((getHeight() * scalePercentage)))),
                        getWidth() - 1, getHeight());
                }
                mBPaint.setColor(Color.argb(mAlpha, mRed, mGreen, mBlue));           
                c.drawRect(mRect, mBPaint);
        
    }

    public void makePaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Fonts.getFont(Fonts.PRIMARY));
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(getContext().getResources().getColor(color.gray7));
        mBPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBPaint.setStyle(Paint.Style.FILL);
        //mBPaint.setColor(Color.argb(mAlpha, mRed, mGreen, mBlue));        
    }
}
