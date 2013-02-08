
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

public class ClearEditText extends EditText {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private final int PADDING_RIGHT = 25;
    private float mPaddingRight;
    private String mClear, mIcon;
    private Paint mClearPaint, mIconPaint;
    private int mIconColor;

    private Rect mClearBounds = new Rect(), mIconBounds = new Rect();

    public String getClear() {
        return mClear;
    }

    public void setClear(String clear) {
        
        if (mClear != null) {
            mClear = clear;
        } else {
            mClear = "X";
        }
        
        invalidate();
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        
        if (icon != null) {
            mIcon = icon;
        } else {
            mIcon = "8";
        }
        
        invalidate();
    }
    
    public int getIconColor() {
        return mIconColor;
    }

    public void setIconColor(int mIconColor) {
        this.mIconColor = mIconColor;
        invalidate();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        
        mPaddingRight = UiUtils.getDynamicPixels(getContext(), PADDING_RIGHT) + getPaddingRight();
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ClearEditText);
        
        setClear(a.getString(R.styleable.ClearEditText_clearText));
        setIcon(a.getString(R.styleable.ClearEditText_iconText));
        setIconColor(a.getColor(R.styleable.ClearEditText_iconTextColor, getCurrentTextColor()));
        
        a.recycle();
        
        setupPaints();
    }
    
    private void setupPaints() {

        mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClearPaint.setStyle(Paint.Style.FILL);
        mClearPaint.setTypeface(Fonts.getFont(Fonts.GLYPH));
        mClearPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 20));
        mClearPaint.setColor(getIconColor());

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setStyle(Paint.Style.FILL);
        mIconPaint.setTypeface(Fonts.getFont(Fonts.GLYPH));
        mIconPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 20));
        mIconPaint.setColor(getIconColor());

        mClearPaint.getTextBounds(mClear, 0, mClear.length(), mClearBounds);
        mIconPaint.getTextBounds(mIcon, 0, mIcon.length(), mIconBounds);
        
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        final int action = ev.getAction();

        switch (action) {

            case MotionEvent.ACTION_UP: {
                
                if (isPointInsideClearBox(ev.getRawX(), ev.getRawY())) {
                    setText("");
                }
                
                break;
            }
        }

        return true;
    }

    protected boolean isPointInsideClearBox(float x, float y) {
        int location[] = new int[2];
        getLocationOnScreen(location);
        
        int viewX = location[0];
        int viewY = location[1];

        // point is inside view bounds
        if ((x > viewX + (getWidth() * .90) && x < (viewX + getWidth())) && (y > viewY && y < (viewY + getHeight()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        
        if (!getText().toString().equals("")) {
            c.drawText(mClear, getWidth() - mPaddingRight + getScrollX(), ((getHeight() / 2) + (mClearBounds.height() / 2) + 5), mClearPaint);
        } else {
            c.drawText(mIcon, getWidth() - mPaddingRight + getScrollX(), ((getHeight() / 2) + (mIconBounds.height() / 2)), mIconPaint);
        }
    }

}
