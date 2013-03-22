package com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class BarChartView extends AdapterView<BaseBarAdapter> {

    public final String TAG = this.getClass().getSimpleName();

    private static final int POPUP_WIDTH = 200;
    private static final int POPUP_HEIGHT = 100;
    private static final float SCALE_TRANSITION = 0.5f;
    private static final int SLOPE = 4;

    private BaseBarAdapter mAdapter;
    private AdapterDataSetObserver mDataSetObserver;

    private Paint mLabelPaint;
    private Paint mChildPaint;

    private int mPopupHeight, mPopupWidth;
    private int mBarPadding;
    private int mMaxBarHeight;
    private int mBarWidth;

    private float[] mPreviousAmounts;
    private int[] mPreviousColors;
    private float[] mTransitionOffsets;

    private ArgbEvaluator colorEvaluator;

    private int mDirection = 1;
    private float mOffset = 0f;

    private boolean mTransitioning = false;

    /** A list of cached (re-usable) item views */
    private final LinkedList<View> mCachedItemViews = new LinkedList<View>();

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initializeView();
    }

    private void initializeView() {

        setWillNotDraw(false);

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(Color.WHITE);

        mChildPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPopupWidth = (int) UiUtils.getDynamicPixels(getContext(), POPUP_WIDTH);
        mPopupHeight = (int) UiUtils.getDynamicPixels(getContext(), POPUP_HEIGHT);

        colorEvaluator = new ArgbEvaluator();

        invalidate();
    }

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this view's content.
     */
    @Override
    public BaseBarAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets the adapter that provides the data and the views to represent the data
     * in this widget.
     *
     * @param adapter The adapter to use to create this view's content.
     */
    @Override
    public void setAdapter(BaseBarAdapter adapter) {

        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        resetChart();

        mAdapter = adapter;

        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }

        requestLayout();
    }

    /**
     * @return The view corresponding to the currently selected item, or null
     *         if nothing is selected
     */
    @Override
    public View getSelectedView() {
        throw new RuntimeException("Not Supported");
    }

    /**
     * Sets the currently selected item. To support accessibility subclasses that
     * override this method must invoke the overriden super method first.
     *
     * @param position Index (starting at 0) of the data item to be selected.
     */
    @Override
    public void setSelection(int position) {
        throw new RuntimeException("Not Supported");
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, (int) (mMaxBarHeight * 0.9f + mPopupHeight), getWidth(), getHeight(), mLabelPaint);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {

        // get the bitmap
        final Bitmap bitmap = child.getDrawingCache();

        if (!mTransitioning || bitmap == null) {
            // if the is null for some reason, default to the standard
            // drawChild implementation
            return super.drawChild(canvas, child, drawingTime);
        }

        final Integer index = (Integer) child.getTag();
        float percent = mTransitionOffsets[index];

        // get top left coordinates
        final int left = child.getLeft();
        final int top = child.getTop();

        // get center x, y coordinates for proper scaling
        final int x = child.getRight() - child.getWidth() / 2;
        final int y = child.getBottom() - child.getHeight() / 2;

        // calculate scale and alpha based on index offset
        final float scale = (mDirection < 0 ? (1 - SCALE_TRANSITION) : 1f) + SCALE_TRANSITION * percent;
        final int alpha = (mDirection < 0 ? (0) : 255) - (int) (255 * percent * mDirection);

        canvas.save();

        canvas.scale(scale, scale, x, y);
        mChildPaint.setAlpha(alpha);

        canvas.drawBitmap(bitmap, left, top, mChildPaint);

        canvas.restore();

        return false;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // if we don't have an adapter, we don't need to do anything
        if (mAdapter == null) {
            return;
        }

        initBarSpecs();

        if (getChildCount() == 0) {
            addBars();
            positionItems();
            transitionBars(true);
        }

        invalidate();
    }

    /**
     * Add the bars to the BarChartView
     */
    private void addBars() {

        for (int i = 0; i < mAdapter.getCount(); i++) {

            final View bar = mAdapter.getView(i, getCachedView(), this);
            addAndMeasureChild(bar, i, mAdapter.getBarModel(i).getAmount());
        }
    }

    /**
     * Adds a view as a child view and takes care of measuring it
     *
     * @param child The view to add
     * @param index The index of the view
     * @param percent The percent of the bar height
     */
    private void addAndMeasureChild(final View child, final int index, final float percent) {

        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        child.setDrawingCacheEnabled(true);
        child.setTag(Integer.valueOf(index));
        addViewInLayout(child, index, params, true);

        child.measure(MeasureSpec.EXACTLY | mBarWidth, MeasureSpec.EXACTLY | mMaxBarHeight);
    }

    /**
     * Positions the children at the "correct" positions
     */
    private void positionItems() {

        int left = 0;

        for (int index = 0; index < getChildCount(); index++) {

            final View child = getChildAt(index);

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            child.layout(left, mPopupHeight, left + width, mPopupHeight + height);
            left += width + mBarPadding;
        }

    }

    void setBarsUpdate(float percent) {

        if (mAdapter.getCount() != getChildCount()) {
            throw new IllegalStateException("Bar Count Changed While Updating");
        }

        for (int i = 0; i < getChildCount(); i++) {

            BarViewTwo barView = (BarViewTwo) getChildAt(i);

            float previous = mPreviousAmounts[i];
            float change = (mAdapter.getBarModel(i).getAmount() - previous) * percent;

            Integer color = (Integer) colorEvaluator.evaluate(percent, mPreviousColors[i],
                    mAdapter.getBarModel(i).getColor());

            barView.setBarAmount(previous + change);
            barView.setBarColor(color.intValue());
        }
    }

    void setBarsTransition(float timePercent) {

        for (int i = 0; i < mTransitionOffsets.length; i++) {

            float percent = SLOPE * timePercent - (mOffset * i);

            if (percent < 0f) {
                percent = 0f;
            } else if (percent > 1f) {
                percent = 1f;
            }

            mTransitionOffsets[i] = percent;
        }

        invalidate();
    }

    private void savePreviousAmounts() {

        mPreviousAmounts = new float[getChildCount()];
        mPreviousColors = new int[getChildCount()];

        for (int i = 0; i < getChildCount(); i++) {
            BarViewTwo barView = (BarViewTwo) getChildAt(i);
            mPreviousAmounts[i] = barView.getBarAmount();
            mPreviousColors[i] = barView.getBarColor();
        }
    }

    private void updateBars() {

        ObjectAnimator transition = ObjectAnimator.ofFloat(this, "barsUpdate", 0f, 1f);
        transition.setDuration(1000);
        transition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                rebuildChildDrawingCache();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        transition.start();
    }

    private void transitionBars(final boolean in) {

        if (getChildCount() == 0) {
            requestLayout();
            return;
        }

        mTransitionOffsets = new float[getChildCount()];
        mOffset = (float) (SLOPE - 1) / (float) getChildCount();
        mDirection = in ? -1 : 1;

        ObjectAnimator transition = ObjectAnimator.ofFloat(this, "barsTransition", 0f, 1f);
        transition.setDuration(600);
        transition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {

                if (!in) {
                    resetChart();
                    requestLayout();
                } else {
                    mTransitioning = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        mTransitioning = true;
        transition.start();
    }

    /**
     * Rebuild the drawing cache of every child as they have
     * changed.
     */
    private void rebuildChildDrawingCache() {

        for (int i = 0; i < getChildCount(); i++) {
            BarViewTwo barView = (BarViewTwo) getChildAt(i);
            barView.buildDrawingCache();
        }
    }

    /**
     * Checks if there is a cached view that can be used
     *
     * @return A cached view or, if none was found, null
     */
    private View getCachedView() {

        if (mCachedItemViews.size() != 0) {
            return mCachedItemViews.removeFirst();
        }

        return null;
    }

    /**
     * Calculate the max height of the largest bar.  Calculate the width
     * of each chart as well as the spacing between bars.
     */
    private void initBarSpecs() {

        mMaxBarHeight = getHeight() - mPopupHeight;

        float barSpace = (float) getWidth() * 0.9f;
        mBarWidth = (int) (barSpace / (float) mAdapter.getCount() + 0.5f);
        mBarPadding = (int) ((getWidth() - mBarWidth * mAdapter.getCount()) / (float) (mAdapter.getCount() - 1) + 0.5f);

        invalidate();
    }

    private void resetChart() {

        for (int i = 0; i < getChildCount(); i++) {
            mCachedItemViews.add(getChildAt(i));
        }

        removeAllViewsInLayout();

        invalidate();
    }

    class AdapterDataSetObserver extends DataSetObserver {

        private Parcelable mInstanceState = null;

        @Override
        public void onChanged() {

            // Detect the case where a cursor that was previously invalidated
            // has been re-populated with new data.
            if (BarChartView.this.getAdapter().hasStableIds() && mInstanceState != null) {

                BarChartView.this.onRestoreInstanceState(mInstanceState);
                clearSavedState();
            }

            if (mAdapter.getCount() != getChildCount()) {
                mAdapter.notifyDataSetInvalidated();
                return;
            }

            savePreviousAmounts();
            updateBars();
        }

        @Override
        public void onInvalidated() {

            if (BarChartView.this.getAdapter().hasStableIds()) {

                // Remember the current state for the case where our hosting
                // activity is being stopped and later restarted
                mInstanceState = BarChartView.this.onSaveInstanceState();
            }

            transitionBars(false);
        }

        public void clearSavedState() {
            mInstanceState = null;
        }
    }
}
