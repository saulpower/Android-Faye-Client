package com.moneydesktop.finance.exception;

import android.content.Intent;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.SplashActivity;

import java.lang.Thread.UncaughtExceptionHandler;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	
	public static final String TAG = "CustomExceptionHandler";
    
    /* 
     * if any of the parameters are null, the respective functionality 
     * will not be used 
     */
    public CustomExceptionHandler() {
        Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
    	
        Log.e(TAG, "Error", e);
        
    	Intent i = new Intent(ApplicationContext.getContext(), SplashActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ApplicationContext.getContext().startActivity(i);
    }
}