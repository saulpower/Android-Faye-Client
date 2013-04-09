package main.java.com.moneydesktop.finance.views.piechart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.CaretDrawable;
import main.java.com.moneydesktop.finance.views.FrictionDynamics;
import main.java.com.moneydesktop.finance.views.piechart.PieChartView.*;
import de.greenrobot.event.EventBus;

public class ExpandablePieChartView extends SurfaceView implements SurfaceHolder.Callback {

    public final String TAG = this.getClass().getSimpleName();

    private static final int INFO_STROKE_WIDTH = 3;

    private static final int SUB_STROKE_WIDTH = 1;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    /** User is not touching the piechart */
    public static final int TOUCH_STATE_RESTING = 0;

    /** Current touch state */
    private int mTouchState = TOUCH_STATE_RESTING;

    /** Distance to drag before we intercept touch events */
    private int mClickThreshold;

    /** X-coordinate of the down event */
    private int mTouchStartX;

    /** Y-coordinate of the down event */
    private int mTouchStartY;

    private DrawThread mDrawThread;
    private Handler mHandler;

    private Bitmap mDrawingCache;

    /** The diameter of the piechart */
    private int mChartDiameter;

    /** The center point of the piechart */
    private PointF mCenter = new PointF();

    private float mStrokeWidth;

    BitmapDrawable mDrawableCache;

    private PieChartView mGroupChart, mChildChart;

    private InfoDrawable mInfoDrawable;

    private CaretDrawable mCaret;

    private Paint mPaint;

    private Paint mStrokePaint;

    private int mInfoAlpha = 255;

    private float mInfoRadius = -1f;

    /** Animator objects used to animate the rotation, scale, and info panel */
    private ThreadAnimator mInfoAnimator;

    private OnPieChartReadyListener mOnPieChartReadyListener;

    /** Bridge Adapter used to manage the two adapters for the two pie charts */
    private PieChartBridgeAdapter mBridgeAdapter;

    private AdapterDataSetObserver mDataSetObserver;

    private OnExpandablePieChartChangeListener mExpandableChartChangeListener;

    private OnExpandablePieChartInfoClickListener mExpandablePieChartInfoClickListener;

    private OnPieChartChangeListener mGroupListener = new OnPieChartChangeListener() {

        @Override
        public void onSelectionChanged(int index) {

            mBridgeAdapter.setGroupPosition(index);
            configureInfo();

            // Propagate change listener to other listeners
            if (mExpandableChartChangeListener != null) {
                mExpandableChartChangeListener.onGroupChanged(index);
            }
        }
    };

    private OnPieChartChangeListener mChildListener = new OnPieChartChangeListener() {

        @Override
        public void onSelectionChanged(int index) {

            configureInfo(index);

            // Propagate change listener to other listeners
            if (mExpandableChartChangeListener != null) {
                mExpandableChartChangeListener.onChildChanged(mBridgeAdapter.getGroupPosition(), index);
            }
        }
    };

    private PieChartView.OnItemClickListener mOnGroupChartClicked = new PieChartView.OnItemClickListener() {

        @Override
        public void onItemClick(boolean secondTap, View parent, Drawable drawable, int position, long id) {

            if (secondTap) {
                toggleGroup();
                return;
            }
        }
    };

    private OnPieChartExpandListener mOnPieChartExpandListener = new OnPieChartExpandListener() {

        @Override
        public void onPieChartExpanded() {

            if (mExpandableChartChangeListener != null) {
                mExpandableChartChangeListener.onGroupExpanded(mBridgeAdapter.getGroupPosition(), mChildChart.getCurrentIndex());
            }
        }

        @Override
        public void onPieChartCollapsed() {

            if (mExpandableChartChangeListener != null) {
                mExpandableChartChangeListener.onGroupCollapsed(mBridgeAdapter.getGroupPosition());
            }
        }
    };

    private OnRotationStateChangeListener mGroupRotationListener = new OnRotationStateChangeListener() {

        @Override
        public void onRotationStateChange(int state) {

            switch (state) {

                case PieChartView.TOUCH_STATE_ROTATE:
                    mChildChart.hideChart();
                    updateCache();
                    break;
            }
        }
    };

    public DrawThread getDrawThread() {
        return mDrawThread;
    }

