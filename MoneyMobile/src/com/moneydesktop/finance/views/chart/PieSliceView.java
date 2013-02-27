package com.moneydesktop.finance.views.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.moneydesktop.finance.util.UiUtils;

public class PieSliceView extends View {
    
    public final String TAG = this.getClass().getSimpleName();

    private final int DEFAULT_STROKE_WIDTH = 2;
    
	private float mDegreeOffset;
	private float mPercent;
	private PointF mCenter;
	private RectF mBounds = new RectF();

	private Paint mPaint, mStrokePaint;

	public float getPercent() {
		return mPercent;
	}
	
	public float getDegrees() {
		return mPercent * 360;
	}
	
	public void setPercent(float percent) {
		mPercent = percent;
		invalidate();
	}
	
	public float getSliceCenter() {
		return mDegreeOffset + getDegrees() / 2;
	}
	
	public void setStokeWidth(float width) {
		mStrokePaint.setStrokeWidth(width);
		updateBounds();
	}
	
	public float getStrokeWidth() {
		return mStrokePaint.getStrokeWidth();
	}
	
	public void setStrokeColor(int color) {
		mStrokePaint.setColor(color);
		invalidate();
	}
	
	public int getStrokeColor() {
		return mStrokePaint.getColor();
	}
	
	/**
	 * Returns whether this slice contains the degree supplied.
	 * 
	 * @param rotationOffset The overall rotational offset of the chart
	 * @param degree The degree to be checked
	 * @return True if this slice contains the degree
	 */
	public boolean containsDegree(float rotationOffset, float degree) {
		
		degree = degree - rotationOffset;
		if (degree < 0) degree += 360;
		degree %= 360;
		
		return mDegreeOffset < degree && degree < (mDegreeOffset + getDegrees());
	}
	
	public int getSliceColor() {
		return mPaint.getColor();
	}
	
	public void setSliceColor(int color) {
		mPaint.setColor(color);
		invalidate();
	}
	
	/**
	 * Create a new pie slice to be used in a pie chart.
	 * 
	 * @param context the context for this view
	 * @param degreeOffset the starting degree offset for the slice
	 * @param percent the percent the slice covers
	 * @param color the color of the slice
	 */
	public PieSliceView(Context context, float degreeOffset, float percent, int color) {
		super(context);
		
		init();
		
		mDegreeOffset = degreeOffset;
		setPercent(percent);
		setSliceColor(color);
	}
	
	/**
	 * Initialize our paints and such
	 */
	private void init() {
		
		float strokeWidth = UiUtils.getDynamicPixels(getContext(), DEFAULT_STROKE_WIDTH);

		mCenter = new PointF();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(strokeWidth);
		mStrokePaint.setColor(Color.WHITE);
	}
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		calculateCenter();
		
		updateBounds();
	}
	
	private void updateBounds() {
		
		// Updates the drawing bounds so the slice is sized correctly given the
		// stroke width
		mBounds.left = mStrokePaint.getStrokeWidth();
		mBounds.top = mStrokePaint.getStrokeWidth();
		mBounds.right = getWidth() - mStrokePaint.getStrokeWidth();
		mBounds.bottom = getHeight() - mStrokePaint.getStrokeWidth();
		
		invalidate();
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		setMeasuredDimension(width, height);
	}
	
	private void calculateCenter() {
        
		// Get the center coordinates of the view
		mCenter.x = (float) (getWidth() / 2);
		mCenter.y = (float) (getHeight() / 2);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// Draw and stroke the pie slice
		canvas.drawArc(mBounds, mDegreeOffset, getDegrees(), true, mPaint);
		canvas.drawArc(mBounds, mDegreeOffset, getDegrees(), true, mStrokePaint);
	}
}
