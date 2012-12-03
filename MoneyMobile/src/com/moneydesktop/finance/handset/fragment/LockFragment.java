package com.moneydesktop.finance.handset.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.moneydesktop.finance.BaseActivity.AppearanceListener;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.util.Enums.LockType;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.WheelToggle;

import de.greenrobot.event.EventBus;

public class LockFragment extends BaseFragment implements AppearanceListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static LockFragment fragment;
	
	public static LockFragment newInstance() {
			
		fragment = new LockFragment();
	
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
	private WheelToggle toggle;
	private Button change;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.activity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        this.activity.updateNavBar(getFragmentTitle());
        
        (new Handler()).postDelayed(new Runnable() {
			
			@Override
			public void run() {

				toggle.setOn(!Preferences.getString(Preferences.KEY_LOCK_CODE, "").equals(""));
			}
		}, 200);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.handset_lock_view, null);
		
		toggle = (WheelToggle) root.findViewById(R.id.toggle);
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				toggleLock(isChecked);
			}
		});
		
		change = (Button) root.findViewById(R.id.change);
		change.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (toggle.isOn())
					EventBus.getDefault().post(new EventMessage().new LockEvent(LockType.CHANGE));
			}
		});
		
		Fonts.applyPrimaryFont(change, 14);
		
		return root;
	}
	
	private void toggleLock(boolean on) {

		animate(change).setDuration(500).alpha(on ? 1.0f : 0.0f).start();
		
		if (on) {
			EventBus.getDefault().post(new EventMessage().new LockEvent(LockType.NEW));
		} else {
			Preferences.saveString(Preferences.KEY_LOCK_CODE, "");
		}
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_lock);
	}
	
	public void onViewDidAppear() {

	}
}
