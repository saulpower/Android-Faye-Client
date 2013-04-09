package main.java.com.moneydesktop.finance.views.navigation;

import android.content.Context;
import android.graphics.*;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.piechart.ThreadAnimator;

/**
 * Used to draw the pointer for the Navigation Wheel indicating which
 * view the navigation is on.
 *
 * @author saulhoward
 *
 */
public class PointerDrawable extends NavItemDrawable {

    private final float SIZE = 180;
    private final float CIRCLE_RADIUS = 155;

    private Paint indicator, circle;
    private float startAngle, sweepAngle, radiusDp;
    private RectF oval;

    public PointerDrawable(Context context, PointF center, int count) {
        super(context, -1, -1, center, center, -1);

        sweepAngle = 360.0f / count;
        startAngle = 270.0f - (sweepAngle / 2.0f);

        radiusDp = UiUtils.getDynamicPixels(context, CIRCLE_RADIUS) / 2.0f;

        mScale = new PointF(0.0f, 0.0f);

        initOval();
        initPaints();

        setAlpha(0);
    }

    @Override
    public void setAlpha(int alpha) {
        indicator.setAlpha(alpha);
        circle.setAlpha(alpha);
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
        indicator.setStrokeWidth(UiUtils.getDynamicPixels(mContext, 8));

        float interval = UiUtils.getDynamicPixels(mContext, 4);
        DashPathEffect dashPath = new DashPathEffect(new float[] {interval, interval}, (float) 1.0);

        circle = new Paint(Paint.ANTI_ALIAS_FLAG);
        circle.setColor(Color.WHITE);
        circle.setStyle(Paint.Style.STROKE);
        circle.setStrokeWidth(UiUtils.getDynamicPixels(mContext, 3));
        circle.setPathEffect(dashPath);
    }

    @Override
    public void draw(Canvas canvas) {

        updateAnimators();

        canvas.save();
        canvas.rotate(mRotation, mPosition.x, mPosition.y);
        canvas.scale(mScale.x, mScale.y, mPosition.x, mPosition.y);

        canvas.drawArc(oval, startAngle, sweepAngle, false, indicator);
        canvas.drawCircle(mCenter.x, mCenter.y, radiusDp, circle);

        canvas.restore();
    }

    @Override
    public void playIntro() {

        reset();

        mAlphaAnimator = ThreadAnimator.ofInt(0, 255);
        mAlphaAnimator.setDuration(250);

        PointF orig = new PointF(0.0f, 0.0f);
        PointF bigger = new PointF(1.0f, 1.0f);

        mScaleAnimator = ThreadAnimator.ofPoint(orig, bigger);
        mScaleAnimator.setInterpolator(new OvershootInterpolator(2.5f));
        mScaleAnimator.setDuration(500);

        mAlphaAnimator.start(300);
        mScaleAnimator.start(300);
    }

    @Override
    public void playOutro(int selectedIndex) {

        mAlphaAnimator = ThreadAnimator.ofInt(255, 0);
        mAlphaAnimator.setDuration(400);

        PointF orig = new PointF(1.0f, 1.0f);
        PointF smaller = new PointF(0.0f, 0.0f);

        mScaleAnimator = ThreadAnimator.ofPoint(orig, smaller);
        mScaleAnimator.setInterpolator(new AnticipateInterpolator());
        mScaleAnimator.setDuration(500);

        mAlphaAnimator.start();
        mScaleAnimator.start();
    }

    @Override
    public void reset() {
        setScale(new PointF(0.0f, 0.0f));
    }
}
