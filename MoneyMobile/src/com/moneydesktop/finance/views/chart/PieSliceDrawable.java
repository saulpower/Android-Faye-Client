package com.moneydesktop.finance.views.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.moneydesktop.finance.util.UiUtils;

public class PieSliceDrawable extends Drawable {
    
    public final String TAG = this.getClass().getSimpleName();

    private final int DEFAULT_STROKE_WIDTH = 2;
    
	private float mDegreeOffset;
	private float mPercent;
	private RectF mBounds = new RectF();

	private Paint mPaint, mStrokePaint;
	
	private Context mContext;
	
	private float mStrokeWidth;
	
	private Path mPathRight, mPathLeft;
	
	public float getDegreeOffset() {
		return mDegreeOffset;
	}

	public void setDegreeOffset(float mDegreeOffset) {
		this.mDegreeOffset = mDegreeOffset;
	}

	public float getPercent() {
		return mPercent;
	}
	
	public float getDegrees() {
		return mPercent * 360;
	}
	
	public void setPercent(float percent) {
		mPercent = percent;
		invalidateSelf();
	}
	
	public float getSliceCenter() {
		return mDegreeOffset + getDegrees() / 2;
	}
	
	public void setStokeWidth(float width) {
		mStrokeWidth = width;
		updateBounds();
	}
	
	public float getStrokeWidth() {
		return mStrokeWidth;
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
		
		return mDegreeOffset < degree && degree <= (mDegreeOffset + getDegrees());
	}
	
	public int getSliceColor() {
		return mPaint.getColor();
	}
	
	public void setSliceColor(int color) {
		mPaint.setColor(color);
		invalidateSelf();
	}
	
	/**
	 * Create a new pie slice to be used in a pie chart.
	 * 
	 * @param context the context for this view
	 * @param degreeOffset the starting degree offset for the slice
	 * @param percent the percent the slice covers
	 * @param color the color of the slice
	 */
	public PieSliceDrawable(Callback cb, Context context) {
		
		setCallback(cb);
		mContext = context;
		
		mStrokeWidth = UiUtils.getDynamicPixels(mContext, DEFAULT_STROKE_WIDTH);
		
		init();
	}

	/**
	 * Initialize our paints and such
	 */
	private void init() {

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mStrokePaint = new Paint(mPaint);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(mStrokeWidth);
		mStrokePaint.setColor(Color.WHITE);
	}
	
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		super.setBounds(left, top, right, bottom);
		
		updateBounds();
	}
	
	private void updateBounds() {
		
		// Updates the drawing bounds so the slice is sized correctly given the
		// stroke width
		mBounds.left = getBounds().left + mStrokeWidth;
		mBounds.top = getBounds().top + mStrokeWidth;
		mBounds.right = getBounds().right - mStrokeWidth;
		mBounds.bottom = getBounds().bottom - mStrokeWidth;
		
		double radians = Math.toRadians(mDegreeOffset + getDegrees());
		float radius = mBounds.width() / 2;
		float x = (float) (radius * Math.cos(radians));
		float y = (float) (radius * Math.sin(radians));
		
		mPathRight = createPath(x, y);
		
		radians = Math.toRadians(mDegreeOffset);
		x = (float) (radius * Math.cos(radians));
		y = (float) (radius * Math.sin(radians));
		
		mPathLeft = createPath(x, y);
		
		invalidateSelf();
	}
	
	private Path createPath(float x, float y) {

		Path path = new Path();
		path.moveTo(mBounds.centerX(), mBounds.centerY());
		path.lineTo(mBounds.centerX() + x, mBounds.centerY() + y);
		path.close();
		
		return path;
	}

	@Override
	public void draw(Canvas canvas) {
		
		// Draw and stroke the pie slice
		canvas.drawArc(mBounds, mDegreeOffset, getDegrees(), true, mPaint);
		canvas.drawPath(mPathRight, mStrokePaint);
		canvas.drawPath(mPathLeft, mStrokePaint);
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
		mPaint.setAlpha(alpha / 255 * 50);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {}
}
