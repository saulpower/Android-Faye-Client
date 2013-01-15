
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

public class LabelEditText extends EditText {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private final int PADDING_RIGHT = 40;
    private Paint labelPaint;
    private String text;
    private Rect bounds = new Rect();
    private Bitmap clearButton, mClearButtonPressed;
    
    private boolean mIsPressed;

    public LabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        makePaint();
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditText);
        
        setLabelText(a.getString(R.styleable.LabelEditText_labelText));
        setTextColor(a.getColor(R.styleable.LabelEditText_labelColor, Color.WHITE));
        setLabelSize(a.getDimension(R.styleable.LabelEditText_labelSize, 10.0f));
        Resources res = getResources();
        clearButton = BitmapFactory.decodeResource(res, R.drawable.clear_button);
        mClearButtonPressed = BitmapFactory.decodeResource(res, R.drawable.clear_button_pressed);
        
        setPadding(getPaddingLeft(), getPaddingTop(), (int) (PADDING_RIGHT * 1.3), getPaddingBottom());
        
        a.recycle();
    }

    public void setTextColor(int color) {
        
        if (labelPaint == null) {
            makePaint();
        }

        labelPaint.setColor(color);
        invalidate();
    }

    public void makePaint() {
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        labelPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 10));
    }

    public void setLabelText(String t) {
        text = t;
        if (labelPaint == null) {
            makePaint();
        }

        labelPaint.getTextBounds(t, 0, t.length(), bounds);
        invalidate();
    }

    public void setLabelSize(float labelSize) {
        if (labelPaint == null) {
            makePaint();
        }

        labelPaint.setTextSize(labelSize);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        final int action = ev.getAction();

        switch (action) {

            case MotionEvent.ACTION_DOWN: {
                
                if (isPointInsideClearBox(ev.getRawX(), ev.getRawY())) {
                    mIsPressed = true;
                }
                
                break;
            }

            case MotionEvent.ACTION_UP: {
                
                mIsPressed = false;
                
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
        this.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        // point is inside view bounds
        if ((x > viewX + (this.getWidth() * .90) && x < (viewX + this.getWidth()))
                && (y > viewY && y < (viewY + this.getHeight()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        
        c.drawText(text, 10 + getScrollX(), (bounds.height() + 10), labelPaint);
        
        if (!this.getText().toString().equals("") && hasFocus()) {
            c.drawBitmap(mIsPressed ? mClearButtonPressed : clearButton, this.getWidth() - PADDING_RIGHT + getScrollX(), ((this.getHeight() / 2) - (clearButton.getHeight() / 2)), null);
        }
    }

}
