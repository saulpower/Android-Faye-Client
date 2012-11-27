package com.moneydesktop.finance.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SpinnerView extends ImageView {

	public static final String TAG = "SpinnerView";

	private final String TYPE_DRAWABLE = "drawable";
	
	private static final int DELAY = 125;

	private boolean mIsPlaying = false;

	private ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();

	private int playFrame = 0;
    private long lastTick = 0;
    
	private Context context;
	
	public SpinnerView(Context context) {
		super(context);
		
		load(context);
	}

	public SpinnerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		load(context);
	}
	
	public SpinnerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		load(context);
	}
	
	private void load(Context context) {

		this.context = context;
		
		loadAnimation("phone_loading_big", 8);
		startAnimation();
	}
	
	@Override
	protected void onDraw(Canvas c) {
		
		if (mIsPlaying) {
			
		    if (playFrame >= bitmapList.size())
		        playFrame = 0;
		    	
	        long time = (System.currentTimeMillis() - lastTick);
	        int drawX = 0;
	        int drawY = 0;
	        
	        if (time >= DELAY) {
	        	
	            lastTick = System.currentTimeMillis();
	            c.drawBitmap(bitmapList.get(playFrame), drawX, drawY, null);
	            playFrame++;
	            postInvalidate();
	            
	        } else  {
	        	
	            c.drawBitmap(bitmapList.get(playFrame), drawX, drawY, null);
	            postInvalidate();
	        }
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
