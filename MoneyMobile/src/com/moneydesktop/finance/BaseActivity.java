package com.moneydesktop.finance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.FloatMath;

import com.moneydesktop.finance.data.Enums.LockType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.handset.activity.LockCodeHandsetActivity;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.LockEvent;
import com.moneydesktop.finance.shared.LockCodeFragment;
import com.moneydesktop.finance.tablet.activity.LockCodeTabletActivity;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;

abstract public class BaseActivity extends FragmentActivity {
	
	public final String TAG = this.getClass().getSimpleName();

    public static final int DEVICE_DISPLAY_SIZE_PHONE = 1;
    public static final int DEVICE_DISPLAY_SIZE_TABLET = 2;
    
    // We decided to call anything 7" or over a "tablet".
    private static final float MINIMUM_TABLET_SCREEN_INCHES = 7f;
    private static final float MINIMUM_LARGE_TABLET_SCREEN_INCHES = 9f;
    
    public boolean isTablet(Context context) {

        DisplayMetrics metrics = UiUtils.getDisplayMetrics(context);
        
        ApplicationContext.setIsLargeTablet(isLargeTablet(context, metrics));

        int size = getDeviceDisplaySize(context, metrics) > MINIMUM_TABLET_SCREEN_INCHES
                ? DEVICE_DISPLAY_SIZE_TABLET
                : DEVICE_DISPLAY_SIZE_PHONE;
        
        return size == DEVICE_DISPLAY_SIZE_TABLET && android.os.Build.VERSION.SDK_INT >= 11;
    }

	public float getDeviceDisplaySize(final Context context, final DisplayMetrics metrics) {
    	
        final float widthInches = metrics.widthPixels / metrics.xdpi;
        final float heightInches = metrics.heightPixels / metrics.ydpi;
        final float screenSizeInInches = FloatMath.sqrt((float) Math.pow(widthInches, 2f) + (float) Math.pow(heightInches, 2f));
        
        return screenSizeInInches;
    }
    
    private boolean isLargeTablet(Context context, DisplayMetrics metrics) {
        
        float size = getDeviceDisplaySize(context, metrics);
        
        return size > MINIMUM_LARGE_TABLET_SCREEN_INCHES;
    }
    
	protected final long TRANSITION_DURATION = 300;

	protected FragmentManager mFm;
    
    protected int mFragmentCount = 0;
	protected boolean mOnFragment = false;
	
	private SharedPreferences mPreferences;
	
	private static long sPause;
	
	public static boolean sInForeground = false;
	
    public SharedPreferences getSharedPreferences () {
        return mPreferences;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	mFm = getSupportFragmentManager();
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sInForeground = true;
		EventBus.getDefault().register(this);
		
		lockScreenCheck();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		sInForeground = false;
		if (!(this instanceof SplashActivity)) sPause = System.currentTimeMillis();
		EventBus.getDefault().unregister(this);
	}
	
	/**
	 * Update the navigation bar with the passed in title.  Configure the
	 * back button if necessary.
	 * 
	 * @param titleString the title for the navigation bar
	 * @param navButtons 
	 * @return 
	 */
	public void updateNavBar(String titleString) {}
	
	protected void updateNavBar(String titleString, boolean fragmentTitle) {}
	
	/**
	 * Pop back on the fragment manager's stack of fragments.
	 * Update the navigation bar based on our fragment count.
	 */
	protected void navigateBack() {
		
		mFragmentCount--;
		getSupportFragmentManager().popBackStack();
		
		updateNavBar(mFragmentCount == 0 ? getActivityTitle() : null, false);
		
		if (mFragmentCount == 0) mOnFragment = false;
	}
	
	/**
	 * Called whenever a new fragment has been added and
	 * attached itself to the host activity.
	 * 
	 * @param fragment
	 */
	public void onFragmentAttached(BaseFragment fragment) {
		
		if (mOnFragment) mFragmentCount++;
	}
	
	public void modalActivity(Class<?> key) {

		Intent intent = new Intent(this, key);
		startActivity(intent);
		overridePendingTransition(R.anim.in_up, R.anim.none);
	}
	
	public void onEvent(LockEvent event) {
		
		showLockScreen(event.getType());
	}
	
	private void lockScreenCheck() {

		if (500 < (System.currentTimeMillis() - sPause) && !(this instanceof SplashActivity)) {
			
			String code = Preferences.getString(Preferences.KEY_LOCK_CODE, "");
			
			if (!code.equals("")) showLockScreen(LockType.LOCK);
		}
	}
	
	private void showLockScreen(LockType type) {
		
		if (!ApplicationContext.isLockShowing()) {
			
		    ApplicationContext.setLockShowing(true);
			
			Intent intent = new Intent(this, ApplicationContext.isTablet() ? LockCodeTabletActivity.class : LockCodeHandsetActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(LockCodeFragment.EXTRA_LOCK, type);
			startActivity(intent);
			overridePendingTransition(R.anim.in_up, R.anim.none);
		}
	}
	
	/**
	 * Called to notify children views they have appeared and
	 * transition animations have completed.
	 */
	protected void viewDidAppear() {
		
		EventBus.getDefault().post(new EventMessage().new ParentAnimationEvent(true, true));
	}
    
    public abstract String getActivityTitle();
}
