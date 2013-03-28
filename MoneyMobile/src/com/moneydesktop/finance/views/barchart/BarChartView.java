package com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
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
public class BarChartView extends AdapterView<BaseBarAdapter> implements BarDrillDown.OnDrillingChangeListener {

    public final String TAG = this.getClass().getSimpleName();

    private static final int POPUP_WIDTH = 225;
    private static final int POPUP_HEIGHT = 125;
    private static final float SCALE_TRANSITION = 0.5f;
    private static final float SLOPE = 2f;
    private static final int TRANSITION_DURATION = 750;
    private static final int UPDATE_DURATION = 1000;

    /** Represents an invalid child index */
    private static final int INVALID_INDEX = -1;

    /** User is not touching the list */
    private static final int TOUCH_STATE_RESTING = 0;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    private static final int TOUCH_STATE_SLIDING = 2;

    /** Current touch state */
    private int mTouchState = TOUCH_STATE_RESTING;

    /** X-coordinate of the down event */
    private int mTouchStartX;

    /** Y-coordinate of the down event */
    private int mTouchStartY;

    private float mTouchThreshold;

    /** Reusable rect */
    private Rect mRect;

    private BarChartPopup mPopup;

    private int mSelectedIndex = 0;

    private BaseBarAdapter mAdapter;
    private AdapterDataSetObserver mDataSetObserver;

    private Paint mLabelPaint;
    private Paint mChildPaint;

    private float mLabelPercent = 0.1f;
    private int mPopupHeight, mPopupWidth;
    private int mBarPadding;
    private int mBarWidth;
    private int mExtraSpace;

    private float mPreviousMax;
    private float[] mPreviousAmounts;
    private int[] mPreviousColors;
    private float[] mTransitionOffsets;

    private ArgbEvaluator colorEvaluator;

    private int mDirection = 1;
    private float mOffset = 0f;

    private boolean mTransitioning = false;
    private boolean mUpdating = false;

    private DecelerateInterpolator mInterpolator;

    private OnDataShowingListener mOnDataShowingListener;

    private OnPopupClickListener mOnPopupClickListener;

    /** Used to check for long press actions */
    private Runnable mLongPressRunnable;

    private Runnable mHightlightRunnable;

    private long mLongStart = 0l;

    private int mStartColor = Color.WHITE;

    private BarDrillDown mDrillDown;

    private int mSelectedBarAlpha = 255;

    private boolean mShowPopup = true;

    /** A list of cached (re-usable) item views */
    private final LinkedList<BarView> mCachedItemViews = new LinkedList<BarView>();

    public void setOnDataShowingListener(OnDataShowingListener mOnDataShowingListener) {
        this.mOnDataShowingListener = mOnDataShowingListener;
    }

    public void setOnPopupClickListener(OnPopupClickListener mOnPopupClickListener) {
        this.mOnPopupClickListener = mOnPopupClickListener;
    }

    public void setShowPopup(boolean mShowPopup) {
        this.mShowPopup = mShowPopup;

        if (mShowPopup) {
            mPopupHeight = (int) UiUtils.getDynamicPixels(getContext(), POPUP_HEIGHT);
        } else {
            mPopupHeight = 0;
        }
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDrillDown = new BarDrillDown(this);

        mTouchThreshold = ViewConfiguration.get(context).getScaledTouchSlop();

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
        mInterpolator = new DecelerateInterpolator();

        invalidate();
    }

    public void showLabel() {
        setLabelPercent(0.1f);
    }

    public void hideLabel() {
        setLabelPercent(0f);
    }

    public void setLabelPercent(float labelPercent) {
        mLabelPercent = labelPercent;
        requestLayout();
    }

    public boolean isAnimating() {
        return mTransitioning || mUpdating || mDrillDown.isDrilling();
    }

    public boolean isTransitioning() {
        return mTransitioning;
    }

    public boolean isUpdating() {
        return mUpdating;
    }

    public int getBarWidth() {
        return mBarWidth;
    }

