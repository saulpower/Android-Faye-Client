package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.moneydesktop.finance.R;

public class VerticalTextView extends TextView {
    
    public final String TAG = this.getClass().getSimpleName();

	private boolean mTopDown = false;

	public VerticalTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (attrs != null) {
            
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalText);
    
            mTopDown = a.getBoolean(R.styleable.VerticalText_isLeft, false);
            
            a.recycle();
        }
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected void onDraw(Canvas canvas) {
	    
		TextPaint textPaint = getPaint();
		textPaint.setColor(getCurrentTextColor());
		textPaint.drawableState = getDrawableState();

		canvas.save();

		if (mTopDown) {
			canvas.translate(getWidth(), 0);
			canvas.rotate(90);
		} else {
		    canvas.translate(0, getHeight());
			canvas.rotate(-90);
		}

		canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
		getLayout().draw(canvas);
		
		canvas.restore();
	}
}
