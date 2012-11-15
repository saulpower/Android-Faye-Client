package com.moneydesktop.finance.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Intent;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.SplashActivity;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	
	public static final String TAG = "CustomExceptionHandler";

    private UncaughtExceptionHandler defaultUEH;
    
    /* 
     * if any of the parameters are null, the respective functionality 
     * will not be used 
     */
    public CustomExceptionHandler() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
    	
    	Intent i = new Intent(ApplicationContext.getContext(), SplashActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ApplicationContext.getContext().startActivity(i);
    }
}