    public void setInfoAnimator(ThreadAnimator mInfoAnimator) {
        this.mInfoAnimator = mInfoAnimator;
        mInfoAnimator.start();
    }

    public void setExpandableChartChangeListener(
            OnExpandablePieChartChangeListener mExpandableChartChangeListener) {
        this.mExpandableChartChangeListener = mExpandableChartChangeListener;
    }

    public void setExpandablePieChartInfoClickListener(
            OnExpandablePieChartInfoClickListener mExpandablePieChartInfoClickListener) {
        this.mExpandablePieChartInfoClickListener = mExpandablePieChartInfoClickListener;
    }

    public void setOnPieChartReadyListener(
            OnPieChartReadyListener mOnPieChartReadyListener) {
        this.mOnPieChartReadyListener = mOnPieChartReadyListener;
    }

    public void toggleGroup() {

        int state = mChildChart.toggleChart();

        switch (state) {
            case PieChartView.CHART_SHOWING:

                configureInfo(mChildChart.getCurrentIndex());

                break;

            case PieChartView.CHART_HIDDEN:

                configureInfo();

                break;
        }
    }

    /**
     * Creates the info panel drawable
     */
    private void createInfo() {

        if (mInfoDrawable == null) {
            mInfoDrawable = new InfoDrawable(this, getContext(), getBounds(), mInfoRadius);
        }
    }

    /**
     * TODO: Rotate depending on our current snap-to position
     *
     * Creates the caret pointer drawable
     */
    private void createCaret() {

        if (mCaret == null) {
            PointF position = new PointF(mCenter.x - mInfoRadius / 2, mCenter.y + mInfoRadius / 3);
            mCaret = new CaretDrawable(getContext(), position, mInfoRadius, mInfoRadius);
            mCaret.setColor(Color.WHITE);
        }
    }

    public float getChartRadius() {
        return mChartDiameter / 2f;
    }

    /**
     * Gets the rectangular bounds of that the current Pie Chart
     * should be drawn within.
     *
     * @return The bounds of the Pie Chart
     */
    private Rect getBounds() {

        int left = (int) (mCenter.x - getChartRadius());
        int top = (int) (mCenter.y - getChartRadius());

        return new Rect(left, top, left + mChartDiameter, top + mChartDiameter);
    }

    private void configureInfo() {

        int groupPosition = mBridgeAdapter.getGroupPosition();
        PieSliceDrawable slice = mGroupChart.getSlice(groupPosition);

        if (mInfoDrawable == null || slice == null) return;

        mBridgeAdapter.getExpandableAdapter().configureGroupInfo(mInfoDrawable, slice, groupPosition);
        updateCache();
    }

    private void configureInfo(int childPosition) {

        if (!mChildChart.isLoaded()) return;

        int groupPosition = mBridgeAdapter.getGroupPosition();
        PieSliceDrawable slice = mChildChart.getSlice(childPosition);

        mBridgeAdapter.getExpandableAdapter().configureChildInfo(mInfoDrawable, slice, groupPosition, childPosition);
        updateCache();
    }

    public ExpandablePieChartView(Context context) {
        super(context, null);
    }

