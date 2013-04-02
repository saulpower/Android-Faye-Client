package com.moneydesktop.finance.views.navigation;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.piechart.ThreadAnimator;

public class NavItemDrawable extends Drawable {
	
	public final String TAG = this.getClass().getSimpleName();

    private final static int PADDING = 25;

	protected int mIndex;

	protected Context mContext;
	
	protected Bitmap mImage;
	protected Paint mPaint;
	protected PointF mTargetPosition;
	protected PointF mPosition;
	protected PointF mCenter;
	protected float mRotation = 0.0f;
	protected PointF mScale;

    protected int mPadding;

    private double mDegree;

    /** Animator objects used to animate the rotation, scale, and info panel */
    protected ThreadAnimator mRotateAnimator, mScaleAnimator, mAlphaAnimator, mPositionAnimator;
	
	public int getIndex() {
		return mIndex;
	}

    public double getDegree() {
        return mDegree;
    }
	
	public NavItemDrawable(Context context, int resource, int index, PointF position, PointF center, double degree) {
		
		this.mContext = context;
        this.mDegree = degree;

        mPadding = (int) (UiUtils.getDynamicPixels(context, PADDING) + 0.5f);

		if (resource != -1)
			mImage = BitmapFactory.decodeResource(context.getResources(), resource);
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setAlpha(0);
		
		mScale = new PointF(1.0f, 1.0f);
		
		this.mIndex = index;
		this.mPosition = position;
		this.mCenter = center;
		this.mTargetPosition = position;
		
		updatePosition();
	}
	
	@Override
	public void draw(Canvas canvas) {

        updateAnimators();

		canvas.save();
		canvas.rotate(mRotation, mPosition.x, mPosition.y);
		canvas.scale(mScale.x, mScale.y, mPosition.x, mPosition.y);
		
		canvas.drawBitmap(mImage, getBounds().left + mPadding, getBounds().top + mPadding, mPaint);
		
		canvas.restore();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}
	
	public int getAlpha() {
		return mPaint.getAlpha();
	}
	
	public PointF getPosition() {
		return mPosition;
	}

	public void setPosition(PointF position) {
		this.mPosition = position;
		updatePosition();
	}

	public float getRotation() {
		return mRotation;
	}

	public void setRotation(float rotation) {
		this.mRotation = rotation;
	}
	
	public void fixRotation(float rotation) {
		this.mRotation = rotation;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {}

	public PointF getScale() {
		return mScale;
	}

	public void setScale(PointF scale) {
		this.mScale = scale;
	}

	// Updates the drawables bounds when it has been translated
	private void updatePosition() {

		if (mImage != null) {
			
			int left = (int) (mPosition.x - ((float) mImage.getWidth() / 2f) - mPadding);
			int top = (int) (mPosition.y - ((float) mImage.getHeight() / 2f) - mPadding);
			int right = left + mImage.getWidth() + mPadding;
			int bottom = top + mImage.getHeight() + mPadding;
			
			setBounds(left, top, right, bottom);
		}
	}
	
	/**
	 * The animations to perform when the drawable is
	 * first displayed.
	 */
	public void playIntro() {
        
	    reset();

        mAlphaAnimator = ThreadAnimator.ofInt(0, 255);
        mAlphaAnimator.setDuration(150);

        mPositionAnimator = ThreadAnimator.ofPoint(mCenter, mTargetPosition);
        mPositionAnimator.setDuration(300);
        mPositionAnimator.setInterpolator(new OvershootInterpolator());

        long offset = 100 + 50 * mIndex;
        mAlphaAnimator.start(offset);
        mPositionAnimator.start(offset);
	}
	
	/**
	 * The animation to perform when the drawable is
	 * being removed.  Will change if the current drawable
	 * is selected.
	 * 
	 * @param selectedIndex
	 */
	public void playOutro(int selectedIndex) {

        boolean selected = mIndex == selectedIndex;

        mAlphaAnimator = ThreadAnimator.ofInt(255, 0);
        mAlphaAnimator.setDuration(1000);

        mPositionAnimator = ThreadAnimator.ofPoint(mTargetPosition, mCenter);
        mPositionAnimator.setDuration(selected ? 500 : 600);
        mPositionAnimator.setInterpolator(new AnticipateInterpolator(2.0f));

        mRotateAnimator = ThreadAnimator.ofFloat(0, 180);
        mRotateAnimator.setDuration(500);

		PointF orig = new PointF(1.0f, 1.0f);
		PointF bigger = new PointF(3.0f, 3.0f);

        mScaleAnimator = ThreadAnimator.ofPoint(orig, bigger);
        mScaleAnimator.setDuration(1000);
		
		if (mIndex == selectedIndex) {

            mAlphaAnimator.start();
            mScaleAnimator.start();
			
		} else {

            long offset = mIndex * 50;

            mAlphaAnimator.start(offset);
            mRotateAnimator.start(offset);
            mPositionAnimator.start(offset);
		}
	}

    /**
     * Update our animators that control animating the
     * rotation, scale, and info panel alpha
     */
    protected void updateAnimators() {

        if (mRotateAnimator != null && mRotateAnimator.isRunning()) {
            setRotation(mRotateAnimator.floatUpdate());
        }

        if (mScaleAnimator != null && mScaleAnimator.isRunning()) {
            setScale(mScaleAnimator.pointUpdate());
        }

        if (mAlphaAnimator != null && mAlphaAnimator.isRunning()) {
            setAlpha(mAlphaAnimator.intUpdate());
        }

        if (mPositionAnimator != null && mPositionAnimator.isRunning()) {
            setPosition(mPositionAnimator.pointUpdate());
        }
    }

    public void popIcon() {

        PointF orig = new PointF(1.0f, 1.0f);
        PointF bigger = new PointF(1.3f, 1.3f);

        mScaleAnimator = ThreadAnimator.ofPoint(orig, bigger);
        mScaleAnimator.setAnimationListener(new ThreadAnimator.AnimationListener() {
            @Override
            public void onAnimationEnded() {
                unpopIcon();
            }
        });
        mScaleAnimator.setDuration(125);
        mScaleAnimator.start();
    }

    private void unpopIcon() {

        PointF orig = new PointF(1.0f, 1.0f);
        PointF bigger = new PointF(1.3f, 1.3f);

        mScaleAnimator = ThreadAnimator.ofPoint(bigger, orig);
        mScaleAnimator.setDuration(125);
        mScaleAnimator.start();
    }
	
	/**
	 * Reset the drawable to a beginning scale and rotation.
	 */
	public void reset() {
		setRotation(0);
		setScale(new PointF(1.0f, 1.0f));
	}
}