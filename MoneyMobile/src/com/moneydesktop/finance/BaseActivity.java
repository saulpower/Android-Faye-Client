package com.moneydesktop.finance;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.handset.activity.LockCodeActivity;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.LockEvent;
import com.moneydesktop.finance.util.Enums.LockType;
import com.moneydesktop.finance.util.Fonts;

import de.greenrobot.event.EventBus;

abstract public class BaseActivity extends FragmentActivity {
	
	public final String TAG = this.getClass().getSimpleName();

    public static final int DEVICE_DISPLAY_SIZE_PHONE = 1;
    public static final int DEVICE_DISPLAY_SIZE_TABLET = 2;
    
    // We decided to call anything 7" or over a "tablet".
    private static final float MINIMUM_TABLET_SCREEN_INCHES = 7f;
    
    public static boolean isTablet(final Context context) {

        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        return getDeviceDisplaySize(context, metrics) == DEVICE_DISPLAY_SIZE_TABLET && android.os.Build.VERSION.SDK_INT >= 11;
    }

    @SuppressLint("FloatMath")
	public static int getDeviceDisplaySize (final Context context, final DisplayMetrics metrics) {
    	
        final float widthInches = metrics.widthPixels / metrics.xdpi;
        final float heightInches = metrics.heightPixels / metrics.ydpi;
        final float screenSizeInInches = (float) Math.sqrt(Math.pow(widthInches, 2f) + Math.pow(heightInches, 2f));

        return screenSizeInInches > MINIMUM_TABLET_SCREEN_INCHES
                ? DEVICE_DISPLAY_SIZE_TABLET
                : DEVICE_DISPLAY_SIZE_PHONE;
    }
    
	protected final long TRANSITION_DURATION = 300;

	protected FragmentManager mFm;
	
	private ViewFlipper mNavFlipper;
    protected RelativeLayout mNavBar;
    private LinearLayout mInfo;
    private TextView mTitle;
    private ImageButton mBack;
    
    protected Animation mPushDown, mPushUp;
    
    protected int mFragmentCount = 0;
    private boolean mBackShowing = false;
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
		navigationCheck();
		setupAnimations();
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
		if (!(this instanceof SplashActivity))
			sPause = System.currentTimeMillis();
		EventBus.getDefault().unregister(this);
	}
	
	private void setupAnimations() {
		mPushDown = AnimationUtils.loadAnimation(this, R.anim.in_down);
		mPushUp = AnimationUtils.loadAnimation(this, R.anim.out_up);
	}
	
	/**
	 * Check if there is a navigation bar present and load it
	 * if so.
	 */
	private void navigationCheck() {
		
		if (mNavFlipper == null) {
			
			mNavFlipper = (ViewFlipper) findViewById(R.id.nav_flipper);
			mNavBar = (RelativeLayout) findViewById(R.id.nav_bar);
			
			if (mNavBar != null) {
				
				setupNavigation();
				updateNavBar(getActivityTitle());
			}
		}
	}
	
	/**
	 * Configure the navigation bar so we can use it
	 */
	private void setupNavigation() {

		mInfo = (LinearLayout) mNavBar.findViewById(R.id.info);
		mTitle = (TextView) mNavBar.findViewById(R.id.title);
		Fonts.applyPrimaryFont(mTitle, 16);
		mBack = (ImageButton) mNavBar.findViewById(R.id.back_button);
		mBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				navigateBack();
			}
		});
	}
	
	/**
	 * Update the navigation bar with the passed in title.  Configure the
	 * back button if necessary.
	 * 
	 * @param titleString the title for the navigation bar
	 */
	public void updateNavBar(String titleString) {
		
		if (mNavBar != null) {
			
			if (titleString != null)
				mTitle.setText(titleString.toUpperCase());
			
			if (mFragmentCount > 1 && !mBackShowing) {
				
				mBackShowing = true;
				configureBackButton(true);
				
			} else if (mFragmentCount <= 1 && mBackShowing) {
				
				mBackShowing = false;
				configureBackButton(false);
				
			}
		}
	}
	
	/**
	 * Flip the navigation bar to transition between dashboard and
	 * various view controllers
	 * 
	 * @param home whether we are returning to the dashboard (home) or not
	 */
	public void configureView(final boolean home) {
		
		if (mNavFlipper == null)
			return;
		
		if (home) {
    		
			AnimationFactory.flipTransition(mNavFlipper, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
			
    	} else {
	    	
	        AnimationFactory.flipTransition(mNavFlipper, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
    	}
	}
	
	/**
	 * Configure the back button
	 * 
	 * @param show whether to show it or not
	 */
	private void configureBackButton(final boolean show) {
		
		int direction = show ? 1 : 0;
		
		int dp = (int) getResources().getDimension(R.dimen.navbar_text_slide);
		animate(mInfo).setDuration(400).translationX(dp * direction).setInterpolator(new OvershootInterpolator());

		Animation fade = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
		fade.setDuration(400);
		fade.setStartOffset(show ? 200 : 0);
		fade.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {

				if (show)
					mBack.setVisibility(View.VISIBLE);
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {

				if (!show)
					mBack.setVisibility(View.GONE);
			}
		});
		
		mBack.startAnimation(fade);
	}
	
	/**
	 * Pop back on the fragment manager's stack of fragments.
	 * Update the navigation bar based on our fragment count.
	 */
	protected void navigateBack() {
		
		mFragmentCount--;
		getSupportFragmentManager().popBackStack();
		
		updateNavBar(mFragmentCount == 0 ? getActivityTitle() : null);
		
		if (mFragmentCount == 0)
			mOnFragment = false;
	}
	
	/**
	 * Called whenever a new fragment has been added and
	 * attached itself to the host activity.
	 * 
	 * @param fragment
	 */
	public void onFragmentAttached() {
		
		if (mOnFragment) {
			mFragmentCount++;
		}
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
			
			if (!code.equals(""))
				showLockScreen(LockType.LOCK);
		}
	}
	
	private void showLockScreen(LockType type) {
		
		if (!LockCodeActivity.sShowing) {
			
			LockCodeActivity.sShowing = true;
			
			Intent intent = new Intent(this, LockCodeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(LockCodeActivity.EXTRA_LOCK, type);
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
