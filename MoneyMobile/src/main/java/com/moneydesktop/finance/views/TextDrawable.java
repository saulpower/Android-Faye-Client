package main.java.com.moneydesktop.finance.views;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import main.java.com.moneydesktop.finance.model.PointEvaluator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class TextDrawable extends Drawable {

    public final String TAG = this.getClass().getSimpleName();

    private Paint mTextPaint;
    private ColorStateList mColors;

    private float mScale = 1;
    private PointF mPosition;

    private String mText = "";
    private Rect mBounds = new Rect();

    public Typeface getTypeFace() {
        return mTextPaint.getTypeface();
    }

    public void setTypeFace(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
        invalidateSelf();
    }

    public int getColor() {
        return mTextPaint.getColor();
    }

    public void setColor(int color) {
        mTextPaint.setColor(color);
        invalidateSelf();
    }

    public void setColors(ColorStateList colors) {

        mColors = colors;
        updateColor();
    }

    public ColorStateList getColors() {
        return mColors;
    }

    private void updateColor() {

        int color = mColors.getColorForState(getState(), Color.WHITE);

        if (color != mTextPaint.getColor()) {
            setColor(color);
        }
    }

    public float getFontSize() {
        return mTextPaint.getTextSize();
    }

    public void setFontSize(float fontSize) {
        mTextPaint.setTextSize(fontSize);
        invalidateSelf();
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
        invalidateSelf();
    }

    public PointF getPosition() {
        return mPosition;
    }

    public void setPosition(PointF position) {
        this.mPosition = position;
        invalidateSelf();
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return mTextPaint.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        mTextPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    public TextDrawable(Callback cb, PointF position) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setCallback(cb);
        setPosition(position);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        super.onStateChange(state);

        updateColor();

        return true;
    }

    private void updateBounds() {

        if (mText != null) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), mBounds);
            setBounds(mBounds);
        }
    }

    @Override
    public void invalidateSelf() {

        updateBounds();
        super.invalidateSelf();
    }

    public void animate(float toScale, PointF toPosition, int toAlpha, long duration) {

        if (getPosition() == null) {
            setPosition(toPosition);
            setAlpha(toAlpha);
            setScale(toScale);
            return;
        }

        final float currentScale = getScale();
        final PointF currentPosition = getPosition();
        final int currentAlpha = getOpacity();

        ObjectAnimator scale = ObjectAnimator.ofFloat(this, "scale", currentScale, toScale);
        ObjectAnimator translate = ObjectAnimator.ofObject(this, "position", new PointEvaluator(), currentPosition, toPosition);
        ObjectAnimator alpha = ObjectAnimator.ofInt(this, "alpha", currentAlpha, toAlpha);

        AnimatorSet set = new AnimatorSet();
        set.play(scale).with(translate).with(alpha);
        set.setDuration(duration);
        set.start();
    }

    @Override
    public void draw(Canvas canvas) {

        if (mText == null || mText.equals("") || mPosition == null) return;

        float shiftX = (float) getBounds().width() - (mScale * (float) getBounds().width());
        shiftX *= mScale / 2;

        float shiftY = (float) getBounds().height() - (mScale * (float) getBounds().height());
        shiftY *= mScale / 2;

        canvas.save();

        canvas.translate(shiftX, shiftY);
        canvas.scale(mScale, mScale, mPosition.x - getBounds().width()/2, mPosition.y - getBounds().height()/2);

        canvas.drawText(mText, mPosition.x, mPosition.y, mTextPaint);

        canvas.restore();
    }
}
