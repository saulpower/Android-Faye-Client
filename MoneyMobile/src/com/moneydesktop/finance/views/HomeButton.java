package com.moneydesktop.finance.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.util.Enums.NavDirection;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class HomeButton extends View {
	
	public final String TAG = this.getClass().getSimpleName();

	private final float SCROLL_DISTANCE = 17.0f;
	
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
    
    private float mLastTouchY, mLastTouchX, mLastDy, mLastDx;
    
    // Scrolling
    private float mScrollX = 0.0f, mScrollY = 0.0f;
    private boolean mAbove = false;
    private float mMoveDistance, mStartPos;
    
	// Touching
	private boolean mTouching = false, mTouchingSlider = false;
    private float mDistance = 0.0f;
	
	// Animation
	private long mDuration = 500;

	/*******************************************************************************
	 * Accessory Methods
	 *******************************************************************************/
	
	public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        invalidate();
    }

    /**
	 * Setter used to show or hide the outer slider button.
	 * 
	 * @param showSlider
	 */
	public void setShowSlider(boolean showSlider) {
		
		this.mShowSlider = showSlider;
		animateSlider(showSlider);
		invalidate();
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
	}

	/**
	 * Begins the animation of the home button and indicates
	 * an animation is occurring.
	 * 
	 * @param animatingButton
	 */
	public void animateButtonBounce() {
        
        ObjectAnimator button = ObjectAnimator.ofFloat(this, "radius", mTouching ? mWidth : mWidth + 15, mTouching ? mWidth + 15 : mWidth);
        button.setDuration(mDuration);
        button.setInterpolator(new BounceInterpolator());
        button.start();
	}

	/**
	 * Begins the animation of the slider and indicates
	 * an animation is occurring.
	 * 
	 * @param animatingSlider
	 */
	public void animateSlider(boolean showSlider) {
	    
	    ObjectAnimator slider = ObjectAnimator.ofFloat(this, "sliderRadius", showSlider ? 0 : mSliderMaxRadius, showSlider ? mSliderMaxRadius : 0);
        slider.setDuration(mDuration);
        slider.setInterpolator(new OvershootInterpolator());

        ObjectAnimator alpha = ObjectAnimator.ofInt(this, "alphaSlider", showSlider ? 0 : 190, showSlider ? 190 : 0);
        alpha.setDuration(mDuration);
        
        ObjectAnimator stroke = ObjectAnimator.ofInt(this, "alphaSliderStroke", showSlider ? 0 : 255, showSlider ? 255 : 0);
        stroke.setDuration(mDuration);
        
        AnimatorSet set = new AnimatorSet();
        set.play(slider).with(alpha).with(stroke);
        set.start();
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
        invalidate();
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
        invalidate();
    }

    /*******************************************************************************
	 * Constructors
	 *******************************************************************************/
	
	public HomeButton(Context context) {
		super(context);
		
		init(null);
	}

	public HomeButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(attrs);
	}

	public HomeButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(attrs);
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
		
		EventBus.getDefault().register(this);
		
		mHome = BitmapFactory.decodeResource(getResources(), R.drawable.ipad_button_home);
		
		if (attrs != null) {
			
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Caret);
	
		    setColor(a.getColor(R.styleable.Caret_color, Color.WHITE));
		    setSecondaryColor(a.getColor(R.styleable.Caret_secondaryColor, Color.CYAN));
		    setButtonWidth(a.getDimension(R.styleable.Caret_width, 10.0f));
		    setButtonHeight(a.getDimension(R.styleable.Caret_height, 10.0f));
			
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
			
	        ObjectAnimator button = ObjectAnimator.ofFloat(this, "posY", mStartPos, mStartPos + mMoveDistance);
	        button.setDuration(mDuration);
	        button.setInterpolator(new AccelerateDecelerateInterpolator());
	        button.start();
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
    public void clickCheck() {
        
    	if (mDistance < (mRadius * 0.5f) && (touchingButton() || touchingSlider())) {
        	
        	mDistance = 0.0f;
        	EventBus.getDefault().post(new EventMessage().new NavigationEvent());
    	}
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
        	
            invalidate();
        }
    }
    
    /**
     * Translates the slider's touches to navigation changes on
     * the navigation wheel.
     * 
     * @param dy
     * @param dx
     */
    private void sliderNavigation(float dy, float dx) {

        if (mTouchingSlider) {

        	// Determine what part of the slider is being touched (top or bottom half)
        	mAbove = mLastTouchY < mCenter;
        	
        	// If we have changed directions reset the movement sum
        	if (Math.signum(mLastDy) != Math.signum(dy))
        		mScrollY = 0.0f;
        	
        	if (Math.signum(mLastDx) != Math.signum(dx))
        		mScrollX = 0.0f;
        	
        	// Sum movement in each direction
        	mScrollX += dx;
        	mScrollY += dy;
        	
        	// If movement was great enough fire off an event to move the navigation wheel
        	if (Math.abs(mScrollX) >= SCROLL_DISTANCE) {

        		// X-axis scrolling changes depends on which half the user is touching
        		mScrollX = mAbove ? -mScrollX : mScrollX;
        		
            	NavDirection dir = (mScrollX < 0) ? NavDirection.NEXT : NavDirection.PREVIOUS;
            	
            	navigate(dir);
        		mScrollX = 0.0f;
        		
        	} else if (Math.abs(mScrollY) >= SCROLL_DISTANCE) {

            	NavDirection dir = (mScrollY >= 0) ? NavDirection.NEXT : NavDirection.PREVIOUS;
            	
            	navigate(dir);
        		mScrollY = 0.0f;
        	}
        }
    }
    
    private void navigate(NavDirection dir) {
    	
    	EventBus.getDefault().post(new EventMessage().new NavigationEvent(dir));
    }

	/*******************************************************************************
	 * Event Methods
	 *******************************************************************************/

    /**
     * Receives the event that the navigation wheel is showing and the
     * slider should not be displayed.
     * 
     * @param event
     */
	public void onEvent(NavigationEvent event) {
		
		if (event.isShowing() != null) {
			setShowSlider(event.isShowing());
		}
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
		mTop = (int) (getHeight() - (mLeft * .2) - mHome.getHeight());
		mMid = (int) (getHeight() - (mHome.getHeight() / 2));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        
        switch (action) {
	        case MotionEvent.ACTION_DOWN: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Remember where we started
	            mLastTouchY = y;
	            mLastTouchX = x;
	            mLastDy = 0.0f;
	            mLastDx = 0.0f;

	            // Check if we are touching either the button or slider
	        	setTouching(true);
	        	setTouchingSlider(true);
	        	
	        	// Reset the distance traveled
	        	mDistance = 0.0f;
	            
	            break;
	        }
	            
	        case MotionEvent.ACTION_MOVE: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Calculate the distance moved
	            final float dy = y - mLastTouchY;
	            final float dx = x - mLastTouchX;
	            
	            mDistance += Math.abs(dy);
	            
	            // Move the object if we are touching the button
	            updateButtonPosition(dy);
	            sliderNavigation(dy, dx);
	            
	            // Remember this touch position for the next move event
	            mLastTouchY = y;
	            mLastTouchX = x;
	            mLastDy = dy;
	            mLastDx = dx;
	            
	            break;
	        }
	        
	        case MotionEvent.ACTION_UP: {
	        	
	            final float y = ev.getY();
	            final float x = ev.getX();
	            
	            // Remember where we finished
	            mLastTouchY = y;
	            mLastTouchX = x;
	        	
	            // Update that the user is no longer touching the screen
	        	setTouching(false);
	        	setTouchingSlider(false);
	            
	        	// Check to see if a click was performed
	        	clickCheck();
	            
	        	break;
	        }
        }
        
        return mTouching || mTouchingSlider;
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
        
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
	    	
	    	if (top > mMid)
	    		top = mMid;
	    }
	    
	    return top;
	}
}
