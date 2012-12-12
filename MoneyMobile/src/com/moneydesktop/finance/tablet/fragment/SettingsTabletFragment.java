package com.moneydesktop.finance.tablet.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.handset.fragment.LockFragment;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.views.SettingButton;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class SettingsTabletFragment extends BaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private SettingButton mLock, mFeedback, mLogout;
	
	public static SettingsTabletFragment newInstance(int position) {

	    SettingsTabletFragment fragment = new SettingsTabletFragment();
	    fragment.setPosition(position);
		
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mActivity.onFragmentAttached(this);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_settings_view, null);
		setupViews();
		configureView();
		
		return mRoot;
	}
    
    @Override
    public void onResume() {
        super.onResume();

        this.mActivity.updateNavBar(getFragmentTitle());
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
		
		mLock = (SettingButton) mRoot.findViewById(R.id.lock);
		mFeedback = (SettingButton) mRoot.findViewById(R.id.feedback);
		mLogout = (SettingButton) mRoot.findViewById(R.id.logout);
	}
	
	private void configureView() {
        setupListeners();
	}
	
	private void setupListeners() {
		
		mLock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

	            Fragment frag = LockFragment.newInstance(true);
	            ((DashboardTabletActivity) mActivity).showPopupFragment(frag);
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
		return getString(R.string.title_activity_settings);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
