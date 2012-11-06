package com.moneydesktop.finance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.crittercism.app.Crittercism;

public class SplashActivity extends Activity {
	
//	private final String TAG = "SplashActivity";

	protected int splashTime = 5000; // time to display the splash screen in ms
    private CountDownTimer timer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.splash_view);
        
        Crittercism.init(getApplicationContext(), "50258d166c36f91a1b000004");
        
        ImageView splash = (ImageView) findViewById(R.id.splash_screen);
        splash.setBackgroundResource(R.drawable.splash);
        
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
    	
    	Intent i = new Intent(getApplicationContext(), LoginActivity.class);
		i.putExtras(getIntent());
    	
		startActivity(i);
		finish();
    }
}
