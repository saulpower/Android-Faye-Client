package com.moneydesktop.finance.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.model.PointEvaluator;
import com.moneydesktop.finance.util.Enums.NavDirection;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

@TargetApi(11)
public class NavWheelView extends View {
	
	public final String TAG = this.getClass().getSimpleName();

	private final float RADIUS = 200;
	
	private PointF mCenter;
	private List<NavItemDrawable> mDrawables;
	private List<Integer> mItems;
	
	private float mLastTouchY, mLastTouchX;
	private PointF mDistance;

	private Paint mBg;

	private PointerDrawable mPointer;

	private boolean mRotating = false;
	private boolean mShowing = false;

    private boolean mShouldHide = false;
	
	private double mDegreeChange = 0.0;
	private int mCurrentIndex = 0;
	private int mLastIndex = 0;
	
	// Trigger timer that the exit animations have completed
	private Handler mHandler = new Handler();
	private Runnable mTask = new Runnable() {
		
		@Override
		public void run() {
			
			setVisibility(View.GONE);
		}
	};

	private onNavigationChangeListener listener;
	
	public int getMAlpha() {
		return mBg.getAlpha();
	}

	public void setMAlpha(int mAlpha) {
		mBg.setAlpha(mAlpha);
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

	/**
	 * Sets the currently selected index item and rotates
	 * the cursor to point at that item.
	 * 
	 * @param currentIndex
	 */
	public void setCurrentIndex(int currentIndex) {
		
		if (mRotating || this.mCurrentIndex == currentIndex)
			return;
		
		this.mCurrentIndex = currentIndex;
		rotatePointer();
	}
	
	public void setOnNavigationChangeListener(onNavigationChangeListener listener) {
		this.listener = listener;
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
        
		float[] size = UiUtils.getScreenMeasurements(context);
		mCenter = new PointF((size[0] / 2.0f), (size[1] / 2.0f));
		
		mBg = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBg.setStyle(Paint.Style.FILL);
		mBg.setColor(context.getResources().getColor(R.color.transparent50percent));
		
		EventBus.getDefault().register(this);
	}
	
	/**
	 * Once a list of navigation items have been passed in they are converted
	 * to drawable items so that can be laid out correctly and manipulated
	 * according to our needs.
	 */
	private void initializeItems() {

		mPointer = new PointerDrawable(getContext(), mCenter, mItems.size());
		mPointer.setCallback(this);
		
		mDrawables = new ArrayList<NavItemDrawable>();
		mDrawables.add(mPointer);

		mDegreeChange = 360.0 / (double) mItems.size();
		
		float radiusDp = UiUtils.getDynamicPixels(getContext(), RADIUS);
		
		for (int i = 0; i < mItems.size(); i++) {
			
			// Determine the x, y position of the item at the given index so all
			// are distributed equally in a circular pattern
			double radians = Math.toRadians(((mDegreeChange * (double) i) + 90.0) % 360.0);
			
			float x = (float) (radiusDp * Math.cos(radians)) * -1;
			float y = (float) (radiusDp * Math.sin(radians)) * -1;
			
			x += mCenter.x;
			y += mCenter.y;
			
			PointF position = new PointF(x, y);
			
			// Create the drawable and add it to our array
			NavItemDrawable mItem = new NavItemDrawable(getContext(), mItems.get(i), i, position, mCenter);
			mItem.setCallback(this);
			mDrawables.add(mItem);
		}
		
		// Initial draw of the items
		invalidate();
	}
	
	/**
	 * Rotate the pointer to point at the correct index.  Adjustments are
	 * made so the pointer rotates in the correct direction to move to the
	 * current index by the most efficient and logical route.
	 */
	private void rotatePointer() {

        mRotating = true;
        
		// Degree position of the index
		float rotateTo = (float) (mCurrentIndex * mDegreeChange);
		
		// Determine if we need to rotate outside of the established pattern
		final boolean change = Math.abs(mLastIndex - mCurrentIndex) > (mItems.size() / 2.0f);
		
		// Make sure we rotate the quickest way to the selected index
		if (change)
			rotateTo = rotateTo + Math.signum(mLastIndex - mCurrentIndex) * 360;
		
		// Animate to the correct rotation
		ObjectAnimator rotate = ObjectAnimator.ofFloat(mPointer, "rotation", rotateTo);
		rotate.setDuration(200);
		rotate.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				
				// Adjustment for irregular movement
				if (change)
					mPointer.fixRotation((float) (mCurrentIndex * mDegreeChange));
				
				mRotating = false;
				
				if (mShouldHide) {
				    mShouldHide = false;
				    hideNav();
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				mRotating = false;
			}
		});
		
		AnimatorSet set = new AnimatorSet();
		set.play(rotate).with(growIcon());
		set.start();
		
