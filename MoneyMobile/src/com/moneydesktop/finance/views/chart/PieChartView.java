package com.moneydesktop.finance.views.chart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.Dynamics;
import com.nineoldandroids.animation.ObjectAnimator;

public class PieChartView extends AdapterView<Adapter> {
    
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

    /** Unit used for the velocity tracker */
    private static final int PIXELS_PER_SECOND = 1000;

    /** Tolerance for the velocity */
    private static final float VELOCITY_TOLERANCE = 10f;

    /** Represents an invalid child index */
    private static final int INVALID_INDEX = -1;
	
	/** User is not touching the list */
    private static final int TOUCH_STATE_RESTING = 0;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    /** User is scrolling the list */
    private static final int TOUCH_STATE_SCROLL = 2;
    
    /** Default degree to snap to */
    private static final float DEFAULT_SNAP_DEGREE = 0f;

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
    
    /** The pixel density of the current device */
    private float mPixelDensity;
    
    /** The center point of the chart */
    private PointF mCenter = new PointF();
    
    /** The current degrees of rotation of the chart */
	private float mRotationDegree = 0;
	
	/** The current snapped-to index */
	private int mCurrentIndex;
	
	Bitmap mSource;

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
     * Set the rotation degree of the chart.
     * 
     * @param rotationDegree the degree to rotate the chart to
     */
	void setRotationDegree(float rotationDegree) {
		
		// Keep rotation degree positive
		if (rotationDegree < 0) rotationDegree += 360;
		
		// Keep rotation degree between 0 - 360
		mRotationDegree = rotationDegree % 360;
		
		invalidate();
	}
	
	public float getRotationDegree() {
		return mRotationDegree;
	}
	
