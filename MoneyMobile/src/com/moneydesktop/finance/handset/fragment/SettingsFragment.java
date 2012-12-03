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

	private TextView lockIcon, feedbackIcon, logoutIcon, logoutLabel;
	private LinearLayout lock, feedback, logout;
	
	public static SettingsFragment newInstance(int position) {
		
		SettingsFragment frag = new SettingsFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.handset_settings_view, null);
		setupViews();
		
		return root;
	}
	
	private void setupViews() {
		
		lock = (LinearLayout) root.findViewById(R.id.lock);
		lockIcon = (TextView) root.findViewById(R.id.lock_icon);
		feedback = (LinearLayout) root.findViewById(R.id.feedback);
		feedbackIcon = (TextView) root.findViewById(R.id.feedback_icon);
		logout = (LinearLayout) root.findViewById(R.id.logout);
		logoutIcon = (TextView) root.findViewById(R.id.logout_icon);
		logoutLabel = (TextView) root.findViewById(R.id.logout_label);
		
		Fonts.applyGlyphFont(lockIcon, 50);
		Fonts.applyGlyphFont(feedbackIcon, 50);
		Fonts.applyGlyphFont(logoutIcon, 50);
		
		String logoutText = Preferences.getBoolean(Preferences.KEY_IS_DEMO_MODE, false) ? getString(R.string.label_exit) : getString(R.string.label_unlink);
		logoutLabel.setText(logoutText.toUpperCase());
		
		setupListeners();
	}
	
	private void setupListeners() {
		
		lock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				activity.showFragment(getPosition());
			}
		});
		
		feedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			    Intent intent = new Intent(Intent.ACTION_SEND);
			    intent.setType("plain/text");
			    intent.putExtra(Intent.EXTRA_EMAIL,new String[] { activity.getString(R.string.feedback_email) });
			    intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.feedback_subject));
			    intent.putExtra(Intent.EXTRA_TEXT, "");
			    startActivity(Intent.createChooser(intent, activity.getString(R.string.feedback_title)));
			}
		});

		logout.setOnClickListener(new OnClickListener() {
			
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

}
