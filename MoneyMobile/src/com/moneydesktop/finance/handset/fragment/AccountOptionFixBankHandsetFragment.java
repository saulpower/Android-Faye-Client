package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.shared.fragment.FixBankFragment;
import com.moneydesktop.finance.util.Fonts;

public class AccountOptionFixBankHandsetFragment extends FixBankFragment{
	
	private static Bank mBank;
	private TextView mTitle, mMessage, mContinue;
	private AccountOptionsCredentialsHandsetFragment mCredentialFragment;
	private AccountOptionMfaQuestionHandsetFragment mMfaQuestionFragment;
	private static AccountOptionFixBankHandsetFragment mCurrentFragment;
	
	@Override
	public FragmentType getType() {
		return null;
	}

	@Override
	public String getFragmentTitle() {
		return null;
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

	public static AccountOptionFixBankHandsetFragment newInstance(Bank bank) {
		
		AccountOptionFixBankHandsetFragment frag = new AccountOptionFixBankHandsetFragment();
		mCurrentFragment = frag;
		mBank = bank;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_account_option_fix_bank_view, null);
		
		mTitle = (TextView)mRoot.findViewById(R.id.handset_bank_broken_notification_title);
		mMessage = (TextView)mRoot.findViewById(R.id.handset_bank_broken_notification_description);
		mContinue = (TextView)mRoot.findViewById(R.id.handset_bank_broken_notification_continue);
		
		Fonts.applyPrimaryBoldFont(mTitle, 18);
		Fonts.applyPrimaryBoldFont(mMessage, 10);
		Fonts.applyPrimaryBoldFont(mTitle, 14);
		
		setupView();
		
		return mRoot;
	}

	private void setupView() {
		
		if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_MFA.index())) {
			mMessage.setText(getString(R.string.status_description_6));
			mContinue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					loadMfaFragment();
				}
			});
		} else if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_LOGIN_FAILED.index())) {
			mMessage.setText(getString(R.string.status_description_5));
			mContinue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					loadCredentialsFragment();
				}
			});
		} else if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_EXCEPTION.index())) {
			mContinue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {					
					loadCredentialsFragment();					
				}
			});
		}
		
	}
	
	private void loadMfaFragment() {
		AccountOptionMfaQuestionHandsetFragment frag = getMfaQuestionFragment(mBank);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(mCurrentFragment.getId(), frag);
		ft.addToBackStack(null);
		ft.commit();
	}
	
	private AccountOptionsCredentialsHandsetFragment getCredentialsFragment(Bank bank) {
		
		if (mCredentialFragment == null) {
			mCredentialFragment = AccountOptionsCredentialsHandsetFragment.newInstance(bank);
		}
		
		return mCredentialFragment;
	}
	
	private AccountOptionMfaQuestionHandsetFragment getMfaQuestionFragment(Bank bank) {
		
		if (mMfaQuestionFragment == null) {
			mMfaQuestionFragment = AccountOptionMfaQuestionHandsetFragment.newInstance(bank);
		}
		
		return mMfaQuestionFragment;
	}

	
	private void loadCredentialsFragment() {
		AccountOptionsCredentialsHandsetFragment frag = getCredentialsFragment(mBank);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(mCurrentFragment.getId(), frag);
		ft.addToBackStack(null);
		ft.commit();
	}
	
}