	public int getCurrentIndex() {
		return mCurrentIndex;
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

	public PieChartView(Context context) {
		this(context, null);
	}

	public PieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 1);
	}

	public PieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mScrollThreshold = ViewConfiguration.get(context).getScaledTouchSlop();
		mPixelDensity = UiUtils.getDisplayMetrics(context).density;
		
		setDrawingCacheEnabled(true);
	}

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
    	
    	if (!inCircle((int) event.getX(), (int) event.getY()) && mTouchState == TOUCH_STATE_RESTING) return false;
    	
        switch (event.getAction()) {
        
            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                return false;

            case MotionEvent.ACTION_MOVE:
                return startScrollIfNeeded(event);

            default:
                endTouch(event.getX(), event.getY(), 0);
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
    	
        if (getChildCount() == 0 || 
        		(!inCircle((int) event.getX(), (int) event.getY()) && 
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
                
                if (mTouchState == TOUCH_STATE_SCROLL) {
                    mVelocityTracker.addMovement(event);
                    rotateChart(event.getX(), event.getY());
                }
                
                break;

            case MotionEvent.ACTION_UP:
            	
            	float velocity = 0;
            	
                if (mTouchState == TOUCH_STATE_CLICK) {
                	
                    clickChildAt((int) event.getX(), (int) event.getY());
                    
                } else if (mTouchState == TOUCH_STATE_SCROLL) {
                	
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
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		// if we don't have an adapter, we don't need to do anything
        if (mAdapter == null) {
            return;
        }
        
        if (changed) {

			final int itemHeight = (int) (getHeight() / 2);
	        final int itemWidth = (int) (getWidth() / 2);
	        
	        boolean useHeight = itemHeight < itemWidth;
	        mChartDiameter = useHeight ? (getHeight() - (getPaddingTop() + getPaddingBottom()))
	        		: (getWidth() - (getPaddingLeft() + getPaddingRight()));
	        
			// Get the center coordinates of the view
			mCenter.x = (float) itemWidth;
			mCenter.y = (float) itemHeight;
			
			invalidate();
        }
		
		if (getChildCount() == 0) {
			
			addPieSlices();
			snapTo();
		}
        
    	buildDrawingCache();
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
        mTouchState = TOUCH_STATE_CLICK;
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
                        postDelayed(this, 16);
                        
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
        mTouchState = TOUCH_STATE_RESTING;
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
            
            mTouchState = TOUCH_STATE_SCROLL;
            
            mRotationStart = (float) Math.toDegrees(Math.atan2(mCenter.y - yPos, mCenter.x - xPos));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Snaps the chart rotation to a given snap degree
     */
    private void snapTo() {
    	
    	for (int index = 0; index < getChildCount(); index++) {
        	
            final View childView = getChildAt(index);
            
            if (childView instanceof PieSliceView && ((PieSliceView) childView).containsDegree(mRotationDegree, mSnapToDegree)) {
            	
            	animateTo((PieSliceView) childView, index);
            	
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
    	animateTo(null, index);
    }
    
    /**
     * Animates the pie chart's rotation to a specific degree
     * 
     * @param slice the PieSliceView to rotate to
     * @param index the index of the PieSliceView
     */
    private void animateTo(PieSliceView slice, int index) {
    	
    	// Update current index
    	mCurrentIndex = index;
    	
    	if (slice == null) {
    		slice = (PieSliceView) getChildAt(index);
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
    	
    	ObjectAnimator rotate = ObjectAnimator.ofFloat(this, "rotationDegree", start, degree);
    	rotate.setDuration(300);
    	rotate.start();
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
     * Returns the index of the child that contains the coordinates given.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The index of the child that contains the coordinates. If no child
     *         is found then it returns INVALID_INDEX
     */
    private int getContainingChildIndex(final int x, final int y) {
    	
    	if (!inCircle(x, y)) return INVALID_INDEX;
    	
        final Bitmap viewBitmap = getDrawingCache();
        
        // Rotate the drawing cache bitmap so it reflects the current state of the chart
        if (mSource == null) {
        	mSource = Bitmap.createBitmap(viewBitmap.getWidth(), viewBitmap.getHeight(), viewBitmap.getConfig());
        }
        
    	Canvas canvas = new Canvas(mSource);
        Matrix matrix = new Matrix();
        matrix.postRotate(mRotationDegree, viewBitmap.getWidth()/2, viewBitmap.getHeight()/2);
        canvas.drawBitmap(viewBitmap, matrix, null);
        
        // Grab the color pixel at the point touched and compare it with the children
        int pixel = mSource.getPixel(x, y);
        
        for (int index = 0; index < getChildCount(); index++) {
        	
            final View childView = getChildAt(index);
            
            if (childView instanceof PieSliceView && ((PieSliceView) childView).getSliceColor() == pixel) {
                return index;
            }
        }
        
        return INVALID_INDEX;
    }
    
    private boolean inCircle(final int x, final int y) {
    	
        double dx = (x - mCenter.x) * (x - mCenter.x);
        double dy = (y - mCenter.y) * (y - mCenter.y);

        if ((dx + dy) < ((mChartDiameter / 2) * (mChartDiameter / 2))) {
            return true;
        } else {
            return false;
        }
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
        
        if (index != INVALID_INDEX) {
        	
            final PieSliceView sliceView = (PieSliceView) getChildAt(index);
            final long id = mAdapter.getItemId(index);
            
            if (getCurrentIndex() != index) {
            	animateTo(sliceView, index);
            }
            
            performItemClick(sliceView, index, id);
        }
    }

    /**
     * Calls the item long click listener for the child with the specified index
     * 
     * @param index Child index
     */
    private void longClickChild(final int index) {
    	
        final View itemView = getChildAt(index);
        final long id = mAdapter.getItemId(index);
        final OnItemLongClickListener listener = getOnItemLongClickListener();
        
        if (listener != null) {
            listener.onItemLongClick(this, itemView, index, id);
        }
    }

    /**
     * Starts at 0 degrees and adds each pie slice
     */
    private void addPieSlices() {
    	
    	float offset = 0;
    	
        int left = (getWidth() - mChartDiameter) / 2;
        int top = (getHeight() - mChartDiameter) / 2;
    	
        for (int index = 0; index < mAdapter.getCount(); index++) {
            
            final PieSliceView childSlice = mAdapter.getSlice(index, offset, this);
            
            addAndMeasureChild(childSlice, index);
            childSlice.layout(left, top, left + mChartDiameter, top + mChartDiameter);
            
            offset += childSlice.getDegrees();
        }
    }

    /**
     * Adds a view as a child view and takes care of measuring it
     * 
     * @param child The view to add
     * @param index The index of the child to be added
     */
    private void addAndMeasureChild(final View child, final int index) {
    	
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        
//        child.setDrawingCacheEnabled(true);
        addViewInLayout(child, index, params, false);
        
        child.measure(MeasureSpec.EXACTLY | mChartDiameter, MeasureSpec.EXACTLY | mChartDiameter);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
    	
    	canvas.save();
		canvas.rotate(mRotationDegree, getWidth() / 2, getHeight() / 2);

        super.dispatchDraw(canvas);
    	
        canvas.restore();
    }

	@Override
	public Adapter getAdapter() {
		throw new RuntimeException(
				"For PieChart, use getPieChartAdapter() instead of "
						+ "getAdapter()");
	}

	@Override
	public View getSelectedView() {
        throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void setAdapter(Adapter adapter) {
		throw new RuntimeException(
				"For PieChart, use setAdapter(PieChartAdapter) instead of "
						+ "setAdapter(Adapter)");
	}
	
	public BasePieChartAdapter getPieChartAdapter() {
		return mAdapter;
	}

	public void setAdapter(BasePieChartAdapter adapter) {
		
		if (!validAdapter(adapter)) {
			Log.e(TAG, "PieChart adapter items must sum to 1");
			return;
		}
		
		mAdapter = adapter;
        removeAllViewsInLayout();
        requestLayout();
	}
	
	private boolean validAdapter(BasePieChartAdapter adapter) {
		
		float total = 0;
		
		for (int i = 0; i < adapter.getCount(); i++) {
			total += adapter.getPercent(i);
		}
		
		return total == 1;
	}

	@Override
	public void setSelection(int position) {
        animateTo(position);
	}
	
	public interface OnPieChartChangeListener {
		public void onSelectionChanged(int index);
	}
}
