
package com.moneydesktop.finance.shared.activity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.data.DemoData;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.handset.activity.IntroHandsetActivity;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.activity.IntroTabletActivity;
import com.moneydesktop.finance.util.Animator;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.EmailUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.LabelEditText;

import de.greenrobot.event.EventBus;

public abstract class LoginBaseActivity extends BaseActivity {

    private final String TAG = "LoginActivity";
    private boolean mIsTablet = false;
    private ViewFlipper mViewFlipper;
    private LinearLayout mButtonView, mCredentialView, mSignupView;
    private Button mLoginViewButton, mDemoButton, mLoginButton, mCancelButton, mSubmitButton,
            mDemoButton2, mReturnHomeButton;
    private LabelEditText mUsername, mPassword, mSignupName, mSignupEmail, mSignupBank;
    private ImageView mLogo;
    private TextView mSignupText, mLoginText, mMessageTitle, mMessageBody, mCancelText,
            mNeedAccount, mNagBank, mThankYou, mTryDemo;
    private Animation mInLeft, mInRight, mOutLeft, mOutRight, mInUp, mOutDown, mNoMovement;
    private boolean mFailed = false;

    @Override
    public void onBackPressed() {
        if (mViewFlipper.indexOfChild(mViewFlipper.getCurrentView()) == 1) {
            cancel(mOutRight, mInLeft);
        }
        else if (mViewFlipper.indexOfChild(mViewFlipper.getCurrentView()) == 2 ||
                mViewFlipper.indexOfChild(mViewFlipper.getCurrentView()) == 3) {
            cancel(mOutDown, mNoMovement);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        toDashboard();
    }

    private void toDashboard() {

        DialogUtils.hideProgress();

        if (User.getCurrentUser() != null) {
        	
            Intent i = new Intent(this, DashboardHandsetActivity.class);
            
            if (isTablet(this)) {
                i = new Intent(this, DashboardTabletActivity.class);
            }
            
            startActivity(i);
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
            
            finish();
        }
    }

    protected void setupView() {
        if (isTablet(this)) {
            mIsTablet = true;
        }
        
        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mButtonView = (LinearLayout) findViewById(R.id.button_view);
        mCredentialView = (LinearLayout) findViewById(R.id.credentials);
        mSignupView = (LinearLayout) findViewById(R.id.signup_info);
        configureFlipper();

        mUsername = (LabelEditText) findViewById(R.id.username_field);
        mPassword = (LabelEditText) findViewById(R.id.password_field);

        mLoginViewButton = (Button) findViewById(R.id.login_view_button);
        mDemoButton = (Button) findViewById(R.id.demo_button);
        mDemoButton2 = (Button) findViewById(R.id.demo_button2);
        mReturnHomeButton = (Button) findViewById(R.id.home_view_button);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);

        mLogo = (ImageView) findViewById(R.id.logo);

        mSignupText = (TextView) findViewById(R.id.signup);
        mLoginText = (TextView) findViewById(R.id.login_text);
        mThankYou = (TextView) findViewById(R.id.thank_you);
        mTryDemo = (TextView) findViewById(R.id.try_demo);
        mSignupName = (LabelEditText) findViewById(R.id.signup_name);
        mSignupEmail = (LabelEditText) findViewById(R.id.signup_email);
        mSignupBank = (LabelEditText) findViewById(R.id.signup_bank);
        mMessageTitle = (TextView) findViewById(R.id.message_title);
        mMessageBody = (TextView) findViewById(R.id.message_body);
        mCancelText = (TextView) findViewById(R.id.cancel_text);
        mSubmitButton = (Button) findViewById(R.id.submit_button);
        mNeedAccount = (TextView) findViewById(R.id.need_account);
        mNagBank = (TextView) findViewById(R.id.nag_bank);
        
        applyFonts();
        addListeners();
    }
    
