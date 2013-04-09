package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.SyncEngine;
import main.java.com.moneydesktop.finance.model.EventMessage.SyncEvent;
import main.java.com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

import de.greenrobot.event.EventBus;

public class BankRefreshIcon extends TextView {

    public final String TAG = this.getClass().getSimpleName();

    private float mRotation = 0;
    private ObjectAnimator mRotate;
    private Rect mTextBounds = new Rect();
    private float mAdjustment = 0;
    private Handler mHandler;

    private boolean mIsRegistered = false;

    private Paint paint;

    public float getRotation() {
        return mRotation;
    }

    public void setRotate(float mRotation) {
        this.mRotation = mRotation;
        invalidate();
    }

    public BankRefreshIcon(Context context) {
        super(context);

        init();
    }

    public BankRefreshIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        init();
    }

    private void init() {

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2);

        mHandler = new Handler();

        mAdjustment = UiUtils.getDynamicPixels(getContext(), 1.6f);


        mRotate = ObjectAnimator.ofFloat(this, "rotate", 360, 0);
        mRotate.setDuration(2000);
        mRotate.setRepeatCount(mRotate.INFINITE);
        mRotate.setInterpolator(new LinearInterpolator());
        mRotate.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mRotate.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setRotate(0);
            }
        });

        startRotation();
    }

    private void startRotation() {

        if (mRotate.isRunning()) return;

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mRotate.start();
                }
            });
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.save();

        String text = getText().toString();
        getPaint().getTextBounds(text, 0, text.length(), mTextBounds);

        float x = getWidth() /2 + mAdjustment;

        float y = getHeight() / 2 + mAdjustment;

        canvas.rotate(mRotation, x, y);

        super.onDraw(canvas);

        canvas.restore();
    }
}
