package com.moneydesktop.finance.tablet.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.SlideFrom;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.BankDao;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.adapter.AccountSettingsTypesAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.SlidingView;


public class AccountSettingsTabletFragment extends BaseFragment{

	private static String mAccountName;
	private static String mAccountId;
	private static String mAccountTypeName;
	
	private EditText mField1;
	private EditText mField2;
	private TextView mField1Label;
	private TextView mField2Label;
	private Button mSave;
	private AccountTypeDao mAccountTypeDAO;
	private List<AccountType> mAllAccountTypes;
	private SlidingView mSlidingView;
	private static BankAccount mBankAccount;
	private AccountType mSelectedAccountType;
	
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
		mAccountId = intent.getExtras().getString(Constant.KEY_BANK_ACCOUNT_ID);
		mAccountTypeName = intent.getExtras().getString(Constant.KEY_ACCOUNT_TYPE);
	
		
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_account_settings, null);
        
        
        BankAccountDao bankAccountDAO = ApplicationContext.getDaoSession().getBankAccountDao();
        mBankAccount = bankAccountDAO.load(Long.valueOf(mAccountId.hashCode()));
        
        mField1 = (EditText) mRoot.findViewById(R.id.account_settings_option1_edittxt);
        mField2 = (EditText) mRoot.findViewById(R.id.account_settings_option2_edittxt);
        mField1Label = (TextView) mRoot.findViewById(R.id.account_settings_option1_title_txt);
        mField2Label = (TextView) mRoot.findViewById(R.id.account_settings_option2_title_txt);
        mSave = (Button) mRoot.findViewById(R.id.account_settings_save_button);
        
        mField1.setText(mAccountName);
        mField2.setText(mAccountTypeName);
        mField1Label.setText(getString(R.string.label_account_name));
        mField2Label.setText(getString(R.string.label_account_type));
        mSave.setText(getString(R.string.save));
        
        Fonts.applyPrimaryBoldFont(mField1, 14);
        Fonts.applyPrimaryBoldFont(mField2, 14);
        Fonts.applyPrimaryBoldFont(mField1Label, 14);
        Fonts.applyPrimaryBoldFont(mField2Label, 14);
        Fonts.applyPrimaryBoldFont(mSave, 18);
        
        mAccountTypeDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
        mAllAccountTypes = mAccountTypeDAO.loadAll();
        
        setupOnClickListeners();
        
        return mRoot;
    }

	private void setupOnClickListeners() {
		
		mField2.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(final View v) {
				ArrayAdapter<AccountType> listAdapter;   
				
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View inflatedView = inflater.inflate(R.layout.tablet_account_type_settings, null);
                final ListView accountTypesListView = (ListView)inflatedView.findViewById(R.id.account_type_settings_list);
                               
                listAdapter = new AccountSettingsTypesAdapter(getActivity(), R.layout.tablet_account_type_settings_list_item, mAllAccountTypes);
                
                accountTypesListView.setAdapter(listAdapter);
                
                mSlidingView = new SlidingView(getActivity(), 0, 0, (ViewGroup)mRoot, inflatedView, SlideFrom.RIGHT, v);
		
				accountTypesListView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							mSlidingView.dismiss();
							
							mSelectedAccountType = ((AccountType)accountTypesListView.getItemAtPosition(position));
							
							mField2.setText(mSelectedAccountType.getAccountTypeName());
						};
				});
			}
		});
		
		mSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBankAccount.setAccountType(mSelectedAccountType);
				mBankAccount.updateSingle();
				
        		//start the sync
        		Intent intent = new Intent(getActivity(), SyncService.class);
        		getActivity().startService(intent);
        		((DropDownTabletActivity)getActivity()).dismissDropdown();
        		
        		
			}
		});
		
	}
}
