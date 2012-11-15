package com.moneydesktop.finance;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.crittercism.app.Crittercism;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.handset.activity.LoginActivity;
import com.moneydesktop.finance.tablet.activity.LoginTabletActivity;

public class SplashActivity extends BaseActivity {
	
//	private final String TAG = "SplashActivity";

	protected int splashTime = 5000; // time to display the splash screen in ms
    private CountDownTimer timer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.splash_view);
        
        if (isTablet(this)) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        Crittercism.init(getApplicationContext(), "50258d166c36f91a1b000004");
                
        // Creates a CountDownTimer object
	    timer = new CountDownTimer(splashTime, 100) {

	        public void onTick(long millisUntilFinished) {}

	        public void onFinish() {
	        	endSplash();
	        }
	        
	     }.start();
	}
    
    @Override
    public boolean onTouchEvent (MotionEvent event) {
    	
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		timer.cancel();
    		endSplash();
    	}
    	
    	return true;
    }
    
    private void endSplash() {    	
    	Intent intent;
    	
    	if (isTablet(this)) {
    		intent = new Intent(getApplicationContext(), LoginTabletActivity.class);
    	} else {
    		intent = new Intent(getApplicationContext(), LoginActivity.class);
    	}
    	
    	intent.putExtras(getIntent());
    	startActivity(intent);
    	finish();
    }
}
