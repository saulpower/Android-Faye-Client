package com.moneydesktop.finance.handset.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.util.Fonts;

import de.greenrobot.event.EventBus;

public class SettingsFragment extends BaseFragment {

    private static SettingsFragment sFragment;
    
	private TextView mLockIcon, mFeedbackIcon, mLogoutIcon, mLogoutLabel;
	private LinearLayout mLock, mFeedback, mLogout;
	
	public static SettingsFragment getInstance(int position) {
		
	    if (sFragment != null) {
	        return sFragment;
	    }
	    
	    sFragment = new SettingsFragment();
	    sFragment.setPosition(position);
        sFragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
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
		mFeedback = (LinearLayout) mRoot.findViewById(R.id.feedback);
		mFeedbackIcon = (TextView) mRoot.findViewById(R.id.feedback_icon);
		mLogout = (LinearLayout) mRoot.findViewById(R.id.logout);
		mLogoutIcon = (TextView) mRoot.findViewById(R.id.logout_icon);
		mLogoutLabel = (TextView) mRoot.findViewById(R.id.logout_label);
	}
	
	private void configureView() {

        Fonts.applyGlyphFont(mLockIcon, 50);
        Fonts.applyGlyphFont(mFeedbackIcon, 50);
        Fonts.applyGlyphFont(mLogoutIcon, 50);
        
        String logoutText = Preferences.getBoolean(Preferences.KEY_IS_DEMO_MODE, false) ? getString(R.string.label_exit) : getString(R.string.label_unlink);
        mLogoutLabel.setText(logoutText.toUpperCase());
        
        setupListeners();
	}
	
	private void setupListeners() {
		
		mLock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mActivity.showFragment(getPosition());
			}
		});
		
		mFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			    Intent intent = new Intent(Intent.ACTION_SEND);
			    intent.setType("plain/text");
			    intent.putExtra(Intent.EXTRA_EMAIL,new String[] { mActivity.getString(R.string.feedback_email) });
			    intent.putExtra(Intent.EXTRA_SUBJECT, mActivity.getString(R.string.feedback_subject));
			    intent.putExtra(Intent.EXTRA_TEXT, "");
			    startActivity(Intent.createChooser(intent, mActivity.getString(R.string.feedback_title)));
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
		return null;
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
