package com.moneydesktop.finance.views.chart;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.CaretDrawable;
import com.moneydesktop.finance.views.Dynamics;
import com.moneydesktop.finance.views.chart.ThreadAnimator.AnimationListener;

/**
 * A view that creates a Pie Chart which is backed by an adapter providing the
 * data for each slice of the pie chart.  The chart can be rotated by touch and
 * automatically snaps to one of the compass points, defaults to {@link PieChartAnchor#RIGHT}.  
 * To turn off the rotation feature simply call {@link #setEnabled(boolean)} and
 * set it to false.
 * 
 * @author Saul Howard
 * @version 1.0
 */
public class PieChartView extends SurfaceView implements SurfaceHolder.Callback {
    
    public final String TAG = this.getClass().getSimpleName();

    public enum PieChartAnchor {
    	
        TOP (270),
        RIGHT (0),
        BOTTOM (90),
        LEFT (180);
        
        private float degrees;
        
        PieChartAnchor(float degrees) {
            this.degrees = degrees;
        }
    };

    private static final int SUB_STROKE_WIDTH = 1;
    private static final int INFO_STROKE_WIDTH = 3;

    /** Unit used for the velocity tracker */
    private static final int PIXELS_PER_SECOND = 1000;

    /** Tolerance for the velocity */
    private static final float VELOCITY_TOLERANCE = 40f;

    /** Represents an invalid child index */
    private static final int INVALID_INDEX = -1;

    /** Represents a touch to the info circle */
    private static final int INFO_INDEX = -2;
	
	/** User is not touching the chart */
    public static final int TOUCH_STATE_RESTING = 0;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    /** User is rotating the chart */
    public static final int TOUCH_STATE_ROTATE = 2;
    
    public static final int CHART_HIDDEN = 0;
    public static final int CHART_SHOWING = 1;
    public static final int CHART_INVALID = 2;
    
    /** Default degree to snap to */
    private static final float DEFAULT_SNAP_DEGREE = 0f;
	
	private DrawThread mDrawThread;

    /** Current touch state */
    private int mTouchState = TOUCH_STATE_RESTING;
    
    /** Distance to drag before we intercept touch events */
    private int mScrollThreshold;

    /** Velocity tracker used to get fling velocities */
    private VelocityTracker mVelocityTracker;

    /** Dynamics object used to handle fling and snap */
    private Dynamics mDynamics;

    /** Runnable used to animate fling and snap */
    private Runnable mDynamicsRunnable;

    /** Used to check for long press actions */
    private Runnable mLongPressRunnable;

    /** The adapter with all the data */
    private BasePieChartAdapter mAdapter;

    /** X-coordinate of the down event */
    private int mTouchStartX;

    /** Y-coordinate of the down event */
    private int mTouchStartY;
    
    /** The degree to snap the chart to when rotating */
    private float mSnapToDegree = DEFAULT_SNAP_DEGREE;
    
    /** Our starting rotation degree */
    private float mRotationStart = 0;
    
    /** The last rotation degree after touch */
    private float mLastRotation = 0;
    
    /** The rotating direction of the chart */
    private boolean mRotatingClockwise;
    
    /** The diameter of the chart */
    private int mChartDiameter;
	
	private float mInfoRadius;
    
    /** The pixel density of the current device */
    private float mPixelDensity;
    
    /** The center point of the chart */
    private PointF mCenter = new PointF();
	
	private float mStrokeWidth;
    
    /** The current degrees of rotation of the chart */
	private float mRotationDegree = 0;
	
	private float mChartScale = 1.0f;
	
	private boolean mChartHidden = false;
	
	private boolean mNeedsToggle = false;
	
	private boolean mNeedsUpdate = false;
	
	private boolean mShowInfo = false;
	
	private boolean mLoaded = false;
	
	private List<PieSliceDrawable> mDrawables;
	
	private LinkedList<PieSliceDrawable> mRecycledDrawables;
	
	private InfoDrawable mInfoDrawable;
	
	private CaretDrawable mCaret;
	
	private int mInfoAlpha = 255;

	/** The current snapped-to index */
	private int mCurrentIndex;

	private Bitmap mDrawingCache;
	
	private OnPieChartChangeListener mOnPieChartChangeListener;
	
	private OnItemLongClickListener mOnItemLongClickListener;
	
	private OnInfoClickListener mOnInfoClickListener;
	
	private OnPieChartExpandListener mOnPieChartExpandListener;
	
	private OnPieChartReadyListener mOnPieChartReadyListener;
    /**
     * The listener that receives notifications when an item is clicked.
     */
    private OnItemClickListener mOnItemClickListener;
    
