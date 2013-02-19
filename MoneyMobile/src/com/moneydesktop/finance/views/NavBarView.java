package com.moneydesktop.finance.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;

@SuppressLint("NewApi")
public class NavBarView extends TextView {
    
    public final String TAG = this.getClass().getSimpleName();
	
	private float mRotation = 0;
	private ObjectAnimator mRotate;
	private Rect mTextBounds = new Rect();
	private boolean mIsRefresh = false;
	private float mAdjustment = 0;
	private Handler mHandler;
	
	private Paint paint;
	
	public float getRotation() {
		return mRotation;
	}

	public void setRotation(float mRotation) {
		this.mRotation = mRotation;
		invalidate();
	}

	public NavBarView(Context context) {
		super(context);
		
		init();
	}

	public NavBarView(Context context, AttributeSet attrs) {
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
		
		if (ApplicationContext.isTablet()) {
			mAdjustment = UiUtils.getDynamicPixels(getContext(), 1.6f);
		}
		
		mIsRefresh = (getText().toString().equals(getContext().getString(R.string.icon_refresh)) || getText().toString().equals(getContext().getString(R.string.nav_icon_refresh)));
		
		if (!mIsRefresh) return;
		
		mRotate = ObjectAnimator.ofFloat(this, "rotation", 360, 0);
		mRotate.setDuration(2000);
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
				setRotation(0);
			}
		});
		
		if (SyncEngine.sharedInstance().isSyncing()) {
			startRotation();
		}
	}
	
	private void startRotation() {

		if (!mIsRefresh || mRotate.isRunning()) return;
		
		mRotate.start();
	}
	
	private void stopRotation() {

		if (!mIsRefresh) return;
		
		mRotate.cancel();
		setRotation(0);
	}
	
	public void onEvent(final SyncEvent event) {
		
		// Make sure we run on the UI Thread
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {

				if (event.isFinished()) {
					stopRotation();
				} else {
					startRotation();
				}
			}
		});
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		EventBus.getDefault().register(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		EventBus.getDefault().unregister(this);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		canvas.save();

		String text = getText().toString();
		getPaint().getTextBounds(text, 0, text.length(), mTextBounds);

		float x = mTextBounds.exactCenterX() + mAdjustment;
		float y = (getHeight() - mTextBounds.height()) + mTextBounds.height() / 2;
		
		if (ApplicationContext.isTablet()) y = getHeight() / 2 + mAdjustment;
		
		canvas.rotate(mRotation, x, y);
		
		super.onDraw(canvas);
		
		canvas.restore();
	}
}
