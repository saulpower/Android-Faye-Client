package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.moneydesktop.finance.R;

public class Caret extends ImageView {
	
	private int mColor = Color.BLUE;
	private float mWidth = 10;
	private float mHeight = 10;
	
	private PointF mPoint1;        
	private PointF mPoint2;    
	private PointF mPoint3;
	private Path mPath;

	private Paint mPaint;
	
	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		this.mColor = color;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    mPaint.setStrokeWidth(1);
	    mPaint.setColor(color);     
	    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	    mPaint.setAntiAlias(true);
	}

	public float getCaretWidth() {
		return mWidth;
	}

	public void setCaretWidth(float width) {
		this.mWidth = width;
		createPath();
	}

	public float getCaretHeight() {
		return mHeight;
	}

	public void setCaretHeight(float height) {
		this.mHeight = height;
		createPath();
	}

	public Caret(Context context) {
		super(context);
		
		createPath();
	}
	
	public Caret(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(attrs);
	}
	
	public Caret(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(attrs);
	}
	
	private void createPath() {
		
		float center = mHeight/5;
		
		mPoint1 = new PointF(0, (mHeight/2 - center));  
		mPoint2 = new PointF(mWidth, (mHeight/2 - center));    
		mPoint3 = new PointF((mWidth/2), (mHeight - center));

	    mPath = new Path();
	    mPath.setFillType(Path.FillType.EVEN_ODD);
	    
	    mPath.moveTo(mPoint1.x,mPoint1.y);
	    mPath.lineTo(mPoint2.x,mPoint2.y);
	    mPath.lineTo(mPoint3.x,mPoint3.y);
	    mPath.lineTo(mPoint1.x,mPoint1.y);
	    mPath.close();
	}

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) getCaretWidth(), (int) getCaretHeight());
    }
	
	private void init(AttributeSet attrs) {
		
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Caret);
		
	    setColor(a.getColor(R.styleable.Caret_color, Color.WHITE));
	    setCaretWidth(a.getDimension(R.styleable.Caret_width, 10.0f));
	    setCaretHeight(a.getDimension(R.styleable.Caret_height, 10.0f));
		    
		a.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	    canvas.drawPath(mPath, mPaint);
	}

}
