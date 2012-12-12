package com.moneydesktop.finance.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.animation.OvershootInterpolator;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.PointEvaluator;
import com.moneydesktop.finance.util.UiUtils;

@TargetApi(11)
public class PointerDrawable extends NavItemDrawable {

	private final float SIZE = 180;
	private final float CIRCLE_RADIUS = 155;
	
	private Paint indicator, circle;
	private float startAngle, sweepAngle, radiusDp;
	private RectF oval;
	
	public PointerDrawable(Context context, PointF center, int count) {
		super(context, -1, -1, center, center);
		
		sweepAngle = 360.0f / count;
		startAngle = 270.0f - (sweepAngle / 2.0f);
		
		radiusDp = UiUtils.getDynamicPixels(context, CIRCLE_RADIUS);
		
		initOval();
		initPaints();
	}

	@Override
	public void setAlpha(int alpha) {
		indicator.setAlpha(alpha);
		circle.setAlpha(alpha);
		invalidateSelf();
	}
	
	public int getAlpha() {
		return indicator.getAlpha();
	}
	
	private void initOval() {
		
		float sizeDp = UiUtils.getDynamicPixels(mContext, SIZE);
		
		float left = mCenter.x - (sizeDp/2.0f);
		float top = mCenter.y - (sizeDp/2.0f);
		float right = left + sizeDp;
		float bottom = top + sizeDp;
		
		oval = new RectF();
		oval.set(left, top, right, bottom);
	}
	
	private void initPaints() {
		
		indicator = new Paint(Paint.ANTI_ALIAS_FLAG);
		indicator.setColor(mContext.getResources().getColor(R.color.primaryColor));
		indicator.setStyle(Paint.Style.STROKE);
		indicator.setAntiAlias(true);
		indicator.setStrokeWidth(8);

		DashPathEffect dashPath = new DashPathEffect(new float[] {4, 4}, (float) 1.0);
		
		circle = new Paint(Paint.ANTI_ALIAS_FLAG);
		circle.setColor(Color.WHITE);
		circle.setStyle(Paint.Style.STROKE);
		circle.setAntiAlias(true);
		circle.setStrokeWidth(3);
		circle.setPathEffect(dashPath);
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		canvas.save();
		canvas.rotate(mRotation, mPosition.x, mPosition.y);
		canvas.scale(mScale.x, mScale.y, mPosition.x, mPosition.y);
		
		canvas.drawArc(oval, startAngle, sweepAngle, false, indicator);
		canvas.drawCircle(mCenter.x, mCenter.y, radiusDp/2.0f, circle);
		
		canvas.restore();
	}

	@Override
	public void playIntro() {

        if (mOutroSet != null) {
            mOutroSet.cancel();
        }
        
        reset();

		ObjectAnimator fade = ObjectAnimator.ofInt(this, "alpha", 0, 255);
		fade.setDuration(150);
		
		PointF orig = new PointF(0.0f, 0.0f);
		PointF bigger = new PointF(1.0f, 1.0f);
		
		ObjectAnimator pop = ObjectAnimator.ofObject(this, "scale", new PointEvaluator(), orig, bigger);
		pop.setInterpolator(new OvershootInterpolator(2.0f));
		pop.setDuration(500);

        mIntroSet = new AnimatorSet();
        mIntroSet.play(pop).with(fade);
        mIntroSet.setStartDelay(100);
        mIntroSet.start();
	}
	
	@Override
	public void playOutro(int selectedIndex) {
        
        if (mIntroSet != null) {
            mIntroSet.cancel();
        }

		ObjectAnimator fade = ObjectAnimator.ofInt(this, "alpha", 255, 0);
		fade.setDuration(400);

		PointF orig = new PointF(1.0f, 1.0f);
		PointF smaller = new PointF(0.0f, 0.0f);
		
		ObjectAnimator pop = ObjectAnimator.ofObject(this, "scale", new PointEvaluator(), orig, smaller);
		pop.setInterpolator(new OvershootInterpolator(2.0f));
		pop.setDuration(500);

        mOutroSet = new AnimatorSet();
        mOutroSet.play(fade).with(pop);
		mOutroSet.start();
	}
	
	@Override
	public void reset() {
		setScale(new PointF(0.0f, 0.0f));
	}
}
