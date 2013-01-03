package com.moneydesktop.finance.util;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.drawable;
import com.moneydesktop.finance.data.Constant;

public class UiUtils {

    public static int getScreenWidth (Activity activity) {
           final DisplayMetrics metrics = new DisplayMetrics();
           activity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);

           return metrics.widthPixels;
       }

    public static int getScreenHeight (Activity activity) {
           final DisplayMetrics metrics = new DisplayMetrics();
           activity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);

           return metrics.heightPixels;
       }

    public static int getMinimumPanalWidth (final Activity activity) {
        double ratio = getDensityRatio(activity);
        return Math.max(Math.min(getScreenWidth(activity), getScreenHeight(activity)) / 5, (int)(170 * ratio));
    }
    
    public static int getMinimumPanalHeight (final Activity activity) {
        return getScreenHeight(activity);
    }

    public static float getDensityRatio (final Activity context) {
        final DisplayMetrics metrics = new DisplayMetrics();
        final Display display = context.getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return (metrics.densityDpi / Constant.STANDARD_DPI);
    }
    
    /**
     * This method convets dp unit to equivalent device specific value in pixels. 
     * 
     * @param dp      -- A value in dp. Which we need to convert into pixels
     * @param context -- Context to get resources and device specific display metrics
     * @return A float value to represent Pixels equivalent to dp according to device
     */
    public static float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi/160f);
        return px;
    }
    
    /**
     * This method converts device specific pixels to device independent pixels.
     * 
     * @param px      -- A value in pixels. Which we need to convert into db
     * @param context -- Context to get resources and device specific display metrics
     * @return A float value to represent db equivalent to px value
     */
    public static float convertPixelsToDp(float px,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;

    }

    /**
     * Returns the width and height measurement in pixels.
     * 
     * @return
     */
	public static float[] getScreenMeasurements(Context context) {
		
		float[] values = new float[2];
		
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        
        values[0] = metrics.widthPixels;
        values[1] = metrics.heightPixels;
        
        return values;
	}
   
    public static float getDynamicPixels(Context context, float pixels) {
    	return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());
    }

}
