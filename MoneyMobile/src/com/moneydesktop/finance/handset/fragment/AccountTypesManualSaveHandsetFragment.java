package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import org.json.JSONObject;

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
				saveManualBank(v);
			}
		});
	}
	
	private void saveManualBank(View v) {
		String accountName = mAccountName.getText().toString();
		
		if (accountName.equals("")) {
	        DialogUtils.alertDialog(getString(R.string.add_account_manually_cannot_save), 
	        		getString(R.string.add_account_manually_cannot_save_message), 
	        		mActivity);
	 
		} else {

			UiUtils.hideKeyboard(mActivity, v);
            createManualBankAccount(mSelectedAccountType);
			
			mActivity.clearBackStack();
		}
	}

    private void createManualBankAccount(AccountType selectedAccountType) {

        double balance = Double.parseDouble(mCurrentBalance.getText().toString());
        String name = mAccountName.getText().toString();

        BankAccount bankAccount = BankAccount.createBankAccount(selectedAccountType, balance, name);
        bankAccount.insertSingle();

        SyncEngine.sharedInstance().syncBankAccount(bankAccount);
    }
	
}