    public ExpandablePieChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 1);
    }

    public ExpandablePieChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {

        mHandler = new Handler();

        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        mDrawThread = new DrawThread(getHolder(), mHandler);

        mClickThreshold = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mStrokeWidth = UiUtils.getDynamicPixels(getContext(), SUB_STROKE_WIDTH);

        initPaints();
    }

    private void initPaints() {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);

        mStrokePaint = new Paint(mPaint);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(UiUtils.getDynamicPixels(getContext(), INFO_STROKE_WIDTH));
        mStrokePaint.setColor(Color.BLACK);
        mStrokePaint.setAlpha(50);
    }

    /**
     * Provides a check to see if the child pie piechart is expanded
     *
     * @return True if the child pie piechart is expanded
     */
    public boolean isExpanded() {

        if (mChildChart == null) return false;

        return !mChildChart.isChartHidden();
    }

    /**
     * Update the drawing cache with the latest changes
     * from the Pie Chart
     */
    private void updateCache() {

        // Wait for the info panel transition
        postDelayed(new Runnable() {

            @Override
            public void run() {
                createCache();
            }
        }, 400);
    }

    /**
     * Create the background drawable for when
     * the piechart is moved.
     */
    private void createCache() {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                if (mDrawingCache == null) {
                    mDrawingCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                }

                Canvas cache = new Canvas(mDrawingCache);
                draw(cache);

                mDrawableCache = new BitmapDrawable(getResources(), mDrawingCache);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                setCachedBackground(mDrawableCache);

                EventBus.getDefault().post(new EventMessage().new ChartImageEvent(mDrawingCache));

                if (mOnPieChartReadyListener != null) {
                    mOnPieChartReadyListener.onPieChartReady();
                }
            }

        }.execute();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setCachedBackground(Drawable drawable) {

        int sdk = android.os.Build.VERSION.SDK_INT;

        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable);
        } else {
            setBackground(drawable);
        }
    }

    @Override
     public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        boolean useHeight = height < width;

        mChartDiameter = (useHeight ? (height - (getPaddingTop() + getPaddingBottom()))
                : (width - (getPaddingLeft() + getPaddingRight()))) - (int) mStrokeWidth;

        int size = useHeight ? height : width;

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Get the center coordinates of the view
        mCenter.x = getWidth() / 2f;
        mCenter.y = getHeight() / 2f;

        if (mInfoRadius == -1f || changed) {
            mInfoRadius = getChartRadius() * 2 / 5;
            createInfo();
            createCaret();
        }

        // if we don't have an adapter, we don't need to do anything
        if (mBridgeAdapter == null)  return;

        if (mGroupChart == null || mChildChart == null) {
            addPieCharts();
        }
    }

    private void addPieCharts() {

        mGroupChart = new PieChartView(getContext());
        mGroupChart.setDynamics(new FrictionDynamics(0.95f));
        mGroupChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
        mGroupChart.setOnRotationStateChangeListener(mGroupRotationListener);
        mGroupChart.setOnPieChartChangeListener(mGroupListener);
        mGroupChart.setOnItemClickListener(mOnGroupChartClicked);

        mChildChart = new PieChartView(getContext());
        mChildChart.setDynamics(new FrictionDynamics(0.95f));
        mChildChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
        mChildChart.setOnPieChartExpandListener(mOnPieChartExpandListener);
        mChildChart.setOnPieChartChangeListener(mChildListener);

        initializeChartData();

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        addAndMeasureChart(mGroupChart, width, height);
        int subChartSize = (int) (mGroupChart.getChartDiameter() * 7 / 10);
        addAndMeasureChart(mChildChart, subChartSize, subChartSize);

        mChildChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        mGroupChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
    }

    /**
     *
     * @param chart
     * @param width
     * @param height
     */
    private void addAndMeasureChart(final PieChartView chart, int width, int height) {
        chart.measure(MeasureSpec.EXACTLY | width, MeasureSpec.EXACTLY | height);
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (mGroupChart == null || mChildChart == null || mInfoDrawable == null) return;

        if (mInfoAnimator != null && mInfoAnimator.isRunning()) {
            mInfoAlpha = mInfoAnimator.intUpdate();
        }

        // Draw the charts
        mGroupChart.onDraw(canvas);
        mChildChart.onDraw(canvas);

        // Draw the info circle
        canvas.drawCircle(mCenter.x, mCenter.y, mInfoRadius, mStrokePaint);
        mCaret.draw(canvas);
        canvas.drawCircle(mCenter.x, mCenter.y, mInfoRadius, mPaint);

        mInfoDrawable.setAlpha(mInfoAlpha);
        mInfoDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final int x = (int) event.getX();
        final int y = (int) event.getY();

        // Touch if in the info circle and we didn't start by touching
        // one of the other charts
        if (inInfoCircle(x, y) && mChildChart.getTouchState() == PieChartView.TOUCH_STATE_RESTING && mGroupChart
                .getTouchState() == PieChartView.TOUCH_STATE_RESTING) {
            onInfoTouch(event);
            return true;
        }

        // Touch if in the child pie chart and we didn't start by touching
        // the group pie chart
        if (!mChildChart.isChartHidden() && (mChildChart.inCircle(x, y) ||
                mChildChart.getTouchState() !=  PieChartView.TOUCH_STATE_RESTING) &&
                mGroupChart.getTouchState() == PieChartView.TOUCH_STATE_RESTING) {
            mChildChart.onTouchEvent(event);
            return true;
        }

        mGroupChart.onTouchEvent(event);

        return true;
    }

    private void onInfoTouch(final MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchState == TOUCH_STATE_CLICK) {
                    clickCheck(event);
                }

                break;

            case MotionEvent.ACTION_UP:

                if (mTouchState == TOUCH_STATE_CLICK) {
                    clickInfo();
                }

                break;
        }
    }
    /**
     * Sets and initializes all things that need to when we start a touch
     * gesture.
     *
     * @param event The down event
     */
    private void startTouch(final MotionEvent event) {

        // save the start place
        mTouchStartX = (int) event.getX();
        mTouchStartY = (int) event.getY();

        // we don't know if it's a click or a scroll yet, but until we know
        // assume it's a click
        mTouchState = TOUCH_STATE_CLICK;
    }

    private void clickCheck(final MotionEvent event) {

        final int xPos = (int) event.getX();
        final int yPos = (int) event.getY();

        if (isEnabled()
                && (xPos < mTouchStartX - mClickThreshold
                || xPos > mTouchStartX + mClickThreshold
                || yPos < mTouchStartY - mClickThreshold
                || yPos > mTouchStartY + mClickThreshold)) {

            mTouchState = TOUCH_STATE_RESTING;
        }
    }

    private void clickInfo() {

        int index = mGroupChart.getCurrentIndex();

        if (!mChildChart.isChartHidden()) {
            index = mChildChart.getCurrentIndex();
        }

        if (mExpandablePieChartInfoClickListener != null) {
            mExpandablePieChartInfoClickListener.onInfoClicked(mBridgeAdapter.getGroupPosition(), index);
        }
    }

    /**
     * Does the touch lie within the bounds of the info panel, if
     * the info panel is currently showing.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     *
     * @return True if the touch was inside the info panel
     */
    private boolean inInfoCircle(final int x, final int y) {

        double dx = (x - mCenter.x) * (x - mCenter.x);
        double dy = (y - mCenter.y) * (y - mCenter.y);

        if ((dx + dy) < (mInfoRadius * mInfoRadius)) {
            return true;
        } else {
            return false;
        }
    }

    private void resetChart() {

    }

    public BaseExpandablePieChartAdapter getAdapter() {
        return mBridgeAdapter.getExpandableAdapter();
    }

    public View getSelectedView() {
        throw new RuntimeException("Not Supported");
    }

    public void setAdapter(BaseExpandablePieChartAdapter adapter) {

        if (mBridgeAdapter != null && mBridgeAdapter.getExpandableAdapter() != null && mDataSetObserver != null) {
            mBridgeAdapter.getExpandableAdapter().unregisterDataSetObserver(mDataSetObserver);
        }

        resetChart();

        mBridgeAdapter = new PieChartBridgeAdapter(adapter);

        if (mBridgeAdapter.getExpandableAdapter() != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mBridgeAdapter.getExpandableAdapter().registerDataSetObserver(mDataSetObserver);
        }

        requestLayout();
    }

    private void initializeChartData() {

        if (mBridgeAdapter == null || mGroupChart == null || mChildChart == null) return;

        mGroupChart.setAdapter(mBridgeAdapter.getGroupAdapter());
        mChildChart.setAdapter(mBridgeAdapter.getChildAdapter());
    }

    public void setSelection(int position) {

        if (mGroupChart != null) {
            mGroupChart.setSelection(position);
        }
    }

    public void setGroupSelection(int groupPosition) {
        setSelection(groupPosition);
    }

    public int getSelectedGroup() {
        return mBridgeAdapter.getGroupPosition();
    }

    public int getSelectedChild() {

        if (!isExpanded()) return -1;

        return mChildChart.getCurrentIndex();
    }

    public void setChildSelection(int childPosition) {

        if (mChildChart != null) {
            mChildChart.setSelection(childPosition);
        }
    }

    public BaseExpandablePieChartAdapter getPieChartAdapter() {
        return mBridgeAdapter.getExpandableAdapter();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (mDrawThread.getState() == Thread.State.TERMINATED) {

            mDrawThread = new DrawThread(getHolder(), mHandler);
            mDrawThread.setRunning(true);
            mDrawThread.start();

        } else {

            mDrawThread.setRunning(true);
            mDrawThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;

        mDrawThread.onResume();
        mDrawThread.setRunning(false);

        while (retry) {
            try {
                mDrawThread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Pause the SurfaceView thread from rendering so not
     * to impact UI thread performance.
     */
    public void onPause() {

        mDrawThread.onPause();
    }

    /**
     * Resume the SurfaceView thread to render
     * the Pie Charts.
     */
    public void onResume() {

        mDrawThread.onResume();
    }

    public boolean isPaused() {
        return mDrawThread.isPaused();
    }

    /**
     * Thread used to draw the Pie Chart
     *
     * @author Saul Howard
     */
    protected class DrawThread extends Thread {

        /** The SurfaceHolder to draw to */
        private SurfaceHolder mSurfaceHolder;

        /** Tracks the running state of the thread */
        private boolean mIsRunning;

        /** Object used to acquire a pause lock on the thread */
        private Object mPauseLock = new Object();

        /** Tracks the pause state of the drawing thread */
        private boolean mPaused;

        private Handler mHandler;

        /**
         * Creates a new DrawThread that will manage drawing the PieChartView onto
         * the SurfaceView
         *
         * @param surfaceHolder the surfaceHolder to draw to
         */
        public DrawThread(SurfaceHolder surfaceHolder, Handler handler) {
            this.mSurfaceHolder = surfaceHolder;
            this.mHandler = handler;
            mIsRunning = false;
            mPaused = true;
        }

        public void setRunning(boolean run) {
            mIsRunning = run;
        }

        public boolean isRunning() {
            return mIsRunning;
        }

        public boolean isPaused() {
            return mPaused;
        }

        /**
         * Pause the drawing to the SurfaceView
         */
        public void onPause() {

            if (mPaused) return;

            synchronized (mPauseLock) {
                cleanUp();
                mPaused = true;
            }
        }

        /**
         * Resume drawing to the SurfaceView
         */
        public void onResume() {

            if (!mPaused) return;

            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }

        @Override
        public void run() {

            // Notify any listener the thread is ready and running
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (mOnPieChartReadyListener != null) {
                        mOnPieChartReadyListener.onPieChartReady();
                    }
                }
            });

            Canvas canvas;

            while (mIsRunning) {

                // Check for a pause lock
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted", e);
                        }
                    }
                }

                canvas = null;

                try {

                    canvas = mSurfaceHolder.lockCanvas(null);

                    synchronized (mSurfaceHolder) {

                        if (canvas != null && !mPaused) {

                            onDraw(canvas);
                        }
                    }

                } finally {

                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        /**
         * Clear the canvas upon termination of the thread
         */
        private void cleanUp() {

            Canvas canvas = null;

            try {

                canvas = mSurfaceHolder.lockCanvas(null);

                synchronized (mSurfaceHolder) {
                    if (canvas != null) {
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    }
                }

            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (canvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    class AdapterDataSetObserver extends DataSetObserver {

        private Parcelable mInstanceState = null;

        @Override
        public void onChanged() {

            initializeChartData();

            // Detect the case where a cursor that was previously invalidated
            // has been re-populated with new data.
            if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds() && mInstanceState != null) {

                ExpandablePieChartView.this.onRestoreInstanceState(mInstanceState);
                mInstanceState = null;
            }
        }

        @Override
        public void onInvalidated() {

            if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds()) {

                // Remember the current state for the case where our hosting
                // activity is being stopped and later restarted
                mInstanceState = ExpandablePieChartView.this.onSaveInstanceState();
            }

            resetChart();

            requestLayout();
        }
    }

    public interface OnExpandablePieChartChangeListener {

        /**
         * Notify that the group piechart has changed
         *
         * @param groupPosition The currently selected groupPosition
         */
        public void onGroupChanged(int groupPosition);

        /**
         * Notify that the child piechart has changed
         *
         * @param groupPosition The currently selected groupPosition
         * @param childPosition The currently selected childPosition
         */
        public void onChildChanged(int groupPosition, int childPosition);
        public void onGroupExpanded(int groupPosition, int childPosition);
        public void onGroupCollapsed(int groupPosition);
    }

    public interface OnExpandablePieChartInfoClickListener {

        /**
         * Notify the info panel has been clicked
         *
         * @param groupPosition The currently selected groupPosition
         * @param childPosition The currently selected childPosition.  Will return
         *             -1 if child piechart is not currently showing.
         */
        public void onInfoClicked(int groupPosition, int childPosition);
    }
}
