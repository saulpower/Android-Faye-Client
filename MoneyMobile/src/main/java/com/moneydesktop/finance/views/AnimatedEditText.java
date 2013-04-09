package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

import main.java.com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class AnimatedEditText extends EditText {

    public final String TAG = this.getClass().getSimpleName();

    private float mAxisXIn, mAxisXOut;

    private Camera mCamera;
    private String mPreviousText = "";
    private boolean mInit = false;
    private boolean mAnimating = false;
    private Rect mBoundsIn = new Rect();
    private Rect mBoundsOut = new Rect();
    private Bitmap mPrevious;
    private Canvas mCanvas;
    private boolean mNeedsDraw = true;
    private Paint mPaint;

    private Matrix mMatrixIn, mMatrixOut;

    public AnimatedEditText(Context context) {
        super(context);

        init();
    }

    public AnimatedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public AnimatedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {

        mCamera = new Camera();
        mMatrixIn = new Matrix();
        mMatrixOut = new Matrix();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!mInit) {
            prepPrevious();
            mInit = true;
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        mPreviousText = getText().toString();
        prepPrevious();
    }

    public void setAnimatedText(String text) {

        if (!mAnimating && mInit && !hasFocus() && !getText().toString().equals(text)) {
            animateTextChange(text);
        }
    }

    public void animateTextChange(final String text) {

        if (getPaint() == null || text == null) return;

        getPaint().getTextBounds(text, 0, text.length(), mBoundsIn);

        mAxisXIn = mBoundsIn.exactCenterX();

        float fromDegrees = FlipDirection.IN_TOP_BOTTOM.getStartDegreeForFirstView();
        float toDegrees = FlipDirection.IN_TOP_BOTTOM.getEndDegreeForFirstView();

        ObjectAnimator flipIn = ObjectAnimator.ofFloat(this, "rotateIn", fromDegrees, toDegrees);
        flipIn.setDuration(1100);
        flipIn.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
                setText(text);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                prepPrevious();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimating = false;
            }
        });

        fromDegrees = FlipDirection.OUT_TOP_BOTTOM.getStartDegreeForFirstView();
        toDegrees = FlipDirection.OUT_TOP_BOTTOM.getEndDegreeForFirstView();

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(this, "rotateOut", fromDegrees, toDegrees);
        flipOut.setDuration(1100);

        ObjectAnimator alpha = ObjectAnimator.ofInt(this, "paintAlpha", 255, 0);
        alpha.setDuration(1100);

        AnimatorSet set = new AnimatorSet();
        set.play(flipIn).with(flipOut).with(alpha);
        set.start();
    }

    private void prepPrevious() {

        if (mAnimating || getWidth() <= 0 || getHeight() <= 0) return;

        if (mPrevious == null) {
            mPrevious = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mPrevious);
            mPaint = new Paint();
        }

        getPaint().getTextBounds(mPreviousText, 0, mPreviousText.length(), mBoundsOut);
        mAxisXOut = mBoundsOut.exactCenterX();

        mCanvas.drawColor(Color.WHITE);

        mNeedsDraw = true;
        invalidate();
    }

    public void setPaintAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    public void setRotateIn(float rotate) {

        mCamera.save();
        mCamera.rotateX(rotate);
        mCamera.getMatrix(mMatrixIn);
        mCamera.restore();

        mMatrixIn.preTranslate(-mAxisXIn, -getHeight());
        mMatrixIn.postTranslate(mAxisXIn, getHeight());

        invalidate();
    }

    public void setRotateOut(float rotate) {

        mCamera.save();
        mCamera.rotateX(rotate);
        mCamera.getMatrix(mMatrixOut);
        mCamera.restore();

        mMatrixOut.preTranslate(-mAxisXOut, -getHeight());
        mMatrixOut.postTranslate(mAxisXOut, getHeight());

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (!mAnimating) {

            super.onDraw(canvas);

            if (mNeedsDraw) {
                mNeedsDraw = false;
                super.onDraw(mCanvas);
            }

            return;
        }

        canvas.save();
        canvas.concat(mMatrixOut);

        canvas.drawBitmap(mPrevious, 0, 0, mPaint);

        canvas.restore();

        canvas.save();
        canvas.concat(mMatrixIn);

        super.onDraw(canvas);

        canvas.restore();
    }
}