    public void setSelectedBarAlpha(int mSelectedBarAlpha) {
        this.mSelectedBarAlpha = mSelectedBarAlpha;
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
            mAdapter.setBarChart(null);
        }

        resetChart();

        mAdapter = adapter;
        mPreviousMax = mAdapter.getMaxAmount();
        mAdapter.setBarChart(this);

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
        return getChildAt(mSelectedIndex);
    }

    /**
     * Sets the currently selected item. To support accessibility subclasses that
     * override this method must invoke the overriden super method first.
     *
     * @param position Index (starting at 0) of the data item to be selected.
     */
    @Override
    public void setSelection(int position) {

        if (mSelectedIndex == position || mDrillDown.isDrilling()) return;

        endLongPress();

        // un-select previous selection
        View previous = getChildAt(mSelectedIndex);
        if (previous != null) {
            previous.setSelected(false);
        }

        // update new selection
        mSelectedIndex = position;
        ((BarView) getChildAt(mSelectedIndex)).setSelected(true, true);

        showPopup();
    }

    public int getSelection() {
        return mSelectedIndex;
    }

    private void showPopup() {

        if (!mShowPopup) return;

        BarView bar = ((BarView) getChildAt(mSelectedIndex));
        BarViewModel model = mAdapter.getBarModel(mSelectedIndex);
        mPopup.changePopup(bar, model);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        super.invalidateDrawable(who);

        invalidate();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Draw popup
        if (!mShowPopup) return;

        mPopup.draw(canvas);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, (int) (getHeight() * (1f - mLabelPercent)), getWidth(), getHeight(), mLabelPaint);
    }

    @Override
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {

        Bitmap bitmap = null;

        try {
            // Get the bitmap for the child view
            bitmap = child.getDrawingCache();
        } catch (Exception ex) {}

        if (!mTransitioning || bitmap == null) {

            canvas.save();

            boolean invalidated = false;
            boolean drawSuper = true;

            // Process any changes necessary while a drill down animation is in progress
            if (mDrillDown.isDrilling()) {
                drawSuper = processDrillDown(canvas, child, bitmap);
            }

            if (drawSuper) {
                invalidated = super.drawChild(canvas, child, drawingTime);
            }

            canvas.restore();

            return invalidated;
        }

        if (mTransitioning) {
            drawChildTransition(canvas, child, bitmap);
            return false;
        }

        return false;
    }

    /**
     * Drilling down has several phases, please see the {@link BarDrillDown} class for
     * more details. Manages adjusting translation, scale, and updating bars as they
     * change throughout the drilldown transition.
     *
     * @param canvas
     * @param child
     * @param bitmap
     *
     * @return whether the child needs to be drawn via the super method
     */
    private boolean processDrillDown(Canvas canvas, View child, Bitmap bitmap) {

        final Integer index = indexOfChild(child);

        if (child != mDrillDown.getSelectedBar()) {

            // Scale and translate the canvas for the specific child
            float[] translation = mDrillDown.getTranslation(child, index);
            canvas.translate(translation[0], translation[1]);
            canvas.scale(mDrillDown.getScaleX(child), mDrillDown.getScaleY(child), child.getLeft(), child.getTop());

            float percent = mDrillDown.getPositionPercent();

            // If the position percent is greater than zero we are
            // animating the bars moving so we want to update their
            // size, color, and opacity
            if (percent > 0) {

                BarView bar = (BarView) child;

                if (bar.isSelected()) bar.setSelected(false);

                bar.setTransitionPercent(percent);
                bar.setBarPaddingTop((int) (percent * mPopupHeight));
                bar.restorePadding(percent);

                if (index < mAdapter.getCount()) {
                    bar.setLabelAlpha((int) (percent * 255));
                    updateBar(bar, index, percent);
                } else {
                    bar.setBarAlpha((int) (255 - percent * 255));
                }
            }

            return true;

        } else {

            // Fade out the original selected bar
            mChildPaint.setAlpha(mSelectedBarAlpha);
            canvas.drawBitmap(bitmap, child.getLeft(), child.getTop(), mChildPaint);

            return false;
        }
    }

    /**
     * During a drill down transition we may have extra views in the layout
     * that are no longer necessary.  We will remove them so the view group
     * represents the actual number of views showing.
     */
    void removeExtraViews() {

        for (int i = getChildCount() - 1; i >= 0; i--) {

            if (i >= mAdapter.getCount()) {
                View view = getChildAt(i);
                removeViewInLayout(view);
            } else {
                break;
            }
        }
    }

    /**
     * Animate the children being removed and added when the adapter is
     * set or invalidated and a new data set needs to be displayed.
     *
     * @param canvas
     * @param child
     * @param bitmap
     */
    private void drawChildTransition(Canvas canvas, View child, Bitmap bitmap) {

        final Integer index = indexOfChild(child);
        float percent = mTransitionOffsets[index];

        // get top left coordinates
        final int left = child.getLeft();
        final int top = child.getTop();

        // get center x, y coordinates for proper scaling
        final int x = child.getRight() - child.getWidth() / 2;
        final int y = ((BarView) child).getBarBounds().bottom;

        // calculate scale and alpha based on index offset
        final float scale = (mDirection < 0 ? (1 - SCALE_TRANSITION) : 1f) + SCALE_TRANSITION * percent;
        final int alpha = (mDirection < 0 ? (0) : 255) - (int) (255 * percent * mDirection);

        canvas.save();

        canvas.scale(scale, scale, x, y);
        mChildPaint.setAlpha(alpha);

        canvas.drawBitmap(bitmap, left, top, mChildPaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        if (!isEnabled() || getChildCount() == 0 || mDrillDown.isDrilling()) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                break;

            case MotionEvent.ACTION_MOVE:
                checkTouchSliding(event);
                clickChildAt((int) event.getX(), (int) event.getY());
                break;

            case MotionEvent.ACTION_UP:
                endTouch(event);
                break;

            default:
                endTouch(event);
                break;
        }
        return true;
    }

    /**
     * Sets and initializes all things that need to when we start a touch
     * gesture.
     */
    private void startTouch(final MotionEvent event) {

        // save the start place
        mTouchStartX = (int)event.getX();
        mTouchStartY = (int)event.getY();

        final int index = clickChildAt((int) event.getX(), (int) event.getY());

        if (index != INVALID_INDEX) {
            // start checking for a long press
            startLongPressCheck();
        }

        // assume it's a click
        mTouchState = TOUCH_STATE_CLICK;
    }

    /**
     * Checks if the user has moved far enough for this to be a scroll and if
     * so, sets the list in scroll mode
     *
     * @param event The (move) event
     * @return true if scroll was started, false otherwise
     */
    private void checkTouchSliding(final MotionEvent event) {

        final int xPos = (int) event.getX();
        final int yPos = (int) event.getY();

        if (xPos < mTouchStartX - mTouchThreshold
                || xPos > mTouchStartX + mTouchThreshold
                || yPos < mTouchStartY - mTouchThreshold
                || yPos > mTouchStartY + mTouchThreshold) {

            mTouchState = TOUCH_STATE_SLIDING;
        }
    }

    /**
     * Resets and recycles all things that need to when we end a touch gesture
     */
    private void endTouch(final MotionEvent event) {

        clickChildAt((int) event.getX(), (int) event.getY(), (mTouchState == TOUCH_STATE_CLICK));

        endLongPress();

        // reset touch state
        mTouchState = TOUCH_STATE_RESTING;
    }

    /**
     * Posts (and creates if necessary) a runnable that will when executed call
     * the long click listener
     */
    private void startLongPressCheck() {

        if (!mAdapter.isLongClickable(mSelectedIndex)) return;

        // create the runnable if we haven't already
        if (mLongPressRunnable == null) {

            mLongPressRunnable = new Runnable() {

                @Override
                public void run() {

                    if (mTouchState != TOUCH_STATE_RESTING) {
                        longClickChild();
                    }
                }
            };
        }

        if (mHightlightRunnable == null) {

            mHightlightRunnable = new Runnable() {

                @Override
                public void run() {

                    float percent = (float) (AnimationUtils.currentAnimationTimeMillis() - mLongStart) / (float) ViewConfiguration.getLongPressTimeout() * 0.75f;

                    Integer color = (Integer) colorEvaluator.evaluate(percent, mStartColor, Color.WHITE);

                    BarView bar = (BarView) getChildAt(mSelectedIndex);
                    bar.setBarColor(color);

                    if (percent < 1f) {
                        postDelayed(this, 16);
                    }
                }
            };
        }

        mLongStart = AnimationUtils.currentAnimationTimeMillis();
        mStartColor = ((BarView) getChildAt(mSelectedIndex)).getBarColor();

        // then post it with a delay
        postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
        postDelayed(mHightlightRunnable, 16);
    }

    private void endLongPress() {

        final BarView bar = (BarView) getChildAt(mSelectedIndex);

        if (mLongPressRunnable != null && bar != null) {

            bar.updateBarColor();

            // remove any existing check for longpress
            removeCallbacks(mLongPressRunnable);
            removeCallbacks(mHightlightRunnable);
        }
    }

    /**
     * Calls the item long click listener for the child with the specified index
     */
    private void longClickChild() {

        removeCallbacks(mHightlightRunnable);

        final BarView itemView = (BarView) getChildAt(mSelectedIndex);
        final long id = mAdapter.getItemId(mSelectedIndex);

        itemView.setBarColor(mStartColor);

        final OnItemLongClickListener listener = getOnItemLongClickListener();

        beginDrillDown();

        if (listener != null) {
            listener.onItemLongClick(this, itemView, mSelectedIndex, id);
        }
    }

    private int clickChildAt(final int x, final int y) {
        return clickChildAt(x, y, false);
    }

    /**
     * Calls the item click listener for the child with at the specified
     * coordinates
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private int clickChildAt(final int x, final int y, boolean clicked) {

        if (mShowPopup && mPopup.getBounds().contains(x, y)) {
            if (clicked) clickPopup();
            return INVALID_INDEX;
        }

        final int index = getContainingChildIndex(x, y);

        if (index != INVALID_INDEX && index != mSelectedIndex) {

            setSelection(index);

            final View itemView = getChildAt(index);
            final int position = index;
            final long id = mAdapter.getItemId(position);

            performItemClick(itemView, position, id);
        }

        return index;
    }

    private void clickPopup() {

        if (mOnPopupClickListener != null) {
            mOnPopupClickListener.onPopupClicked();
        }
    }

    /**
     * Begins the drill down transition
     */
    private void beginDrillDown() {

        BarViewModel model = mAdapter.getBarModel(mSelectedIndex);

        if (model.getAmount() == 0) return;

        if (mShowPopup) {
            mPopup.hide();
        }
        mDrillDown.setSelectedBar(mSelectedIndex);
        mDrillDown.start();
    }

    /**
     * Returns the index of the child that contains the coordinates given.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The index of the child that contains the coordinates. If no child
     *         is found then it returns INVALID_INDEX
     */
    private int getContainingChildIndex(final int x, final int y) {

        if (mRect == null) {
            mRect = new Rect();
        }

        for (int index = 0; index < getChildCount(); index++) {

            final View itemView = getChildAt(index);
            itemView.getHitRect(mRect);

            if (mRect.contains(x, y)) {
                return index;
            }
        }

        return INVALID_INDEX;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && getChildCount() > 0) {
            resetChart();
        }


        if (mShowPopup && mPopup == null) {
            mPopup = new BarChartPopup(getContext(), this, mPopupWidth, mPopupHeight, getWidth());
        }

        if (changed) {
            mPopup.setMaxWidth(getWidth());
        }

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

            final BarView bar = (BarView) mAdapter.getView(i, getCachedView(), this);
            bar.setSelected(false);
            bar.setLabelPercent(mLabelPercent);
            addAndMeasureChild(bar, i);
        }
    }
    void addAndMeasureChild(final BarView child, final int index) {
        addAndMeasureChild(child, index, index);
    }

    /**
     * Adds a view as a child view and takes care of measuring it
     *
     * @param child The view to add
     * @param index The index of the view
     */
    void addAndMeasureChild(final BarView child, final int index, final int position) {

        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        int paddingLeft = position == 0 ? 0 : mBarPadding;
        int paddingRight = position == mAdapter.getCount() - 1 ? 0 : mBarPadding;

        int barWidth = mBarWidth;

        if (mExtraSpace > 0) {
            barWidth++;
            mExtraSpace--;
        }

        child.setDrawingCacheEnabled(true);
        child.setBarPadding(paddingLeft, mPopupHeight, paddingRight, 0);
        addViewInLayout(child, index, params, true);

        child.measure(MeasureSpec.EXACTLY | (barWidth + paddingLeft + paddingRight), MeasureSpec.EXACTLY | getHeight());
    }


    void positionItems() {
        positionItems(-1);
    }

    /**
     * Positions the children at the "correct" positions
     */
    void positionItems(int excludeIndex) {

        int left = 0;

        for (int index = 0; index < getChildCount(); index++) {

            if (index == excludeIndex) continue;

            final BarView child = (BarView) getChildAt(index);

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            child.layout(left, 0, left + width, height);
            left += (width - child.getTrueBarPaddingRight());
        }

    }

    void setBarsUpdate(float percent) {

        if (mAdapter.getCount() != getChildCount()) {
            throw new IllegalStateException("Bar Count Changed While Updating");
        }

        if (mShowPopup && mPopup != null) {
            mPopup.updateAmount(percent);
            mPopup.updatePosition();
        }

        final float currentMax = mAdapter.getMaxAmount();

        for (int i = 0; i < getChildCount(); i++) {

            BarView barView = (BarView) getChildAt(i);
            barView.setMaxAmount(mPreviousMax + (currentMax - mPreviousMax) * percent);
            updateBar(barView, i, percent);
        }
    }

    /**
     * Adjust the bar view according to the percent completion of the
     * animation.  Updates the bars size and color.
     *
     * @param barView The {@link BarView} bar to adjust
     * @param index The index of the bar
     * @param percent The percent completion of the transition (0 - 1)
     */
    private void updateBar(BarView barView, int index, float percent) {

        BarViewModel model = mAdapter.getBarModel(index);

        float previous = mPreviousAmounts[index];
        float change = (model.getAmount() - previous) * percent;

        int currentColor = model.getColors().getColorForState(barView.getDrawableState(), model.getColor());
        Integer color = (Integer) colorEvaluator.evaluate(percent, mPreviousColors[index], currentColor);

        barView.setBarAmount(previous + change);
        barView.setBarColor(color.intValue());
    }

    void setBarsTransition(float timePercent) {

        for (int i = 0; i < mTransitionOffsets.length; i++) {

            float percent = SLOPE * timePercent - (mOffset * i);

            if (percent < 0f) {
                percent = 0f;
            } else if (percent > 1f) {
                percent = 1f;
            }

            percent = mInterpolator.getInterpolation(percent);

            mTransitionOffsets[i] = percent;
        }

        invalidate();
    }

    /**
     * Save the previous color and amount of each bar so
     * we can animate the change to a new color and amount.
     */
    void savePreviousAmounts() {

        mPreviousAmounts = new float[getChildCount()];
        mPreviousColors = new int[getChildCount()];

        for (int i = 0; i < getChildCount(); i++) {
            BarView barView = (BarView) getChildAt(i);
            mPreviousAmounts[i] = barView.getBarAmount();
            mPreviousColors[i] = barView.getBarColor();
        }
    }

    /**
     * Animate the bars to reflect the updated data from the adapter
     */
    private void updateBars() {

        if (mShowPopup && mPopup != null) {
            // Update the popup bar model
            mPopup.setBarModel(mAdapter.getBarModel(mSelectedIndex));
        }

        ObjectAnimator update = ObjectAnimator.ofFloat(this, "barsUpdate", 0f, 1f);
        update.setDuration(UPDATE_DURATION);
        update.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                rebuildChildDrawingCache();
                mPreviousMax = mAdapter.getMaxAmount();
                mUpdating = false;

                if (mOnDataShowingListener != null) {
                    mOnDataShowingListener.onDataShowing(false);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mUpdating = true;
        update.start();
    }

    /**
     * Animate the bars out and in when the data set has completely changed.
     *
     * @param in
     */
    private void transitionBars(final boolean in) {

        if (getChildCount() == 0) {
            requestLayout();
            return;
        }

        if (mShowPopup && mPopup != null) {
            mPopup.hide();
        }

        rebuildChildDrawingCache();

        mTransitionOffsets = new float[getChildCount()];
        mOffset = (SLOPE - 1f) / (float) getChildCount();
        mDirection = in ? -1 : 1;

        ObjectAnimator transition = ObjectAnimator.ofFloat(this, "barsTransition", 0f, 1f);
        transition.setDuration(TRANSITION_DURATION);
        transition.setInterpolator(new LinearInterpolator());
        transition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {

                if (!in) {

                    if (mOnDataShowingListener != null) {
                        mOnDataShowingListener.onDataShowing(false);
                    }

                    resetChart();
                    requestLayout();

                } else {

                    refreshSelection();
                    mPreviousMax = mAdapter.getMaxAmount();
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

    private void refreshSelection() {

        // Select the first bar with a non-zero amount
        int position = mAdapter.getCount() - 1;

        while (mAdapter.getBarModel(position).getAmount() == 0 && position >= 0) {
            position--;
        }

        setSelection(position);

        BarView bar = (BarView) getChildAt(mSelectedIndex);
        bar.setSelected(true, true);

        if (!mShowPopup || mPopup == null) return;

        mPopup.changePopup(bar, mAdapter.getBarModel(mSelectedIndex));
    }

    /**
     * Rebuild the drawing cache of every child as they have
     * changed.
     */
    public void rebuildChildDrawingCache() {

        for (int i = 0; i < getChildCount(); i++) {
            BarView barView = (BarView) getChildAt(i);
            barView.destroyDrawingCache();
            barView.buildDrawingCache();
        }
    }

    /**
     * Checks if there is a cached view that can be used
     *
     * @return A cached view or, if none was found, null
     */
    BarView getCachedView() {

        if (mCachedItemViews.size() != 0) {
            BarView bar = mCachedItemViews.removeFirst();
            bar.recycleBar();
            return bar;
        }

        return null;
    }

    /**
     * Calculate the max height of the largest bar.  Calculate the width
     * of each chart as well as the spacing between bars.
     */
    void initBarSpecs() {

        float barSpace = (float) getWidth() * 0.9f;
        mBarWidth = (int) (barSpace / (float) mAdapter.getCount() + 0.5f);
        mBarPadding = (int) ((getWidth() - mBarWidth * mAdapter.getCount()) / (float) (mAdapter.getCount() - 1) - 0.5f);

        mExtraSpace = getWidth() - (mBarWidth * mAdapter.getCount() + mBarPadding * (mAdapter.getCount() - 1));

        invalidate();
    }

    /**
     * Reset the chart by removing all bars from the layout and recycling them
     */
    private void resetChart() {

        for (int i = 0; i < getChildCount(); i++) {
            mCachedItemViews.add((BarView) getChildAt(i));
        }

        removeAllViewsInLayout();

        invalidate();
    }

    @Override
    public void onDrillingCompleted() {
        refreshSelection();

        if (mOnDataShowingListener != null) {
            mOnDataShowingListener.onDataShowing(true);
        }
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

    public interface OnDataShowingListener {
        public void onDataShowing(boolean fromDrillDown);
    }

    public interface OnPopupClickListener {
        public void onPopupClicked();
    }
}
