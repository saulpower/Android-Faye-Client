package com.moneydesktop.finance.handset.activity;

import android.app.Activity;
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

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.DebugActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.anim;
import com.moneydesktop.finance.R.id;
import com.moneydesktop.finance.R.layout;
import com.moneydesktop.finance.R.string;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.database.DaoMaster;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.DialogUtils;

import de.greenrobot.event.EventBus;

public class LoginActivity extends Activity {
	
	private final String TAG = "LoginActivity";
	
	private ViewFlipper viewFlipper;
	private Button loginViewButton, demoButton, loginButton, cancelButton;
	private EditText username, password;
	private ImageView logo;
	
	private Animation inLeft, inRight, outLeft, outRight;
	
	@Override
	public void onBackPressed() {
		if (viewFlipper.indexOfChild(viewFlipper.getCurrentView()) == 1) {
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
        
        addDemoCredentials();
        
        User.clear();
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		toDashboard();
	}
	
	private void toDashboard() {
		
		if (User.getCurrentUser() != null) {
			
	    	Intent i = new Intent(this, DashboardActivity.class);
	    	startActivity(i);
		}
	}
	
	private void addDemoCredentials() {
		
		username.setText("saul.howard@moneydesktop.com");
		password.setText("password123");
		
		DaoMaster.dropAllTables(ApplicationContext.getDb(), true);
		DaoMaster.createAllTables(ApplicationContext.getDb(), true);
	}
	
	private void setupView() {
		
		viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		configureFlipper();
		
		username = (EditText) findViewById(R.id.username_field);
		password = (EditText) findViewById(R.id.password_field);
		
		loginViewButton = (Button) findViewById(R.id.login_view_button);
		demoButton = (Button) findViewById(R.id.demo_button);
		
		loginButton = (Button) findViewById(R.id.login_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		
		logo = (ImageView) findViewById(R.id.logo);
		
		addListeners();
	}
	
	private void addListeners() {
		
		loginViewButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				toLoginView();
			}
		});
		
		demoButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				demoMode();
			}
		});
		
		loginButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				login();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				cancel();
			}
		});
		
		logo.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
		    	Intent i = new Intent(LoginActivity.this, DebugActivity.class);
		    	startActivity(i);
			}
		});
	}
	
	private void setupAnimations() {
		
		inLeft = AnimationUtils.loadAnimation(this, R.anim.in_left);
		outRight = AnimationUtils.loadAnimation(this, R.anim.out_right);
		outLeft = AnimationUtils.loadAnimation(this, R.anim.out_left);
		inRight = AnimationUtils.loadAnimation(this, R.anim.in_right);
		
		inLeft.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {

				configureButtons(false);
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {

				configureButtons(true);
			}
		});
		
		inRight.setAnimationListener(new AnimationListener() {
			
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

		viewFlipper.setInAnimation(inRight);
		viewFlipper.setOutAnimation(outLeft);
	}
	
	private void configureButtons(boolean enabled) {
		
		loginViewButton.setEnabled(enabled);
		demoButton.setEnabled(enabled);
		loginButton.setEnabled(enabled);
		cancelButton.setEnabled(enabled);
	}
	
	private void toLoginView() {
		
		viewFlipper.showNext();
		viewFlipper.setInAnimation(outRight);
		viewFlipper.setOutAnimation(inLeft);
	}
	
	private void demoMode() {
		
	}
	
	private void login() {
        
		if (loginCheck()) {
	        
			authenticate();
	        
		} else {
			
			DialogUtils.alertDialog(getString(R.string.error_title), getString(R.string.error_login_incomplete), this, null);
		}
	}
	
	private boolean loginCheck() {
		
		return !username.getText().toString().equals("") && !password.getText().toString().equals("");
	}
	
	private void authenticate() {

		DialogUtils.showProgress(this, getString(R.string.loading));
		
		new AsyncTask<Void, Void, Boolean>() {
    		
			@Override
			protected Boolean doInBackground(Void... params) {

				DataBridge dataBridge = DataBridge.sharedInstance();
		        
		        try {
		        	
					dataBridge.authenticateUser(username.getText().toString(), password.getText().toString());
					
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
    				
    				DialogUtils.alertDialog(getString(R.string.error_login_failed), getString(R.string.error_login_invalid), LoginActivity.this, null);
    			
    			} else {
    				
    				EventBus eventBus = EventBus.getDefault();
    				eventBus.post(new EventMessage().new LoginEvent());
    				
    				toDashboard();
    			}
    		}
			
		}.execute();
	}
	
	private void cancel() {
		
		username.clearFocus();
		password.clearFocus();
		hideKeyboard();
		
		viewFlipper.showPrevious();
		viewFlipper.setInAnimation(inRight);
		viewFlipper.setOutAnimation(outLeft);
	}
	
	private void hideKeyboard() {

    	InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(viewFlipper.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
}
