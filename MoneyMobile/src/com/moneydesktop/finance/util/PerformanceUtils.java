package com.moneydesktop.finance.util;

import android.util.Log;

public class PerformanceUtils {
    
    public static final String TAG = "PerformanceUtils";

	private static int sCount = 0;
	private static long sStart = 0, sFrameStart = 0;
	
	public static void frame() {
		
		sCount++;
		
		if (sFrameStart == 0) {
			sFrameStart = System.currentTimeMillis();
		} else if (System.currentTimeMillis() - sFrameStart >= 1000) {
			Log.i(TAG, "FPS: " + sCount);
			sFrameStart = System.currentTimeMillis();
			sCount = 0;
		}
	}
	
	public static void start() {
		sStart = System.currentTimeMillis();
	}
	
	public static void finish() {
		long total = System.currentTimeMillis() - sStart;
		Log.i(TAG, "Time: " + total);
	}
}
