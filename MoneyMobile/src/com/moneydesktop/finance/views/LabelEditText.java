
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

public class LabelEditText extends AnimatedEditText {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private final int PADDING_RIGHT = 40;
    
    private float mPaddingRight;
    
    private Paint mLabelPaint, mCancelPaint;
    private ColorStateList mLabelColors;
    private Rect mCancelBounds = new Rect();
    
    private String mText = "", mCancel;
    private float mLabelHeight = 0;
    private float mTextHeight = 0;
    private int mOrgRightPadding = -1;
    
    private boolean mIsPressed, mCancelShowing, mIsReversed = false;

    public LabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(attrs);
    }
    
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        int adjustment = 0;
        
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            adjustment = (int) (mLabelHeight * 1.5f);
            adjustment += mTextHeight;
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

    private void setupCancelButton() {
    	
    	mCancel = getContext().getString(R.string.wingding_cancel);
    	
        mCancelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCancelPaint.setTypeface(Fonts.getFont(Fonts.WINGDINGS));
        mCancelPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 30));
        mCancelPaint.setColor(Color.BLACK);
        mCancelPaint.setAlpha(100);
        
        mCancelPaint.getTextBounds(mCancel, 0, mCancel.length(), mCancelBounds);
    }

    private void init(AttributeSet attrs) {
    	
        mPaddingRight = UiUtils.getDynamicPixels(getContext(), PADDING_RIGHT);
        
        makeLabelPaint();
        setupCancelButton();
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditText);
        
        mIsReversed = a.getBoolean(R.styleable.LabelEditText_labelIsReverserd, mIsReversed);
        mText = a.getString(R.styleable.LabelEditText_labelText);
        setLabelTextColors(a.getColorStateList(R.styleable.LabelEditText_labelColor));
        setLabelSize(a.getFloat(R.styleable.LabelEditText_labelSize, 10.0f));
        mOrgRightPadding = a.getDimensionPixelSize(R.styleable.LabelEditText_android_paddingRight, 0);
        
        a.recycle();
        
        setGravity(Gravity.TOP);
    }
    
    private void adjustPadding() {

        int paddingRight = mCancelShowing ? (int) (mPaddingRight * 1.3) : mOrgRightPadding;
        int paddingTop;
        if (!mIsReversed) {
        	paddingTop = (int) (mLabelHeight * 1.25f);
        } else {
        	paddingTop = 0;
        }
        
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

		float additional = UiUtils.getScreenAdjustment();
    	labelSize = UiUtils.getScaledPixels(getContext(), labelSize) * additional;
    	labelSize = UiUtils.getScaledPixels(getContext(), labelSize);
    	
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
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    	super.onFocusChanged(focused, direction, previouslyFocusedRect);
    	
    	mCancelShowing = !getText().toString().equals("") && hasFocus();
    	adjustPadding();
    }
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    	super.onTextChanged(text, start, lengthBefore, lengthAfter);
    	
    	mCancelShowing = !getText().toString().equals("") && hasFocus();
    	adjustPadding();
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
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        

        if (!mIsReversed) {
        	canvas.drawText(mText, getPaddingLeft() + getScrollX(), mLabelHeight, mLabelPaint);
        } else {
        	canvas.drawText(mText, getPaddingLeft() + getScrollX(), (getHeight() - getPaddingBottom()) - mTextHeight + (mCancelBounds.height() / 2), mLabelPaint);
        }

        if (mCancelShowing) {

            float left = getWidth() - (mCancelBounds.width() / 2) - (mPaddingRight * 2 / 3) + getScrollX();
            float top = getHeight() * 2 / 3;
            
            canvas.drawText(mCancel, left, top, mCancelPaint);
        }
    }

}
