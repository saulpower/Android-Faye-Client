package com.moneydesktop.finance.handset.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.shared.adapter.SelectAccountTypesAdapter;
import com.moneydesktop.finance.shared.adapter.SelectPropertyTypesAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.shared.fragment.FixBankFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

public class AccountTypesManualSaveHandsetFragment extends BaseFragment{

	private static AccountTypesManualSaveHandsetFragment mCurrentFragment;
	private static AccountType mSelectedAccountType;
	private EditText mAccountName, mCurrentBalance;
	private TextView mSave;
	private JSONObject mAddManualBankJsonRequest;
	
	@Override
	public FragmentType getType() {
		return null;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.label_account_type);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    //    EventBus.getDefault().register(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
     //   EventBus.getDefault().unregister(this);
    }
    
	@Override
	public boolean onBackPressed() {
		return false;
	}

	public static AccountTypesManualSaveHandsetFragment newInstance(AccountType accountType) {
		
		AccountTypesManualSaveHandsetFragment frag = new AccountTypesManualSaveHandsetFragment();
		mCurrentFragment = frag;
		mSelectedAccountType = accountType;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_save_manual_account, null);	

		mAccountName = (EditText)mRoot.findViewById(R.id.handset_add_bank_manually_account_name);
		mCurrentBalance = (EditText)mRoot.findViewById(R.id.handset_add_bank_manually_current_balance_edittext);
		mSave = (TextView)mRoot.findViewById(R.id.handset_add_bank_manually_save);
		
		Fonts.applyPrimaryBoldFont(mSave, 14);
		
		setupView();
		
		return mRoot;
	}

	private void setupView() {
		mSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String balance = mCurrentBalance.getText().toString();
				String accountName = mAccountName.getText().toString();
				
				if (accountName.equals("")) {
			        DialogUtils.alertDialog(getString(R.string.add_account_manually_cannot_save), 
			        		getString(R.string.add_account_manually_cannot_save_message), 
			        		mActivity);
			 
				} else {
				
					UiUtils.hideKeyboard(mActivity, v);
					mAddManualBankJsonRequest = new JSONObject();
	
					try {
						mAddManualBankJsonRequest.putOpt(Constant.KEY_ACCOUNT_TYPE, mSelectedAccountType.getAccountTypeId());
					
						mAddManualBankJsonRequest.putOpt(Constant.KEY_USER_GUID, User.getCurrentUser().getUserId());
						mAddManualBankJsonRequest.putOpt(Constant.KEY_IS_HIDDEN, false);
						mAddManualBankJsonRequest.putOpt(Constant.KEY_BALANCE, balance);
						mAddManualBankJsonRequest.putOpt(Constant.KEY_ORG_BALANCE, balance);
						mAddManualBankJsonRequest.putOpt(Constant.KEY_IS_DELETED, false);
						mAddManualBankJsonRequest.putOpt(Constant.KEY_NAME, accountName);
						mAddManualBankJsonRequest.putOpt(Constant.KEY_USER_NAME, accountName);
						
						//the "." means that its a subtype of the account type "Property"
						if (mSelectedAccountType.getAccountTypeId().contains(".")) {
							
							String[] splitID = mSelectedAccountType.getAccountTypeId().split("\\.");
							mAddManualBankJsonRequest.putOpt(Constant.KEY_PROPERTY_TYPE, splitID[1]);
							
						}
					
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					
					new Thread(new Runnable() {			
						public void run() {	
							DataBridge.sharedInstance().saveManualAccount(mAddManualBankJsonRequest);
							DataController.save();
							
							Handler test = new Handler(Looper.getMainLooper());
				    	    test.post(new Runnable() {
				        	    public void run()
				        	    {
									Intent intent = new Intent(mActivity, SyncService.class);
						    		mActivity.startService(intent);
				        	    }
				        	});
						}
					}).start();
					
					mActivity.popBackStackTo(0);
				}
			}
		});
	}
	
}