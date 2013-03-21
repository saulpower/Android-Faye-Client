package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.shared.fragment.LockFragment;
import com.moneydesktop.finance.util.Fonts;
import de.greenrobot.event.EventBus;

public class SettingsHandsetFragment extends BaseFragment {
    
	private TextView mLockIcon, mFeedbackIcon, mLogoutIcon, mLogoutLabel, mLockLabel, mFeedbackLabel;
	private LinearLayout mLock, mFeedback, mLogout;
	
	public static SettingsHandsetFragment newInstance() {
	    
		SettingsHandsetFragment fragment = new SettingsHandsetFragment();
		fragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}

	@Override
	public FragmentType getType() {
		return FragmentType.SETTINGS;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_settings_view, null);
		setupViews();
		configureView();
		
		return mRoot;
	}
    
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        configureView();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)  {
        super.onSaveInstanceState(outState);
    }
	
	private void setupViews() {
		
		mLock = (LinearLayout) mRoot.findViewById(R.id.lock);
		mLockIcon = (TextView) mRoot.findViewById(R.id.lock_icon);
		mLockLabel = (TextView) mRoot.findViewById(R.id.lock_label);
		mFeedback = (LinearLayout) mRoot.findViewById(R.id.feedback);
		mFeedbackIcon = (TextView) mRoot.findViewById(R.id.feedback_icon);
		mFeedbackLabel = (TextView) mRoot.findViewById(R.id.feedback_label);
		mLogout = (LinearLayout) mRoot.findViewById(R.id.logout);
		mLogoutIcon = (TextView) mRoot.findViewById(R.id.logout_icon);
		mLogoutLabel = (TextView) mRoot.findViewById(R.id.logout_label);
	}
	
	private void configureView() {

        Fonts.applyGlyphFont(mLockIcon, 35);
        Fonts.applyGlyphFont(mFeedbackIcon, 35);
        Fonts.applyGlyphFont(mLogoutIcon, 35);
        
        Fonts.applyPrimaryFont(mLogoutLabel, 9);
        Fonts.applyPrimaryFont(mLockLabel, 9);
        Fonts.applyPrimaryFont(mFeedbackLabel, 9);
        
        String logoutText = Preferences.getBoolean(Preferences.KEY_IS_DEMO_MODE, false) ? getString(R.string.label_exit) : getString(R.string.label_unlink);
        mLogoutLabel.setText(logoutText.toUpperCase());
        
        setupListeners();
	}
	
	private void setupListeners() {
		
		mLock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

                mActivity.pushFragment(R.id.settings_fragment, LockFragment.newInstance(false));
			}
		});
		
		mFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				EventBus.getDefault().post(new EventMessage().new FeedbackEvent());
			}
		});

		mLogout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				EventBus.getDefault().post(new EventMessage().new LogoutEvent());
			}
		});
	}
	
	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_settings).toUpperCase();
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
