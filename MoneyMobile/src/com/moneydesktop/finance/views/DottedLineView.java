package com.moneydesktop.finance.views;

import com.moneydesktop.finance.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DottedLineView extends View {
   private Paint mLinePaint;
   
    @SuppressLint("NewApi")
	public DottedLineView(Context context, AttributeSet attrs) {
        super(context,attrs);
        
        initPaints(2,1);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } 
    }
    
    public void setDash(float dashwhite, float dashblack) {
        initPaints(dashwhite,dashblack);
    }
    
    private void initPaints(float dashwhite, float dashblack) {
        
        DashPathEffect dashPath = new DashPathEffect(new float[] {dashwhite, dashblack}, 1.0f);
        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.light_gray1));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setPathEffect(dashPath);
        
        invalidate();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.drawLine(0, 0, (int) (this.getWidth()), 0, mLinePaint);
    }
    
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),1);
    }
}
