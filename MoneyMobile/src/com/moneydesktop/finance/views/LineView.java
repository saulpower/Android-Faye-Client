package com.moneydesktop.finance.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.moneydesktop.finance.R;

public class LineView extends View {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private Paint mLinePaint;
    private boolean mIsDashed = false;
    private float mDashLength = 1.0f;
    private boolean mIsVertical = false;
    
    public int getColor() {
        return mLinePaint.getColor();
    }

    public void setColor(int color) {
        mLinePaint.setColor(color);
        invalidate();
    }
    
    public boolean isDashed() {
        return mIsDashed;
    }

    public void setIsDashed(boolean isDashed) {
        mIsDashed = isDashed;
        
        configureDashes();
        invalidate();
    }

    public float getLineWidth() {
        return mLinePaint.getStrokeWidth();
    }

    public void setLineWidth(float lineWidth) {
        mLinePaint.setStrokeWidth(lineWidth);
        invalidate();
    }

    public float getDashLength() {
        return mDashLength;
    }

    public void setDashLength(float dashLength) {
        mDashLength = dashLength;
        
        if (mDashLength > 0) {
            mIsDashed = true;
            configureDashes();
        } else {
            mIsDashed = false;
            mLinePaint.setPathEffect(null);
        }
        
        invalidate();
    }

    public boolean isVertical() {
        return mIsVertical;
    }

    public void setIsVertical(boolean isVertical) {
        this.mIsVertical = isVertical;
        invalidate();
        requestLayout();
    }

    @SuppressLint("NewApi")
    public LineView(Context context, AttributeSet attrs) {
        super(context,attrs);
        
        initPaints();
        
        if (attrs != null) {
            
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Line);
            
            setColor(a.getColor(R.styleable.Line_lineColor, Color.BLACK));
            setIsDashed(a.getBoolean(R.styleable.Line_isDashed, false));
            setLineWidth(a.getFloat(R.styleable.Line_lineStrokeWidth, 1.0f));
            setDashLength(a.getFloat(R.styleable.Line_dashLength, mIsDashed ? 1.0f : 0.0f));
            setIsVertical(a.getBoolean(R.styleable.Line_isVertical, false));
            
            a.recycle();
        }
        
        if (mIsDashed && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
    private void initPaints() {
        
        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.light_gray1));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1.0f);
    }
    
    private void configureDashes() {

        if (mIsDashed) {
            DashPathEffect dashPath = new DashPathEffect(new float[] {mDashLength, mDashLength}, 1.0f);
            mLinePaint.setPathEffect(dashPath);
        }
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        
        canvas.rotate(mIsVertical ? 90.0f : 0.0f);
        
        canvas.drawLine(0, 0, mIsVertical ? getHeight() : getWidth(), 0, mLinePaint);
        
        canvas.restore();
    }
    
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) mLinePaint.getStrokeWidth();
        
        if (mIsVertical) {
            width  = (int) mLinePaint.getStrokeWidth();
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        
        setMeasuredDimension(width, height);
    }
}
