package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.util.Fonts;


public class AccountSettingsTabletFragment extends BaseFragment{

	private static String mAccountName;
	private static String mAccountType;
	
	private EditText mField1;
	private EditText mField2;
	private TextView mField1Label;
	private TextView mField2Label;
	private Button mSave;
	
	@Override
	public String getFragmentTitle() {
		return mAccountName;
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        mActivity.onFragmentAttached(this);
	}

	public static AccountSettingsTabletFragment newInstance(Intent intent) {
		
		AccountSettingsTabletFragment fragment = new AccountSettingsTabletFragment();
        
		mAccountName = intent.getExtras().getString(Constant.KEY_ACCOUNT_NAME);
		mAccountType = intent.getExtras().getString(Constant.KEY_ACCOUNT_TYPE);
		
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_account_settings, null);
        
        mField1 = (EditText) mRoot.findViewById(R.id.account_settings_option1_edittxt);
        mField2 = (EditText) mRoot.findViewById(R.id.account_settings_option2_edittxt);
        mField1Label = (TextView) mRoot.findViewById(R.id.account_settings_option1_title_txt);
        mField2Label = (TextView) mRoot.findViewById(R.id.account_settings_option2_title_txt);
        mSave = (Button) mRoot.findViewById(R.id.account_settings_save_button);
        
        mField1.setText(mAccountName);
        mField2.setText(mAccountType);
        mField1Label.setText(getString(R.string.label_account_name));
        mField2Label.setText(getString(R.string.label_account_type));
        mSave.setText(getString(R.string.save));
        
        Fonts.applyPrimaryBoldFont(mField1, 14);
        Fonts.applyPrimaryBoldFont(mField2, 14);
        Fonts.applyPrimaryBoldFont(mField1Label, 14);
        Fonts.applyPrimaryBoldFont(mField2Label, 14);
        Fonts.applyPrimaryBoldFont(mSave, 18);
        
        setupOnClickListeners();
        
        return mRoot;
    }

	private void setupOnClickListeners() {
		mField1.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Toast.makeText(getActivity(), "Field 1 touched", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		
		mField2.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Toast.makeText(getActivity(), "Field 2 touched", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		
		mSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "save clicked", Toast.LENGTH_SHORT).show();
			}
		});
		
	}
}
