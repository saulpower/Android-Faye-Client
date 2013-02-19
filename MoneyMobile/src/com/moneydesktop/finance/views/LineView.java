package com.moneydesktop.finance.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.UiUtils;

public class LineView extends View {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private Paint mLinePaint;
    private boolean mIsDashed = false;
    private float mDashLength = 1.0f;
    private boolean mIsVertical = false;
    private float mDensity;
    private ColorStateList mLineColors;
    
    public int getColor() {
        return mLinePaint.getColor();
    }

    public void setColor(int color) {
        mLinePaint.setColor(color);
        invalidate();
    }
    
    public void setLineColors(ColorStateList colors) {
        
        mLineColors = colors;
        updateLineColor();
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
    
    private void updateLineColor() {
        
        int color = mLineColors.getColorForState(getDrawableState(), 0);
        
        if (color != mLinePaint.getColor()) {
            setColor(color);
        }
    }

    @SuppressLint("NewApi")
    public LineView(Context context, AttributeSet attrs) {
        super(context,attrs);
        
        mDensity = UiUtils.getDensityRatio(getContext());
        
        initPaints();
        
        if (attrs != null) {
            
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Line);

            setLineColors(a.getColorStateList(R.styleable.Line_lineColor));
            setIsDashed(a.getBoolean(R.styleable.Line_isDashed, false));
            setLineWidth(a.getDimension(R.styleable.Line_lineStrokeWidth, 1.0f));
            setDashLength(a.getDimension(R.styleable.Line_dashLength, mIsDashed ? 1.0f : 0.0f));
            setIsVertical(a.getBoolean(R.styleable.Line_isVertical, false));
            
            a.recycle();
        }
        
        if (mIsDashed && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        
        updateLineColor();
    }
    
    private void initPaints() {
        
        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.light_gray1));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(0);
        mLinePaint.setAntiAlias(false);
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
        height /= mDensity;
        
        height = (height <= 0) ? 1 : height;
        
        if (mIsVertical) {
            width  = (int) mLinePaint.getStrokeWidth();
            width /= mDensity;
            width = (width <= 0) ? 1 : width;
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        
        setMeasuredDimension(width, height);
    }
}
