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
	
	public static boolean showing = false;
	
	private LinearLayout container;
	private TextView cancel;
	private ViewFlipper flipper;
	private LockCodeView currentLockCode, lockCode1, lockCode2;
	
	private String confirmation = null;
	private LockType type;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.handset_lock_code_view);
        
        type = (LockType) getIntent().getSerializableExtra(EXTRA_LOCK);
        
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
		
		showing = false;
	}
	
	@Override
	public void onBackPressed() {
		
		if (type == LockType.LOCK) {
			
			moveTaskToBack(true);
			return;
		}
		
		super.onBackPressed();
	}
	
	public void setupViews() {

		flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.setInAnimation(this, R.anim.in_right);
		flipper.setOutAnimation(this, R.anim.out_left);
		
		lockCode1 = (LockCodeView) findViewById(R.id.lock_code1);
		lockCode1.setListener(this);

		lockCode2 = (LockCodeView) findViewById(R.id.lock_code2);
		lockCode2.setListener(this);
		
		currentLockCode = lockCode1;
		
		setMessage();

		cancel = (TextView) findViewById(R.id.cancel);
		cancel.setVisibility(type == LockType.LOCK ? View.GONE : View.VISIBLE);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dismissModal();
			}
		});
		
		Fonts.applyPrimaryFont(cancel, 14);
		
		// The container prevents the user from selecting
		// the text fields manually
		container = (LinearLayout) findViewById(R.id.container);
		container.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				currentLockCode.showKeyboard();
				
				return true;
			}
		});
	}
	
	private void setMessage() {

		String message = "";
		
		switch (type) {
		case LOCK:
			message = getString(R.string.text_lock_app);
			break;
		case CHANGE:
			message = getString(R.string.text_lock);
			break;
		case NEW:
			boolean confirm = (confirmation == null);
			message = getString(confirm ? R.string.text_lock_create : R.string.text_lock_confirm);
			break;
		}
		
		currentLockCode.setMessage(message);
	}
	
	public void processCode() {

		String savedCode = Preferences.getString(Preferences.KEY_LOCK_CODE, "");
		String code = currentLockCode.getCode();
		currentLockCode.resetFields();
		
		if (type == LockType.LOCK) {
			
			processLocked(code, savedCode);
			return;
		}
		
		if (type == LockType.CHANGE) {
			
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
		
		if (confirmation == null) {
			
			confirmationCode(code);
			
		} else if (code.equals(confirmation)) {
			
			Preferences.saveString(Preferences.KEY_LOCK_CODE, code);
			dismissModal();
		
		} else {
			
			wrongCode();
		}
	}
	
	private void confirmationCode(String code) {
		
		confirmation = code;
		type = LockType.NEW;
		
		if (currentLockCode.equals(lockCode1))
			currentLockCode = lockCode2;
		else
			currentLockCode = lockCode1;
		
		setMessage();
		
		flipper.showNext();
	}
	
	private void wrongCode() {

		currentLockCode.shakeContainer();
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
