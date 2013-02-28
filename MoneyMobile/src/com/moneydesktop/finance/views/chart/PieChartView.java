package com.moneydesktop.finance.views.chart;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;

import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.CaretDrawable;
import com.moneydesktop.finance.views.Dynamics;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

public class PieChartView extends SurfaceView implements SurfaceHolder.Callback {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private static final String MESSAGE = "message";

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
    
    private enum MessageType {
    	CACHE_READY,
    	SNAP_TO
    };
    
    private static final int STROKE_WIDTH = 3;

    /** Unit used for the velocity tracker */
    private static final int PIXELS_PER_SECOND = 1000;

    /** Tolerance for the velocity */
    private static final float VELOCITY_TOLERANCE = 40f;

    /** Represents an invalid child index */
    private static final int INVALID_INDEX = -1;

    /** Represents a touch to the info circle */
    private static final int INFO_INDEX = -2;
	
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
	
	private float mInfoRadius;
    
    /** The pixel density of the current device */
    private float mPixelDensity;
    
    /** The center point of the chart */
    private PointF mCenter = new PointF();
    
    /** The current degrees of rotation of the chart */
	private float mRotationDegree = 0;
	
	/** The current snapped-to index */
	private int mCurrentIndex;
	
	private List<PieSliceDrawable> mDrawables;
	
	private Bitmap mSource;
	
	private OnItemLongClickListener mOnItemLongClickListener;
	
	private DrawThread mDrawThread;

    /**
     * The listener that receives notifications when an item is clicked.
     */
    private OnItemClickListener mOnItemClickListener;
	
	private Bitmap mDrawingCache, mCoverCache;
	
	private MessageHandler mHandler;
	
	private ImageView mCoverView;
	
	private Paint mPaint;
	private Paint mStrokePaint;
	private CaretDrawable mCaret;
	
	public Bitmap getDrawingCache() {
		return mDrawingCache;
	}
	
	public Bitmap getCoverCache() {
		return mCoverCache;
	}
	
	public void setCoverView(ImageView coverView) {
		mCoverView = coverView;
	}
	
	public ImageView getCoverView() {
		return mCoverView;
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
		return mCurrentIndex;
	}
	
