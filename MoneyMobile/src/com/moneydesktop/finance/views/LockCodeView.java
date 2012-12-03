package com.moneydesktop.finance.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

public class LockCodeView extends RelativeLayout {

    private Context mContext;
    private LinearLayout container;
	private EditText currentField, nextField, field1, field2, field3, field4;
	private TextView text;
	private ProcessCodeListener listener;
	private Animation shake;

	public void setListener(ProcessCodeListener listener) {
		this.listener = listener;
	}

	/**
	 * Once text is entered move to the next field
	 */
	private TextWatcher watcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			
			if (s.length() > 0) {
				
				nextField = (EditText) currentField.focusSearch(View.FOCUS_DOWN);
				
				if (nextField != null) {
					
					nextField.requestFocus();
					currentField = nextField;
				
				} else {
					
					listener.processCode();
				}
			}
		}
	};
	
	/**
	 * When the delete key is pressed move to the previous
	 * field and delete its content.
	 */
	private View.OnKeyListener keyListener = new View.OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			
			if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
				
				nextField = (EditText) currentField.focusSearch(View.FOCUS_UP);
				
				if (nextField != null) {

					nextField.setText("");
					nextField.requestFocus();
					currentField = nextField;
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
	private OnFocusChangeListener focus = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			
			if (hasFocus)
				((EditText) v).setText("");
		}
	};
    
	public LockCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        inflater.inflate(R.layout.lock_code_view, this, true);
        setupViews();
        
        shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
	}
	
	private void setupViews() {

		container = (LinearLayout) findViewById(R.id.container);
		
		text = (TextView) findViewById(R.id.lock_text);
		Fonts.applyPrimaryFont(text, 14);
		
		field1 = (EditText) findViewById(R.id.field1);
		field2 = (EditText) findViewById(R.id.field2);
		field3 = (EditText) findViewById(R.id.field3);
		field4 = (EditText) findViewById(R.id.field4);
		
		field1.setOnKeyListener(keyListener);
		field2.setOnKeyListener(keyListener);
		field3.setOnKeyListener(keyListener);
		field4.setOnKeyListener(keyListener);
		
		field1.setOnFocusChangeListener(focus);
		field2.setOnFocusChangeListener(focus);
		field3.setOnFocusChangeListener(focus);
		field4.setOnFocusChangeListener(focus);
		
		field1.addTextChangedListener(watcher);
		field2.addTextChangedListener(watcher);
		field3.addTextChangedListener(watcher);
		field4.addTextChangedListener(watcher);
		
		currentField = field1;
		showKeyboard();
	}
	
	public void resetFields() {
		
		field1.setText("");
		field2.setText("");
		field3.setText("");
		field4.setText("");
		
		field1.requestFocus();
		currentField = field1;
	}
	
	public void showKeyboard() {
		
		field1.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(field1, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 100);

	}
	
	public String getCode() {
		
		String code = "";
		
		code = field1.getText().toString() + field2.getText().toString() + field3.getText().toString() + field4.getText().toString();
		
		return validateCode(code) ? code : "";
	}
	
	private boolean validateCode(String code) {
		
		boolean result = false;

		result = code.length() == 4;
		result = code.matches("[0-9]{4}") && result;
		
		return result;
	}
	
	public void setMessage(String message) {
		text.setText(message);
	}
	
	public void shakeContainer() {
		
		container.startAnimation(shake);
	}
	
	public interface ProcessCodeListener {
		public void processCode();
	}
}