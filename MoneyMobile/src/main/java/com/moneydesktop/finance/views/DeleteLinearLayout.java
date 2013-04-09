package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.widget.LinearLayout;

import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.tablet.adapter.DeleteBaseAdapter;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.ObjectAnimator;

public class DeleteLinearLayout extends LinearLayout {

    public final String TAG = this.getClass().getSimpleName();

    private DeleteBaseAdapter<?> mAdapter;

    private String mIcon;
    private float mStartX, mEndX, mPercent;
    private Paint mPaint;
    private Rect mTextBounds = new Rect();
    private Rect mDeleteBounds;
    private boolean mDeletePressed = false;
    private boolean mIgnoreTouches = false;
    private OnCellDeletedListener mOnCellDeletedListener;

    public void setDeleteBaseAdapter(DeleteBaseAdapter<?> adapter) {
        mAdapter = adapter;
    }

    public void setOnCellDeletedListener(OnCellDeletedListener mOnCellDeletedListener) {
        this.mOnCellDeletedListener = mOnCellDeletedListener;
    }

    public DeleteLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIcon = context.getString(R.string.icon_trash);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getContext().getResources().getColor(R.color.budgetColorRedRingColor));
        mPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 32));
        mPaint.setTypeface(Fonts.getFont(Fonts.GLYPH));
        mPaint.setAlpha(0);
        mPaint.getTextBounds(mIcon, 0, 1, mTextBounds);
    }

    public void setDeleteAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidate();
    }

    public int getDeleteAlpha() {
        return mPaint.getAlpha();
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mDeleteBounds == null) {
            mDeleteBounds = new Rect(getWidth() - (int) (mTextBounds.width() * 2.5) - getPaddingRight(), 0, getWidth(), getHeight());
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();

        switch (action) {

            case MotionEvent.ACTION_DOWN: {

                if (mPercent == 1) {
                    mIgnoreTouches = true;
                    mDeletePressed = mDeleteBounds.contains((int) ev.getX(), (int) ev.getY());
                } else {
                    mStartX = ev.getX();
                    mEndX = getWidth() * 0.75f - mStartX;
                }

                break;
            }

            case MotionEvent.ACTION_UP: {

                mIgnoreTouches = false;
                setSelected(false);
                setPressed(false);
                getParent().requestDisallowInterceptTouchEvent(false);

                boolean clicked = false;

                if (mDeletePressed) {
                    mDeletePressed = false;

                    if (mDeleteBounds.contains((int) ev.getX(), (int) ev.getY())) {

                        if (mOnCellDeletedListener != null) {
                            post(new Runnable() {

                                @Override
                                public void run() {
                                    mOnCellDeletedListener.onCellDeleted();
                                }
                            });
                        }

                        setDeleteAlpha(0);
                        clicked = true;
                        playSoundEffect(SoundEffectConstants.CLICK);
                    }

                    fadeOutDelete();

                } else if (mPercent > 0.75) {
                    fadeInDelete();
                } else {
                    fadeOutDelete();
                }

                if (mPercent == 1 || clicked) return true;

                break;
            }

            case MotionEvent.ACTION_MOVE: {

                if (mIgnoreTouches) break;

                float x = ev.getX() - mStartX;

                if (x > 0) {

                    mPercent = (x / mEndX) > 1 ? 1 : (x / mEndX);

                } else {

                    mPercent = 0;
                }

                setDeleteAlpha((int) (mPercent * 255));

                getParent().requestDisallowInterceptTouchEvent((mPercent > 0.3));

                break;
            }

            case MotionEvent.ACTION_CANCEL: {

                fadeOutDelete();
                setSelected(false);
                setPressed(false);
                getParent().requestDisallowInterceptTouchEvent(false);

                break;
            }
        }

        return super.onTouchEvent(ev);
    }

    public void fadeOutDelete() {
        mPercent = 0;

        final int current = mPaint.getAlpha();
        ObjectAnimator fade = ObjectAnimator.ofInt(this, "deleteAlpha", current, 0);
        fade.setDuration(300);
        fade.start();
    }

    public void fadeInDelete() {
        mPercent = 1;

        if (mAdapter != null) {
            mAdapter.setDeletingCell(this);
        }

        final int current = mPaint.getAlpha();
        ObjectAnimator fade = ObjectAnimator.ofInt(this, "deleteAlpha", current, 255);
        fade.setDuration(300);
        fade.start();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(mIcon, mDeleteBounds.left + (mTextBounds.width() * 3 / 4), (mDeleteBounds.height() / 2) + (mTextBounds.height() / 2), mPaint);
    }

    public interface OnCellDeletedListener {
        public void onCellDeleted();
    }

}