    private void applyFonts() {

        Fonts.applyPrimaryFont(mLoginViewButton, 12);
        Fonts.applyPrimaryFont(mDemoButton, 12);
        Fonts.applyPrimaryFont(mDemoButton2, 12);
        Fonts.applyPrimaryFont(mReturnHomeButton, 12);
        Fonts.applyPrimaryFont(mLoginButton, 12);
        Fonts.applyPrimaryFont(mCancelButton, 12);
        Fonts.applyPrimaryFont(mSubmitButton, 12);
        Fonts.applyPrimaryFont(mSignupText, 12);
        Fonts.applyPrimaryFont(mLoginText, 12);
        Fonts.applyPrimaryFont(mCancelText, 12);
        Fonts.applyPrimaryBoldFont(mThankYou, 12);
        Fonts.applyPrimaryFont(mTryDemo, 12);
        Fonts.applyPrimarySemiBoldFont(mUsername, 16);
        Fonts.applyPrimarySemiBoldFont(mPassword, 16);
        Fonts.applyPrimarySemiBoldFont(mSignupName, 16);
        Fonts.applyPrimarySemiBoldFont(mSignupBank, 16);
        Fonts.applyPrimarySemiBoldFont(mPassword, 16);
        Fonts.applyPrimaryBoldFont(mNeedAccount, 12);
        Fonts.applyPrimaryFont(mNagBank, 10);
    }

