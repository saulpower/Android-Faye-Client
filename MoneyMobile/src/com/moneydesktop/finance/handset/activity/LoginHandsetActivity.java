package com.moneydesktop.finance.handset.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.DebugActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.LoginBaseActivity;
import com.moneydesktop.finance.util.Animator;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;

public class LoginHandsetActivity extends LoginBaseActivity {
	
	private final String TAG = "LoginActivity";
	
	private ViewFlipper mViewFlipper;
	private LinearLayout mButtonView, mCredentialView;
	private Button mLoginViewButton, mDemoButton, mLoginButton, mCancelButton;
	private EditText mUsername, mPassword;
	private ImageView mLogo;
	private TextView mSignupText, mLoginText, mUsernameLabel, mPasswordLabel, mMessageTitle, mMessageBody;
	
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
        
        setContentView(R.layout.handset_login_view);

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
			
	    	Intent i = new Intent(this, DashboardHandsetActivity.class);
	    	startActivity(i);
            overridePendingTransition(R.anim.fade_in_fast, R.anim.none);
		}
	}
	
	private void setupView() {
		
		mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		mButtonView = (LinearLayout) findViewById(R.id.button_view);
		mCredentialView = (LinearLayout) findViewById(R.id.credentials);
		configureFlipper();
		
		mUsername = (EditText) findViewById(R.id.username_field);
		mPassword = (EditText) findViewById(R.id.password_field);
		
		mLoginViewButton = (Button) findViewById(R.id.login_view_button);
		mDemoButton = (Button) findViewById(R.id.demo_button);
		
		mLoginButton = (Button) findViewById(R.id.login_button);
		mCancelButton = (Button) findViewById(R.id.cancel_button);
		
		mLogo = (ImageView) findViewById(R.id.logo);
		
		mSignupText = (TextView) findViewById(R.id.signup);
		mLoginText = (TextView) findViewById(R.id.login_text);
		mUsernameLabel = (TextView) findViewById(R.id.email_label);
		mPasswordLabel = (TextView) findViewById(R.id.password_label);
		mMessageTitle = (TextView) findViewById(R.id.message_title);
		mMessageBody = (TextView) findViewById(R.id.message_body);
		
		Fonts.applyPrimaryBoldFont(mLoginViewButton, 15);
		Fonts.applyPrimaryBoldFont(mDemoButton, 15);
		Fonts.applyPrimaryBoldFont(mLoginButton, 15);
		Fonts.applyPrimaryBoldFont(mCancelButton, 15);
		Fonts.applyPrimaryBoldFont(mSignupText, 12);
		Fonts.applySecondaryItalicFont(mLoginText, 12);
		Fonts.applySecondaryItalicFont(mUsernameLabel, 9);
		Fonts.applySecondaryItalicFont(mPasswordLabel, 9);
		
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
				
		    	Intent i = new Intent(LoginHandsetActivity.this, DebugActivity.class);
		    	startActivity(i);
			}
		});
		
		mUsername.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			public void onFocusChange(View v, boolean hasFocus) {
				
				mUsernameLabel.setTextColor(getResources().getColor(hasFocus ? R.color.white : R.color.light_gray1));
			}
		});
		
		mPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			public void onFocusChange(View v, boolean hasFocus) {
				
				mPasswordLabel.setTextColor(getResources().getColor(hasFocus ? R.color.white : R.color.light_gray1));
			}
		});
		
		mMessageBody.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				return true;
			}
		});
		
		mMessageTitle.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				return true;
			}
		});
		
		mLoginText.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.setup_url)));
				startActivity(browserIntent);
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
	
	protected void resetLogin() {
		super.resetLogin();
		
		toggleAnimations(true);
		
		mLoginButton.setText(getString(R.string.button_login));
		mCancelButton.setText(getString(R.string.button_cancel));
	}
	
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
					
					Log.e(TAG, "Error Authenticating: " + e.getLocalizedMessage());
					return false;
				}

				return true;
			}
    		
    		@Override
    		protected void onPostExecute(Boolean result) {

    			DialogUtils.hideProgress();

				mFailed = !result;
				
    			if (!result) {
    				
    				mMessageTitle.setText(getString(R.string.error_login_failed));
    				mMessageBody.setText(getString(R.string.error_login_invalid));
							
					toggleAnimations(false);
					
					mLoginButton.setText(getString(R.string.button_return_login));
					mCancelButton.setText(getString(R.string.button_setup));
    			
    			} else {
    				
    				EventBus eventBus = EventBus.getDefault();
    				eventBus.post(new EventMessage().new LoginEvent());
    				
    				toDashboard();
    			}
    		}
			
		}.execute();
	}
	
	private void toggleAnimations(boolean reset) {
		
		int offset = (int) (.20 * UiUtils.getScreenMeasurements(this)[1]) * (reset ? -1 : 1);
		long duration = 750;
		long delay = reset ? 250 : 0;
		
		Animator.translateView(mButtonView, new float[] {0, offset}, duration);
		Animator.fadeView(mLoginText, !reset, duration, delay);
		Animator.fadeView(mCredentialView, !reset, duration, delay);
		Animator.fadeView(mUsernameLabel, !reset, duration, delay);
		Animator.fadeView(mPasswordLabel, !reset, duration, delay);
		Animator.fadeView(mPassword, !reset, duration, delay);
		Animator.fadeView(mUsername, !reset, duration, delay);
		
		delay = reset ? 0 : 250;
		
		Animator.fadeView(mMessageTitle, reset, duration, delay);
		Animator.fadeView(mMessageBody, reset, duration, delay);
		
		Animator.startAnimations();
	}
	
	private void cancel() {
		
		if (mFailed) {
			
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.setup_url)));
			startActivity(browserIntent);
			
			resetLogin();
			
			return;
		}
		
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
