
package com.moneydesktop.finance;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.handset.activity.LoginHandsetActivity;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.activity.LoginTabletActivity;

public class SplashActivity extends BaseActivity {
    
    public final String TAG = this.getClass().getSimpleName();

    protected int mSplashTime = 5000; // time to display the splash screen in ms
    private Handler mHandler;
    private Runnable mTask = new Runnable() {
        
        @Override
        public void run() {
            
            endSplash();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_view);

        Log.i(TAG, "onCreate");
        
        if (isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // Creates a CountDownTimer object
        mHandler = new Handler();
        mHandler.postDelayed(mTask, mSplashTime);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mHandler.removeCallbacks(mTask);
            endSplash();
        }

        return true;
    }

    private void endSplash() {

        Intent i = new Intent(getApplicationContext(), DashboardHandsetActivity.class);

        ApplicationContext.setIsTablet(isTablet(this));
        ApplicationContext.setIsLargeTablet(isLargeTablet(this));
        
        if (User.getCurrentUser() != null) {

            if (ApplicationContext.isTablet()) {
                i = new Intent(getApplicationContext(), DashboardTabletActivity.class);
            }

        } else {

            if (ApplicationContext.isTablet()) {
                i = new Intent(getApplicationContext(), LoginTabletActivity.class);
            } else {
                i = new Intent(getApplicationContext(), LoginHandsetActivity.class);
            }

        }
        
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtras(getIntent());
        startActivity(i);
        overridePendingTransition(R.anim.fade_in_fast, R.anim.none);
        finish();
    }

    @Override
    public String getActivityTitle() {
        return null;
    }
}
