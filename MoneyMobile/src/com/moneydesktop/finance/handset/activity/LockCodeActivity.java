package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.util.Enums.LockType;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.LockCodeView;
import com.moneydesktop.finance.views.LockCodeView.ProcessCodeListener;

public class LockCodeActivity extends BaseActivity implements ProcessCodeListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	public final String EXTRA_CONFIRMATION = "extra_confirmation";
	public static final String EXTRA_LOCK = "extra_lock";
	
	public static boolean sShowing = false;
	
	private LinearLayout mContainer;
	private TextView mCancel;
	private ViewFlipper mFlipper;
	private LockCodeView mCurrentLockCode, mLockCode1, mLockCode2;
	
	private String mConfirmation = null;
	private LockType mType;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.handset_lock_code_view);
        
        mType = (LockType) getIntent().getSerializableExtra(EXTRA_LOCK);
        
        setupViews();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		sShowing = false;
	}
	
	@Override
	public void onBackPressed() {
		
		if (mType == LockType.LOCK) {
			
			moveTaskToBack(true);
			return;
		}
		
		super.onBackPressed();
	}
	
	public void setupViews() {

		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
		mFlipper.setInAnimation(this, R.anim.in_right);
		mFlipper.setOutAnimation(this, R.anim.out_left);
		
		mLockCode1 = (LockCodeView) findViewById(R.id.lock_code1);
		mLockCode1.setListener(this);

		mLockCode2 = (LockCodeView) findViewById(R.id.lock_code2);
		mLockCode2.setListener(this);
		
		mCurrentLockCode = mLockCode1;
		
		setMessage();

		mCancel = (TextView) findViewById(R.id.cancel);
		mCancel.setVisibility(mType == LockType.LOCK ? View.GONE : View.VISIBLE);
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dismissModal();
			}
		});
		
		Fonts.applyPrimaryFont(mCancel, 14);
		
		// The container prevents the user from selecting
		// the text fields manually
		mContainer = (LinearLayout) findViewById(R.id.block_container);
		mContainer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				mCurrentLockCode.showKeyboard();
				
				return true;
			}
		});
		mContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				mCurrentLockCode.showKeyboard();
			}
		});
	}
	
	private void setMessage() {

		String message = "";
		
		switch (mType) {
		case LOCK:
			message = getString(R.string.text_lock_app);
			break;
		case CHANGE:
			message = getString(R.string.text_lock);
			break;
		case NEW:
			boolean confirm = (mConfirmation == null);
			message = getString(confirm ? R.string.text_lock_create : R.string.text_lock_confirm);
			break;
		}
		
		mCurrentLockCode.setMessage(message);
	}
	
	public void processCode() {

		String savedCode = Preferences.getString(Preferences.KEY_LOCK_CODE, "");
		String code = mCurrentLockCode.getCode();
		mCurrentLockCode.resetFields();
		
		if (mType == LockType.LOCK) {
			
			processLocked(code, savedCode);
			return;
		}
		
		if (mType == LockType.CHANGE) {
			
			processCodeChange(code, savedCode);
			return;
		}
		
		processConfirmation(code);
	}
	
	private void processLocked(String code, String savedCode) {
		
		if (code.equals(savedCode)) {

			dismissModal();
			return;
		}
			
		wrongCode();
	}
	
	private void processCodeChange(String code, String savedCode) {
		
		if (code.equals(savedCode)) {
			
			confirmationCode(null);
			return;
		}
		
		wrongCode();
	}
	
	private void processConfirmation(String code) {
		
		if (mConfirmation == null) {
			
			confirmationCode(code);
			
		} else if (code.equals(mConfirmation)) {
			
			Preferences.saveString(Preferences.KEY_LOCK_CODE, code);
			dismissModal();
		
		} else {
			
			wrongCode();
		}
	}
	
	private void confirmationCode(String code) {
		
		mConfirmation = code;
		mType = LockType.NEW;
		
		if (mCurrentLockCode.equals(mLockCode1))
			mCurrentLockCode = mLockCode2;
		else
			mCurrentLockCode = mLockCode1;
		
		setMessage();
		
		mFlipper.showNext();
	}
	
	private void wrongCode() {

		mCurrentLockCode.shakeContainer();
	}
	
	private void dismissModal() {
		
		finish();
		overridePendingTransition(R.anim.none, R.anim.out_down);
	}

	@Override
	public String getActivityTitle() {
		return null;
	}
}
