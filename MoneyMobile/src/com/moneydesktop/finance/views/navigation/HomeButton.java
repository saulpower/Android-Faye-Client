package com.moneydesktop.finance.views.navigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.Dynamics;
import com.moneydesktop.finance.views.FrictionDynamics;
import com.moneydesktop.finance.views.piechart.ThreadAnimator;

public class HomeButton extends View {
	
	public final String TAG = this.getClass().getSimpleName();

    /** Unit used for the velocity tracker */
    private static final int PIXELS_PER_SECOND = 1000;

    /** Tolerance for the velocity */
    private static final float VELOCITY_TOLERANCE = 40f;

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

    private NavWheelView mNavWheel;
    private float mStartDegree = 0;

	// Animation
	private static final long DURATION = 500;
	
	// Button
	private Bitmap mHome;
	private int mColor = Color.BLUE, mSecondaryColor = Color.CYAN;
	private Paint mPaint, mStroke;
	private float mRadius;
	
	// Slider
	private Paint mPaintSlider, mStrokeSlider;
	private boolean mShowSlider = false;
    private float mSliderRadius = 0;
    private float mSliderMaxRadius = 30;

	// Positioning
	private int mLeft, mTop, mMid, mCenter;
	private float mWidth = 10;
	private float mHeight = 10;

    private float mPosY;
    
    private float mLastTouchY, mLastTouchX;

    /** X-coordinate of the down event */
    private int mTouchStartX;

    /** Y-coordinate of the down event */
    private int mTouchStartY;

    // Trigger timer that the exit animations have completed
    private Handler mHandler;

    /** Velocity tracker used to get fling velocities */
    private VelocityTracker mVelocityTracker;

    /** Dynamics object used to handle fling and snap */
    private Dynamics mDynamics;

    /** Runnable used to animate fling and snap */
    private Runnable mDynamicsRunnable;

    /** The pixel density of the current device */
    private float mPixelDensity;
    
    // Scrolling
    private float mMoveDistance, mStartPos;
    
	// Touching
	private boolean mTouching = false, mTouchingSlider = false;

	/*******************************************************************************
	 * Accessory Methods
	 *******************************************************************************/
	
	public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    /**
	 * Setter used to show or hide the outer slider button.
	 * 
	 * @param showSlider
	 */
	public void setShowSlider(boolean showSlider) {
		
		if (showSlider == mShowSlider) return;
		
		this.mShowSlider = showSlider;
		animateSlider(showSlider);
	}
	
	/**
	 * Setter used to dictate whether the home button is being
	 * touched by the user.  The button's size and paint are
	 * adjusted accordingly.
	 * 
	 * @param touching
	 */
	public void setTouching(boolean touching) {
		
		boolean wasTouching = this.mTouching;
		
		this.mTouching = touching && touchingButton();
    	
		if (this.mTouching || (wasTouching && !this.mTouching)) {
			animateButtonBounce();
		}
		
		configurePaint();
		configureButton();
	}
	
	/**
	 * Setter used to dictate whether the slider is being touched
	 * by the user, if it is present on screen.
	 * 
	 * @param touchingSlider
	 */
	public void setTouchingSlider(boolean touchingSlider) {
			
		this.mTouchingSlider = touchingSlider && touchingSlider() && mShowSlider;

        if (mTouchingSlider) {
            mNavWheel.updateStartRotation();
            mStartDegree = getDegree(mTouchStartX, mTouchStartY);
        }
	}

	/**
	 * Begins the animation of the home button and indicates
	 * an animation is occurring.
	 */
	public void animateButtonBounce() {

        mRadiusAnimator = ThreadAnimator.ofFloat(mTouching ? mWidth : mWidth + 15, mTouching ? mWidth + 15 : mWidth);
        mRadiusAnimator.setDuration(DURATION);
        mRadiusAnimator.setInterpolator(new BounceInterpolator());
        mRadiusAnimator.start();
	}

    /** Animator objects used to animate the rotation, scale, and info panel */
    private ThreadAnimator mAlphaSlider, mAlphaSliderStroke, mSliderRadiusAnimator, mRadiusAnimator, mPositionAnimator;

	/**
	 * Begins the animation of the slider and indicates
	 * an animation is occurring.
	 * 
	 * @param showSlider
	 */
	public void animateSlider(boolean showSlider) {

        mSliderRadiusAnimator = ThreadAnimator.ofFloat(showSlider ? 0 : mSliderMaxRadius, showSlider ? mSliderMaxRadius : 0);
        mSliderRadiusAnimator.setDuration(DURATION);
        mSliderRadiusAnimator.setInterpolator(new OvershootInterpolator());

        mAlphaSlider = ThreadAnimator.ofInt(showSlider ? 0 : 190, showSlider ? 190 : 0);
        mAlphaSlider.setDuration(DURATION);

        mAlphaSliderStroke = ThreadAnimator.ofInt(showSlider ? 0 : 255, showSlider ? 255 : 0);
        mAlphaSliderStroke.setDuration(DURATION);

        mSliderRadiusAnimator.start();
        mAlphaSlider.start();
        mAlphaSliderStroke.start();
	}

