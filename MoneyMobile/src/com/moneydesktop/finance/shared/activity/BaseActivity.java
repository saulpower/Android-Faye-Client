package com.moneydesktop.finance.shared.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.SplashActivity;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.LockType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.handset.activity.PopupHandsetActivity;
import com.moneydesktop.finance.model.EventMessage.BackEvent;
import com.moneydesktop.finance.model.EventMessage.FeedbackEvent;
import com.moneydesktop.finance.model.EventMessage.LockEvent;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.shared.fragment.LockCodeFragment;
import com.moneydesktop.finance.tablet.activity.LockCodeTabletActivity;
import com.moneydesktop.finance.util.UiUtils;
import de.greenrobot.event.EventBus;

import java.util.HashMap;
import java.util.Map;

abstract public class BaseActivity extends FragmentActivity {
	
	public final String TAG = this.getClass().getSimpleName();

    public static final int DEVICE_DISPLAY_SIZE_PHONE = 1;
    public static final int DEVICE_DISPLAY_SIZE_TABLET = 2;
    
    /** We decided to call anything 7" or over a "tablet" */
    private static final float MINIMUM_TABLET_SCREEN_INCHES = 7f;
    private static final float MINIMUM_LARGE_TABLET_SCREEN_INCHES = 9f;
    
	protected final long TRANSITION_DURATION = 300;

	protected FragmentManager mFragmentManager;

	protected BaseFragment mFragment;
    private int mStackCount = 0;
	
	private SharedPreferences mPreferences;
	
	private static long sPause;
	
	public static boolean sInForeground = false;

    protected Map<FragmentType, BaseFragment> mFragments = new HashMap<FragmentType, BaseFragment>();

    public int getStackCount() {
        return mStackCount;
    }

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
        final float screenSizeInInches = (float) Math.sqrt((float) Math.pow(widthInches, 2f) + (float) Math.pow(heightInches, 2f));

        return screenSizeInInches;
    }

    private boolean isLargeTablet(Context context, DisplayMetrics metrics) {

        float size = getDeviceDisplaySize(context, metrics);

        return size > MINIMUM_LARGE_TABLET_SCREEN_INCHES;
    }
    
    public void setCurrentFragment(BaseFragment fragment) {
    	mFragment = fragment;
    }
	
    public SharedPreferences getSharedPreferences () {
        return mPreferences;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	mFragmentManager = getSupportFragmentManager();
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
	 */
	public void updateNavBar(String titleString) {}
	
	/**
	 * Called whenever a new fragment has been added and
	 * attached itself to the host activity.
	 * 
	 * @param fragment
	 */
	public void onFragmentAttached(BaseFragment fragment) {

        if (fragment.getType() != null) {
            mFragments.put(fragment.getType(), fragment);
        }

        setCurrentFragment(fragment);
    }

    public void onFragmentDetached(BaseFragment fragment) {

        if (fragment.getType() != null) {
            mFragments.remove(fragment.getType());
        }
    }

    protected void fragmentShowing(FragmentType fragmentType) {

        if (mFragments.containsKey(fragmentType)) {

            mFragments.get(fragmentType).isShowing();
        }
    }
	
	public void onEvent(LockEvent event) {
		
		showLockScreen(event.getType());
	}
	
	public void onEvent(FeedbackEvent event) {
		
		showFeedback();
	}
	
	public void onEvent(BackEvent event) {
		
		popFragment();
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
			
			Intent intent = new Intent(this, ApplicationContext.isTablet() ? LockCodeTabletActivity.class : PopupHandsetActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.LOCK_SCREEN);
			intent.putExtra(LockCodeFragment.EXTRA_LOCK, type);
			startActivity(intent);
			overridePendingTransition(R.anim.in_up, R.anim.none);
		}
	}
	
	private void showFeedback() {
		
		Intent intent = new Intent(this, PopupHandsetActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.FEEDBACK);
		startActivity(intent);
		overridePendingTransition(R.anim.in_up, R.anim.none);
	}
    
    public abstract String getActivityTitle();

    public void showFragment(FragmentType fragment, boolean moveUp) {}

    protected void loadFragment(int containerViewId, BaseFragment fragment) {

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(containerViewId, fragment);
        ft.commit();
    }

    /**
     * Push a new fragment on to the back stack
     *
     * @param containerViewId the resourceId to push the fragment onto
     * @param fragment The fragment to display
     */
    public void pushFragment(int containerViewId, BaseFragment fragment) {

        mStackCount++;
        stackChange();

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
        ft.replace(containerViewId, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Pop back on the fragment manager's stack of fragments.
     * Update the navigation bar based on our fragment count.
     */
    public void popFragment() {

        if (mStackCount > 0) {
            popBackStack();
        }
    }

    /**
     * Pop the back stack
     */
    private void popBackStack() {

        mStackCount--;
        stackChange();

        mFragmentManager.popBackStack();
    }

    /**
     * Notify sub classes that the back stack has changed
     */
    public void stackChange() {}

    /**
     * Pop all items off the current BackStack
     */
    public void clearBackStack() {

        if (getStackCount() > 0) {

            mStackCount = 0;

            int fragId = mFragmentManager.getBackStackEntryAt(0).getId();
            mFragmentManager.popBackStackImmediate(fragId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
