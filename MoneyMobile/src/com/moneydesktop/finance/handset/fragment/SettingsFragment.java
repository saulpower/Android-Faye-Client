package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

public class SettingsFragment extends BaseFragment {

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
		
		return root;
	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