	public void setCurrentIndex(int index) {
		mCurrentIndex = index;
		mCoverCache = null;
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
	
	public void setSelection(int index) {
		animateTo(index);
	}
	
	public DrawThread getDrawThread() {
		return mDrawThread;
	}

	public PieChartView(Context context) {
		this(context, null);
	}

	public PieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 1);
	}

	public PieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mHandler = new MessageHandler(this);
		
        getHolder().addCallback(this);
		
		setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        
        mDrawThread = new DrawThread(getHolder(), mHandler);
		
		mScrollThreshold = ViewConfiguration.get(context).getScaledTouchSlop();
		mPixelDensity = UiUtils.getDisplayMetrics(context).density;
		
		mDrawables = new ArrayList<PieSliceDrawable>();
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.WHITE);
		
		mStrokePaint = new Paint(mPaint);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(UiUtils.getDynamicPixels(context, STROKE_WIDTH));
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
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
        
        boolean useHeight = height < width;
        mChartDiameter = useHeight ? (height - (getPaddingTop() + getPaddingBottom()))
        		: (width - (getPaddingLeft() + getPaddingRight()));
		
		mInfoRadius = getChartRadius() * 2 / 5;
		
        int size = useHeight ? height : width;
        
		setMeasuredDimension(size, size);
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
            
			// Get the center coordinates of the view
			mCenter.x = (float) itemWidth;
			mCenter.y = (float) itemHeight;
        }
	}

    /**
     * Starts at 0 degrees and adds each pie slice
     */
    private void addPieSlices() {
    	
    	mDrawables.clear();
    	
    	float offset = 0;
    	
        int left = getLeft() + getPaddingLeft();
        int top = getTop() + getPaddingTop();
    	
        for (int index = 0; index < mAdapter.getCount(); index++) {
            
            final PieSliceDrawable childSlice = mAdapter.getSlice(this, index, offset);
            
            childSlice.setBounds(left, top, left + mChartDiameter, top + mChartDiameter);
            mDrawables.add(childSlice);
            
            offset += childSlice.getDegrees();
        }
    }
	
	private void createCaret() {
		
		if (mCaret == null) {
			PointF position = new PointF(mCenter.x - mInfoRadius / 2, mCenter.y + mInfoRadius / 3);
	        mCaret = new CaretDrawable(getContext(), position, mInfoRadius, mInfoRadius);
	        mCaret.setColor(Color.WHITE);
		}
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
    	
    	for (int index = 0; index < mDrawables.size(); index++) {
        	
            final PieSliceDrawable slice = mDrawables.get(index);
            
            if (slice.containsDegree(mRotationDegree, mSnapToDegree)) {
            	
            	animateTo(slice, index);
            	
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
    private void animateTo(PieSliceDrawable slice, final int index) {
    	
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
    	
    	ObjectAnimator rotate = ObjectAnimator.ofFloat(this, "rotationDegree", start, degree);
    	rotate.setDuration(300);
    	rotate.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				setCurrentIndex(index);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
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
    	
    	if (inInfoCircle(x, y)) return INFO_INDEX;
    	
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
        
        for (int index = 0; index < mDrawables.size(); index++) {
        	
            final PieSliceDrawable slice = mDrawables.get(index);
            
            if (slice.getSliceColor() == pixel) {
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
    
    private boolean inInfoCircle(final int x, final int y) {
    	
        double dx = (x - mCenter.x) * (x - mCenter.x);
        double dy = (y - mCenter.y) * (y - mCenter.y);

        if ((dx + dy) < (mInfoRadius * mInfoRadius)) {
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
        
        if (index == INFO_INDEX) {
        	
        	Log.i(TAG, "Info clicked");
        	
        } else if (index != INVALID_INDEX) {
        	
            final PieSliceDrawable sliceView = mDrawables.get(index);
            final long id = mAdapter.getItemId(index);
            
            if (getCurrentIndex() != index) {
            	animateTo(sliceView, index);
            }
            
            performItemClick(sliceView, index, id);
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
    public boolean performItemClick(PieSliceDrawable view, int position, long id) {
    	
        if (mOnItemClickListener != null) {
        	
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnItemClickListener.onItemClick(this, view, position, id);
            
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
	
	public BasePieChartAdapter getPieChartAdapter() {
		return mAdapter;
	}

	public void setAdapter(BasePieChartAdapter adapter) {
		
		if (!validAdapter(adapter)) {
			Log.e(TAG, "PieChart adapter items must sum to 1");
			return;
		}
		
		mAdapter = adapter;
	}
	
	private boolean validAdapter(BasePieChartAdapter adapter) {
		
		float total = 0;
		
		for (int i = 0; i < adapter.getCount(); i++) {
			total += adapter.getPercent(i);
		}
		
		return total % 1f < 0.0001f;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		
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
		Log.i(TAG, "surfaceDestroyed");
		
		boolean retry = true;
		
		mDrawThread.setRunning(false);
		
		while (retry) {
			try {
				mDrawThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}
	
	public interface OnPieChartChangeListener {
		public void onSelectionChanged(int index);
	}
	
	public interface OnItemClickListener {
		public void onItemClick(View parent, Drawable drawable, int position, long id);
	}
	
	public interface OnItemLongClickListener {
		public void onItemLongClick(View parent, Drawable drawable, int position, long id);
	}
	
	static class MessageHandler extends Handler {
		
		private final WeakReference<PieChartView> mPieChartView; 

		MessageHandler(PieChartView pieChartView) {
	    	mPieChartView = new WeakReference<PieChartView>(pieChartView);
	    }
	    
		@Override
        public void handleMessage(Message m) {

    		MessageType type = (MessageType) m.getData().getSerializable(MESSAGE);
    		
    		PieChartView chart = mPieChartView.get();
    		
    		switch (type) {
	    		case SNAP_TO:
	    			chart.snapTo();
	    			break;
	    		case CACHE_READY:
	    			chart.mCoverView.setImageBitmap(chart.getCoverCache());
	    			break;
    		}
        }
	}
	
	protected class DrawThread extends Thread {

        /** Message handler used by thread to interact with UI */
        private Handler mHandler;
		
		private SurfaceHolder surfaceHolder;
		private boolean isRunning;

		public DrawThread(SurfaceHolder surfaceHolder, Handler handler) {
			this.surfaceHolder = surfaceHolder;
            mHandler = handler;
			isRunning = false;
		}

		public void setRunning(boolean run) {
			isRunning = run;
		}
		
		public boolean isRunning() {
			return isRunning;
		}
		
		@Override
		public void run() {
			
			Log.i(TAG, "Drawing started");
			
			Canvas c;
			
			while (isRunning) {

				if (mDrawables.size() == 0 && mAdapter != null) {
					addPieSlices();

					sendMessage(MessageType.SNAP_TO);
				}
				
				if (mDrawingCache == null) {
					createDrawingCache();
				}
				
				if (mCoverCache == null) {
					createCoverCache();
				}
				
				c = null;
				
				try {
					
					c = surfaceHolder.lockCanvas(null);
					
					synchronized (surfaceHolder) {
				    	doDraw(c);
					}
					
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
		
		private void createDrawingCache() {
			
			if (mDrawingCache == null) {
				mDrawingCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			}
			
			Canvas cache = new Canvas(mDrawingCache);
			doDraw(cache);
		}
		
		private void createCoverCache() {
			
			if (mCoverCache == null) {
				mCoverCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			}
			
			Canvas cache = new Canvas(mCoverCache);
			doDraw(cache);
			
			sendMessage(MessageType.CACHE_READY);
		}
		
		private void sendMessage(MessageType type) {

            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putSerializable(MESSAGE, type);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
		}
		
		private void doDraw(Canvas canvas) {
			
			if (canvas == null) return;
			
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			
			canvas.save();
			canvas.rotate(mRotationDegree, mCenter.x, mCenter.y);
	    	canvas.translate(getPaddingLeft(), getPaddingTop());
	    	
	        for (PieSliceDrawable slice : mDrawables) {
	        	slice.draw(canvas);
	        }
	        
	        canvas.restore();
	        
			createCaret();

	        canvas.drawCircle(mCenter.x, mCenter.y, getChartRadius() * 2 / 5, mStrokePaint);
	        mCaret.draw(canvas);
	        canvas.drawCircle(mCenter.x, mCenter.y, getChartRadius() * 2 / 5, mPaint);
		}
	}
}