    /**
     * Update our animators that control animating the
     * rotation, scale, and info panel alpha
     */
    private void updateAnimators() {

        if (mPositionAnimator != null && mPositionAnimator.isRunning()) {
            setPosY(mPositionAnimator.floatUpdate());
        }

        if (mRadiusAnimator != null && mRadiusAnimator.isRunning()) {
            setRadius(mRadiusAnimator.floatUpdate());
        }

        if (mSliderRadiusAnimator != null && mSliderRadiusAnimator.isRunning()) {
            setSliderRadius(mSliderRadiusAnimator.floatUpdate());
        }

        if (mAlphaSlider != null && mAlphaSlider.isRunning()) {
            setAlphaSlider(mAlphaSlider.intUpdate());
        }

        if (mAlphaSliderStroke != null && mAlphaSliderStroke.isRunning()) {
            setAlphaSliderStroke(mAlphaSliderStroke.intUpdate());
        }
    }

	public void setColor(int color) {
		this.mColor = color;
		
		configurePaint();
	}

	public void setSecondaryColor(int secondaryColor) {
		this.mSecondaryColor = secondaryColor;
		
		configureSliderPaint();
	}

	public void setButtonWidth(float width) {
		mWidth = width;
		mRadius = mWidth;
		mSliderMaxRadius = mWidth * 3.0f;
	}
	
	public float getButtonWidth() {
		return mWidth;
	}

	public void setButtonHeight(float height) {
		this.mHeight = height;
	}
	
	public float getButtonHeight() {
		return mHeight;
	}

	public float getSliderRadius() {
        return mSliderRadius;
    }

    public void setSliderRadius(float sliderRadius) {
        this.mSliderRadius = sliderRadius;
    }

    public int getAlphaSlider() {
        return mPaintSlider.getAlpha();
    }

    public void setAlphaSlider(int alphaSlider) {
        mPaintSlider.setAlpha(alphaSlider);
    }

    public int getAlphaSliderStroke() {
        return mStrokeSlider.getAlpha();
    }

    public void setAlphaSliderStroke(int alphaSliderStroke) {
        mStrokeSlider.setAlpha(alphaSliderStroke);
    }
    
    public float getPosY() {
        return mPosY;
    }
    
    public void setPosY(float posY) {
        mPosY = posY;
    }

    /**
     * Set the dynamics object used for fling and snap behavior.
     *
     * @param dynamics The dynamics object
     */
    public void setDynamics(final Dynamics dynamics) {

        if (mDynamics != null) {
            dynamics.setState(mNavWheel.getRotationDegree(), mDynamics.getVelocity(), AnimationUtils
                    .currentAnimationTimeMillis());
        }

        mDynamics = dynamics;
    }

    /*******************************************************************************
	 * Constructors
	 *******************************************************************************/
	
	public HomeButton(Context context) {
		this(context, null);
	}

	public HomeButton(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public HomeButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(attrs);
	}

    public HomeButton(Context context, NavWheelView navWheel, int color, int secondaryColor, float width,
                      float height) {
        super(context);

        mNavWheel = navWheel;

        setColor(color);
        setSecondaryColor(secondaryColor);
        setButtonWidth(width);
        setButtonHeight(height);

        init(null);
    }

	/*******************************************************************************
	 * Methods
	 *******************************************************************************/
	
	/**
	 * Initializes the basic attributes and Paints to be used for the
	 * view.
	 * 
	 * @param attrs
	 */
	private void init(AttributeSet attrs) {

        mHandler = new Handler();

        mClickThreshold = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mPixelDensity = UiUtils.getDisplayMetrics(getContext()).density;
		mHome = BitmapFactory.decodeResource(getResources(), R.drawable.ipad_button_home);

        setDynamics(new FrictionDynamics(0.95f));

		if (attrs != null) {
			
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CaretView);
	
		    setColor(a.getColor(R.styleable.CaretView_color, Color.WHITE));
		    setSecondaryColor(a.getColor(R.styleable.CaretView_secondaryColor, Color.CYAN));
		    setButtonWidth(a.getDimension(R.styleable.CaretView_width, 10.0f));
		    setButtonHeight(a.getDimension(R.styleable.CaretView_height, 10.0f));
			
			a.recycle();
		}
		
