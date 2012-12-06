package com.moneydesktop.finance.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

@TargetApi(11)
public class NavItemDrawable extends Drawable {
	
	public final String TAG = this.getClass().getSimpleName();

	protected int index;

	protected Context context;
	
	protected Bitmap image;
	protected Paint paint;
	protected PointF targetPosition;
	protected PointF position;
	protected PointF center;
	protected float rotation = 0.0f;
	protected PointF scale;
	
	public int getIndex() {
		return index;
	}
	
	public NavItemDrawable(Context context, int resource, int index, PointF position, PointF center) {
		
		this.context = context;
		
		if (resource != -1)
			image = BitmapFactory.decodeResource(context.getResources(), resource);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAlpha(0);
		
		scale = new PointF(1.0f, 1.0f);
		
		this.index = index;
		this.position = position;
		this.center = center;
		this.targetPosition = position;
		
		updatePosition();
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		canvas.save();
		canvas.rotate(rotation, position.x, position.y);
		canvas.scale(scale.x, scale.y, position.x, position.y);
		
		canvas.drawBitmap(image, getBounds().left, getBounds().top, paint);
		
		canvas.restore();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
		invalidateSelf();
	}
	
	public int getAlpha() {
		return paint.getAlpha();
	}
	
	public PointF getPosition() {
		return position;
	}

	public void setPosition(PointF position) {
		this.position = position;
		updatePosition();
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
		invalidateSelf();
	}
	
	public void fixRotation(float rotation) {
		this.rotation = rotation;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {}

	public PointF getScale() {
		return scale;
	}

	public void setScale(PointF scale) {
		this.scale = scale;
		invalidateSelf();
	}

	// Updates the drawables bounds when it has been translated
	private void updatePosition() {

		if (image != null) {
			
			int left = (int) (position.x - (image.getWidth()/2));
			int top = (int) (position.y - (image.getHeight()/2));
			int right = left + image.getWidth();
			int bottom = top + image.getHeight();
			
			setBounds(left, top, right, bottom);
			
			invalidateSelf();
		}
	}
	
	/**
	 * The animations to perform when the drawable is
	 * first displayed.
	 */
	public void playIntro() {
		
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "alpha", 0, 255);
		fade.setDuration(150);
		
		ObjectAnimator translate = ObjectAnimator.ofObject(this, "position", new PointEvaluator(), center, targetPosition);
		translate.setDuration(500);
		translate.setInterpolator(new OvershootInterpolator(2.0f));
		
		AnimatorSet set = new AnimatorSet();
		set.play(translate).with(fade);
		set.setStartDelay(100);
		set.start();
	}
	
	/**
	 * The animation to perform when the drawable is
	 * being removed.  Will change if the current drawable
	 * is selected.
	 * 
	 * @param selectedIndex
	 */
	public void playOutro(int selectedIndex) {
		
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "alpha", 255, 0);
		fade.setDuration(1000);
		
		ObjectAnimator translate = ObjectAnimator.ofObject(this, "position", new PointEvaluator(), targetPosition, center);
		translate.setDuration(500);
		translate.setInterpolator(new AnticipateInterpolator(2.0f));
		
		ObjectAnimator rotate = ObjectAnimator.ofFloat(this, "rotation", 0, 180);
		rotate.setDuration(500);

		PointF orig = new PointF(1.0f, 1.0f);
		PointF bigger = new PointF(3.0f, 3.0f);
		
		ObjectAnimator pop = ObjectAnimator.ofObject(this, "scale", new PointEvaluator(), orig, bigger);
		pop.setDuration(1000);

		AnimatorSet set = new AnimatorSet();
		
		if (index == selectedIndex) {
			
			set.play(fade).with(pop);
			
		} else {
			
			fade.setDuration(600);
			set.play(fade).with(translate).with(rotate);
		}

		set.start();
	}
	
	/**
	 * Reset the drawable to a beginning scale and rotation.
	 */
	public void reset() {
		rotation = 0;
		scale = new PointF(1.0f, 1.0f);
	}
}