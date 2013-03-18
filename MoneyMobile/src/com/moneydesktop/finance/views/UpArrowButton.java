package com.moneydesktop.finance.views;

import com.moneydesktop.finance.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;

public class UpArrowButton extends Button {
    private boolean mShowArrow;
    private Bitmap mArrow;

    public UpArrowButton(Context context, AttributeSet attrs) {
        super(context,attrs);
        mShowArrow = false;
        mArrow = BitmapFactory.decodeResource(getResources(), R.drawable.ipad_dashboard_uparrow);
    }
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mShowArrow == true){
            canvas.drawBitmap(mArrow, (this.getWidth()/2)-(mArrow.getWidth()/2),0, null);
        }
    }
    public void showArrow(boolean arrow){
        mShowArrow = arrow;
        invalidate();
    }

}