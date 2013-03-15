package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.color;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.GraphBarTouchEvent;
import com.moneydesktop.finance.model.EventMessage.GraphDataZoomEvent;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.ObjectAnimator;
import de.greenrobot.event.EventBus;

public class BarView extends View {
    private String mLabelText;


    private String mPopupText;
    private float mTextSize;
    private Paint mTextPaint;
    private Paint mBarPaint;
    private int mRed;
    private int mBlue;
    private int mGreen;
    private int mAlpha;
    private double mAmount;
    private double mScaleAmount;
    private int mSelectedColor;
    private Rect mRect;
    private boolean mShowLabel;
    private long mTime;
    private GestureDetector mGestureDetector;

    public BarView(Context context) {
        super(context);
        mTextSize = UiUtils.getScaledPixels(getContext(), 14);
        makePaint();
        mLabelText = "text";
        mShowLabel = false;
        int defColor = getResources().getColor(color.gray3);
        mRed = Color.red(defColor);
        mGreen = Color.green(defColor);
        mBlue = Color.blue(defColor);
        mAlpha = Color.alpha(defColor);
        mSelectedColor = getResources().getColor(R.color.primaryColor);
        mAmount = 5;
        mScaleAmount = 100;
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    public BarView(Context context, String day, double amount,
                   double scale_amount) {
        super(context);
        mTextSize = UiUtils.getScaledPixels(getContext(), 11);
        makePaint();
        mLabelText = day;
        mShowLabel = true;
        int defColor = getResources().getColor(color.gray3);
        mRed = Color.red(defColor);
        mGreen = Color.green(defColor);
        mBlue = Color.blue(defColor);
        mAlpha = Color.alpha(defColor);
        mAmount = amount;
        mScaleAmount = scale_amount;
        mGestureDetector = new GestureDetector(getContext(),
                new GestureListener());
    }

    public BarView(Context context, double amount, double scale_amount) {
        super(context);
        makePaint();
        mShowLabel = false;
        int defColor = getResources().getColor(color.gray3);
        mRed = Color.red(defColor);
        mGreen = Color.green(defColor);
        mBlue = Color.blue(defColor);
        mAlpha = Color.alpha(defColor);
        mAmount = amount;
        mScaleAmount = scale_amount;
        mGestureDetector = new GestureDetector(getContext(),
                new GestureListener());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowLabel) {
            Rect bounds = new Rect();
            mTextPaint.getTextBounds(mLabelText, 0, mLabelText.length(), bounds);

            canvas.drawText(mLabelText,
                    (getWidth() / 2) - (bounds.width() / 2),
                    (float) (getHeight() * .95) + (bounds.height() / 2), mTextPaint);
        }
        double scalePercentage = mAmount / mScaleAmount;
        if (mRect == null) {
            mRect = new Rect();
        }

        if (mShowLabel) {
            if (mAmount > 10)
                mRect.set(
                        1,
                        (int) ((getHeight()
                                - (.90 * (getHeight() * scalePercentage)) - (getHeight() * .10))),
                        getWidth() - 1, (int) (getHeight() * .90));
            else {
                mRect.set(
                        1,
                        (int) ((getHeight()
                                - (.90 * (getHeight() * (10 / mScaleAmount))) - (getHeight() * .10))),
                        getWidth() - 1, (int) (getHeight() * .90));
            }
        } else {
            if (mAmount > 10) {
                mRect.set(1,
                        (int) (getHeight() - (getHeight() * scalePercentage)),
                        getWidth() - 1, getHeight());
            } else {
                mRect.set(1,
                        (int) (getHeight() - (getHeight() * (10 / mScaleAmount))),
                        getWidth() - 1, getHeight());
            }
        }
        mBarPaint.setColor(Color.argb(mAlpha, mRed, mGreen, mBlue));
        canvas.drawRect(mRect, mBarPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }

    public void setTime(long time) {
        mTime = time;
    }

    public Rect getRect() {
        return mRect;
    }

    public long getTime() {
        return mTime;
    }

    public void setTextSize(float size) {
        mTextSize = size;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public void setLabelText(String text) {
        mLabelText = text;
    }

    public void showLabel(boolean toShow) {
        mShowLabel = toShow;
        invalidate();
    }

    public double getScaleAmount() {
        return mScaleAmount;
    }

    public double getAmount() {
        return (float) mAmount;
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSelectedColor(int mSelectedColor) {
        this.mSelectedColor = mSelectedColor;
    }

    public void setScaleAmount(float amount) {
        mScaleAmount = amount;
        invalidate();
    }

    public void setAmount(float amount) {
        mAmount = amount;
        invalidate();
    }

    //These are used by the object animator, do not remove them.
    private void setRed(int red) {
        mRed = red;
    }

    private void setBlue(int blue) {
        mBlue = blue;
    }

    private void setGreen(int green) {
        mGreen = green;
    }

    private void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    public String getPopupText() {
        return mPopupText;
    }

    public void setPopupText(String mPopupText) {
        this.mPopupText = mPopupText;
    }

    public void setAmountAnimated(float a) {
        final float start = (float) mAmount;
        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(this, "amount", start,
                (float) a);

        animator.setDuration(500);
        animator.start();

        invalidate();
    }

    public void setColorAnimated(int color) {
        final int startR = mRed;
        final int startG = mGreen;
        final int startB = mBlue;
        final int startA = mAlpha;

        ObjectAnimator redAnimator = ObjectAnimator.ofInt(this, "red", startR,
                Color.red(color));
        ObjectAnimator greenAnimator = ObjectAnimator.ofInt(this, "green",
                startG, Color.green(color));
        ObjectAnimator blueAnimator = ObjectAnimator.ofInt(this, "blue",
                startB, Color.blue(color));
        ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(this, "alpha",
                startA, Color.alpha(color));

        redAnimator.setDuration(200);
        greenAnimator.setDuration(200);
        blueAnimator.setDuration(200);
        alphaAnimator.setDuration(200);

        redAnimator.start();
        greenAnimator.start();
        blueAnimator.start();
        alphaAnimator.start();

        mRed = Color.red(color);
        mGreen = Color.green(color);
        mBlue = Color.blue(color);
        mAlpha = Color.alpha(color);

        invalidate();

    }

    public Boolean isShowingLabel() {
        return mShowLabel;
    }

    public String getLabel() {
        return mLabelText;
    }

    public void setBarColor(int color) {
        if (mBarPaint == null) {
            makePaint();
        }

        mRed = Color.red(color);
        mGreen = Color.green(color);
        mBlue = Color.blue(color);
        mAlpha = Color.alpha(color);

        invalidate();
    }

    public void setLabelColor(int color) {
        if (mTextPaint == null) {
            makePaint();
        }
        mTextPaint.setColor(color);
        invalidate();
    }

    public double getMax() {
        return mScaleAmount;
    }

    public void makePaint() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTypeface(Fonts.getFont(Fonts.PRIMARY));
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(getContext().getResources().getColor(color.gray7));

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setStyle(Paint.Style.FILL);
    }

    private BarView getInstance() {
        return this;
    }

    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {

        public boolean onDown(MotionEvent evt) {
            return true;
        }

        public boolean onSingleTapUp(MotionEvent evt) {
            GraphBarTouchEvent event = new EventMessage().new GraphBarTouchEvent();
            event.setBar(getInstance());
            EventBus.getDefault().post(event);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            GraphDataZoomEvent event = new EventMessage().new GraphDataZoomEvent();
            event.setDate(mTime);

            EventBus.getDefault().post(event);
            return true;

        }
    }
}