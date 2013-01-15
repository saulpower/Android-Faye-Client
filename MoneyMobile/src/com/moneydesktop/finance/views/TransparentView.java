package com.moneydesktop.finance.views;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.UiUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class TransparentView extends View {

	public final String TAG = this.getClass().getSimpleName();
	private Paint bg;
	private int mX;
	private int mY;
	private int mWidth;
	private int mHeight;
	private Context mContext;
    
    
    private Activity mActivity;

	
	public TransparentView(Context context, AttributeSet attrs) {
		super(context, attrs);
        
		mContext = context;
		mActivity = (Activity)mContext;
		
		bg = new Paint();
		bg.setColor(mActivity.getResources().getColor(R.color.transparent50percent));
		
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//Top
		canvas.drawRect(new Rect(0, 0, UiUtils.getScreenWidth(mActivity), mY), bg);
		
		//Bottom
		canvas.drawRect(new Rect(0, mY + mHeight, UiUtils.getScreenWidth(mActivity), UiUtils.getScreenHeight(mActivity)), bg);
		
		//Right
		canvas.drawRect(new Rect(mX, mY, UiUtils.getScreenWidth(mActivity), mY + mHeight), bg);
		
		//Left
		canvas.drawRect(new Rect(0, mY, mX - mWidth, mY + mHeight), bg);
	}


	public void setTransparentArea(int x, int y, int width, int height) {
		mX = x;
		mY = y;
		mWidth = width;
		mHeight = height;
	
		invalidate();
	}
	
	public void setViewVisibility (int visibility) {
		setVisibility(visibility);
	}
	
}