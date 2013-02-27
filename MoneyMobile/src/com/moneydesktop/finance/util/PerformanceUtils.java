package com.moneydesktop.finance.util;

import android.util.Log;

public class PerformanceUtils {
    
    public static final String TAG = "PerformanceUtils";

	private static int sCount = 0;
	private static long sStart = 0;
	
	public static void frame() {
		
		sCount++;
		
		if (sStart == 0) {
			sStart = System.currentTimeMillis();
		} else if (System.currentTimeMillis() - sStart >= 1000) {
			Log.i(TAG, "FPS: " + sCount);
			sStart = System.currentTimeMillis();
			sCount = 0;
		}
	}
	
	public static void start() {
		sStart = System.currentTimeMillis();
	}
	
	public static long finish() {
		return sStart - System.currentTimeMillis();
	}
}
