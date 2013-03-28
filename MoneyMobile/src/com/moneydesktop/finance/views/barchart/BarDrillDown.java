package com.moneydesktop.finance.views.barchart;

import android.graphics.Point;
import android.view.View;
import com.moneydesktop.finance.tablet.adapter.TransactionChartAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/24/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class BarDrillDown {

    public final String TAG = this.getClass().getSimpleName();

    private static final long HIDE_DURATION = 500;
    private static final long FADE_DURATION = 200;
    private static final long START_DELAY = 300;
    private static final long TRANSITION_DURATION = 1000;

    private boolean mDrilling = false;
    private boolean mDown = false;

    private float mDownTranslation = 0f;

    private BarChartView mBarChart;

    private BarView mSelectedBar;
    private int mSelectedIndex;

    private int[] mMatrixSize = new int[2];
    private List<Point> mPoints = new ArrayList<Point>();

    private int mSubBarWidth;
    private int mSubBarHeight;

    private float mPositionPercent = 0f;

    private OnDrillingChangeListener mOnDrillingChangeListener;

    public BarDrillDown(BarChartView barChartView) {
        mBarChart = barChartView;
        mOnDrillingChangeListener = mBarChart;
        resetDrillDown();
    }

    public boolean isDrilling() {
        return mDrilling;
    }

    private void setIsDrilling(boolean isDrilling) {
        mDrilling = isDrilling;

        if (!mDrilling) {

            if (mOnDrillingChangeListener != null) {
                mOnDrillingChangeListener.onDrillingCompleted();
            }

            resetDrillDown();
        }
    }

    public BarView getSelectedBar() {
        return mSelectedBar;
    }

    /**
     * Set the index of the bar from the Bar Chart that is selected and
     * will be used to animate the drill down transition.
     *
     * @param index The index of the selected bar
     */
    public void setSelectedBar(int index) {

        if (isDrilling()) return;

        this.mSelectedBar = (BarView) mBarChart.getSelectedView();
        this.mSelectedBar.buildDrawingCache();
        this.mSelectedIndex = index;

        calculateTranslationAndScale();
    }

    /**
     * Provides the amount of columns and rows to be used to fill the
     * selected bar with.  The total values of bars (rows x columns)
     * should be equal to or greater than the number of bars needed at
     * the end of the drill down transition.
     *
     * @param matrixSize the size of the matrix
     */
    private void setMatrixSize(int[] matrixSize) {
        mMatrixSize = matrixSize;
        calculateTranslationAndScale();
    }

    /**
     * Returns the scale the current bar needs to be in order to fit
     * within the bounds of the selected bar.  This value changes
     * during the position animation.
     *
     * @param bar
     * @return
     */
    public float getScaleX(View bar) {

        if (mDown || mSubBarWidth == 0) return 1f;

        float scale = (float) mSubBarWidth / (float) bar.getWidth();
        float adjustment = 1f - (scale);
        scale += adjustment * mPositionPercent;

        return scale;
    }

    /**
     * Returns the scale the current bar needs to be in order to fit
     * within the bounds of the selected bar.  This value changes
     * during the position animation.
     *
     * @param bar
     * @return
     */
    public float getScaleY(View bar) {

        if (mDown || mSubBarHeight == 0) return 1f;

        float scale = (float) mSubBarHeight / (float) bar.getHeight();
        float adjustment = 1f - (scale);
        scale += adjustment * mPositionPercent;

        return scale;
    }

    public void setPositionPercent(float mPositionPercent) {
        this.mPositionPercent = mPositionPercent;
        mBarChart.invalidate();
    }

    public float getPositionPercent() {
        return mPositionPercent;
    }

    /**
     * Calculates the position for each sub chart to be displayed
     * within the bounds of the currently selected bar
     */
    private void calculateTranslationAndScale() {

        if (mMatrixSize == null || mSelectedBar == null) return;

        int width = mSelectedBar.getBarBounds().width();
        int height = mSelectedBar.getBarBounds().height();

        mSubBarWidth = width / mMatrixSize[1];
        mSubBarHeight = height / mMatrixSize[0];

        int extraWidth = width - (mSubBarWidth * mMatrixSize[1]);
        int extraHeight = height - (mSubBarHeight * mMatrixSize[0]);

        // Calculate the top left for each bar
        int top = mSelectedBar.getBarTop();

        for (int row = 0; row < mMatrixSize[0]; row++) {

            int left = mSelectedBar.getBarLeft();
            int additional = extraWidth;

            for (int column = 0; column < mMatrixSize[1]; column++) {

                Point point = new Point(left, top);
                mPoints.add(point);

                if (additional > 0) {
                    left++;
                    additional--;
                }

                left += mSubBarWidth;
            }

            if (extraHeight > 0) {
                top++;
                extraHeight--;
            }

            top += mSubBarHeight;
        }

        Collections.reverse(mPoints);
    }

    /**
     * Returns an array holding the x and y translation necessary
     * for the given view at the given index.
     *
     * @param view the child view
     * @param index the index of that view in the ViewGroup
     *
     * @return a float array containing the x, y translations
     */
    public float[] getTranslation(View view, int index) {

        float[] position = new float[2];

        if (mDown) {

            position[0] = 0f;
            position[1] = view.isSelected() ? 0f : mDownTranslation;

        } else if (mPoints.size() > index) {

            position[0] = mPoints.get(index).x - view.getLeft();
            position[1] = mPoints.get(index).y - view.getTop();

            position[0] -= position[0] * mPositionPercent;
            position[1] -= position[1] * mPositionPercent;
        }

        return position;
    }

    /**
     * Resets the drill down object
     */
    private void resetDrillDown() {
        mPositionPercent = 0f;
        mBarChart.setSelectedBarAlpha(255);
        mPoints.clear();
        mSelectedBar = null;
        mMatrixSize = null;
        mSubBarWidth = 0;
        mSubBarHeight = 0;
    }

    /**
     * Begins the drill down transition
     *
     * <b>The selected bar index and the matrix size must be set before calling
     * this method</b>
     *
     * @throws {@link RuntimeException} If the selected bar index or matrix is not set
     */
    public void start() {

        if (isDrilling()) return;

        if (mSelectedBar == null) {
            throw new RuntimeException("Set a selected bar");
        }

        setMatrixSize(((TransactionChartAdapter) mBarChart.getAdapter()).getDrillDownMatrix());

        if (mMatrixSize == null) return;

        setIsDrilling(true);
        transitionBarsOut();
    }

    /**
     * Sets the amount the bars should be translated down and
     * out of view.
     *
     * @param percent The percent completion of the animation (0 - 1)
     */
    void setBarDown(float percent) {

        mDownTranslation = mBarChart.getHeight() * percent;
        mBarChart.invalidate();
    }

    /**
     * PHASE 1: Remove non-selected bars
     */
    private void transitionBarsOut() {

        ObjectAnimator transition = ObjectAnimator.ofFloat(this, "barDown", 0f, 1f);
        transition.setDuration(HIDE_DURATION);
        transition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mDown = false;
                transitionNewBarsIn();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        mDown = true;
        transition.start();
    }

    /**
     * PHASE 2: Add bars for new data and any extra bars necessary
     *
     * 1. Remove all bars except selected bar
     * 2. Add and show the number of bars necessary for matrix
     * 3. Fade out selected bar
     * 4. Update used bars and fade out unused bars
     * 5. Clean up and remove unused bars
     */
    private void transitionNewBarsIn() {

        // Remove bars
        removeBars();

        // Load new data in adapter and update chart view
        updateAdapterData();

        // Add new bars accounting for current selected bar
        addNewBars();

        // Fade out the selected bar
        fadeOutSelected();
    }

    /**
     * Remove all bars except selected bar
     */
    private void removeBars() {

        final int count = mBarChart.getChildCount();

        List<View> views = new ArrayList<View>();

        for (int i = 0; i < count; i++) {

            if (i != mSelectedIndex) {
                views.add(mBarChart.getChildAt(i));
            }
        }

        for (View view : views) {
            mBarChart.removeViewInLayout(view);
        }
    }

    /**
     * Add new bars to the {@link BarChartView} and prep
     * for animating the changes to the bars.
     */
    private void addNewBars() {

        // Accounts for the extra bar in the view
        int total = mMatrixSize[0] * mMatrixSize[1] + 1;

        for (int i = 0; i < total; i++) {

            if (i == mSelectedIndex) {
                continue;
            }

            int index = i > mSelectedIndex ? i - 1 : i;
            BarView.BarPosition position = BarView.BarPosition.fromInteger(index % mMatrixSize[1]);

            final BarView bar = getView(mBarChart.getCachedView(), index);
            mBarChart.addAndMeasureChild(bar, i, index);
            bar.setBarPaddingTop(0);
            bar.setBarPosition(position);
        }

        mBarChart.positionItems(mSelectedIndex);
        mSelectedBar.bringToFront();
        mBarChart.invalidate();
    }

    /**
     * Update the BarChartView's adapter data so the new bars have
     * the right sizing and the data is prepped.
     */
    private void updateAdapterData() {

        TransactionChartAdapter adapter = (TransactionChartAdapter) mBarChart.getAdapter();
        adapter.drillDown();
        mBarChart.initBarSpecs();
    }

    /**
     * Configures the bars views based on the adapter data or
     * standard configuration for extra bars.
     *
     * @param convertView
     * @param index
     *
     * @return A configured {@link BarView}
     */
    private BarView getView(BarView convertView, int index) {

        BarView barView = convertView;

        if (barView == null) {
            barView = new BarView(mBarChart.getContext());
            barView.setDuplicateParentStateEnabled(false);
        }

        float max = mBarChart.getAdapter().getMaxAmount();

        if (index < mBarChart.getAdapter().getCount()) {

            mBarChart.getAdapter().getView(index, barView, mBarChart);

        } else {

            barView.setLabelText("");
            barView.setBarColor(mSelectedBar.getBarColor());
        }

        barView.setMaxAmount(max);
        barView.setBarAmount(max);
        barView.setSelected(true, false);
        barView.setLabelAlpha(0);

        return barView;
    }

    /**
     * Fades out the selected bar
     */
    private void fadeOutSelected() {

        ObjectAnimator fade = ObjectAnimator.ofInt(mBarChart, "selectedBarAlpha", 255, 0);
        fade.setDuration(FADE_DURATION);
        fade.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mBarChart.removeViewInLayout(mSelectedBar);
                mBarChart.savePreviousAmounts();
                positionBars();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        fade.start();
    }

    /**
     * Begins animating the bars to their proper position, size, and color
     */
    private void positionBars() {
        ObjectAnimator position = ObjectAnimator.ofFloat(this, "positionPercent", 0f, 1f);
        position.setDuration(TRANSITION_DURATION);
        position.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBarChart.removeViewInLayout(mSelectedBar);
                mBarChart.removeExtraViews();
                mBarChart.rebuildChildDrawingCache();
                setIsDrilling(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        position.setStartDelay(START_DELAY);
        position.start();
    }

    public interface OnDrillingChangeListener {
        public void onDrillingCompleted();
    }
}
