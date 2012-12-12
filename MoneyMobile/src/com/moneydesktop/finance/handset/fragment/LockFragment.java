package com.moneydesktop.finance.handset.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.util.Enums.LockType;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.WheelToggle;

import de.greenrobot.event.EventBus;

public class LockFragment extends BaseFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static final String ARG_FRAG = "fragment";
	
	public static LockFragment newInstance(boolean inFragment) {
			
	    LockFragment fragment = new LockFragment();
	
        Bundle args = new Bundle();
        args.putBoolean(ARG_FRAG, inFragment);
        fragment.setArguments(args);
        
        return fragment;
	}
	
	private WheelToggle mToggle;
	private Button mChange;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        mActivity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        this.mActivity.updateNavBar(getFragmentTitle());
        
        (new Handler()).postDelayed(new Runnable() {
			
			@Override
			public void run() {

				mToggle.setOn(!Preferences.getString(Preferences.KEY_LOCK_CODE, "").equals(""));
			}
		}, 200);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_lock_view, null);
		
		mToggle = (WheelToggle) mRoot.findViewById(R.id.toggle);
		mToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				toggleLock(isChecked);
			}
		});
		
		mChange = (Button) mRoot.findViewById(R.id.change);
		mChange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (mToggle.isOn()) {
				    
				    if (!getArguments().getBoolean(ARG_FRAG)) {
				        EventBus.getDefault().post(new EventMessage().new LockEvent(LockType.CHANGE));
				        return;
				    }

		            showFragment(LockType.CHANGE);
				}
			}
		});
		
		Fonts.applyPrimaryFont(mChange, 14);
		
		return mRoot;
	}
	
	private void toggleLock(boolean on) {

		animate(mChange).setDuration(500).alpha(on ? 1.0f : 0.0f).start();
		
		if (on) {
		    
		    if (!getArguments().getBoolean(ARG_FRAG)) {
		        
		        EventBus.getDefault().post(new EventMessage().new LockEvent(LockType.NEW));
		        return;
		    }
		    
		    showFragment(LockType.NEW);
		    
		} else {
			Preferences.saveString(Preferences.KEY_LOCK_CODE, "");
		}
	}
	
	private void showFragment(LockType lockType) {

        Fragment fragment = LockCodeFragment.newInstance(lockType);
        
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.out_right, R.anim.in_left);
        ft.replace(R.id.fragment, fragment);
        ft.addToBackStack(null);
        ft.commit();
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_lock);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
