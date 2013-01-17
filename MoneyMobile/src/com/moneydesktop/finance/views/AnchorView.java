package com.moneydesktop.finance.views;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.PointEvaluator;
import com.moneydesktop.finance.util.UiUtils;

@SuppressLint("NewApi")
public class AnchorView extends Drawable {

    private final int PADDING = 30;
    private final int WIDTH = 15;
    
    private Context mContext;
    private Paint mPaint;
    private Path mPath;
    private PointF mPosition;
    private float mPadding;
    private float mWidth;
    private float mHeight;
    
    private boolean mIsLeft;
    
    private AnchorMoveListener mListener;
    
    private Context getContext() {
        return mContext;
    }
    
    public void setPosition(PointF position) {
        mPosition = position;
        
        updateBounds();
        createPath();
        
        if (mListener != null) {
            mListener.anchorDidMove();
        }
        
        invalidateSelf();
    }
    
    public PointF getPosition() {
        return mPosition;
    }
    
    public void animateToPosition(PointF position) {
        
        ObjectAnimator move = ObjectAnimator.ofObject(this, "position", new PointEvaluator(), position);
        move.setInterpolator(new DecelerateInterpolator());
        move.setDuration(300);
        move.start();
    }
    
    public void setAnchorMoveListener(AnchorMoveListener listener) {
        mListener = listener;
    }
    
    public AnchorView(Context context, PointF position, float height, boolean isLeft) {
        
        mContext = context;
        mPadding = UiUtils.getDynamicPixels(context, PADDING);
        mWidth = UiUtils.getDynamicPixels(context, WIDTH);
        mHeight = height;
        mIsLeft = isLeft;
        
        initPaint();
        setPosition(position);
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
        
        float leftX = mPosition.x - mWidth / 2;
        float rightX = mPosition.x + mWidth / 2;
        float topY = mPosition.y + mHeight / 3.25f;
        float pointY = mPosition.y + mHeight / 5;
        
        mPath.reset();
        mPath.moveTo(leftX, (mPosition.y + mHeight));
        mPath.lineTo(leftX, topY);
        mPath.lineTo(mPosition.x, pointY);
        mPath.lineTo(rightX, topY);
        mPath.lineTo(rightX, (mPosition.y + mHeight));
        mPath.lineTo(leftX, (mPosition.y + mHeight));
        mPath.close();
    }
    
    private void updateBounds() {

        int left = (int) (mPosition.x - mWidth / 2.0f);
        int top = (int) mPosition.y;
        int right = (int) (left + mWidth + mPadding);
        int bottom = (int) (top + mHeight);
        
        if (mIsLeft) {
            right = (int) (mPosition.x + mWidth / 2.0f);
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
    public void setAlpha(int alpha) {
        
    }

    @Override
    public void setColorFilter(ColorFilter cf) {}
    
    public interface AnchorMoveListener {
        public void anchorDidMove();
    }

}
