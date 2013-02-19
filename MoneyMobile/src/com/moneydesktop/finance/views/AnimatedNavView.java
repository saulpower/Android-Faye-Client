package com.moneydesktop.finance.views;

import java.util.ArrayList;
import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.ObjectAnimator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

@SuppressLint("NewApi")
public class AnimatedNavView extends View implements OnClickListener {
	
    public final String TAG = this.getClass().getSimpleName();

	private final float BACK_SCALE = 0.8f;
	
	private List<String> mStack = new ArrayList<String>();
	private TextDrawable mBackLabel, mTitleLabel, mTempLabel;
	private PointF mBackPosition, mTitlePosition, mLeftPosition, mRightPosition;
	private int mLeftPadding = 0, mBottomPadding = 0;
	
	private Typeface mTypeface;
	private float mFontSize = -1;
	private int mFontColor = -1;
	
	private int mStackSize = 0;
	
	private Handler mHandler;
	
	private Paint mTextPaint;
	private Rect mTextBounds = new Rect();
	private String mText;
	
	private NavigationListener mNavigationListener;

	public void setNavigationListener(NavigationListener mNavigationListener) {
		this.mNavigationListener = mNavigationListener;
	}
	
	public void setArrowAlpha(int alpha) {
		mTextPaint.setAlpha(alpha);
		invalidate();
	}
	
	public int getArrowAlpha() {
		return mTextPaint.getAlpha();
	}

	public AnimatedNavView(Context context) {
		super(context);
		
		init();
	}

	public AnimatedNavView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}
	
    public AnimatedNavView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	
    	init();
    }
    
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
    	initPositions();
    }
	
	public void setNavigationTitle(String title) {
		mStackSize++;
		mTitleLabel.setText(title);
	}
	
	public void popNav() {

		if (mStackSize <= 1) return;

		mStackSize--;
		
		if (mStack.size() > 0) {
		
			int last = mStack.size() - 1;
			
			mTempLabel.setText(mStack.get(last));
			mStack.remove(last);
		}
		
		moveTitle(true);
		moveBack(true);
		moveTemp(true);
		
		if (mStackSize == 1) {
			configureArrow(false);
		}

		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				final TextDrawable holder = mBackLabel;

				mBackLabel = mTempLabel;
				mTempLabel = mTitleLabel;
				mTitleLabel = holder;
				
				invalidate();
			}
		}, 400);
		
		if (mNavigationListener != null) {
    		mNavigationListener.onNavigationPopped();
    	}
	}
	
	public void pushNav(String title) {
		
		mStackSize++;
		
		mTempLabel.setText(title);
		
		if (mBackLabel.getText() != null && !mBackLabel.getText().equals("")) {
			mStack.add(mBackLabel.getText());
		}
		
		moveTitle(false);
		moveBack(false);
		moveTemp(false);
		
		if (mStackSize == 2) {
			configureArrow(true);
		}

		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				final TextDrawable holder = mTempLabel;
				
				mTempLabel = mBackLabel;
				mBackLabel = mTitleLabel;
				mTitleLabel = holder;
				
				invalidate();
			}
		}, 400);
	}
	
	private void configureArrow(boolean in) {
		
		ObjectAnimator fade = ObjectAnimator.ofInt(this, "arrowAlpha", in ? 0 : 255, in ? 255 : 0);
		fade.setDuration(400);
		fade.start();
	}
	
	public void setTypeface(Typeface typeface) {
		mTypeface = typeface;
		applyTypeface();
	}
	
	public void setFontSize(float fontSize) {
		mFontSize = fontSize;
		applyFontSize();
	}
	
	public void setFontColor(int color) {
		mFontColor = color;
		applyFontColor();
	}
	
	private void applyTypeface() {
		
		if (mTypeface == null) return;
		
		mBackLabel.setTypeFace(mTypeface);
		mTitleLabel.setTypeFace(mTypeface);
		mTempLabel.setTypeFace(mTypeface);
	}
	
	private void applyFontSize() {
		
		mBackLabel.setFontSize(mFontSize);
		mTitleLabel.setFontSize(mFontSize);
		mTempLabel.setFontSize(mFontSize);
	}
	
	private void applyFontColor() {
		
		mBackLabel.setColor(mFontColor);
		mTitleLabel.setColor(mFontColor);
		mTempLabel.setColor(mFontColor);
	}
	
	private void init() {
        
		setOnClickListener(this);
		
		mHandler = new Handler();
		
		mLeftPadding = getPaddingLeft();
		mBottomPadding = getPaddingBottom();
		setPadding(0, 0, 0, 0);
		
		mBackLabel = new TextDrawable(this, mBackPosition);
		mTitleLabel = new TextDrawable(this, mTitlePosition);
		mTempLabel = new TextDrawable(this, mRightPosition);
		
		mText = getContext().getString(R.string.icon_left_arrow);
		
		mTextPaint = new Paint();
		mTextPaint.setTypeface(Fonts.getFont(Fonts.GLYPH));
		mTextPaint.setTextSize(UiUtils.getScaledPixels(getContext(), 12));
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
		mTextPaint.setAlpha(0);
	}
	
	private void initPositions() {
		
		mLeftPosition = new PointF(getMeasuredWidth() / -2, getMeasuredHeight() / 2);
		mBackPosition = new PointF(mLeftPadding, getMeasuredHeight() / 2);
		mTitlePosition = new PointF(mLeftPadding, getMeasuredHeight() - mBottomPadding);
		mRightPosition = new PointF(getMeasuredWidth() / 2 + mLeftPadding, getMeasuredHeight() - mBottomPadding);
		
		mBackLabel.setPosition(mBackPosition);
		mTitleLabel.setPosition(mTitlePosition);
		mTempLabel.setPosition(mRightPosition);
		
		invalidate();
	}
	
	private void moveTitle(boolean pop) {
		
		if (pop) {
			
			mTitleLabel.animate(1, mRightPosition, 0, 400);
			
		} else {

			mTitleLabel.animate(BACK_SCALE, mBackPosition, 255, 400);
		}
	}
	
	private void moveBack(boolean pop) {

		if (pop) {
			
			mBackLabel.animate(1, mTitlePosition, 255, 400);
			
		} else {

			mBackLabel.animate(BACK_SCALE, mLeftPosition, 0, 400);
		}
	}
	
	private void moveTemp(boolean pop) {

		mTempLabel.setAlpha(0);
		
		if (pop) {
			
			mTempLabel.setPosition(mLeftPosition);
			mTempLabel.animate(BACK_SCALE, mBackPosition, 255, 400);
			
		} else {

			mTempLabel.setPosition(mRightPosition);
			mTempLabel.animate(1, mTitlePosition, 255, 400);
		}
	}
	
	@Override
	public void invalidateDrawable(Drawable who) {
		super.invalidateDrawable(who);
		
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.save();
		canvas.translate(mTextBounds.width(), 0);
		
		mBackLabel.draw(canvas);
		mTitleLabel.draw(canvas);
		mTempLabel.draw(canvas);
		
		canvas.restore();
		
		canvas.drawText(mText, mBackPosition.x - mTextBounds.width(), mBackPosition.y, mTextPaint);
	}
	
	public interface NavigationListener {
		public void onNavigationPopped();
	}

	@Override
	public void onClick(View v) {
		
		if (mBackLabel != null || mBackLabel.getText().equals("")) {
    		popNav();
    	}
	}

}
