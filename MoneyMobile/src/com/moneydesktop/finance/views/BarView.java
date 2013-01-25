
package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.moneydesktop.finance.R.color;
import com.moneydesktop.finance.util.Fonts;

public class BarView extends View {
    private final String mText;
    private Paint mPaint;
    private Paint mBPaint;
    private final double mAmount;
    private final double mScaleAmount;
    private Rect mRect;

    public BarView(Context context, String day, double amount, double scale_amount) {
        super(context);
        makePaint();
        mText = day;
        mAmount = amount;
        mScaleAmount = scale_amount;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawText(mText, (getWidth() / 2) - 4, getHeight(), mPaint);
        c.drawRect(mRect, mBPaint);
    }
    @Override
    public void onLayout(boolean b, int i, int j, int k, int l){
        super.onLayout(b, i, j, k, l);
        double scalePercentage = mAmount / mScaleAmount;

        if(mRect == null){
            mRect = new Rect(
                1,
                (int) ((getHeight() - (.90 * (getHeight() * scalePercentage)) - (getHeight() * .10))),
                getWidth() - 1, (int) (getHeight() * .90));
    }
    }

    public void makePaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Fonts.getFont(Fonts.PRIMARY));
        mPaint.setTextSize(11);
        mPaint.setColor(getContext().getResources().getColor(color.gray7));
        mBPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBPaint.setStyle(Paint.Style.FILL);        
        mBPaint.setColor(getContext().getResources().getColor(color.gray1));        
    }
}