    private OnRotationStateChangeListener mOnRotationStateChangeListener;
	
	private AdapterDataSetObserver mDataSetObserver;
	
	private Handler mHandler;
	
	private Paint mPaint;
	
	private Paint mStrokePaint;
	
	private void setTouchState(int touchState) {
		
		mTouchState = touchState;
		
		if (mOnRotationStateChangeListener != null) {
        	mOnRotationStateChangeListener.onRotationStateChange(mTouchState);
        }
	}
	
	public Bitmap getDrawingCache() {
		return mDrawingCache;
	}

	/**
     * Set the dynamics object used for fling and snap behavior.
     * 
     * @param dynamics The dynamics object
     */
    public void setDynamics(final Dynamics dynamics) {
    	
        if (mDynamics != null) {
            dynamics.setState((float) getRotationDegree(), mDynamics.getVelocity(), AnimationUtils
                    .currentAnimationTimeMillis());
        }
        
        mDynamics = dynamics;
    }
    
    /**
     * Is the current Pie Chart hidden.
     * 
     * @return True if the chart is hidden, false otherwise.
     */
    public boolean isChartHidden() {
    	return mChartHidden;
    }
	
    /**
     * Set the rotation degree of the chart.
     * 
     * @param rotationDegree the degree to rotate the chart to
     */
	void setRotationDegree(float rotationDegree) {
		
		// Keep rotation degree positive
		if (rotationDegree < 0) rotationDegree += 360;
		
		// Keep rotation degree between 0 - 360
		mRotationDegree = rotationDegree % 360;
	}
	
	public float getRotationDegree() {
		return mRotationDegree;
	}

	public int getCurrentIndex() {
		
		if (!isLoaded()) return 0;
		
		return mCurrentIndex;
	}
	
