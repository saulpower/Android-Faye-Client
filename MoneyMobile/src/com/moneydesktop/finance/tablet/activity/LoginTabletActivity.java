package com.moneydesktop.finance.tablet.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.DebugActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.LoginBaseActivity;
import com.moneydesktop.finance.util.DialogUtils;

import de.greenrobot.event.EventBus;

public class LoginTabletActivity extends LoginBaseActivity {
	
	private final String TAG = "LoginActivity";
	
	private ViewFlipper mViewFlipper;
	private Button mLoginViewButton, mDemoButton, mLoginButton, mCancelButton;
	private EditText mUsername, mPassword;
	private ImageView mLogo;
	
	private Animation mInLeft, mInRight, mOutLeft, mOutRight;
	
	@Override
	public void onBackPressed() {
		if (mViewFlipper.indexOfChild(mViewFlipper.getCurrentView()) == 1) {
			cancel();
		} else {
			super.onBackPressed();
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.login_view);

        setupAnimations();
        setupView();
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		toDashboard();
	}
	
	@Override
	protected void toDashboard() {

		DialogUtils.hideProgress();
		
		if (User.getCurrentUser() != null) {
			
			Log.i(TAG, "toDashboard");
	    	Intent i = new Intent(this, DashboardTabletActivity.class);
	    	startActivity(i);
	    	overridePendingTransition(R.anim.fade_in_fast, R.anim.none);
		}
	}
	
	private void setupView() {
		
		mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		configureFlipper();
		
		mUsername = (EditText) findViewById(R.id.username_field);
		mPassword = (EditText) findViewById(R.id.password_field);
		
		mLoginViewButton = (Button) findViewById(R.id.login_view_button);
		mDemoButton = (Button) findViewById(R.id.demo_button);
		
		mLoginButton = (Button) findViewById(R.id.login_button);
		mCancelButton = (Button) findViewById(R.id.cancel_button);
		
		mLogo = (ImageView) findViewById(R.id.logo);
		
		addListeners();
	}
	
	private void addListeners() {
		
		mLoginViewButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				toLoginView();
			}
		});
		
		mDemoButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				demoMode();
			}
		});
		
		mLoginButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				login();
			}
		});
		
		mCancelButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				cancel();
			}
		});
		
		mLogo.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
		    	Intent i = new Intent(LoginTabletActivity.this, DebugActivity.class);
		    	startActivity(i);
			}
		});
	}
	
	private void setupAnimations() {
		
		mInLeft = AnimationUtils.loadAnimation(this, R.anim.in_left);
		mOutRight = AnimationUtils.loadAnimation(this, R.anim.out_right);
		mOutLeft = AnimationUtils.loadAnimation(this, R.anim.out_left);
		mInRight = AnimationUtils.loadAnimation(this, R.anim.in_right);
		
		mInLeft.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {

				configureButtons(false);
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {

				configureButtons(true);
			}
		});
		
		mInRight.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {

				configureButtons(false);
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {

				configureButtons(true);
			}
		});
	}
	
	private void configureFlipper() {

		mViewFlipper.setInAnimation(mInRight);
		mViewFlipper.setOutAnimation(mOutLeft);
	}
	
	private void configureButtons(boolean enabled) {
		
		mLoginViewButton.setEnabled(enabled);
		mDemoButton.setEnabled(enabled);
		mLoginButton.setEnabled(enabled);
		mCancelButton.setEnabled(enabled);
	}
	
	private void toLoginView() {
		
		mViewFlipper.showNext();
		mViewFlipper.setInAnimation(mOutRight);
		mViewFlipper.setOutAnimation(mInLeft);
	}
	
	@Override
	protected boolean loginCheck() {
		
		return !mUsername.getText().toString().equals("") && !mPassword.getText().toString().equals("");
	}
	
	@Override
	protected void authenticate() {

		DialogUtils.showProgress(this, getString(R.string.loading));
		
		new AsyncTask<Void, Void, Boolean>() {
    		
			@Override
			protected Boolean doInBackground(Void... params) {

				DataBridge dataBridge = DataBridge.sharedInstance();
		        
		        try {
		        	
					dataBridge.authenticateUser(mUsername.getText().toString(), mPassword.getText().toString());
					
				} catch (Exception e) {
					
					Log.e(TAG, "Error Authenticating", e);
					return false;
				}

				return true;
			}
    		
    		@Override
    		protected void onPostExecute(Boolean result) {

    			DialogUtils.hideProgress();
    			
    			if (!result) {
    				
    				DialogUtils.alertDialog(getString(R.string.error_login_failed), getString(R.string.error_login_invalid), LoginTabletActivity.this, null);
    			
    			} else {
    				
    				EventBus eventBus = EventBus.getDefault();
    				eventBus.post(new EventMessage().new LoginEvent());
    				
    				toDashboard();
    			}
    		}
			
		}.execute();
	}
	
	private void cancel() {
		
		mUsername.clearFocus();
		mPassword.clearFocus();
		hideKeyboard();
		
		mViewFlipper.showPrevious();
		mViewFlipper.setInAnimation(mInRight);
		mViewFlipper.setOutAnimation(mOutLeft);
	}
	
	private void hideKeyboard() {

    	InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mViewFlipper.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public String getActivityTitle() {
		return null;
	}
}