		configurePaint();
		configureSliderPaint();
	}
	
	/**
	 * Moves the home button to the bottom of the screen
	 * if it is close enough.
	 */
	private void configureButton() {
		
		if (!mTouching && Math.abs(mPosY) <= mRadius) {
			
			mMoveDistance = Math.abs(mPosY);
			mStartPos = mPosY;

            mPositionAnimator = ThreadAnimator.ofFloat(mStartPos, mStartPos + mMoveDistance);
            mPositionAnimator.setDuration(DURATION);
            mPositionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mPositionAnimator.start();
		}
	}
	
	/**
	 * Sets up the paints to be used for the home button.
	 */
	private void configurePaint() {

		// If the button is detached from the bottom, or selected,
		// it will be slightly transparent
		int alpha = (mTouching || Math.abs(mPosY) > mRadius) ? 190 : 255;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    mPaint.setColor(mColor);
	    mPaint.setAlpha(alpha);
	    mPaint.setStyle(Paint.Style.FILL);
	    mPaint.setAntiAlias(true);
		
		mStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStroke.setStrokeWidth(UiUtils.getDynamicPixels(getContext(), 4));
		mStroke.setColor(Color.WHITE);
	    mPaint.setAlpha(alpha);   
		mStroke.setStyle(Paint.Style.STROKE);
		mStroke.setAntiAlias(true);
	}
	
	/**
	 * Sets up the paints to be used with the slider
	 */
	private void configureSliderPaint() {

		mPaintSlider = new Paint(Paint.ANTI_ALIAS_FLAG);

		mPaintSlider.setColor(mSecondaryColor);
		mPaintSlider.setAlpha(190);
		mPaintSlider.setStyle(Paint.Style.FILL);
		mPaintSlider.setAntiAlias(true);
		
		mStrokeSlider = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStrokeSlider.setStrokeWidth(UiUtils.getDynamicPixels(getContext(), 6));
		mStrokeSlider.setColor(Color.WHITE);
		mStrokeSlider.setStyle(Paint.Style.STROKE);
		mStrokeSlider.setAntiAlias(true);
	}
	
	/**
	 * Checks to see if the home button is being touched
	 * by the user based on the last touched coordinates.
	 * 
	 * @return
	 */
	private boolean touchingButton() {
    	
    	int center = (int) (getHeight() + mPosY);
    	int buttonTop = (int) (center - mRadius);
    	int buttonBottom = (int) (center + mRadius);
    	int buttonRight = (int) mRadius;
    	
    	return mLastTouchY <= buttonBottom && mLastTouchY >= buttonTop && mLastTouchX <= buttonRight;
	}
	
	/**
	 * Checks to see if the slider is being touched
	 * by the user based on the last touched coordinates
	 * @return
	 */
	private boolean touchingSlider() {
    	
		float sliderRadius = mRadius * 3.0f;
		
    	mCenter = (int) (getHeight() + mPosY);
    	int buttonTop = (int) (mCenter - sliderRadius);
    	int buttonBottom = (int) (mCenter + sliderRadius);
    	int buttonRight = (int) sliderRadius;
    	
    	
    	return mLastTouchY <= buttonBottom && mLastTouchY >= buttonTop && mLastTouchX <= buttonRight && !touchingButton();
	}
    
	/**
	 * Used to see if the touches by the user can be
	 * considered a click.
	 */
    public boolean clickCheck() {
        
    	if (mTouchState == TOUCH_STATE_CLICK && (touchingButton() || touchingSlider())) {

            playSoundEffect(SoundEffectConstants.CLICK);
        	mNavWheel.toggleNav();

            return true;
    	}

        return false;
    }
    
    /**
     * Keeps the y offset of the home button within the
     * bounds of the screen and draws the changes.
     * 
     * @param dy
     */
    private void updateButtonPosition(float dy) {
    	
    	// Move the object if we are touching the button
        if (mTouching) {
        
            mPosY += dy;
            
            // keep within bounds
            if (mPosY > 0)
            	mPosY = 0;
            
            if (Math.abs(mPosY) > (getHeight() - mRadius)) {
            	mPosY = -1 * (getHeight() - mRadius);
            }
        }
    }
    
    /**
     * Translates the slider's touches to navigation changes on
     * the navigation wheel.
     * 
     * @param dy
     * @param dx
     */
    private void sliderNavigation(MotionEvent event, float dy, float dx) {

        if (mTouchState == TOUCH_STATE_CLICK) {
            startRotationIfNeeded(event);
        }

        if (mTouchingSlider && mTouchState == TOUCH_STATE_ROTATE) {

            mVelocityTracker.addMovement(event);
            float degree = getDegree(dx, dy) - mStartDegree;
            mNavWheel.rotateWheelBy(degree);
        }
    }

    private float getDegree(float x, float y) {

        float centerY = getHeight() + mPosY;
        float degree = 90f - (float) (Math.toDegrees(Math.atan2(centerY - y, x)));
        degree *= 5f;

        return degree;
    }

	/*******************************************************************************
	 * Overridden Methods
	 *******************************************************************************/

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (getButtonWidth() * 3.5), MeasureSpec.getSize(heightMeasureSpec));
		
		mLeft = mHome.getWidth();
		mTop = (int) (getMeasuredHeight() - (mLeft * .2) - mHome.getHeight());
		mMid = getMeasuredHeight() - (mHome.getHeight() / 2);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        boolean clicked = false;
        
        switch (action) {
	        case MotionEvent.ACTION_DOWN: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Remember where we started
                mTouchStartX = (int) x;
	            mTouchStartY = (int) y;

                // Remember this touch position for the next move event
                mLastTouchY = y;
                mLastTouchX = x;

                // obtain a velocity tracker and feed it its first event
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);

                mTouchState = TOUCH_STATE_CLICK;

	            // Check if we are touching either the button or slider
	        	setTouching(true);
	        	setTouchingSlider(true);
	            
	            break;
	        }
	            
	        case MotionEvent.ACTION_MOVE: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Calculate the distance moved
	            final float dy = y - mLastTouchY;
	            
	            // Move the object if we are touching the button
	            updateButtonPosition(dy);
	            sliderNavigation(ev, y, x);
	            
	            // Remember this touch position for the next move event
	            mLastTouchY = y;
	            mLastTouchX = x;
	            
	            break;
	        }
	        
	        case MotionEvent.ACTION_UP: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Remember where we finished
	            mLastTouchY = y;
	            mLastTouchX = x;

                if (mTouchingSlider) {

                    mVelocityTracker.addMovement(ev);
                    mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND);

                    float velocity = calculateVelocity();