    private void addListeners() {
        mCancelText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                cancel(mOutDown, mNoMovement);
            }
        });
        mLoginViewButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                toLoginView();
            }
        });
        mReturnHomeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                cancel(mOutDown, mNoMovement);
            }
        });
        mDemoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                demoMode();
            }
        });
        mDemoButton2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                demoMode();
            }
        });
        mLoginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                login();
            }
        });
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        mCancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                cancel(mOutRight, mInLeft);
            }
        });

        mLogo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginBaseActivity.this, DebugActivity.class);
                startActivity(i);
            }
        });
        mUsername.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mUsername.setSelection(hasFocus ? mUsername.getText().length() : 0);
                mUsername.setTextColor(getResources().getColor(hasFocus ? R.color.white : R.color.light_gray1));
            }
        });

        mPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mPassword.setSelection(hasFocus ? mPassword.getText().length() : 0);
                mPassword.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });
        mPassword.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                     mCredentialView.requestFocus();
                     UiUtils.hideKeyboard(LoginBaseActivity.this, mViewFlipper);
                     login();
                }
                
                return false;
            }
        });

        mMessageBody.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });
        mSignupName.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mSignupName.setSelection(hasFocus ? mSignupName.getText().length() : 0);
                mSignupName.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });
        mSignupEmail.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mSignupEmail.setSelection(hasFocus ? mSignupEmail.getText().length() : 0);
                mSignupEmail.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });
        mSignupBank.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mSignupBank.setSelection(hasFocus ? mSignupBank.getText().length() : 0);
                mSignupBank.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });
        mSignupBank.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSignupView.requestFocus();
                    UiUtils.hideKeyboard(LoginBaseActivity.this, mViewFlipper);
                    signup();
                }
                
                return false;
            }
        });
        mMessageTitle.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });

        mLoginText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse(getString(R.string.setup_url)));
                startActivity(browserIntent);
            }
        });
        mSignupText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                toSignupView();
            }
        });
    }

    protected void setupAnimations() {

        mInLeft = AnimationUtils.loadAnimation(this, R.anim.in_left);
        mOutRight = AnimationUtils.loadAnimation(this, R.anim.out_right);
        mOutLeft = AnimationUtils.loadAnimation(this, R.anim.out_left);
        mInRight = AnimationUtils.loadAnimation(this, R.anim.in_right);
        mInUp = AnimationUtils.loadAnimation(this, R.anim.in_up);
        mOutDown = AnimationUtils.loadAnimation(this, R.anim.out_down);
        mNoMovement = AnimationUtils.loadAnimation(this, R.anim.none);
        mInLeft.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

                configureButtons(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                configureButtons(true);
            }
        });

        mInRight.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

                configureButtons(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
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

        mViewFlipper.setOutAnimation(mOutLeft);
        mViewFlipper.setInAnimation(mInRight);
        mViewFlipper.setDisplayedChild(1);
    }

    private void toSignupView() {

        mViewFlipper.setOutAnimation(mNoMovement);
        mViewFlipper.setInAnimation(mInUp);
        mViewFlipper.setDisplayedChild(2);
    }

    private void demoMode() {
        
        fadeCurrentView();
        
        User.registerDemoUser();

        DialogUtils.showProgress(this, getString(R.string.demo_message));

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                DemoData.createDemoData();

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                toDashboard();
            }

        }.execute();
    }
    
    private void fadeCurrentView() {

        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mViewFlipper.getCurrentView().setVisibility(View.GONE);
            }
        });
        mViewFlipper.getCurrentView().startAnimation(fade);
    }

    private void signup() {
        if (signupCheck() && EmailUtils.validateEmail(mSignupEmail.getText().toString())) {
            submit();
        }

        else if (!signupCheck()) {
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_login_incomplete), this, null);
        }

        else if (!EmailUtils.validateEmail(mSignupEmail.getText().toString())) {
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_email_invalid), this, null);
        }
    }

    private void submit() {

        // TODO add data submission code here
        mSignupName.setText("");
        mSignupEmail.setText("");
        mSignupBank.setText("");
        
        toThankYou();
    }

    private void toThankYou() {

        mViewFlipper.setOutAnimation(mInLeft);
        mViewFlipper.setInAnimation(mOutRight);
        mViewFlipper.setDisplayedChild(3);
        UiUtils.hideKeyboard(this, mViewFlipper);
    }

    private void login() {

        if (mFailed) {

            mPassword.setText("");
            resetLogin();

            return;
        }
        if (!EmailUtils.validateEmail(mUsername.getText().toString()))
        {
            mPassword.setText("");
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_email_invalid), this, null);
            return;
        }
        if (loginCheck()) {

            authenticate();

        } else {

            mPassword.setText("");
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_login_incomplete), this, null);
        }
    }

    private void resetLogin() {

        toggleAnimations(true);

        mLoginButton.setText(getString(R.string.button_login));
        mCancelButton.setText(getString(R.string.button_cancel));

        mFailed = false;
    }

    private boolean signupCheck() {
        return !mSignupName.getText().toString().equals("")
                && !mSignupEmail.getText().toString().equals("")
                && !mSignupBank.getText().toString().equals("");
    }

    private boolean loginCheck() {

        return !mUsername.getText().toString().equals("")
                && !mPassword.getText().toString().equals("");
    }

    private void authenticate() {

        DialogUtils.showProgress(this, getString(R.string.loading));

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                DataBridge dataBridge = DataBridge.sharedInstance();

                try {

                    dataBridge.authenticateUser(mUsername.getText().toString(), mPassword.getText()
                            .toString());

                } catch (Exception e) {

                    Log.e(TAG, "Error Authenticating", e);
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
                    if (mIsTablet == true) {
                        Intent i = new Intent(LoginBaseActivity.this, IntroTabletActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else {
                        Intent i = new Intent(LoginBaseActivity.this, IntroHandsetActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            }

        }.execute();
    }

    private void toggleAnimations(boolean reset) {

        int offset = (int) (.20 * UiUtils.getScreenMeasurements(this)[1]) * (reset ? -1 : 1);
        long duration = 400;
        long delay = reset ? 250 : 0;

        Animator.translateView(mButtonView, new float[] {
                0, offset
        }, duration);
        Animator.fadeView(mLoginText, !reset, duration, delay);
        Animator.fadeView(mCredentialView, !reset, duration, delay);
        Animator.fadeView(mUsername, !reset, duration, delay);
        Animator.fadeView(mPassword, !reset, duration, delay);

        delay = reset ? 0 : 250;

        Animator.fadeView(mMessageTitle, reset, duration, delay);
        Animator.fadeView(mMessageBody, reset, duration, delay);

        Animator.startAnimations();
    }

    private void cancel(Animation out, Animation in) {

        if (mFailed) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.setup_url)));
            startActivity(browserIntent);
            resetLogin();

            return;
        }

        mUsername.clearFocus();
        mUsername.setText("");
        mPassword.clearFocus();
        mPassword.setText("");
        mSignupName.setText("");
        mSignupEmail.setText("");
        mSignupBank.setText("");
        UiUtils.hideKeyboard(this, mViewFlipper);
        mViewFlipper.setOutAnimation(out);
        mViewFlipper.setInAnimation(in);
        mViewFlipper.setDisplayedChild(0);
    }

    @Override
    public String getActivityTitle() {

        return null;
    }

}
