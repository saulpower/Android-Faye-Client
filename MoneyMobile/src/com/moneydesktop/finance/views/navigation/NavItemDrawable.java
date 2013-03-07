package com.moneydesktop.finance.views.navigation;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.moneydesktop.finance.model.PointEvaluator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

@TargetApi(11)
public class NavItemDrawable extends Drawable {
	
	public final String TAG = this.getClass().getSimpleName();

	protected int mIndex;

	protected Context mContext;
	
	protected Bitmap mImage;
	protected Paint mPaint;
	protected PointF mTargetPosition;
	protected PointF mPosition;
	protected PointF mCenter;
	protected float mRotation = 0.0f;
	protected PointF mScale;
	protected AnimatorSet mOutroSet, mIntroSet;
	
	public int getIndex() {
		return mIndex;
	}
	
	public NavItemDrawable(Context context, int resource, int index, PointF position, PointF center) {
		
		this.mContext = context;
		
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
		
		canvas.save();
		canvas.rotate(mRotation, mPosition.x, mPosition.y);
		canvas.scale(mScale.x, mScale.y, mPosition.x, mPosition.y);
		
		canvas.drawBitmap(mImage, getBounds().left, getBounds().top, mPaint);
		
		canvas.restore();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
		invalidateSelf();
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
		invalidateSelf();
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
		invalidateSelf();
	}

	// Updates the drawables bounds when it has been translated
	private void updatePosition() {

		if (mImage != null) {
			
			int left = (int) (mPosition.x - (mImage.getWidth()/2));
			int top = (int) (mPosition.y - (mImage.getHeight()/2));
			int right = left + mImage.getWidth();
			int bottom = top + mImage.getHeight();
			
			setBounds(left, top, right, bottom);
			
			invalidateSelf();
		}
	}
	
	/**
	 * The animations to perform when the drawable is
	 * first displayed.
	 */
	public void playIntro() {

        if (mOutroSet != null) {
            mOutroSet.cancel();
        }
        
	    reset();
	    
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "alpha", 0, 255);
		fade.setDuration(150);
		
		ObjectAnimator translate = ObjectAnimator.ofObject(this, "position", new PointEvaluator(), mCenter, mTargetPosition);
		translate.setDuration(300);
		translate.setInterpolator(new OvershootInterpolator());

		mIntroSet = new AnimatorSet();
		mIntroSet.play(translate).with(fade);
		mIntroSet.setStartDelay(100 + 50 * mIndex);
		mIntroSet.start();
	}
	
	/**
	 * The animation to perform when the drawable is
	 * being removed.  Will change if the current drawable
	 * is selected.
	 * 
	 * @param selectedIndex
	 */
	public void playOutro(int selectedIndex) {
		
	    if (mIntroSet != null) {
	        mIntroSet.cancel();
	    }
	    
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "alpha", 255, 0);
		fade.setDuration(1000);
		
		ObjectAnimator translate = ObjectAnimator.ofObject(this, "position", new PointEvaluator(), mTargetPosition, mCenter);
		translate.setDuration(500);
		translate.setInterpolator(new AnticipateInterpolator(2.0f));
		
		ObjectAnimator rotate = ObjectAnimator.ofFloat(this, "rotation", 0, 180);
		rotate.setDuration(500);

		PointF orig = new PointF(1.0f, 1.0f);
		PointF bigger = new PointF(3.0f, 3.0f);
		
		ObjectAnimator pop = ObjectAnimator.ofObject(this, "scale", new PointEvaluator(), orig, bigger);
		pop.setDuration(1000);

		mOutroSet = new AnimatorSet();
		
		if (mIndex == selectedIndex) {
			
		    mOutroSet.play(fade).with(pop);
			
		} else {
			
			fade.setDuration(600);
			mOutroSet.play(fade).with(translate).with(rotate);
	        mOutroSet.setStartDelay(mIndex * 50);
		}

		mOutroSet.start();
	}
	
	/**
	 * Reset the drawable to a beginning scale and rotation.
	 */
	public void reset() {
		setRotation(0);
		setScale(new PointF(1.0f, 1.0f));
	}
}