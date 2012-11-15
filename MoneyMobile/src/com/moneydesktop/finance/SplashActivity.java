package com.moneydesktop.finance;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.crittercism.app.Crittercism;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.handset.activity.DashboardActivity;
import com.moneydesktop.finance.handset.activity.LoginActivity;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.activity.LoginTabletActivity;
import com.moneydesktop.finance.model.User;

public class SplashActivity extends BaseActivity {
	
	public final String TAG = "SplashActivity";

	protected int splashTime = 5000; // time to display the splash screen in ms
    private CountDownTimer timer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.splash_view);

        resetApp();
        
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
	
	private void resetApp() {
		
        User.clear();
	}
    
    private void endSplash() {    	
    	    	
    	Intent i = null;
    	
    	if (User.getCurrentUser() != null) {
			
        	if (isTablet(this)) {
        		i = new Intent(getApplicationContext(), DashboardTabletActivity.class);
        	} else {
        		i = new Intent(getApplicationContext(), DashboardActivity.class);
        	}
	    	
		} else {
			
	    	if (isTablet(this)) {
	    		i = new Intent(getApplicationContext(), LoginTabletActivity.class);
	    	} else {
	    		i = new Intent(getApplicationContext(), LoginActivity.class);
	    	}
	    	
		}
    	
		i.putExtras(getIntent());
    	
    	i.putExtras(getIntent());
    	startActivity(i);
    	finish();
    }
}
