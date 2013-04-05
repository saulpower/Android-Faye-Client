package com.moneydesktop.finance.util;

import android.os.Debug;
import android.util.Log;
import android.view.animation.AnimationUtils;

public class PerformanceUtils {
    
    public static final String TAG = "PerformanceUtils";

	private static int sCount = 0;
	private static long sStart = 0, sFrameStart = 0;
	
	public static void frame() {
		
		sCount++;
		
		if (sFrameStart == 0) {
			sFrameStart = AnimationUtils.currentAnimationTimeMillis();
		} else if (AnimationUtils.currentAnimationTimeMillis() - sFrameStart >= 1000) {
			Log.i(TAG, "FPS: " + sCount);
			sFrameStart = AnimationUtils.currentAnimationTimeMillis();
			sCount = 0;
		}
	}
	
	public static void start(String traceName) {
        Debug.startMethodTracing(traceName);
		sStart = AnimationUtils.currentAnimationTimeMillis();
	}
	
	public static void finish() {
		long total = AnimationUtils.currentAnimationTimeMillis() - sStart;
        Log.i(TAG, "Time: " + total);

        Debug.stopMethodTracing();
	}
}
