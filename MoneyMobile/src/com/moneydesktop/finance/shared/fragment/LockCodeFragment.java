package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.LockType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.handset.activity.PopupHandsetActivity;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.LockCodeView;
import com.moneydesktop.finance.views.LockCodeView.ProcessCodeListener;

public class LockCodeFragment extends BaseFragment implements ProcessCodeListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	public static LockCodeFragment newInstance(LockType lockType) {
        
	    LockCodeFragment fragment = new LockCodeFragment();
    
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_LOCK, lockType);
        fragment.setArguments(args);
        
        return fragment;
    }
	
	public final String EXTRA_CONFIRMATION = "extra_confirmation";
	public static final String EXTRA_LOCK = "extra_lock";
	
	private PopupHandsetActivity mLockActivity;
	
	private LinearLayout mContainer;
	private TextView mCancel;
	private ViewFlipper mFlipper;
	private LockCodeView mCurrentLockCode, mLockCode1, mLockCode2;
	
	private String mConfirmation = null;
	private LockType mType;
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
        
	    if (activity instanceof PopupHandsetActivity) {
	        mLockActivity = (PopupHandsetActivity) activity;
	    }
	}

	@Override
	public FragmentType getType() {
		return FragmentType.LOCK_SCREEN;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.handset_lock_code_view, null);

        mType = (LockType) getArguments().getSerializable(EXTRA_LOCK);
        
        if (mType == null) {
            mType = LockType.NEW;
        }
        
        setupViews();
        
        if (mActivity instanceof DropDownTabletActivity) {
            ((DropDownTabletActivity) mActivity).setEditText(mLockCode1.getCurrentField());
        }
        
        return mRoot;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mLockActivity != null) {
		    getActivity().finish();
		}
	}
	
	@Override
	public boolean onBackPressed() {
		
		if (mType == LockType.NEW && mLockActivity != null) {
			
			dismissModal();
			return true;
			
		} else if (mType == LockType.LOCK && mLockActivity != null) {
			
			getActivity().moveTaskToBack(true);
			return true;
			
		} else {

	    	mActivity.popBackStack();
		    return true;
		}
	}
	
	public void setupViews() {

		mFlipper = (ViewFlipper) mRoot.findViewById(R.id.flipper);
		mFlipper.setInAnimation(mActivity, R.anim.in_right);
		mFlipper.setOutAnimation(mActivity, R.anim.out_left);
		
		mLockCode1 = (LockCodeView) mRoot.findViewById(R.id.lock_code1);
		mLockCode1.setListener(this);

		mLockCode2 = (LockCodeView) mRoot.findViewById(R.id.lock_code2);
		mLockCode2.setListener(this);
		
		mCurrentLockCode = mLockCode1;
		
		setMessage();

		mCancel = (TextView) mRoot.findViewById(R.id.cancel);
		mCancel.setVisibility(mType == LockType.LOCK ? View.GONE : View.VISIBLE);
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dismissModal();
			}
		});
		
		Fonts.applyPrimaryFont(mCancel, 12);
		
		// The container prevents the user from selecting
		// the text fields manually
		mContainer = (LinearLayout) mRoot.findViewById(R.id.block_container);
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

        UiUtils.hideKeyboard(getActivity(), mContainer);
        
	    if (mLockActivity != null) {
	        mLockActivity.dismissModal();
	    } else {
	    	mActivity.popBackStack();
	    }
	}

    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_activity_lock_code).toUpperCase();
    }
}
