package com.moneydesktop.finance.views;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.moneydesktop.finance.R;

public class SpinnerView extends ImageView {

	public static final String TAG = "SpinnerView";

	private final String TYPE_DRAWABLE = "drawable";
	
	private static final int DELAY = 125;

	private boolean mIsPlaying = false;

	private ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();

	private int playFrame = 0;
    private long lastTick = 0;
    
	private Context context;
	private Paint mPaint;
	private ColorFilter mFilter;
	
	private void setSpinnerColor(int color) {
		
		if (color != -1) {

			mFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			mPaint.setColorFilter(mFilter);
		}
	}
	
	public SpinnerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		load(context, attrs);
	}
	
	private void load(Context context, AttributeSet attrs) {

		this.context = context;
		mPaint = new Paint();
		
		if (attrs != null) {
            
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Spinner);

            setSpinnerColor(a.getColor(R.styleable.Spinner_spinnerColor, -1));
            
            a.recycle();
        }
		
		loadAnimation("phone_loading_big", 8);
		startAnimation();
	}
	
	@Override
	public void startAnimation(Animation animation) {
		stopAnimation();
		
		super.startAnimation(animation);
	}
	
	@Override
	protected void onDraw(Canvas c) {
		
		if (mIsPlaying) {
			
		    if (playFrame >= bitmapList.size()) playFrame = 0;
		    	
	        long time = (System.currentTimeMillis() - lastTick);
	        
	        if (time >= DELAY) {
	        	
	            lastTick = System.currentTimeMillis();
	            c.drawBitmap(bitmapList.get(playFrame), 0, 0, mPaint);
	            playFrame++;
	            
	        } else  {
	        	
	            c.drawBitmap(bitmapList.get(playFrame), 0, 0, mPaint);
	        }
	        
            postInvalidate();
		}
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
    	int width = 0;
    	int height = 0;
    	
    	if (bitmapList.size() > 0) {
    		
    		Bitmap sample = bitmapList.get(0);
    		width = sample.getWidth();
    		height = sample.getHeight();
    	}

        setMeasuredDimension(width, height);
    }
    
    private void loadAnimation(String prefix, int nframes) {
    	
    	bitmapList.clear();
        
    	for (int x = 1; x < (nframes + 1); x++) {
        	
        	String name = prefix + "_" + x;
        	int resourceId = context.getResources().getIdentifier(name, TYPE_DRAWABLE, context.getPackageName());
        	
        	BitmapDrawable d = (BitmapDrawable) context.getResources().getDrawable(resourceId);
        	bitmapList.add(d.getBitmap());
        }
    }
    
    public void startAnimation() {
    	
    	mIsPlaying = true;
    	postInvalidate();
    }
    
    public void stopAnimation() {
    	
    	mIsPlaying = false;
    }

}