//                    endTouch(velocity);
                }

	            // Update that the user is no longer touching the screen
	        	setTouching(false);
	        	setTouchingSlider(false);
	            
	        	// Check to see if a click was performed
	        	clicked = clickCheck();
	            
	        	break;
	        }
        }
        
        return mTouching || mTouchingSlider || clicked;
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

            mTouchState = TOUCH_STATE_ROTATE;

            return true;
        }

        return false;
    }

    /**
     * Calculates the overall vector velocity given both the x and y
     * velocities and normalized to be pixel independent.
     *
     * @return the overall vector velocity
     */
    private float calculateVelocity() {

        int direction = mNavWheel.getRotatingClockwise() ? 1 : -1;

        float velocityX = mVelocityTracker.getXVelocity() / mPixelDensity;
        float velocityY = mVelocityTracker.getYVelocity() / mPixelDensity;
        float velocity = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY) * direction / 2;

        return velocity;
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
                    mNavWheel.rotateWheelBy(mDynamics.getPosition() % 360);

                    if (!mDynamics.isAtRest(VELOCITY_TOLERANCE)) {

                        // the list is not at rest, so schedule a new frame
                        mHandler.postDelayed(this, 8);
                    } else {
                        mNavWheel.snapTo();
                    }

                }
            };
        }

        if (mDynamics != null && Math.abs(velocity) > ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity()) {
            // update the dynamics with the correct position and start the runnable
            mDynamics.setState(mNavWheel.getRotationDegree(), velocity, AnimationUtils.currentAnimationTimeMillis());
            mHandler.post(mDynamicsRunnable);

        }
    }
	
	@Override
	protected void onDraw(Canvas canvas) {

        updateAnimators();

        // Draw the vertical change in position of the button
        canvas.save();
        canvas.translate(0, mPosY);
        
        // Animate the slider being displayed
	    canvas.drawCircle(0, getHeight(), mSliderRadius, mPaintSlider);
	    canvas.drawCircle(0, getHeight(), mSliderRadius, mStrokeSlider);

	    canvas.drawCircle(0, getHeight(), mRadius, mPaint);
	    canvas.drawCircle(0, getHeight(), mRadius, mStroke);
	    canvas.drawBitmap(mHome, (int) (mLeft * .2), getHousePosition(), null);
	    
        canvas.restore();
	}
	
	/*******************************************************************************
	 * Drawing Helper Methods
	 *******************************************************************************/
	
	/**
	 * Get the position for the house icon
	 * taking into account the button being
	 * moved.
	 * 
	 * @return
	 */
	private int getHousePosition() {

		int top = mTop;
	    
	    if (mPosY < 0) {
	    	
	    	top = (int) (mTop - mPosY);
	    	
	    	if (top > mMid) top = mMid;
	    }
	    
	    return top;
	}
}