		// Update the last selected index
		mLastIndex = mCurrentIndex;
	}
	
	/**
	 * Animates the grow/shrink effect of an icon when
	 * the pointer is pointing at the given index.
	 * 
	 * @return
	 */
	private ObjectAnimator growIcon() {
		
		NavItemDrawable item = mDrawables.get(mCurrentIndex + 1);
		
		PointF orig = new PointF(1.0f, 1.0f);
		PointF bigger = new PointF(1.3f, 1.3f);
		
		ObjectAnimator pop = ObjectAnimator.ofObject(item, "scale", new PointEvaluator(), orig, bigger, orig);
		pop.setDuration(250);
		
		return pop;
	}
	
	/**
	 * Check among all of our drawables to see if we are touching
	 * within the bounds of any of the items.
	 * 
	 * @return
	 */
	private boolean itemTouchCheck() {

		for (NavItemDrawable item : mDrawables) {
		    
        	if (item.getBounds().contains((int) mLastTouchX, (int) mLastTouchY)) {
        	
        		setCurrentIndex(item.getIndex());
        		return true;
        	}
        }

		return false;
	}
	
	/**
	 * Show the navigation wheel on screen with the appropriate animations
	 */
	public void showNav() {
		
	    mShowing = true;
	    
	    mHandler.removeCallbacks(mTask);
	    
		setVisibility(View.VISIBLE);
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "mAlpha", 0, 255);
		fade.setDuration(250);
		fade.start();
		
		for (NavItemDrawable item : mDrawables)
			item.playIntro();
		
		// Notify the system the navigation wheel is showing
		EventBus.getDefault().post(new EventMessage().new NavigationEvent(true));
	}
	
	/**
	 * Hide the navigation wheel from the user with the appropriate animations
	 */
	public void hideNav() {

        mShowing = false;
		
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "mAlpha", 255, 0);
		fade.setDuration(250);
		fade.start();
		
		for (NavItemDrawable item : mDrawables)
			item.playOutro(mCurrentIndex);
		
		// Timer to handle hide animations have completed
		mHandler.postDelayed(mTask, 1200);
		
		// Notify the system the navigation is no longer showing
		EventBus.getDefault().post(new EventMessage().new NavigationEvent(false));
		
		// Update any listener of the index change
		if (listener != null) {
			listener.onNavigationChanged(mCurrentIndex);
		}
	}
	
	/**
	 * Receives notifications when the item index should be
	 * moved to the next or previous index.
	 * 
	 * @param event
	 */
	public void onEvent(NavigationEvent event) {
		
		if (event.getDirection() != null) {
	    	
			if (event.getDirection() == NavDirection.NEXT)
				toNext();
			else
				toPrevious();
		}
	}
	
	/**
	 * Moves to the next index
	 */
	private void toNext() {
		
		int index = mCurrentIndex + 1;
		
		if (index == mItems.size())
			index = 0;
		
		setCurrentIndex(index);
	}
	
	/**
	 * Moves to the previous index
	 */
	private void toPrevious() {

		int index = mCurrentIndex - 1;
		
		if (index < 0)
			index = mItems.size() - 1;
		
		setCurrentIndex(index);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent ev) {

	    if (!mShowing) {
	        return false;
	    }
	    
        final int action = ev.getAction();
        
        switch (action) {
        
	        case MotionEvent.ACTION_DOWN: {
	        	
	            mLastTouchY = ev.getY();
	            mLastTouchX = ev.getX();
	        	
	        	mDistance = new PointF(0.0f, 0.0f);
	            
	            break;
	        }
            
            case MotionEvent.ACTION_UP: {

                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();

                // Touched to dismiss navigation
                if (mDistance.x <= 10.0f && mDistance.y <= 10.0f) {
                    
                    mShouldHide = true;
                    
                    if (!itemTouchCheck() || (mShowing && !mRotating)) {
                        mShouldHide = false;
                        hideNav();
                    }
                    
                    return true;
                }
                
                break;
            }
	            
	        case MotionEvent.ACTION_MOVE: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Remember this touch position for the next move event
	            final float dy = y - mLastTouchY;
	            final float dx = x - mLastTouchX;

	            mDistance.x += Math.abs(dx);
	            mDistance.y += Math.abs(dy);
	            
	            // Remember this touch position for the next move event
	            mLastTouchY = y;
	            mLastTouchX = x;
	            
	            break;
	        }
        }
        
        itemTouchCheck();
        
        return true;
    }
	
    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
    	int width = MeasureSpec.getSize(widthMeasureSpec);
    	int height = MeasureSpec.getSize(heightMeasureSpec);
    	
        setMeasuredDimension(width, height);
    }

	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		// Draw the pointer and background
		c.drawRect(0, 0, getWidth(), getHeight(), mBg);
		mPointer.draw(c);
		
		// Draw all of the navigation items
		for (NavItemDrawable item : mDrawables)
			item.draw(c);
	}
	
	@Override
	public void invalidateDrawable(Drawable who) {
		super.invalidateDrawable(who);
		
		invalidate();
	}

	public interface onNavigationChangeListener {
		public void onNavigationChanged(int index);
	}
}