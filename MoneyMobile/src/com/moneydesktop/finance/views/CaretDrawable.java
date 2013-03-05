package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.moneydesktop.finance.util.UiUtils;

public class CaretDrawable extends Drawable {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private static final int STROKE_WIDTH = 3;
    
    private Context mContext;
    
    private int mColor = Color.BLUE;
    private float mWidth = 10;
    private float mHeight = 10;
    private PointF mPosition;

    private Paint mPaint;
    private Paint mShadowPaint;
    
    private PointF mPoint1;        
    private PointF mPoint2;    
    private PointF mPoint3;
    private Path mPath;
    
    private boolean mShadow = false;
    
    private float mDegrees = 0.0f;
    
    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
            mPaint.setStrokeWidth(1); 
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        
        mPaint.setColor(color);
        
        invalidateSelf();
    }

    public float getCaretWidth() {
        return mWidth;
    }

    public void setCaretWidth(float width) {
        this.mWidth = width;
        createPath();
        invalidateSelf();
    }

    public float getCaretHeight() {
        return mHeight;
    }

    public void setCaretHeight(float height) {
        this.mHeight = height;
        createPath();
        invalidateSelf();
    }
    
    public void setCaretRotation(float degrees) {
        mDegrees = degrees;
        invalidateSelf();
    }
    
    public float getCaretRotation() {
        return mDegrees;
    }
    
    public CaretDrawable(Context context, PointF position, float width, float height) {
    	this(position, width, height);
    	
    	mContext = context;
        
    	mShadow = true;
    	
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setStyle(Paint.Style.STROKE);
        mShadowPaint.setStrokeWidth(UiUtils.getDynamicPixels(mContext, STROKE_WIDTH));
        mShadowPaint.setAlpha(50);
    }
    
    public CaretDrawable(PointF position, float width, float height) {
        
        mPosition = position;
        mWidth = width;
        mHeight = height;
        
        setColor(Color.BLACK);
        createPath();
    }
    
    private void createPath() {
        
        mPoint1 = new PointF(mPosition.x, mPosition.y + mHeight / 4);  
        mPoint2 = new PointF(mPosition.x + mWidth, mPosition.y + mHeight / 4);    
        mPoint3 = new PointF(mPosition.x + (mWidth / 2), mPosition.y + (mHeight * 3 / 4));

        mPath = new Path();
        mPath.setFillType(Path.FillType.EVEN_ODD);
        
        mPath.moveTo(mPoint1.x, mPoint1.y);
        mPath.lineTo(mPoint2.x, mPoint2.y);
        mPath.lineTo(mPoint3.x, mPoint3.y);
        mPath.lineTo(mPoint1.x, mPoint1.y);
        
        mPath.close();
    }
    
    @Override
    public void draw(Canvas canvas) {

        canvas.save();
        
        canvas.rotate(mDegrees, mPosition.x + mWidth/2, mPosition.y + mHeight/2);
        
        if (mShadow) {
            canvas.drawPath(mPath, mShadowPaint);
        }
        
        canvas.drawPath(mPath, mPaint);
        
        canvas.restore();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

}
