package main.java.com.moneydesktop.finance.views.navigation;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.Dynamics;
import main.java.com.moneydesktop.finance.views.FrictionDynamics;
import main.java.com.moneydesktop.finance.views.piechart.ThreadAnimator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class NavWheelView extends SurfaceView implements SurfaceHolder.Callback {

    public final String TAG = this.getClass().getSimpleName();

    private final float RADIUS = 200;
    private final int ALPHA = 200;

    /** Unit used for the velocity tracker */
    private static final int PIXELS_PER_SECOND = 1000;

    /** Tolerance for the velocity */
    private static final float VELOCITY_TOLERANCE = 40f;

    /** Represents an invalid child index */
    private static final int INVALID_INDEX = -1;

    /** User is not touching the piechart */
    public static final int TOUCH_STATE_RESTING = 0;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    /** User is rotating the piechart */
    public static final int TOUCH_STATE_ROTATE = 2;

    /** Current touch state */
    private int mTouchState = TOUCH_STATE_RESTING;

    /** Distance to drag before we intercept touch events */
    private int mClickThreshold;

    /** X-coordinate of the down event */
    private int mTouchStartX;

    /** Y-coordinate of the down event */
    private int mTouchStartY;

    /** Velocity tracker used to get fling velocities */
    private VelocityTracker mVelocityTracker;

    /** Dynamics object used to handle fling and snap */
    private Dynamics mDynamics;

    /** Runnable used to animate fling and snap */
    private Runnable mDynamicsRunnable;

    private DrawThread mDrawThread;

    /** The rotating direction of the piechart */
    private boolean mRotatingClockwise;

    /** The current degrees of rotation of the piechart */
    private float mRotationDegree = 0;

    /** Our starting rotation degree */
    private float mRotationStart = 0;

    /** The last rotation degree after touch */
    private float mLastRotation = 0;

    /** The pixel density of the current device */
    private float mPixelDensity;

    /** Animator objects used to animate the rotation, scale, and info panel */
    private ThreadAnimator mRotateAnimator;

    private PointF mCenter;

    private List<NavItemDrawable> mDrawables;

    private HomeButton mHomeButton;

    private List<Integer> mItems;

    private Paint mBackgroundPaint;

    private PointerDrawable mPointer;

    private boolean mRotating = false;

    private boolean mShowing = false;

    private boolean mClicked = false;

    private double mDegreeChange = 0.0;

    private int mCurrentIndex = 0;

    // Trigger timer that the exit animations have completed
    private Handler mHandler;

    private onNavigationChangeListener mOnNavigationChangeListener;

    public int getBackgroundAlpha() {
        return mBackgroundPaint.getAlpha();
    }

    public void setBackgroundAlpha(int mAlpha) {
        mBackgroundPaint.setAlpha(mAlpha);
        invalidate();
    }

    public void setItems(List<Integer> items) {
        this.mItems = items;
        initializeItems();
    }

    public boolean isShowing() {
        return mShowing;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    private void setTouchState(int touchState) {
        mTouchState = touchState;
    }

    public int getTouchState() {
        return mTouchState;
    }

    /**
     * Set the dynamics object used for fling and snap behavior.
     *
     * @param dynamics The dynamics object
     */
    public void setDynamics(final Dynamics dynamics) {

        if (mDynamics != null) {
            dynamics.setState(getRotationDegree(), mDynamics.getVelocity(), AnimationUtils
                    .currentAnimationTimeMillis());
        }

        mDynamics = dynamics;
    }

    /**
     * Set the rotation degree of the piechart.
     *
     * @param rotationDegree the degree to rotate the piechart to
     */
    void setRotationDegree(float rotationDegree) {

        // Keep rotation degree positive
        if (rotationDegree < 0) rotationDegree += 360;

        // Keep rotation degree between 0 - 360
        mRotationDegree = rotationDegree % 360;

        mPointer.setRotation(mRotationDegree);
    }

    public float getRotationDegree() {
        return mRotationDegree;
    }

    public void updateStartRotation() {
        mLastRotation = getRotationDegree();
    }

    /**
     * Checks which way the piechart is rotating.
     *
     * @param previous The previous degree the piechart was rotated to
     */
    private void setRotatingClockwise(float previous) {

        final float change = (mRotationDegree - previous);
        mRotatingClockwise = (change > 0 && Math.abs(change) < 300) || (Math.abs(change) > 300 && mRotatingClockwise);
    }

    public boolean getRotatingClockwise() {
        return mRotatingClockwise;
    }

    /**
     * Sets the currently selected index item and rotates
     * the cursor to point at that item.
     *
     * @param currentIndex
     */
    public void setCurrentIndex(int currentIndex) {

        if (mRotating) return;

        this.mCurrentIndex = currentIndex;

        growIcon();

        if (mRotationDegree != mDrawables.get(mCurrentIndex).getDegree()) {
            rotateWheel(null, mCurrentIndex, false);
        }
    }

    public void setOnNavigationChangeListener(onNavigationChangeListener listener) {
        this.mOnNavigationChangeListener = listener;
    }

    public int getColor(int colorId) {
        return getContext().getResources().getColor(colorId);
    }

    /**
     * Constructor
     *
     * Sets up the paints and other necessary elements
     *
     * @param context
     * @param attrs
     */
    public NavWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {

        setWillNotDraw(false);

        mHandler = new Handler();

        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        mDrawThread = new DrawThread(getHolder(), mHandler);

        setDynamics(new FrictionDynamics(0.95f));

        mPixelDensity = UiUtils.getDisplayMetrics(getContext()).density;
        mClickThreshold = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        initPaint();
    }

    private void createHomeButton() {

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        float size = UiUtils.getDynamicPixels(getContext(), 80);

        mHomeButton = new HomeButton(getContext(), this, getColor(R.color.gray7), getColor(R.color.gray3), size, size);
        mHomeButton.setLayoutParams(params);
        mHomeButton.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY | getHeight());
        mHomeButton.layout(0, 0, mHomeButton.getMeasuredWidth(), mHomeButton.getMeasuredHeight());
    }

    private void initPaint() {

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setAlpha(0);
    }

    /**
     * Once a list of navigation items have been passed in they are converted
     * to drawable items so that can be laid out correctly and manipulated
     * according to our needs.
     */
    private void initializeItems() {

        if (mCenter == null) {
            return;
        }

        mPointer = new PointerDrawable(getContext(), mCenter, mItems.size());
        mPointer.setCallback(this);

        if (mDrawables == null) {
            mDrawables = new ArrayList<NavItemDrawable>();
        } else {
            mDrawables.clear();
        }

        mDegreeChange = 360.0 / (double) mItems.size();

        float radiusDp = UiUtils.getDynamicPixels(getContext(), RADIUS);

        if (!ApplicationContext.isLargeTablet()) {
            radiusDp *= 0.85f;
        }

        for (int i = 0; i < mItems.size(); i++) {

            // Determine the x, y position of the item at the given index so all
            // are distributed equally in a circular pattern
            double degrees = ((mDegreeChange * (double) i) + 90.0) % 360.0;
            double radians = Math.toRadians(degrees);

            degrees -= 90;
            if (degrees < 0) degrees += 360;

            float x = (float) (radiusDp * Math.cos(radians)) * -1;
            float y = (float) (radiusDp * Math.sin(radians)) * -1;

            x += mCenter.x;
            y += mCenter.y;

            PointF position = new PointF(x, y);

            // Create the drawable and add it to our array
            NavItemDrawable mItem = new NavItemDrawable(getContext(), mItems.get(i), i, position, mCenter, degrees);
            mItem.setCallback(this);
            mDrawables.add(i, mItem);
        }

        mDrawables.add(mPointer);
    }

    private void rotateWheel(NavItemDrawable navItem, int index, boolean animated) {

        synchronized (mDrawables) {

            if (mDrawables.size() == 0
                    || mDrawables.size() <= index
                    || !isEnabled()) return;

            if (navItem == null) {
                navItem = mDrawables.get(index);
            }

            float degree = (float) navItem.getDegree();

            // Normalize to a valid 360 degree range
            if (degree < 0) degree += 360;

            float start = getDegreeDistance(getRotationDegree(), degree);

            if (animated) {
                animateTo(start, degree, index);
            } else {
                setRotationDegree(degree);
                setCurrentIndex(index);
            }
        }
    }

    private float getDegreeDistance(float start, float degree) {

        float distance = start;

        // Make sure we rotate the correct direction to take the
        // shortest distance to the target degree
        float angle = Math.abs(start - degree) % 360f;

        if (angle > 180.0) {
            distance = start > degree ? (360 - start) * -1 : (360 + start);
        }

        return distance;
    }

    /**
     * Rotate the wheel based on a given (x,y) coordinate
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private void rotateWheel(final float x, final float y) {

        float degree = (float) (Math.toDegrees(Math.atan2(mCenter.y - y, mCenter.x - x)) - mRotationStart);

        // Rotate from the last rotation position to prevent rotation jumps
        rotateWheelBy(degree);
    }

    /**
     * Rotates the piechart rotation degree. Takes care of rotation (if enabled) and
     * snapping
     *
     * @param degree The degree to rotate to
     */
    private void rotateWheel(float degree) {

        final float previous = getRotationDegree();

        setRotationDegree(degree);

        // Pop icon when rotating
        for (NavItemDrawable navItem : mDrawables) {
            if (navItem.getDegree() != -1 && Math.abs(getShortestDistance((float) navItem.getDegree(),
                    mRotationDegree)) < 5f) {
                growIcon(navItem);
                break;
            }
        }

        setRotatingClockwise(previous);
    }

    public void rotateWheelBy(float degree) {
        rotateWheel(mLastRotation + degree);
    }

    private void growIcon() {
        NavItemDrawable item = mDrawables.get(mCurrentIndex);
        growIcon(item);
    }

    /**
     * Animates the grow/shrink effect of an icon when
     * the pointer is pointing at the given index.
     *
     * @return
     */
    private void growIcon(NavItemDrawable item) {
        item.popIcon();
    }

    public void toggleNav() {

        if (!isShowing()) {
            showNav();
        } else {
            hideNav();
        }
    }

    /**
     * Show the navigation wheel on screen with the appropriate animations
     */
    public void showNav() {

        mShowing = true;
        mHomeButton.setShowSlider(true);

        ObjectAnimator fade = ObjectAnimator.ofInt(this, "backgroundAlpha", 0, ALPHA);
        fade.setDuration(250);
        fade.start();

        for (NavItemDrawable item : mDrawables) {
            item.playIntro();
        }

        // Notify the system the navigation wheel is showing
        EventBus.getDefault().post(new EventMessage().new NavigationEvent(true));
    }

    /**
     * Hide the navigation wheel from the user with the appropriate animations
     */
    public void hideNav() {

        mHomeButton.setShowSlider(false);

        mShowing = false;

        ObjectAnimator fade = ObjectAnimator.ofInt(this, "backgroundAlpha", ALPHA, 0);
        fade.setDuration(250);
        fade.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                // Update any mOnNavigationChangeListener of the index change
                if (mOnNavigationChangeListener != null) {

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mOnNavigationChangeListener.onNavigationChanged(mCurrentIndex);
                        }
                    });
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new EventMessage().new NavigationEvent(false));
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        fade.start();

        for (NavItemDrawable item : mDrawables) {
            item.playOutro(mCurrentIndex);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        if (mHomeButton.onTouchEvent(event)) return true;

        if (!mShowing) return false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchState == TOUCH_STATE_CLICK) {
                    startRotationIfNeeded(event);
                }

                if (mTouchState == TOUCH_STATE_ROTATE) {
                    mVelocityTracker.addMovement(event);
                    rotateWheel(event.getX(), event.getY());
                }

                break;

            case MotionEvent.ACTION_UP:

                float velocity = 0;

                if (mTouchState == TOUCH_STATE_CLICK) {

                    clickChildAt((int) event.getX(), (int) event.getY());

                } else if (mTouchState == TOUCH_STATE_ROTATE) {

                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND);

                    velocity = calculateVelocity();
                }

                endTouch(velocity);

                break;

            default:
                endTouch(0);
                break;
        }

        return true;
    }

    /**
     * Calls the item click mOnNavigationChangeListener for the child with at the specified
     * coordinates
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private void clickChildAt(final int x, final int y) {

        if (!isEnabled()) return;

        final int index = getContainingChildIndex(x, y);

        if (index != INVALID_INDEX) {

            mClicked = true;

            final NavItemDrawable navItem = mDrawables.get(index);

            if (getCurrentIndex() != index) {
                animateTo(navItem, index);
            } else {
                hideNav();
            }

            playSoundEffect(SoundEffectConstants.CLICK);
            performItemClick(navItem, index);

            return;
        }

        hideNav();
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

        for (int index = 0; index < mDrawables.size() - 1; index++) {

            final NavItemDrawable navItem = mDrawables.get(index);

            if (navItem.getBounds().contains(x, y)) {
                return index;
            }
        }

        return INVALID_INDEX;
    }

    /**
     * Call the OnItemClickListener, if it is defined.
     *
     * @param navItem The drawable within the View that was clicked.
     * @param position The position of the view in the adapter.
     * @return True if there was an assigned OnItemClickListener that was
     *         called, false otherwise is returned.
     */
    public boolean performItemClick(NavItemDrawable navItem, int position) {



        return false;
    }

    /**
     * Calculates the overall vector velocity given both the x and y
     * velocities and normalized to be pixel independent.
     *
     * @return the overall vector velocity
     */
    private float calculateVelocity() {

        int direction = mRotatingClockwise ? 1 : -1;

        float velocityX = mVelocityTracker.getXVelocity() / mPixelDensity;
        float velocityY = mVelocityTracker.getYVelocity() / mPixelDensity;
        float velocity = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY) * direction / 2;

        return velocity;
    }

    /**
     * Sets and initializes all things that need to when we start a touch
     * gesture.
     *
     * @param event The down event
     */
    private void startTouch(final MotionEvent event) {

        // user is touching the list -> no more fling
        removeCallbacks(mDynamicsRunnable);

        updateStartRotation();

        // save the start place
        mTouchStartX = (int) event.getX();
        mTouchStartY = (int) event.getY();

        // obtain a velocity tracker and feed it its first event
        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        // we don't know if it's a click or a scroll yet, but until we know
        // assume it's a click
        setTouchState(TOUCH_STATE_CLICK);
    }

    /**
     * Checks if the user has moved far enough for this to be a scroll and if
     * so, sets the list in scroll mode
     *
     * @param event The (move) event
     * @return true if scroll was started, false otherwise
     */
    private boolean startRotationIfNeeded(final MotionEvent event) {

        final int xPos = (int) event.getX();
        final int yPos = (int) event.getY();

        if (isEnabled()
                && (xPos < mTouchStartX - mClickThreshold
                || xPos > mTouchStartX + mClickThreshold
                || yPos < mTouchStartY - mClickThreshold
                || yPos > mTouchStartY + mClickThreshold)) {

            setTouchState(TOUCH_STATE_ROTATE);

            mRotationStart = (float) Math.toDegrees(Math.atan2(mCenter.y - yPos, mCenter.x - xPos));

            return true;
        }

        return false;
    }

    /**
     * Resets and recycles all things that need to when we end a touch gesture
     */
    private void endTouch(final float velocity) {

        // recycle the velocity tracker
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }

        // create the dynamics runnable if we haven't before
        if (mDynamicsRunnable == null) {

            mDynamicsRunnable = new Runnable() {

                public void run() {

                    // if we don't have any dynamics set we do nothing
                    if (mDynamics == null) {
                        return;
                    }

                    // we pretend that each frame of the fling/snap animation is
                    // one touch gesture and therefore set the start position
                    // every time
                    mDynamics.update(AnimationUtils.currentAnimationTimeMillis());

                    // Keep the rotation amount between 0 - 360
                    rotateWheel(mDynamics.getPosition() % 360);

                    if (!mDynamics.isAtRest(VELOCITY_TOLERANCE)) {

                        // the list is not at rest, so schedule a new frame
                        mHandler.postDelayed(this, 8);

                    } else {

                        snapTo();
                    }

                }
            };
        }

        if (mDynamics != null && Math.abs(velocity) > ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity()) {
            // update the dynamics with the correct position and start the runnable
            mDynamics.setState(getRotationDegree(), velocity, AnimationUtils.currentAnimationTimeMillis());
            mHandler.post(mDynamicsRunnable);

        } else if (mTouchState != TOUCH_STATE_CLICK) {

            snapTo();
        }

        // reset touch state
        setTouchState(TOUCH_STATE_RESTING);
    }

    /**
     * Snaps the piechart rotation to a given snap degree
     */
    public void snapTo() {
        snapTo(true);
    }

    /**
     * Snaps the piechart rotation to a given snap degree
     */
    private void snapTo(boolean animated) {

        float closest = 360;
        int toIndex = 0;

        for (int index = 0; index < mDrawables.size() - 1; index++) {

            final NavItemDrawable navItem = mDrawables.get(index);
            float distance = Math.abs(getShortestDistance(mRotationDegree, (float) navItem.getDegree()));

            if (distance < closest) {

                closest = distance;
                toIndex = index;
            }
        }

        rotateWheel(null, toIndex, animated);
    }

    private float getShortestDistance(float start, float end) {

        // Make sure we rotate the correct direction to take the
        // shortest distance to the target degree
        float angle = Math.abs(start - end) % 360f;

        if (angle > 180.0) {
            angle = (360 - start);
        }

        return angle;
    }

    private void animateTo(NavItemDrawable navItem, int index) {
        rotateWheel(navItem, index, true);
    }

    /**
     * Animates the pie piechart's rotation to a specific degree
     *
     * @param start the PieSliceView to rotate to
     * @param end the index of the PieSliceView
     */
    private void animateTo(float start, float end, final int index) {

        // Animate the rotation and update the current index
        mRotateAnimator = ThreadAnimator.ofFloat(start, end);
        mRotateAnimator.setDuration(300);
        mRotateAnimator.setAnimationListener(new ThreadAnimator.AnimationListener() {
            @Override
            public void onAnimationEnded() {

                setCurrentIndex(index);

                if (mClicked) {
                    mClicked = false;

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            hideNav();
                        }
                    });
                }
            }
        });
        mRotateAnimator.start();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mCenter == null || changed) {

            mCenter = new PointF((getMeasuredWidth() / 2.0f), (getMeasuredHeight() / 2.0f));
            initializeItems();

            createHomeButton();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the pointer and background
        canvas.drawColor(mBackgroundPaint.getColor());
    }

    private void doDraw(Canvas canvas) {

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        updateAnimators();

        mHomeButton.draw(canvas);
        mPointer.draw(canvas);

        // Draw all of the navigation items
        for (NavItemDrawable item : mDrawables) {
            item.draw(canvas);
        }
    }

    /**
     * Update our animators that control animating the
     * rotation, scale, and info panel alpha
     */
    private void updateAnimators() {

        if (mRotateAnimator != null && mRotateAnimator.isRunning()) {
            setRotationDegree(mRotateAnimator.floatUpdate());
        }
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

        onResume();
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

                            doDraw(canvas);
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
    }

    public interface onNavigationChangeListener {
        public void onNavigationChanged(int index);
    }
}