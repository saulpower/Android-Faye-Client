package com.moneydesktop.finance.views;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.UiUtils;

@SuppressLint("NewApi")
public class AnchorView extends Drawable {
    
    public final String TAG = this.getClass().getSimpleName();

    private final int PADDING = 30;
    private final int WIDTH = 15;
    
    private Context mContext;
    private Paint mPaint;
    private Path mPath;
    private float mPosition;
    private float mPadding;
    private float mWidth;
    private float mHeight;
    
    private boolean mIsLeft;
    
    private AnchorMoveListener mListener;
    
    private Context getContext() {
        return mContext;
    }
    
    public void setPosition(float positionX) {
        mPosition = positionX;
        
        updateBounds();
        createPath();
        
        if (mListener != null) {
            mListener.anchorDidMove();
        }
        
        invalidateSelf();
    }
    
    public float getPosition() {
        return mPosition;
    }
    
    public void animateToPosition(float position) {
        
        ObjectAnimator move = ObjectAnimator.ofFloat(this, "position", position);
        move.setInterpolator(new DecelerateInterpolator());
        move.setDuration(300);
        move.start();
    }
    
    public void setAnchorMoveListener(AnchorMoveListener listener) {
        mListener = listener;
    }
    
    public AnchorView(Context context, float positionX, float height, boolean isLeft) {
        
        mContext = context;
        mPadding = UiUtils.getDynamicPixels(context, PADDING);
        mWidth = UiUtils.getDynamicPixels(context, WIDTH);
        mHeight = height;
        mIsLeft = isLeft;
        
        initPaint();
        setPosition(positionX);
    }
    
    private void initPaint() {
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getContext().getResources().getColor(R.color.gray7));
        mPaint.setStyle(Paint.Style.FILL);
    }
    
    private void createPath() {

        if (mPath == null) {
            mPath = new Path();
            mPath.setFillType(Path.FillType.EVEN_ODD);
        }
        
        float leftX = mPosition - mWidth / 2;
        float rightX = mPosition + mWidth / 2;
        float topY = mHeight / 3.25f;
        float pointY = mHeight / 5;
        
        mPath.reset();
        mPath.moveTo(leftX, mHeight);
        mPath.lineTo(leftX, topY);
        mPath.lineTo(mPosition, pointY);
        mPath.lineTo(rightX, topY);
        mPath.lineTo(rightX, mHeight);
        mPath.lineTo(leftX, mHeight);
        mPath.close();
    }
    
    private void updateBounds() {

        int left = (int) (mPosition - mWidth / 2.0f);
        int top = 0;
        int right = (int) (left + mWidth + mPadding);
        int bottom = (int) (top + mHeight);
        
        if (mIsLeft) {
            right = (int) (mPosition + mWidth / 2.0f);
            left = (int) (right - mWidth - mPadding);
        }
        
        setBounds(left, top, right, bottom);
    }
    
    @Override
    public void draw(Canvas canvas) {
        
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter cf) {}
    
    public interface AnchorMoveListener {
        public void anchorDidMove();
    }

}
