package main.java.com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.view.View;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class BarView extends View {

    public final String TAG = this.getClass().getSimpleName();

    enum BarPosition {
        LEFT(0), MIDDLE(-1), RIGHT(1);

        private int index;

        BarPosition(int index) {
            this.index = index;
        }

        public static BarPosition fromInteger(Integer value) {

            switch(value) {
                case 0:
                    return RIGHT;
                case 1:
                    return LEFT;
                default:
                    return MIDDLE;
            }
        }

        public int index() {
            return index;
        }
    }

    private BarPosition mBarPosition;
    private String mLabelText = "";

    private Paint mBarPaint;
    private Paint mLabelPaint;
    private Paint mOutlinePaint;

    private ColorStateList mBarColors;

    private Rect mBarBounds;
    private Rect mLabelBounds;

    private Point mLabelPoint = new Point(0, 0);

    private int mLabelHeight = 0;
    private float mBarAmount = 0f;
    private float mMaxAmount = 0f;

    private float mBarScale = 1f;

    private int mMinHeight = 0;

    private float mLabelPercent = 0.1f;

    private int[] mBarPadding = new int[4];
    private int[] mBarPaddingOriginal = new int[4];
    private int[] mBarPaddingChange = new int[4];

    private boolean mTransitioning = false;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public BarView(Context context) {
        super(context);

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint = new Paint(mBarPaint);
        mOutlinePaint = new Paint(mBarPaint);
        mOutlinePaint.setColor(Color.WHITE);

        mBarBounds = new Rect();
        mLabelBounds = new Rect();
    }

    void recycleBar() {

        mTransitioning = false;
        mBarPaddingOriginal = new int[4];
        mBarPaddingChange = new int[4];
        mBarPadding = new int[4];
        mBarPosition = null;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    public void setSelected(boolean selected, boolean animate) {
        setSelected(selected);

        if (animate && selected) bounceBar();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mLabelHeight = (int) (getHeight() * mLabelPercent);

        updateBarBounds();
        updateLabelBounds();

        invalidate();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        updateBarColor();
    }

    private void bounceBar() {

        ObjectAnimator bounce = new ObjectAnimator().ofFloat(this, "barScale", 1f, 1.2f, 1f);
        bounce.setDuration(250);
        bounce.start();
    }

    public void updateBarColor() {

        if (mBarColors == null) return;

        int color = mBarColors.getColorForState(getDrawableState(), Color.WHITE);

        if (color != mBarPaint.getColor()) {
            setBarColor(color);
        }
    }

    void setBarPosition(BarPosition mBarPosition) {
        this.mBarPosition = mBarPosition;

        mBarPaddingOriginal[0] = getBarPaddingLeft();
        mBarPaddingOriginal[2] = getBarPaddingRight();

        switch (mBarPosition) {
            case LEFT:
                mBarPadding[2] = getBarPaddingLeft() / 2;
                mBarPadding[0] = 0;
                break;
            case RIGHT:
                mBarPadding[0] = getBarPaddingRight() / 2;
                mBarPadding[2] = 0;
                break;
        }

        mBarPaddingChange[0] = mBarPadding[0] - mBarPaddingOriginal[0];
        mBarPaddingChange[2] = mBarPadding[2] - mBarPaddingOriginal[2];

        updateBarBounds();
        updateLabelBounds();

        invalidate();
    }

    void setTransitionPercent(float percent) {

        if (percent > 0f && percent < 0.95f) {
            mTransitioning = true;
            return;
        }

        mTransitioning = false;
        invalidate();
    }

    void restorePadding(float percent) {

        if (mBarPosition == null) return;

        percent = 1f - percent;

        mBarPadding[2] = mBarPaddingOriginal[2] + (int) (percent * (float) mBarPaddingChange[2]);
        mBarPadding[0] = mBarPaddingOriginal[0] + (int) (percent * (float) mBarPaddingChange[0]);

        updateBarBounds();
        updateLabelBounds();

        invalidate();
    }

    public void setBarColors(ColorStateList colors) {

        mBarColors = colors;
        updateBarColor();
    }

    public void setBarColor(int color) {
        mBarPaint.setColor(color);
        invalidate();
    }

    public int getBarColor() {
        return mBarPaint.getColor();
    }

    public void setBarAmount(float amount) {
        mBarAmount = amount;
        updateBarBounds();
        invalidate();
    }

    public float getBarAmount() {
        return mBarAmount;
    }

    public Rect getBarBounds() {
        return mBarBounds;
    }

    public int getBarTop() {
        return getTop() + mBarBounds.top;
    }

    public int getBarLeft() {
        return getLeft() + mBarBounds.left;
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

    public void setLabelAlpha(int labelAlpha) {
        mLabelPaint.setAlpha(labelAlpha);
        invalidate();
    }

    public void setLabelTextSize(float size) {
        mLabelPaint.setTextSize(size);
        updateLabelBounds();
    }

    public void setBarAlpha(int barAlpha) {
        mBarPaint.setAlpha(barAlpha);
        invalidate();
    }

    public void setMinBarHeight(int height) {
        mMinHeight = height;
    }

    public void setLabelPercent(float labelPercent) {
        mLabelPercent = labelPercent;
        requestLayout();
    }

    public void setBarScale(float mBarScale) {
        this.mBarScale = mBarScale;
        invalidate();
    }

    public int getBarPaddingLeft() {
        return mBarPadding[0];
    }

    public int getBarPaddingTop() {
        return mBarPadding[1];
    }

    public int getBarPaddingRight() {
        return mBarPadding[2];
    }

    public int getBarPaddingBottom() {
        return mBarPadding[3];
    }

    int getTrueBarPaddingRight() {

        if (mBarPaddingOriginal[2] > mBarPadding[2]) return mBarPaddingOriginal[2];

        return getBarPaddingRight();
    }

    public void setBarPaddingTop(int mBarPaddingTop) {
        this.mBarPadding[1] = mBarPaddingTop;

        updateBarBounds();

        invalidate();
    }

    public void setBarPadding(int left, int top, int right, int bottom) {

        mBarPadding[0] = left;
        mBarPadding[1] = top;
        mBarPadding[2] = right;
        mBarPadding[3] = bottom;

        updateBarBounds();

        invalidate();
    }

    private void updateLabelBounds() {

        mLabelPaint.getTextBounds(mLabelText, 0, mLabelText.length(), mLabelBounds);

        int y = getHeight() - (mLabelHeight - mLabelBounds.height()) / 2;
        int x = (getWidth() - getBarPaddingRight() + getBarPaddingLeft() - mLabelBounds.width()) / 2;

        mLabelPoint = new Point(x, y);

        invalidate();
    }

    private void updateBarBounds() {

        float percent = mBarAmount / mMaxAmount;
        float maxBarHeight = (getHeight() - getBarPaddingTop() - mLabelHeight);

        int barTop = (int) (maxBarHeight - (maxBarHeight * percent)) + getBarPaddingTop();

        // Keep the bar at least 2px high
        if (mMinHeight > 0 && (getHeight() - mLabelHeight - barTop) < mMinHeight) {
            barTop = (getHeight() - mLabelHeight - mMinHeight);
        }

        mBarBounds.left = getBarPaddingLeft();
        mBarBounds.top = barTop;
        mBarBounds.right = getWidth() - getBarPaddingRight();
        mBarBounds.bottom = getHeight() - mLabelHeight;
    }

    void setMaxAmount(float max) {
        mMaxAmount = max;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(mBarScale, mBarScale, getWidth() / 2, mBarBounds.bottom);

        // Provide a white border during drill down transition so bars pop more
        if (mTransitioning) {
            canvas.drawRect(0, mBarBounds.top, getWidth(), mBarBounds.bottom, mOutlinePaint);
        }

        canvas.drawRect(mBarBounds, mBarPaint);

        if (!mLabelText.equals("")) {
            canvas.drawText(mLabelText, mLabelPoint.x, mLabelPoint.y, mLabelPaint);
        }

        canvas.restore();
    }
}
