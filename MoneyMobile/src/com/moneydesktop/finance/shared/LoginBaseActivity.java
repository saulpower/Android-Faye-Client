
package com.moneydesktop.finance.shared;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.DebugActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.data.DemoData;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.activity.IntroTabletActivity;
import com.moneydesktop.finance.util.Animator;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.LabelEditText;

import de.greenrobot.event.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LoginBaseActivity extends BaseActivity {

    private final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-\\+]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final String TAG = "LoginActivity";

    private ViewFlipper mViewFlipper;
    private LinearLayout mButtonView, mCredentialView;
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
            cancel(mInLeft, mOutRight);
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
            Intent i;
            if (BaseActivity.isTablet(this)) {
                i = new Intent(this, DashboardTabletActivity.class);
            }
            else {
                i = new Intent(this, DashboardHandsetActivity.class);
            }
            startActivity(i);
            overridePendingTransition(R.anim.fade_in_fast, R.anim.none);
        }
    }

    protected void addDemoCredentials() {

        mUsername.setText("saul.howard@moneydesktop.com");
        mPassword.setText("password123");
    }

    @SuppressLint("NewApi")
    protected void setupView() {

        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mButtonView = (LinearLayout) findViewById(R.id.button_view);
        mCredentialView = (LinearLayout) findViewById(R.id.credentials);
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

        Fonts.applyPrimaryFont(mLoginViewButton, 20);
        Fonts.applyPrimaryFont(mDemoButton, 20);
        Fonts.applyPrimaryFont(mDemoButton2, 20);
        Fonts.applyPrimaryFont(mReturnHomeButton, 20);
        Fonts.applyPrimaryFont(mLoginButton, 20);
        Fonts.applyPrimaryFont(mCancelButton, 20);
        Fonts.applyPrimaryFont(mSubmitButton, 20);
        Fonts.applyPrimaryFont(mSignupText, 15);
        Fonts.applyPrimaryFont(mLoginText, 15);
        Fonts.applyPrimaryFont(mCancelText, 15);
        Fonts.applyPrimaryBoldFont(mThankYou, 20);
        Fonts.applyPrimaryFont(mTryDemo, 20);
        Fonts.applyPrimarySemiBoldFont(mUsername, 25);
        Fonts.applyPrimarySemiBoldFont(mPassword, 25);
        Fonts.applyPrimaryBoldFont(mNeedAccount, 20);
        Fonts.applySecondaryItalicFont(mNagBank, 15);
        Fonts.applyPrimaryBoldFont(mSubmitButton, 15);
        addListeners();
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

                cancel(mInLeft, mOutRight);
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

                mUsername.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });

        mPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mPassword.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
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

                mSignupName.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });
        mSignupEmail.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mSignupEmail.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
            }
        });
        mSignupBank.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mSignupBank.setTextColor(getResources().getColor(
                        hasFocus ? R.color.white : R.color.light_gray1));
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

    private void signup() {

        if (signupCheck() && validateEmail(mSignupEmail.getText().toString())) {
            submit();
        }

        else if (!signupCheck()) {
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_login_incomplete), this, null);
        }

        else if (!validateEmail(mSignupEmail.getText().toString())) {
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_email_invalid), this, null);
        }
    }

    private void submit() {

        // TODO add data submission code here
        toThankYou();
    }

    private void toThankYou() {

        mViewFlipper.setOutAnimation(mInLeft);
        mViewFlipper.setInAnimation(mOutRight);
        mViewFlipper.setDisplayedChild(3);
        hideKeyboard();
    }

    private void login() {

        if (mFailed) {

            resetLogin();

            return;
        }
        if (!validateEmail(mUsername.getText().toString()))
        {
            DialogUtils.alertDialog(getString(R.string.error_title),
                    getString(R.string.error_email_invalid), this, null);
            return;
        }
        if (loginCheck()) {

            authenticate();

        } else {

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

    private boolean validateEmail(String email) {

        Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
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

                    Intent i = new Intent(LoginBaseActivity.this, IntroTabletActivity.class);
                    startActivity(i);
                    finish();
                }
            }

        }.execute();
    }

    private void toggleAnimations(boolean reset) {

        int offset = (int) (.20 * UiUtils.getScreenMeasurements(this)[1]) * (reset ? -1 : 1);
        long duration = 750;
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
        mPassword.clearFocus();
        hideKeyboard();
        mViewFlipper.setOutAnimation(out);
        mViewFlipper.setInAnimation(in);
        mViewFlipper.setDisplayedChild(0);
    }

    private void hideKeyboard() {

        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mViewFlipper.getApplicationWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public String getActivityTitle() {

        return null;
    }

}
