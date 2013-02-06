package com.moneydesktop.finance.tablet.fragment;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.util.EmailUtils;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.SettingButton;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class SettingsTabletFragment extends BaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private SettingButton mLock, mFeedback, mLogout;
	
	public static SettingsTabletFragment newInstance(FragmentType type) {

	    SettingsTabletFragment fragment = new SettingsTabletFragment();
	    fragment.setType(type);
		
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
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
        
        setupTitleBar(getActivity());
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

    private void setupTitleBar(final Activity activity) {
        
        String[] icons = new String[0];
        
        ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
        
        new NavBarButtons(activity, icons, onClickListeners);
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

	            ((DashboardTabletActivity) mActivity).showDropdownFragment(FragmentType.LOCK_SCREEN);
			}
		});
		
		mFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			    EmailUtils.sendEmail(mActivity, mActivity.getString(R.string.feedback_subject), "", new String[] { mActivity.getString(R.string.feedback_email) });
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
