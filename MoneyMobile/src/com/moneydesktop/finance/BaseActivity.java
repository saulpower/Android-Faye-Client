package com.moneydesktop.finance;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
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
import com.moneydesktop.finance.data.Constant;

abstract public class BaseActivity extends FragmentActivity {
	
	public final String TAG = this.getClass().getSimpleName();

    public static final int DEVICE_DISPLAY_SIZE_PHONE = 1;
    public static final int DEVICE_DISPLAY_SIZE_TABLET = 2;
    
    // We decided to call anything 7" or over a "tablet".
    private static final float MINIMUM_TABLET_SCREEN_INCHES = 7f;
    
    public static boolean isTablet (final Context context) {

        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        return getDeviceDisplaySize(context, metrics) == DEVICE_DISPLAY_SIZE_TABLET;
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
	
	private ViewFlipper navFlipper;
    protected RelativeLayout navBar;
    private LinearLayout info;
    private ImageButton back;
    protected Animation pushDown, pushUp;
    protected int fragmentCount = 0;
    private boolean backShowing = false;
	protected boolean onFragment = false;
	private SharedPreferences mPreferences;
	private ArrayList<AppearanceListener> listeners = new ArrayList<AppearanceListener>();
	
    public SharedPreferences getSharedPreferences () {
        return mPreferences;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		navigationCheck();
		setupAnimations();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	private void setupAnimations() {
		pushDown = AnimationUtils.loadAnimation(this, R.anim.in_down);
		pushUp = AnimationUtils.loadAnimation(this, R.anim.out_up);
	}

    /**
     * Returns the width and height measurement in pixels.
     * 
     * @return
     */
	public float[] getScreenMeasurements() {
		
		float[] values = new float[2];
		
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        
        values[0] = metrics.widthPixels;
        values[1] = metrics.heightPixels;
        
        return values;
	}
	
	private void navigationCheck() {
		
		if (navFlipper == null) {
			
			navFlipper = (ViewFlipper) findViewById(R.id.nav_flipper);
			navBar = (RelativeLayout) findViewById(R.id.nav_bar);
			
			if (navBar != null) {
				
				setupNavigation();
				updateNavBar(getActivityTitle());
			}
		}
	}
	
	private void setupNavigation() {

		info = (LinearLayout) navBar.findViewById(R.id.info);
		back = (ImageButton) navBar.findViewById(R.id.back_button);
		back.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				navigateBack();
			}
		});
	}
	
	public void updateNavBar(String titleString) {
		
		if (navBar != null) {
			
			if (titleString != null) {
				
				TextView title = (TextView) navBar.findViewById(R.id.title);
				title.setText(titleString.toUpperCase());
			}
			
			if (fragmentCount > 1 && !backShowing) {
				
				backShowing = true;
				configureBackButton(true);
				
			} else if (fragmentCount <= 1 && backShowing) {
				
				backShowing = false;
				configureBackButton(false);
				
			}
		}
	}
	
	public void configureView(final boolean home) {
		
		if (home) {
    		
			AnimationFactory.flipTransition(navFlipper, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
			
    	} else {
	    	
	        AnimationFactory.flipTransition(navFlipper, home ? FlipDirection.RIGHT_LEFT : FlipDirection.LEFT_RIGHT, TRANSITION_DURATION);
    	}
	}
	
	private void configureBackButton(final boolean show) {
		
		int direction = show ? 1 : 0;
		
		int dp = (int) getResources().getDimension(R.dimen.navbar_text_slide);
		animate(info).setDuration(400).translationX(dp * direction).setInterpolator(new OvershootInterpolator());

		Animation fade = new AlphaAnimation(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
		fade.setDuration(400);
		fade.setStartOffset(show ? 200 : 0);
		fade.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {

				if (show)
					back.setVisibility(View.VISIBLE);
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {

				if (!show)
					back.setVisibility(View.GONE);
			}
		});
		
		back.startAnimation(fade);
	}
	
	protected void navigateTo(Class<?> activity) {
		
		Intent intent = new Intent(this, activity);
		intent.putExtra(Constant.KEY_HAS_PREVIOUS, (getActivityTitle() != null));
		startActivity(intent);
	}
	
	protected void navigateBack() {
		
		fragmentCount--;
		listeners.remove(listeners.size() - 1);
		getSupportFragmentManager().popBackStack();
		
		updateNavBar(fragmentCount == 0 ? getActivityTitle() : null);
		
		if (fragmentCount == 0)
			onFragment = false;
	}
	
	public void onFragmentAttached(AppearanceListener fragment) {
		
		if (onFragment) {
			
			fragmentCount++;
			
			if (fragment != null)
				listeners.add(fragment);
		}
	}
	
	protected void viewDidAppear() {
		
		for (AppearanceListener listener : listeners) {
			
			if (listener != null)
				listener.onViewDidAppear();
		}
	}
    
    public abstract String getActivityTitle();
	
	public interface AppearanceListener {
		public abstract void onViewDidAppear();
	}
}
