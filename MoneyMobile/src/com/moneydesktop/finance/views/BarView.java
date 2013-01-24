
package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;

import com.moneydesktop.finance.R.color;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.ObjectAnimator;

public class BarView extends View {
    private String mLabelText;
    private float mTextSize;
    private Paint mPaint;
    private Paint mBPaint;
    private double mAmount;
    private double mScaleAmount;
    private Rect mRect;
    private boolean mShowLabel;
    public BarView(Context context, String day, double amount, double scale_amount) {
        super(context);
        mTextSize = UiUtils.getDynamicPixels(getContext(), 11);
        makePaint();
        mLabelText = day;
        mShowLabel = true;
        mAmount = amount;
        mScaleAmount = scale_amount;
    }
    public BarView (Context context){
        super(context);
        makePaint();
        mLabelText = "Test Bar";
        mShowLabel = true;
        mAmount = 125;
        mScaleAmount = 250;
    }
    public BarView(Context context, double amount, double scale_amount){
        super(context);     
        makePaint();
        mShowLabel = false;
        mAmount = amount;
        mScaleAmount = scale_amount;
    }
    public void setTextSize(float s){
        mTextSize = s;
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
    public void setScaleAmount(float a){
        mScaleAmount = a;
        invalidate();
    }
    public void setAmount(float a){
        mAmount = a;
        invalidate();
    }
    public void setAmountAnimated(float a){
        final float start = (float) mAmount;
        ObjectAnimator v = ObjectAnimator.ofFloat(this, "amount", start,(float)a);
        v.setDuration(500);
        //v.setInterpolator(new DecelerateInterpolator());
        v.start();        
    }

    public void setBarColor(int color){
        if(mBPaint == null){
            makePaint();
        }
        mBPaint.setColor(color);
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
            if(mShowLabel){
                mRect = new Rect(
                        1,
                        (int) ((getHeight() - (.90 * (getHeight() * scalePercentage)) - (getHeight() * .10))),
                        getWidth() - 1, (int) (getHeight() * .90));
                }
                else{
                   mRect = new Rect(
                        1,
                        (int) ((getHeight() - ((getHeight() * scalePercentage)))),
                        getWidth() - 1, getHeight());
                }   
            }
            else{
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
            }
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
        mBPaint.setColor(getContext().getResources().getColor(color.gray1));        
    }
}
