package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.moneydesktop.finance.R;

public class VerticalTextView extends TextView {
    
    public final String TAG = this.getClass().getSimpleName();

	private boolean mTopDown = false;
	private Rect mBounds = new Rect();
	private Canvas mDummy;

	public VerticalTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDummy = new Canvas();
		
		if (attrs != null) {
            
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalText);
    
            mTopDown = a.getBoolean(R.styleable.VerticalText_isLeft, false);
            
            a.recycle();
        }
	}
	
	@Override
	public void setTextSize(float size) {
	    super.setTextSize(size);
	    
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), mBounds);
        invalidate();
	}
	
	private Rect getBounds() {
	    
	    if (mBounds.width() == 0 && mBounds.height() == 0) {
	        getPaint().getTextBounds(getText().toString(), 0, getText().length(), mBounds);
	    }
	    
	    return mBounds;
	}
	
	public void setText(String text) {
	    super.setText(text);
	    
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), mBounds);
        invalidate();
	}
	
	private int getGravityAdjustment() {
	    
	    if ((getGravity() & Gravity.LEFT) > 1 ) {
	        return -Math.abs(getHeight()/2 - getBounds().width()/2);
	    }
	    
	    if ((getGravity() & Gravity.RIGHT) > 1) {
	        return Math.abs(getHeight()/2 - getBounds().width()/2);
	    }
	    
	    return 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
        
        super.onDraw(mDummy);

		canvas.save();

		if (mTopDown) {
			canvas.rotate(90, getWidth()/2, getHeight()/2);
		} else {
			canvas.rotate(-90, getWidth()/2, getHeight()/2);
		}
		
        canvas.translate(getGravityAdjustment(), 0);
        
        canvas.drawText(getText().toString(), getWidth()/2 - getBounds().width()/2, getHeight()/2 + getBounds().height()/2, getPaint());
        
		canvas.restore();
	}
}
