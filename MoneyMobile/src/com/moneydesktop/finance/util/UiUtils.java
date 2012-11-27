package com.moneydesktop.finance.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

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

    public static float getDensityRatio (final Activity context) {
        final DisplayMetrics metrics = new DisplayMetrics();
        final Display display = context.getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return (metrics.densityDpi / Constant.STANDARD_DPI);
    }
    
    public static float getDynamicPixels(Context context, float pixels) {
    	return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());
    }
}
