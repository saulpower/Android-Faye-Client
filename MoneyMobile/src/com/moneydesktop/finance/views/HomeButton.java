package com.moneydesktop.finance.views;

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

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.util.Enums.NavDirection;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class HomeButton extends View {
	
	public final String TAG = this.getClass().getSimpleName();

	private final float SCROLL_DISTANCE = 17.0f;
	
	// Drawing
	private Bitmap home;
	private int color = Color.BLUE, secondaryColor = Color.CYAN;
	private Paint paint, stroke, paintSlider, strokeSlider;
	private float radius;
	private boolean showSlider = false;

	// Positioning
	private int mLeft, mTop, mMid, center;
	private float width = 10;
	private float height = 10;

    private float mPosY;
    
    private float mLastTouchY, mLastTouchX, mLastDy, mLastDx;
    
    // Scrolling
    private float scrollX = 0.0f, scrollY = 0.0f;
    private boolean above = false;
    private float moveDistance, startPos;
    
	// Touching
	private boolean touching = false, touchingSlider = false;
    private float mDistance = 0.0f;
	
	// Animation
	private BounceInterpolator bounce = new BounceInterpolator();
	private AccelerateDecelerateInterpolator accelDecel = new AccelerateDecelerateInterpolator();
	private long duration = 500;
	private long start, sliderStart, positionStart;
	private boolean animatingButton = false, animatingSlider = false, animatingPosition = false;

	/*******************************************************************************
	 * Accessory Methods
	 *******************************************************************************/
	
	/**
	 * Setter used to show or hide the outer slider button.
	 * 
	 * @param showSlider
	 */
	public void setShowSlider(boolean showSlider) {
		
		this.showSlider = showSlider;
		setAnimatingSlider(true);
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
		
		boolean wasTouching = this.touching;
		
		this.touching = touching && touchingButton();
    	
		if (this.touching || (wasTouching && !this.touching))
			setAnimating(true);
		
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
			
		this.touchingSlider = touchingSlider && touchingSlider() && showSlider;
	}

	/**
	 * Begins the animation of the home button and indicates
	 * an animation is occurring.
	 * 
	 * @param animatingButton
	 */
	public void setAnimating(boolean animatingButton) {
		this.animatingButton = animatingButton;
		start = System.currentTimeMillis();
    	
        invalidate();
	}

	/**
	 * Begins the animation of the slider and indicates
	 * an animation is occurring.
	 * 
	 * @param animatingSlider
	 */
	public void setAnimatingSlider(boolean animatingSlider) {
		this.animatingSlider = animatingSlider;
		sliderStart = System.currentTimeMillis();
    	
        invalidate();
	}

	public void setColor(int color) {
		this.color = color;
		
		configurePaint();
	}

	public void setSecondaryColor(int secondaryColor) {
		this.secondaryColor = secondaryColor;
		
		configureSliderPaint();
	}

	public void setButtonWidth(float width) {
		this.width = width;
	}
	
	public float getButtonWidth() {
		return width;
	}

	public void setButtonHeight(float height) {
		this.height = height;
	}
	
	public float getButtonHeight() {
		return height;
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
		
		home = BitmapFactory.decodeResource(getResources(), R.drawable.ipad_button_home);
		
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
		
		if (!touching && Math.abs(mPosY) <= radius) {
			
			animatingPosition = true;
			positionStart = System.currentTimeMillis();
			moveDistance = Math.abs(mPosY);
			startPos = mPosY;
			invalidate();
		}
	}
	
	/**
	 * Sets up the paints to be used for the home button.
	 */
	private void configurePaint() {

		// If the button is detached from the bottom, or selected,
		// it will be slightly transparent
		int alpha = (touching || Math.abs(mPosY) > radius) ? 190 : 255;
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    paint.setColor(color);
	    paint.setAlpha(alpha);
	    paint.setStyle(Paint.Style.FILL);
	    paint.setAntiAlias(true);
		
		stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		stroke.setStrokeWidth(4);
		stroke.setColor(Color.WHITE);
	    paint.setAlpha(alpha);   
		stroke.setStyle(Paint.Style.STROKE);
		stroke.setAntiAlias(true);
	}
	
	/**
	 * Sets up the paints to be used with the slider
	 */
	private void configureSliderPaint() {

		paintSlider = new Paint(Paint.ANTI_ALIAS_FLAG);

		paintSlider.setColor(secondaryColor);
		paintSlider.setAlpha(190);
		paintSlider.setStyle(Paint.Style.FILL);
		paintSlider.setAntiAlias(true);
		
		strokeSlider = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokeSlider.setStrokeWidth(6);
		strokeSlider.setColor(Color.WHITE);
		strokeSlider.setStyle(Paint.Style.STROKE);
		strokeSlider.setAntiAlias(true);
	}
	
	/**
	 * Checks to see if the home button is being touched
	 * by the user based on the last touched coordinates.
	 * 
	 * @return
	 */
	private boolean touchingButton() {
    	
    	int center = (int) (getHeight() + mPosY);
    	int buttonTop = (int) (center - radius);
    	int buttonBottom = (int) (center + radius);
    	int buttonRight = (int) radius;
    	
    	return mLastTouchY <= buttonBottom && mLastTouchY >= buttonTop && mLastTouchX <= buttonRight;
	}
	
	/**
	 * Checks to see if the slider is being touched
	 * by the user based on the last touched coordinates
	 * @return
	 */
	private boolean touchingSlider() {
    	
		float sliderRadius = radius * 3.0f;
		
    	center = (int) (getHeight() + mPosY);
    	int buttonTop = (int) (center - sliderRadius);
    	int buttonBottom = (int) (center + sliderRadius);
    	int buttonRight = (int) sliderRadius;
    	
    	
    	return mLastTouchY <= buttonBottom && mLastTouchY >= buttonTop && mLastTouchX <= buttonRight && !touchingButton();
	}
    
	/**
	 * Used to see if the touches by the user can be
	 * considered a click.
	 */
    public void clickCheck() {
    	
    	if (mDistance < 5.0f && touchingButton()) {
        	
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
        if (touching) {
        
            mPosY += dy;
            
            // keep within bounds
            if (mPosY > 0)
            	mPosY = 0;
            
            if (Math.abs(mPosY) > (getHeight() - radius)) {
            	mPosY = -1 * (getHeight() - radius);
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

        if (touchingSlider) {

        	// Determine what part of the slider is being touched (top or bottom half)
        	above = mLastTouchY < center;
        	
        	// If we have changed directions reset the movement sum
        	if (Math.signum(mLastDy) != Math.signum(dy))
        		scrollY = 0.0f;
        	
        	if (Math.signum(mLastDx) != Math.signum(dx))
        		scrollX = 0.0f;
        	
        	// Sum movement in each direction
        	scrollX += dx;
        	scrollY += dy;
        	
        	// If movement was great enough fire off an event to move the navigation wheel
        	if (Math.abs(scrollX) >= SCROLL_DISTANCE) {

        		// X-axis scrolling changes depends on which half the user is touching
        		scrollX = above ? -scrollX : scrollX;
        		
            	NavDirection dir = (scrollX < 0) ? NavDirection.NEXT : NavDirection.PREVIOUS;
            	
            	navigate(dir);
        		scrollX = 0.0f;
        		
        	} else if (Math.abs(scrollY) >= SCROLL_DISTANCE) {

            	NavDirection dir = (scrollY >= 0) ? NavDirection.NEXT : NavDirection.PREVIOUS;
            	
            	navigate(dir);
        		scrollY = 0.0f;
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
		
		mLeft = home.getWidth();
		mTop = (int) (getHeight() - (mLeft * .2) - home.getHeight());
		mMid = (int) (getHeight() - (home.getHeight() / 2));
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
        
        return touching || touchingSlider;
    }
	
	@Override
	protected void onDraw(Canvas canvas) {

		// Animate the vertical position of the button
        if (animatingPosition) {

        	float percent = (float) (System.currentTimeMillis() - positionStart) / (float) duration;
        	mPosY = startPos + (int) (moveDistance * accelDecel.getInterpolation(percent));
        	
        	if (percent >= 1.0f)
        		animatingPosition = false;
        }
        
        // Draw the vertical change in position of the button
        canvas.save();
        canvas.translate(0, mPosY);
        
        // Animate the slider being displayed
        if (showSlider || animatingSlider) {

            float sliderRadius = getSliderSize();

    	    canvas.drawCircle(0, getHeight(), sliderRadius, paintSlider);
    	    canvas.drawCircle(0, getHeight(), sliderRadius, strokeSlider);
        }
        
        // Get the button's radius to draw it
        radius = getButtonSize();

	    canvas.drawCircle(0, getHeight(), radius, paint);
	    canvas.drawCircle(0, getHeight(), radius, stroke);
	    canvas.drawBitmap(home, (int) (mLeft * .2), getHousePosition(), null);
	    
        canvas.restore();
        
        // Continue to call the onDraw method while we are animating anything
        if (animatingButton || animatingSlider || animatingPosition)
        	invalidate();
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
	
	/**
	 * Used to animate the button
	 * 
	 * @return
	 */
	private float getButtonSize() {
		
		int selected = (touching || animatingButton) ? 15 : 0;

        if (animatingButton) {
        	
        	float percent = (float) (System.currentTimeMillis() - start) / (float) duration;
        	selected = (int) (selected * bounce.getInterpolation(percent));
        	
        	if (!touching)
        		selected = 15 - selected;
        	
        	if (percent >= 1.0f)
        		animatingButton = false;
        }
        
        return width + selected;
	}
	
	/**
	 * Used to animate the slider
	 * @return
	 */
	private float getSliderSize() {
    	
    	float size = width * 3f;

        if (animatingSlider) {
        	
        	float percent = (float) (System.currentTimeMillis() - sliderStart) / (float) duration;
        	size = (int) (size * accelDecel.getInterpolation(percent));
        	
        	if (!showSlider)
        		size = (width * 3f) - size;
        	
        	if (percent >= 1.0f)
        		animatingSlider = false;
        }
        
        return size;
	}
}