	/**
	 * <b>Internal Use Only</b> Sets the currently selected index
	 * and fires of the selection change listener if one is
	 * attached.
	 * 
	 * @param index The current index
	 */
	private void setCurrentIndex(final int index) {
		
		mCurrentIndex = index;

		if (mNeedsToggle) {
			mNeedsToggle = false;
			toggleChart();
		}
		
		if (mOnPieChartChangeListener != null && !mChartHidden && isLoaded()) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {

					mOnPieChartChangeListener.onSelectionChanged(index);
				}
			});
		}
	}
	
	public void setSnapToAnchor(PieChartAnchor anchor) {
		mSnapToDegree = anchor.degrees;
		snapTo();
	}
	
	public float getChartDiameter() {
		return mChartDiameter;
	}
	
	public float getChartRadius() {
		return mChartDiameter / 2f;
	}
	
	public synchronized boolean isLoaded() {
		return mLoaded;
	}

	public synchronized void setLoaded(boolean mLoaded) {
		this.mLoaded = mLoaded;
	}

	/**
	 * Gets the {@link InfoDrawable} used to draw the info panel.
	 * 
	 * @return The {@link InfoDrawable}
	 */
	public InfoDrawable getInfoDrawable() {
		
		createInfo();
		
		return mInfoDrawable;
	}

	/**
	 * Sets the Pie Chart's slice selection.
	 * 
	 * @param index The index to select
	 */
	public void setSelection(int index) {
		animateTo(index);
	}
	
	/**
	 * Gets the drawing thread
	 * 
	 * @return {@link DrawingThread}
	 */
	public DrawThread getDrawThread() {
		return mDrawThread;
	}

	public OnPieChartChangeListener getOnPieChartChangeListener() {
		return mOnPieChartChangeListener;
	}

	public void setOnPieChartChangeListener(
			OnPieChartChangeListener mOnPieChartChangeListener) {
		this.mOnPieChartChangeListener = mOnPieChartChangeListener;
	}
	
	public void setOnPieChartExpandListener(
			OnPieChartExpandListener mOnPieChartExpandListener) {
		this.mOnPieChartExpandListener = mOnPieChartExpandListener;
	}

	public void setOnPieChartReadyListener(
			OnPieChartReadyListener mOnPieChartReadyListener) {
		this.mOnPieChartReadyListener = mOnPieChartReadyListener;
	}
	
	public OnRotationStateChangeListener getOnRotationStateChangeListener() {
		return mOnRotationStateChangeListener;
	}

	public void setOnRotationStateChangeListener(
			OnRotationStateChangeListener mOnRotationStateChangeListener) {
		this.mOnRotationStateChangeListener = mOnRotationStateChangeListener;
	}

    public OnItemLongClickListener getOnItemLongClickListener() {
		return mOnItemLongClickListener;
	}

	public void setOnItemLongClickListener(
			OnItemLongClickListener mOnItemLongClickListener) {
		this.mOnItemLongClickListener = mOnItemLongClickListener;
	}

    public OnItemClickListener getOnItemClickListener() {
		return mOnItemClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
		this.mOnItemClickListener = mOnItemClickListener;
	}
	
	public void setOnInfoClickListener(OnInfoClickListener mOnInfoClickListener) {
		this.mOnInfoClickListener = mOnInfoClickListener;
	}
	
	/**
	 * Returns the {@link PieSliceDrawable} for the given index
	 * 
	 * @param index The position of the slice
	 * 
	 * @return A {@link PieSliceDrawable}
	 */
	public PieSliceDrawable getSlice(int index) {
	
		synchronized(mDrawables) {
			
			if (mDrawables.size() > index) {
				return mDrawables.get(index);
			}
		}
		
		return null;
	}

	/**
	 * Show the info panel
	 */
	public void showInfo() {
		mShowInfo = true;
		mChartHidden = true;
		mChartScale = 0f;
	}
	
	/**
	 * Hide the info panel
	 */
	public void hideInfo() {
		mShowInfo = false;
		mChartHidden = false;
		mChartScale = 1f;
	}
	
	public PieChartView(Context context) {
		super(context);
		
		init();
	}
	
	public PieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}
	
	public PieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}

	/**
	 * Creates and initializes a new PieChartView
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public PieChartView(Context context, OnPieChartReadyListener listener) {
		super(context);
		
		setOnPieChartReadyListener(listener);
		init();
	}
	
	private void init() {

		Context context = getContext();
		
		mHandler = new Handler();
		
        getHolder().addCallback(this);
		setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        
        mDrawThread = new DrawThread(getHolder());
		
		mScrollThreshold = ViewConfiguration.get(context).getScaledTouchSlop();
		mPixelDensity = UiUtils.getDisplayMetrics(context).density;
		mStrokeWidth = UiUtils.getDynamicPixels(context, SUB_STROKE_WIDTH);
		
		mDrawables = new ArrayList<PieSliceDrawable>();
		mRecycledDrawables = new LinkedList<PieSliceDrawable>();
		
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

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
    	
        if ((!inCircle((int) event.getX(), (int) event.getY()) && 
				mTouchState == TOUCH_STATE_RESTING)) {
        	return false;
        }
        
        switch (event.getAction()) {
        
            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                break;

            case MotionEvent.ACTION_MOVE:
            	
                if (mTouchState == TOUCH_STATE_CLICK) {
                    startScrollIfNeeded(event);
                }
                
                if (mTouchState == TOUCH_STATE_ROTATE) {
                    mVelocityTracker.addMovement(event);
                    rotateChart(event.getX(), event.getY());
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

                endTouch(event.getX(), event.getY(), velocity);
                
                break;

            default:
                endTouch(event.getX(), event.getY(), 0);
                break;
        }
        
        return true;
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
        
        mLastRotation = getRotationDegree();
    	
        // save the start place
        mTouchStartX = (int) event.getX();
        mTouchStartY = (int) event.getY();

        // start checking for a long press
        startLongPressCheck();

        // obtain a velocity tracker and feed it its first event
        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        // we don't know if it's a click or a scroll yet, but until we know
        // assume it's a click
        setTouchState(TOUCH_STATE_CLICK);
    }

    /**
     * Resets and recycles all things that need to when we end a touch gesture
     */
    private void endTouch(final float x, final float y, final float velocity) {
    	
        // recycle the velocity tracker
    	if (mVelocityTracker != null) {
	        mVelocityTracker.recycle();
	        mVelocityTracker = null;
    	}
    	
        // remove any existing check for long-press
        removeCallbacks(mLongPressRunnable);

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
                    rotateChart(mDynamics.getPosition() % 360);

                    if (!mDynamics.isAtRest(VELOCITY_TOLERANCE)) {
                    	
                        // the list is not at rest, so schedule a new frame
                        postDelayed(this, 8);
                        
                    } else {

                    	snapTo();
                    }

                }
            };
        }

        if (mDynamics != null && Math.abs(velocity) > ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity()) {
            // update the dynamics with the correct position and start the runnable
            mDynamics.setState((float) getRotationDegree(), velocity, AnimationUtils.currentAnimationTimeMillis());
            post(mDynamicsRunnable);
            
        } else if (mTouchState != TOUCH_STATE_CLICK) {
        	
        	snapTo();
        }
        
        // reset touch state
        setTouchState(TOUCH_STATE_RESTING);
    }

    /**
     * Checks if the user has moved far enough for this to be a scroll and if
     * so, sets the list in scroll mode
     * 
     * @param event The (move) event
     * @return true if scroll was started, false otherwise
     */
    private boolean startScrollIfNeeded(final MotionEvent event) {
    	
        final int xPos = (int) event.getX();
        final int yPos = (int) event.getY();
        
        if (isEnabled()
        		&& (xPos < mTouchStartX - mScrollThreshold
                || xPos > mTouchStartX + mScrollThreshold
                || yPos < mTouchStartY - mScrollThreshold
                || yPos > mTouchStartY + mScrollThreshold)) {
        	
            // we've moved far enough for this to be a scroll
            removeCallbacks(mLongPressRunnable);
            
            setTouchState(TOUCH_STATE_ROTATE);
            
            mRotationStart = (float) Math.toDegrees(Math.atan2(mCenter.y - yPos, mCenter.x - xPos));
            
            return true;
        }
        
        return false;
    }

    /**
     * Posts (and creates if necessary) a runnable that will when executed call
     * the long click listener
     */
    private void startLongPressCheck() {
    	
    	if (!isEnabled()) return;
    	
        // create the runnable if we haven't already
        if (mLongPressRunnable == null) {
        	
            mLongPressRunnable = new Runnable() {
            	
                public void run() {
                	
                    if (mTouchState == TOUCH_STATE_CLICK) {
                    	
                        final int index = getContainingChildIndex(mTouchStartX, mTouchStartY);
                        
                        if (index != INVALID_INDEX) longClickChild(index);
                    }
                }
            };
        }

        // then post it with a delay
        postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Calls the item click listener for the child with at the specified
     * coordinates
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private void clickChildAt(final int x, final int y) {
    	
    	if (!isEnabled()) return;
    	
        final int index = getContainingChildIndex(x, y);
        
        if (index == INFO_INDEX) {
        	
        	if (mOnInfoClickListener != null) {
                playSoundEffect(SoundEffectConstants.CLICK);
        		mOnInfoClickListener.onInfoClicked(mChartHidden ?  -1 : getCurrentIndex());
        	}
        	
        } else if (index != INVALID_INDEX) {
        	
            final PieSliceDrawable sliceView = mDrawables.get(index);
            final long id = mAdapter.getItemId(index);
            boolean secondTap = false;
            
            if (getCurrentIndex() != index) {
            	animateTo(sliceView, index);
            } else {
            	secondTap = true;
            }

            playSoundEffect(SoundEffectConstants.CLICK);
            performItemClick(secondTap, sliceView, index, id);
        }
    }

    /**
     * Call the OnItemClickListener, if it is defined.
     *
     * @param view The drawable within the View that was clicked.
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     * @return True if there was an assigned OnItemClickListener that was
     *         called, false otherwise is returned.
     */
    public boolean performItemClick(boolean secondTap, PieSliceDrawable view, int position, long id) {
    	
        if (mOnItemClickListener != null) {
        	
            mOnItemClickListener.onItemClick(secondTap, this, view, position, id);
            
            return true;
        }

        return false;
    }

	/**
     * Calls the item long click listener for the child with the specified index
     * 
     * @param index Child index
     */
    private void longClickChild(final int index) {
    	
        final PieSliceDrawable slice = mDrawables.get(index);
        final long id = mAdapter.getItemId(index);
        final OnItemLongClickListener listener = getOnItemLongClickListener();
        
        if (listener != null) {
            listener.onItemLongClick(null, slice, index, id);
        }
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
    	
    	// Check if we touched the info panel
    	if (inInfoCircle(x, y)) return INFO_INDEX;
    	
    	// Check if we did not touch within the bounds of the Pie Chart
    	if (!inCircle(x, y)) return INVALID_INDEX;
    	
    	// Get the drawing cache to aid in calculating which slice was touched
        final Bitmap viewBitmap = getDrawingCache();
        
        if (viewBitmap == null) return INVALID_INDEX;
        
        // Grab the color pixel at the point touched and compare it with the children
        int pixel = viewBitmap.getPixel(x, y);
        
        for (int index = 0; index < mDrawables.size(); index++) {
        	
            final PieSliceDrawable slice = mDrawables.get(index);
            
            if (slice.getSliceColor() == pixel) {
                return index;
            }
        }
        
        return INVALID_INDEX;
    }
    
    /**
     * Does the touch lie within the bounds of the current Pie Chart.
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     * 
     * @return True if the touch was inside the Pie Chart's circular bounds
     */
    private boolean inCircle(final int x, final int y) {
    	
    	if ((mChartHidden || !isLoaded()) && !inInfoCircle(x, y)) return false;
    	
        double dx = (x - mCenter.x) * (x - mCenter.x);
        double dy = (y - mCenter.y) * (y - mCenter.y);

        if ((dx + dy) < ((mChartDiameter / 2) * (mChartDiameter / 2))) {
            return true;
        } else {
            return false;
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
    	
    	if (!mShowInfo) return false;
    	
        double dx = (x - mCenter.x) * (x - mCenter.x);
        double dy = (y - mCenter.y) * (y - mCenter.y);

        if ((dx + dy) < (mInfoRadius * mInfoRadius)) {
            return true;
        } else {
            return false;
        }
    }
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
        boolean useHeight = height < width;
        
        mChartDiameter = (useHeight ? (height - (getPaddingTop() + getPaddingBottom()))
        		: (width - (getPaddingLeft() + getPaddingRight()))) - (int) mStrokeWidth;
		
		mInfoRadius = getChartRadius() / 2;
		
        int size = useHeight ? height : width;
        
		setMeasuredDimension(size, size);
	}
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
        
		// Get the center coordinates of the view
		mCenter.x = (float) Math.abs(left - right) / 2;
		mCenter.y = (float) Math.abs(top - bottom) / 2;
	}

    /**
     * Starts at 0 degrees and adds each pie slice as provided
     * by the adapter
     */
    private void addPieSlices() {
    	
    	synchronized (mDrawables) {
    		
	    	float offset = 0;
	    	
	        for (int index = 0; index < mAdapter.getCount(); index++) {
	            
	        	// Check for any recycled PieSliceDrawables
	        	PieSliceDrawable recycled = getRecycledSlice();
	        	
	        	// Get the slice from the adapter
	            final PieSliceDrawable childSlice = mAdapter.getSlice(this, recycled, index, offset);
	            
	            childSlice.setBounds(getBounds());
	            mDrawables.add(childSlice);
	            
	            offset += childSlice.getDegrees();
	        }
	        
	        setLoaded(true);
    	}
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
    
    /**
     * Returns a recycled {@link PieSliceDrawable} if one is available.
     * 
     * @return A PieSliceDrawable if one exists, null otherwise
     */
    private PieSliceDrawable getRecycledSlice() {
        
    	if (mRecycledDrawables.size() != 0) {
            return mRecycledDrawables.removeFirst();
        }
        
        return null;
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
	
	/**
	 * Creates the info panel drawable
	 */
	private void createInfo() {
		
		if (mInfoDrawable == null) {
			mInfoDrawable = new InfoDrawable(getDrawThread(), getContext(), getBounds(), mInfoRadius);
		}
	}
    
    /**
     * Hide the current Pie Chart if showing.
     */
    public void hideChart() {
    	
    	if (!mChartHidden) {
    		toggleChart();
    	}
    }
    
    /**
     * Show the current Pie Chart if hidden.
     */
    public void showChart() {
    	
    	if (mChartHidden) {
    		toggleChart();
    	}
    }
    
    /**
     * Toggle the Pie Chart to show or hide depending on its current
     * state.
     * 
     * @return True if the chart was hidden, false otherwise.
     */
    public int toggleChart() {
    	
    	final float start = mChartScale;
    	final float end = start == 1f ? 0f : 1f;
		
		if (mChartHidden && !isLoaded()) {
			
			mNeedsToggle = true;
			
			return CHART_INVALID;
		}

		mNeedsToggle = false;
		mChartHidden = (end == 0);
    	
    	ThreadAnimator scale = ThreadAnimator.ofFloat(start, end);
    	scale.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationEnded() {
				mDrawingCache = null;
				
				if (mNeedsUpdate) {
					mNeedsUpdate = false;
					resetChart();
				}
			}
		});
    	
    	scale.setDuration(400);
    	
    	if (end == 1) {
    		scale.setInterpolator(new OvershootInterpolator());
    	}
    	
    	getDrawThread().setScaleAnimator(scale);
    	
    	onChartChanged(mChartHidden);
    	
    	return (end == 0) ? CHART_HIDDEN : CHART_SHOWING;
    }
    
    private void onChartChanged(final boolean didCollapse) {
    	
    	if (mOnPieChartExpandListener != null) {

    		if (didCollapse) {
    			mOnPieChartExpandListener.onPieChartCollapsed();
    		} else {
    			mOnPieChartExpandListener.onPieChartExpanded();
    		}
    	}
    }

    /**
     * Snaps the chart rotation to a given snap degree
     */
    private void snapTo() {
    	snapTo(true);
    }
    
    /**
     * Snaps the chart rotation to a given snap degree
     */
    private void snapTo(boolean animated) {
    	
    	for (int index = 0; index < mDrawables.size(); index++) {
        	
            final PieSliceDrawable slice = mDrawables.get(index);
            
            if (slice.containsDegree(mRotationDegree, mSnapToDegree)) {
            	
            	rotateChart(slice, index, animated);
            	
            	break;
            }
    	}
    }
    
    /**
     * Animates the pie chart's rotation to a specific degree
     * 
     * @param index the index of the PieSliceView
     */
    private void animateTo(int index) {
    	rotateChart(null, index, true);
    }
    
    private void animateTo(PieSliceDrawable slice, int index) {
    	rotateChart(slice, index, true);
    }
    
    /**
     * Animates the pie chart's rotation to a specific degree
     * 
     * @param slice the PieSliceView to rotate to
     * @param index the index of the PieSliceView
     */
    private void animateTo(float start, float end) {

    	// Animate the rotation and update the current index
    	ThreadAnimator rotate = ThreadAnimator.ofFloat(start, end);
    	rotate.setDuration(300);
    	rotate.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationEnded() {
				
				if (mOnRotationStateChangeListener != null) {
		        	mOnRotationStateChangeListener.onRotationStateChange(TOUCH_STATE_RESTING);
		        }
				
				mDrawingCache = null;
			}
		});
    	
    	if (mOnRotationStateChangeListener != null) {
        	mOnRotationStateChangeListener.onRotationStateChange(TOUCH_STATE_ROTATE);
        }
    	
    	getDrawThread().setRotateAnimator(rotate);
    }
    
    private void rotateChart(PieSliceDrawable slice, int index, boolean animated) {
    	
    	synchronized (mDrawables) {
    		
	    	if (mDrawables.size() == 0
	    			|| mDrawables.size() <= index
	    			|| !isEnabled()) return;
	    	
	    	if (slice == null) {
	    		slice = mDrawables.get(index);
	    	}
	    	
	        float degree = slice.getSliceCenter();
	    	
	    	// Adjust for our snap degree
	    	degree = mSnapToDegree - degree;
	    	
	    	// Normalize to a valid 360 degree range
	    	if (degree < 0) degree += 360;
	    	
	    	float start = getRotationDegree();
	    	
	    	// Make sure we rotate the correct direction to take the
	    	// shortest distance to the target degree
	    	float rawDiff = Math.abs(start - degree);
	    	float modDiff = rawDiff % 360f;
	    	
	    	if (modDiff > 180.0) {
	    		start = start > degree ? (360 - start) * -1 : (360 + start);
	    	}
	
	    	if (animated) {
	    		animateTo(start, degree);
	    	} else {
	    		setRotationDegree(degree);
				mDrawingCache = null;
	    	}
	    	
			setCurrentIndex(index);
    	}
    }
    
    /**
     * Rotate the chart based on a given (x,y) coordinate
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private void rotateChart(final float x, final float y) {
    	
    	float degree = (float) (Math.toDegrees(Math.atan2(mCenter.y - y, mCenter.x - x)) - mRotationStart);
    	
    	// Rotate from the last rotation position to prevent rotation jumps
    	rotateChart(mLastRotation + degree);
    }

    /**
     * Rotates the chart rotation degree. Takes care of rotation (if enabled) and
     * snapping
     * 
     * @param degree The degree to rotate to
     */
    private void rotateChart(float degree) {
    	
    	final float previous = getRotationDegree();
    	
    	setRotationDegree(degree);

    	setRotatingClockwise(previous);
    }
    
    /**
     * Checks which way the chart is rotating.
     * 
     * @param previous The previous degree the chart was rotated to
     */
    private void setRotatingClockwise(float previous) {

    	final float change = (mRotationDegree - previous);
    	mRotatingClockwise = (change > 0 && Math.abs(change) < 300) || (Math.abs(change) > 300 && mRotatingClockwise);
    }
    /**
     * Returns the current PieChartAdapter
     * 
     * @return The PieChartAdapter
     */
	public BasePieChartAdapter getPieChartAdapter() {
		return mAdapter;
	}

	/**
	 * Sets the adapter that will provide the data for this
	 * Pie Chart.
	 * 
	 * @param adapter The PieChart adapter
	 */
	public void setAdapter(BasePieChartAdapter adapter) {
		
		// Unregister the old data change observer
		if (mAdapter != null && mDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		
		// Perform validation check
		float total = validAdapter(adapter);
		if ((1f - total) > 0.0001f) {
			return;
		}
		
		resetChart();
		
		mAdapter = adapter;
		
		// Register the data change observer
		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);
		}
	}
	
	/**
	 * Get the sum of all the percents from the adapter
	 * to help with validation.  We need an approximate
	 * total of 1.0f so that the chart can be rendered
	 * properly.
	 * 
	 * @param adapter The adapter supplying the chart's data
	 * @return The sum of all percentages provided by the adapter
	 */
	private float validAdapter(BasePieChartAdapter adapter) {
		
		float total = 0;
		
		for (int i = 0; i < adapter.getCount(); i++) {
			total += adapter.getPercent(i);
		}
		
		return total;
	}
	
	/**
	 * Resets the chart and recycles all PieSliceDrawables
	 */
	private void resetChart() {
		
		synchronized(mDrawables) {
			
			setLoaded(false);
			
			mRecycledDrawables.addAll(mDrawables);
			mDrawables.clear();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		if (mDrawThread.getState() == Thread.State.TERMINATED) {
			
			mDrawThread = new DrawThread(getHolder());
			mDrawThread.setRunning(true);
			mDrawThread.start();
			
        } else {
        	
        	mDrawThread.setRunning(true);
        	mDrawThread.start();
        }
		
		if (mOnPieChartReadyListener != null) {
			mOnPieChartReadyListener.onPieChartReady();
		}
		
		mDrawThread.onPause();
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
	 * Thread used to draw the Pie Chart
	 * 
	 * @author Saul Howard
	 */
	protected class DrawThread extends Thread {
		
		/** The SurfaceHolder to draw to */
		private SurfaceHolder surfaceHolder;
		
		/** Tracks the running state of the thread */
		private boolean isRunning;
		
		/** Object used to acquire a pause lock on the thread */
		private Object mPauseLock = new Object();  
		
		/** Tracks the pause state of the drawing thread */
		private boolean mPaused;
		
		/** Animator objects used to animate the rotation, scale, and info panel */
		private ThreadAnimator mRotateAnimator, mScaleAnimator, mInfoAnimator;

		/**
		 * Creates a new DrawThread that will manage drawing the PieChartView onto
		 * the SurfaceView
		 * 
		 * @param surfaceHolder the surfaceHolder to draw to
		 */
		public DrawThread(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
			isRunning = false;
		}

		public void setRunning(boolean run) {
			isRunning = run;
		}
		
		public boolean isRunning() {
			return isRunning;
		}
		
		public boolean isPaused() {
			return mPaused;
		}
		
		public void setRotateAnimator(ThreadAnimator mRotateAnimator) {
			this.mRotateAnimator = mRotateAnimator;
			mRotateAnimator.start();
		}

		public void setScaleAnimator(ThreadAnimator mScaleAnimator) {
			this.mScaleAnimator = mScaleAnimator;
			mScaleAnimator.start();
		}

		public void setInfoAnimator(ThreadAnimator mInfoAnimator) {
			this.mInfoAnimator = mInfoAnimator;
			mInfoAnimator.start();
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
			
			Canvas canvas;
			
			while (isRunning) {

				// If there are no PieSliceDrawables and we have an
				// adapter create the necessary slices, the drawing
				// cache and snap to the closest position
				if (mDrawables.size() == 0 && mAdapter != null) {
					
					addPieSlices();
					buildDrawingCache();
					snapTo();
				}
				
				// If the drawing cache is null build it
				if (mDrawingCache == null) {
					buildDrawingCache();
				}
				
				canvas = null;
				
				try {
					
					canvas = surfaceHolder.lockCanvas(null);
					
					synchronized (surfaceHolder) {
						
						if (canvas != null) {
							
							// Update our animator objects
							updateAnimators();
							
							// Clear the canvas
							canvas.drawColor(0, PorterDuff.Mode.CLEAR);
							
							// Draw the PieChart
					    	doDraw(canvas, mRotationDegree, mChartScale, mShowInfo);
						}
					}
					
				} finally {
					
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
				
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

			if (mScaleAnimator != null && mScaleAnimator.isRunning()) {
				mChartScale = mScaleAnimator.floatUpdate();
			}

			if (mInfoAnimator != null && mInfoAnimator.isRunning()) {
				mInfoAlpha = mInfoAnimator.intUpdate();
			}
		}
		
		/**
		 * Clear the canvas upon termination of the thread
		 */
		private void cleanUp() {
			
			Canvas canvas = null;
			
			try {
				
				canvas = surfaceHolder.lockCanvas(null);
				
				synchronized (surfaceHolder) {
					if (canvas != null) {
						canvas.drawColor(0, PorterDuff.Mode.CLEAR);
					}
				}
				
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		/**
		 * Creates a drawing cache to aid in click selection
		 */
		private void buildDrawingCache() {
			
			if (mDrawingCache == null) {
				mDrawingCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			}
			
			Canvas cache = new Canvas(mDrawingCache);
	    	doDraw(cache, mRotationDegree, mChartScale, mShowInfo);
		}
		
		/**
		 * Draw the PieChart to a canvas, making sure that
		 * all slices have been added and the chart has snapped
		 * to its anchor.
		 * 
		 * @param canvas the canvas to draw to
		 */
		public void drawCache(Canvas canvas) {

			if (mDrawables.size() == 0 && mAdapter != null) {
				
				addPieSlices();
				snapTo(false);
			}
			
			doDraw(canvas);
		}
		
		/**
		 * Draw to the supplied canvas using the current state of the
		 * rotation, scale and info panel variables.
		 * 
		 * @param canvas the canvas to draw to
		 */
		public void doDraw(Canvas canvas) {
			doDraw(canvas, mRotationDegree, mChartScale, mShowInfo);
		}
		
		/**
		 * Draw the pie chart
		 * 
		 * @param canvas The canvas to draw the chart on
		 * @param rotationDegree The current rotation of the chart
		 * @param scale The scale of the chart
		 * @param showInfo Should the info panel be drawn
		 */
		private void doDraw(Canvas canvas, float rotationDegree, float scale, boolean showInfo) {
			
			if (canvas == null || mAdapter == null) return;
			
			if (scale != 0) {
			
				// Scale and rotate the canvas
				canvas.save();
				canvas.scale(scale, scale, mCenter.x, mCenter.y);
				canvas.rotate(rotationDegree, mCenter.x, mCenter.y);
		    	canvas.translate(getPaddingLeft(), getPaddingTop());
		    	
		    	// Draw a background circle
				canvas.drawCircle(mCenter.x, mCenter.y, getChartRadius() + mStrokeWidth, mPaint);
		    	
				// Draw all of the pie slices
				synchronized (mDrawables) {
			        for (PieSliceDrawable slice : mDrawables) {
			        	slice.draw(canvas);
			        }
				}
		        
		        canvas.restore();
			}
	        
			// Draw the info panel if it has been set to show
	        if (showInfo) {
	        	
				createCaret();
				createInfo();
	
		        canvas.drawCircle(mCenter.x, mCenter.y, mInfoRadius, mStrokePaint);
		        mCaret.draw(canvas);
		        canvas.drawCircle(mCenter.x, mCenter.y, mInfoRadius, mPaint);
		        
		        mInfoDrawable.setAlpha(mInfoAlpha);
		        mInfoDrawable.draw(canvas);
	        }
		}
	}
	
	/**
	 * Interfaces Used
	 */
	
	public interface OnPieChartExpandListener {
		public void onPieChartExpanded();
		public void onPieChartCollapsed();
	}
	
	public interface OnPieChartChangeListener {
		public void onSelectionChanged(int index);
	}
	
	public interface OnItemClickListener {
		public void onItemClick(boolean secondTap, View parent, Drawable drawable, int position, long id);
	}
	
	public interface OnInfoClickListener {
		public void onInfoClicked(int index);
	}
	
	public interface OnItemLongClickListener {
		public void onItemLongClick(View parent, Drawable drawable, int position, long id);
	}
	
	public interface OnRotationStateChangeListener {
		public void onRotationStateChange(int state);
	}
	
	public interface OnPieChartReadyListener {
		public void onPieChartReady();
	}
	
	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged() {

			if (mChartScale != 0f) {
				mNeedsUpdate = true;
				return;
			}
			
        	resetChart();
			
			// Detect the case where a cursor that was previously invalidated
			// has been re-populated with new data.
			if (PieChartView.this.getPieChartAdapter().hasStableIds() && mInstanceState != null) {
				
				PieChartView.this.onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			}
		}

		@Override
		public void onInvalidated() {

			if (PieChartView.this.getPieChartAdapter().hasStableIds()) {
				
				// Remember the current state for the case where our hosting
				// activity is being stopped and later restarted
				mInstanceState = PieChartView.this.onSaveInstanceState();
			}
			
			if (mChartScale != 0f) {
				mNeedsUpdate = true;
				return;
			}
			
			resetChart();
		}

		public void clearSavedState() {
			mInstanceState = null;
		}
	}
}
