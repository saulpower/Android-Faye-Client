package com.moneydesktop.finance.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.util.Fonts;

public class LockCodeView extends RelativeLayout {
    
    public final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private LinearLayout mContainer;
	private EditText mCurrentField, mNextField, mField1, mField2, mField3, mField4;
	private TextView mText;
	private ProcessCodeListener mListener;
	private Animation mShake;

	public void setListener(ProcessCodeListener listener) {
		this.mListener = listener;
	}

	/**
	 * Once text is entered move to the next field
	 */
	private TextWatcher mWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			
			if (s.length() > 0) {
				
				mNextField = (EditText) mCurrentField.focusSearch(View.FOCUS_DOWN);
				
				if (mNextField != null) {
					
					mNextField.requestFocus();
					mCurrentField = mNextField;
				
				} else {
					
					mListener.processCode();
				}
			}
		}
	};
	
	/**
	 * When the delete key is pressed move to the previous
	 * field and delete its content.
	 */
	private View.OnKeyListener mKeyListener = new View.OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			
			if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
				
				mNextField = (EditText) mCurrentField.focusSearch(View.FOCUS_UP);
				
				if (mNextField != null) {

					mNextField.setText("");
					mNextField.requestFocus();
					mCurrentField = mNextField;
				}
				
				return true;
				
			} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
				
				return true;
			}

			return false;
		}
	};
	
	/**
	 * Make sure the currently focused field's content
	 * is cleared.
	 */
	private OnFocusChangeListener mFocus = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			
			if (hasFocus) {
				((EditText) v).setText("");
				mCurrentField = (EditText) v;
			}
		}
	};
    
	public LockCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        inflater.inflate(R.layout.lock_code_view, this, true);
        setupViews();
        
        mShake = AnimationFactory.createShakeAnimation(getContext(), 30);
	}
	
	private void setupViews() {

		mContainer = (LinearLayout) findViewById(R.id.container);
		
		mText = (TextView) findViewById(R.id.lock_text);
		Fonts.applyPrimaryFont(mText, 14);
		
		mField1 = (EditText) findViewById(R.id.field1);
		mField2 = (EditText) findViewById(R.id.field2);
		mField3 = (EditText) findViewById(R.id.field3);
		mField4 = (EditText) findViewById(R.id.field4);
		
		mField1.setOnKeyListener(mKeyListener);
		mField2.setOnKeyListener(mKeyListener);
		mField3.setOnKeyListener(mKeyListener);
		mField4.setOnKeyListener(mKeyListener);
		
		mField1.setOnFocusChangeListener(mFocus);
		mField2.setOnFocusChangeListener(mFocus);
		mField3.setOnFocusChangeListener(mFocus);
		mField4.setOnFocusChangeListener(mFocus);
		
		mField1.addTextChangedListener(mWatcher);
		mField2.addTextChangedListener(mWatcher);
		mField3.addTextChangedListener(mWatcher);
		mField4.addTextChangedListener(mWatcher);
		
		mCurrentField = mField1;
		showKeyboard();
	}
	
	public void resetFields() {
		
		mField1.setText("");
		mField2.setText("");
		mField3.setText("");
		mField4.setText("");
		
		mField1.requestFocus();
		mCurrentField = mField1;
	}
	
	public void showKeyboard() {
	    
	    mCurrentField.requestFocus();
	    mCurrentField.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mField1, InputMethodManager.SHOW_FORCED);
			}
		}, 100);

	}
	
	public String getCode() {
		
		String code = "";
		
		code = mField1.getText().toString() + mField2.getText().toString() + mField3.getText().toString() + mField4.getText().toString();
		
		return validateCode(code) ? code : "";
	}
	
	private boolean validateCode(String code) {
		
		boolean result = false;

		result = code.length() == 4;
		result = code.matches("[0-9]{4}") && result;
		
		return result;
	}
	
	public void setMessage(String message) {
		mText.setText(message);
	}
	
	public void shakeContainer() {
		
		mContainer.startAnimation(mShake);
	}
	
	public interface ProcessCodeListener {
		public void processCode();
	}
}