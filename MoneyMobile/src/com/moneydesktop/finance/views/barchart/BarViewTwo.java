package com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class BarViewTwo extends View {

    public final String TAG = this.getClass().getSimpleName();

    private String mLabelText = "";

    private Paint mBarPaint;
    private Paint mLabelPaint;

    private Rect mBarBounds;
    private Rect mLabelBounds;

    private Point mLabelPoint = new Point(0, 0);

    private int mLabelHeight;
    private float mBarAmount = 0f;
    private float mMaxAmount = 0f;

    private int mMinHeight = 0;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public BarViewTwo(Context context) {
        super(context);

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint = new Paint(mBarPaint);

        mBarBounds = new Rect();
        mLabelBounds = new Rect();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mLabelHeight = (int) (getHeight() * 0.1f);

        updateBarBounds();
        updateLabelBounds();

        invalidate();
    }

    private void updateBarBounds() {

        float percent = mBarAmount / mMaxAmount;
        float maxBarHeight = (getHeight() - mLabelHeight);

        int barTop = (int) (maxBarHeight - (maxBarHeight * percent));

        // Keep the bar at least 2px high
        if (mMinHeight > 0 && (getHeight() - mLabelHeight - barTop) < mMinHeight) {
            barTop = (getHeight() - mLabelHeight - mMinHeight);
        }

        mBarBounds.left = 0;
        mBarBounds.top = barTop;
        mBarBounds.right = getWidth();
        mBarBounds.bottom = getHeight() - mLabelHeight;
    }

    public void setBarColor(int color) {
        mBarPaint.setColor(color);
        invalidate();
    }

    public void setBarAmount(float amount) {
        mBarAmount = amount;
        updateBarBounds();
        invalidate();
    }

    public float getBarAmount() {
        return mBarAmount;
    }

    public void setLabelText(String text) {
        mLabelText = text;
        updateLabelBounds();
    }

    public void setLabelTextColor(int color) {
        mLabelPaint.setColor(color);
        invalidate();
    }

    public void setLabelTypeface(Typeface typeface) {
        mLabelPaint.setTypeface(typeface);
        updateLabelBounds();
    }

    public void setLabelTextSize(float size) {
        mLabelPaint.setTextSize(size);
        updateLabelBounds();
    }

    public void setMinBarHeight(int height) {
        mMinHeight = height;
    }

    private void updateLabelBounds() {

        mLabelPaint.getTextBounds(mLabelText, 0, mLabelText.length(), mLabelBounds);

        int y = getHeight() - (mLabelHeight + mLabelBounds.height()) / 2;
        int x = (getWidth() - mLabelBounds.width()) / 2;

        mLabelPoint = new Point(x, y);

        invalidate();
    }

    void setMaxAmount(float max) {
        mMaxAmount = max;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(mBarBounds, mBarPaint);
        canvas.drawText(mLabelText, mLabelPoint.x, mLabelPoint.y, mLabelPaint);
    }
}
