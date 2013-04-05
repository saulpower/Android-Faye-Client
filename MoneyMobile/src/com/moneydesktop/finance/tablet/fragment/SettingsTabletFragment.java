package com.moneydesktop.finance.tablet.fragment;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.views.SettingButton;
import com.moneydesktop.finance.views.navigation.NavBarButtons;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

@TargetApi(11)
public class SettingsTabletFragment extends BaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private SettingButton mLock, mFeedback, mLogout;
	
	public static SettingsTabletFragment newInstance() {

	    SettingsTabletFragment fragment = new SettingsTabletFragment();
		
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
		
		mRoot = inflater.inflate(R.layout.tablet_settings_view, null);
		setupViews();
		configureView();
		
		return mRoot;
	}
    
    @Override
    public void onResume() {
        super.onResume();
        
        setupTitleBar();
    }

    @Override
    public void isShowing() {

        setupTitleBar();
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

    private void setupTitleBar() {
        
        String[] icons = new String[0];
        
        ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
        
        new NavBarButtons(mActivity, icons, onClickListeners);
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

	            ((DashboardTabletActivity) mActivity).showDropdownFragment(FragmentType.FEEDBACK);
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
