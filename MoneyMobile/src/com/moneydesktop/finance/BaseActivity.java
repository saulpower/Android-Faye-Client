package com.moneydesktop.finance;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class BaseActivity extends Activity {

    public static final int DEVICE_DISPLAY_SIZE_PHONE = 1;
    public static final int DEVICE_DISPLAY_SIZE_TABLET = 2;
    
    // We decided to call anything 7" or over a "tablet".
    private static final float MINIMUM_TABLET_SCREEN_INCHES = 7f;
	
	SharedPreferences mPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
    public SharedPreferences getSharedPreferences () {
        return mPreferences;
    }
    
    
    public static boolean isTablet (final Context context) {

        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        return getDeviceDisplaySize(context, metrics) == DEVICE_DISPLAY_SIZE_TABLET;
    }
    

    public static int getDeviceDisplaySize (final Context context, final DisplayMetrics metrics) {
    	
        final float widthInches = metrics.widthPixels / metrics.xdpi;
        final float heightInches = metrics.heightPixels / metrics.ydpi;
        final float screenSizeInInches = (float) Math.sqrt(Math.pow(widthInches, 2f) + Math.pow(heightInches, 2f));

        return screenSizeInInches > MINIMUM_TABLET_SCREEN_INCHES
                ? DEVICE_DISPLAY_SIZE_TABLET
                : DEVICE_DISPLAY_SIZE_PHONE;
    }

    /**
     * Returns the width and height measurement in pixels.
     * 
     * @return
     */
	public float[] getScreenMeasurements() {
		
		float[] values = new float[2];
		
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        
        values[0] = metrics.widthPixels;
        values[1] = metrics.heightPixels;
        
        return values;
	}
	
}
