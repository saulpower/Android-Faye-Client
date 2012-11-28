package com.moneydesktop.finance.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
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

    
    public static void setupTitleBar(Activity activity, String titleName, String userEmail, 
    		boolean displayIcon1To3,
    		boolean displayIcon4,
    		int srcIDIcon1,
    		int srcIDIcon2,
    		int srcIDIcon3,
    		int srcIDIcon4) {
    	
    	TextView test = (TextView)activity.findViewById(R.id.title_bar_name);
    	test.setText(titleName + ((userEmail != null) ? ( " " + userEmail) : ("")));
    	
    	ImageView icon1 = (ImageView)activity.findViewById(R.id.title_bar_icon1);
    	ImageView icon2 = (ImageView)activity.findViewById(R.id.title_bar_icon2);
    	ImageView icon3 = (ImageView)activity.findViewById(R.id.title_bar_icon3);
    	ImageView icon4 = (ImageView)activity.findViewById(R.id.title_bar_icon4);
    	
    	if (displayIcon1To3) {
    		icon1.setBackgroundResource(srcIDIcon1);
    		icon1.setVisibility(View.VISIBLE);
    		
    		icon2.setBackgroundResource(srcIDIcon2);
    		icon2.setVisibility(View.VISIBLE);
    		
    		icon3.setBackgroundResource(srcIDIcon3);
    		icon4.setVisibility(View.VISIBLE);
    	} else if (displayIcon4) {
    		icon4.setBackgroundResource(srcIDIcon4);
    		icon4.setVisibility(View.VISIBLE);
    	}	
    }
    
    public static float getDynamicPixels(Context context, float pixels) {
    	return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());
    }

}
