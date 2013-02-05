
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

public class LabelEditText extends EditText {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private final int PADDING_RIGHT = 40;
    
    private float mPaddingRight;
    
    private Paint mLabelPaint;
    private ColorStateList mLabelColors;
    
    private String mText = "";
    private float mLabelHeight = 0;
    private float mTextHeight = 0;
    private Bitmap mClearButton, mClearButtonPressed;
    
    private boolean mIsPressed;

    public LabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mPaddingRight = UiUtils.getDynamicPixels(getContext(), PADDING_RIGHT);
        init(attrs);
    }
    
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        int adjustment = 0;
        
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            adjustment = (int) (mLabelHeight * 1.5f);
        }
        
        setMeasuredDimension(width, height + adjustment);
        adjustPadding();
    }
    
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        
        updateLabelTextColor();
    }
    
    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        
        updateTextHeight();
    }
    
    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
        
        updateTextHeight();
    }
    
    private void updateLabelTextColor() {
        
        int color = mLabelColors.getColorForState(getDrawableState(), 0);
        
        if (color != mLabelPaint.getColor()) {
            setLabelTextColor(color);
        }
    }
    
    private void updateLabelHeight() {

        Paint.FontMetrics metrics = mLabelPaint.getFontMetrics();
        mLabelHeight = metrics.bottom - metrics.top;
        
        adjustPadding();
    }
    
    private void updateTextHeight() {

        Paint.FontMetrics metrics = getPaint().getFontMetrics();
        mTextHeight = metrics.bottom - metrics.top;
        
        adjustPadding();
    }

    private void makeLabelPaint() {
        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setStyle(Paint.Style.FILL);
        mLabelPaint.setTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mLabelPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 10));
    }

    private void init(AttributeSet attrs) {

        makeLabelPaint();
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditText);
        
        setLabelText(a.getString(R.styleable.LabelEditText_labelText));
        setLabelTextColors(a.getColorStateList(R.styleable.LabelEditText_labelColor));
        setLabelSize(a.getDimension(R.styleable.LabelEditText_labelSize, 10.0f));
        Resources res = getResources();
        mClearButton = BitmapFactory.decodeResource(res, R.drawable.clear_button);
        mClearButtonPressed = BitmapFactory.decodeResource(res, R.drawable.clear_button_pressed);
        
        a.recycle();
        
        setGravity(Gravity.TOP);
    }
    
    private void adjustPadding() {

        int paddingRight = (int) (mPaddingRight * 1.3);
        int paddingTop = (int) mLabelHeight;
        
        paddingTop += (int) (((getMeasuredHeight() - paddingTop) / 2) - (mTextHeight / 2));
        
        setPadding(getPaddingLeft(), paddingTop, paddingRight, getPaddingBottom());
        
        scrollTo(0, 0);
        
        requestLayout();
        invalidate();
    }
    
    public void setLabelTextColors(ColorStateList colors) {
        
        mLabelColors = colors;
        updateLabelTextColor();
    }

    public void setLabelTextColor(int color) {

        mLabelPaint.setColor(color);
        invalidate();
    }

    public void setLabelText(String text) {
        
        mText = text;
        invalidate();
    }

    public void setLabelSize(float labelSize) {

        mLabelPaint.setTextSize(labelSize);
        
        updateLabelHeight();
        invalidate();
    }
    
    public void setLabelFont(Typeface typeface) {

        mLabelPaint.setTypeface(typeface);
        
        updateLabelHeight();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        final int action = ev.getAction();

        switch (action) {

            case MotionEvent.ACTION_DOWN: {
                
                if (isPointInsideClearBox(ev.getRawX(), ev.getRawY()) && hasFocus()) {
                    mIsPressed = true;
                }
                
                break;
            }

            case MotionEvent.ACTION_UP: {
                
                if (isPointInsideClearBox(ev.getRawX(), ev.getRawY()) && mIsPressed) {
                    setText("");
                }
                
                mIsPressed = false;
                
                break;
            }
        }

        return true;
    }

    protected boolean isPointInsideClearBox(float x, float y) {
        
        int location[] = new int[2];
        this.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        // point is inside view bounds
        if ((x > viewX + (getWidth() * .90) && x < (viewX + getWidth()))
                && (y > viewY && y < (viewY + this.getHeight()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        
        c.drawText(mText, getPaddingLeft() + getScrollX(), mLabelHeight, mLabelPaint);
        
        if (!getText().toString().equals("") && hasFocus()) {
            
            float left = getWidth() - (mClearButton.getWidth() / 2) - (mPaddingRight * 2 / 3) + getScrollX();
            float top = (((getHeight() - getPaddingTop()) / 2) - (mClearButton.getHeight() / 2)) + getPaddingTop();
            
            c.drawBitmap(mIsPressed ? mClearButtonPressed : mClearButton, left, top, null);
        }
    }

}
