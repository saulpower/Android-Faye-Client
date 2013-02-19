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
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.SlidingView;


public class AccountSettingsTabletFragment extends BaseFragment{

	private static String mAccountName;
	private static String mAccountId;
	private static String mAccountTypeName;
	
	private EditText mField1;
	private EditText mField2;
	private EditText mField3;
	private EditText mField4;
	private EditText mField5;
	private TextView mField1Label;
	private TextView mField2Label;
	private TextView mField3Label;
	private TextView mField4Label;
	private TextView mField5Label;
	private Button mSave;
	private AccountTypeDao mAccountTypeDAO;
	private List<AccountType> mFilteredAccountTypes;
	private List<AccountType> mFilteredPropertyTypes;
	private SlidingView mSlidingView;
	private static BankAccount mBankAccount;
	private AccountType mSelectedAccountType;
	private String mSelectedAccountTypeName;
	private String mSelectedPropertyTypeName;
	
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
        mField3 = (EditText) mRoot.findViewById(R.id.account_settings_option3_edittxt);
        mField4 = (EditText) mRoot.findViewById(R.id.account_settings_option4_edittxt);
        mField5 = (EditText) mRoot.findViewById(R.id.account_settings_option5_edittxt);
        mField1Label = (TextView) mRoot.findViewById(R.id.account_settings_option1_title_txt);
        mField2Label = (TextView) mRoot.findViewById(R.id.account_settings_option2_title_txt);
        mField3Label = (TextView) mRoot.findViewById(R.id.account_settings_option3_title_txt);
        mField4Label = (TextView) mRoot.findViewById(R.id.account_settings_option4_title_txt);
        mField5Label = (TextView) mRoot.findViewById(R.id.account_settings_option5_title_txt);
        mSave = (Button) mRoot.findViewById(R.id.account_settings_save_button);
       
        mField1.setText(mAccountName);
        mField2.setText(mAccountTypeName);
        mField1Label.setText(getString(R.string.label_account_name));
        mField2Label.setText(getString(R.string.label_account_type));
        mSave.setText(getString(R.string.save));
        
        preventKeyboardInterruption();
             
        Fonts.applyPrimaryBoldFont(mField1, 14);
        Fonts.applyPrimaryBoldFont(mField2, 14);
        Fonts.applyPrimaryBoldFont(mField3, 14);
        Fonts.applyPrimaryBoldFont(mField4, 14);
        Fonts.applyPrimaryBoldFont(mField5, 14);
        Fonts.applyPrimaryBoldFont(mField1Label, 14);
        Fonts.applyPrimaryBoldFont(mField2Label, 14);
        Fonts.applyPrimaryBoldFont(mField3Label, 14);
        Fonts.applyPrimaryBoldFont(mField4Label, 14);
        Fonts.applyPrimaryBoldFont(mField5Label, 14);
        Fonts.applyPrimaryBoldFont(mSave, 18);
        
        mSelectedAccountTypeName = mAccountTypeName;
        
        getAccountTypesLists();
        setupSecondaryOptions();
        setupOnClickListeners();
        
        return mRoot;
    }

	private void preventKeyboardInterruption() {
		mField3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (mActivity instanceof DropDownTabletActivity && hasFocus) {
		            ((DropDownTabletActivity) mActivity).setEditText(mField3);
		        }	
			}
		});
        
        mField4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (mActivity instanceof DropDownTabletActivity && hasFocus) {
		            ((DropDownTabletActivity) mActivity).setEditText(mField4);
		        }	
			}
		});
        
        mField5.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (mActivity instanceof DropDownTabletActivity && hasFocus) {
		            ((DropDownTabletActivity) mActivity).setEditText(mField5);
		        }	
			}
		});
	}

	private void getAccountTypesLists() {
		mAccountTypeDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
        List<AccountType> allAccountTypes = mAccountTypeDAO.loadAll();
        mFilteredAccountTypes = new ArrayList<AccountType>();
        mFilteredPropertyTypes = new ArrayList<AccountType>();
        
        for (AccountType accountType : allAccountTypes) {
        	if (!accountType.getAccountTypeName().equals("Unknown") 
        			&& !(accountType.getAccountTypeId().contains("."))) {
        		mFilteredAccountTypes.add(accountType);
        		
        	} else if (!accountType.getAccountTypeName().equals("Unknown")
        			&& (accountType.getAccountTypeId().contains("."))) {
        		mFilteredPropertyTypes.add(accountType);
        	}
        }
	}

	private void setupSecondaryOptions() {
		
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (mSelectedAccountTypeName.equals("Credit Card")) {
			
			mField3Label.setVisibility(View.VISIBLE);
			mField3.setVisibility(View.VISIBLE);
			
			mField4Label.setVisibility(View.VISIBLE);
			mField4.setVisibility(View.VISIBLE);
			
			mField5Label.setVisibility(View.VISIBLE);
			mField5.setVisibility(View.VISIBLE);
			
			//This is to disable the onclicklistener that was created when user selects account type "Property"
			mField3.setOnClickListener(null);
			
			if (mBankAccount.getInterestRate() != null) {
				mField3.setText("% " + mBankAccount.getInterestRate().toString());
			} else {
				mField3.setText("% 0");
			}
			mField3Label.setText(getString(R.string.label_account_interest_rate));
			
			if (mBankAccount.getDueDay() != null) {
				mField4.setText(mBankAccount.getDueDay().toString());
			} else {
				mField4.setText("0");
			}
			mField4Label.setText(getString(R.string.label_account_due_day));
			
			if (mBankAccount.getCreditLimit() != null) {
				mField5.setText("$ " + mBankAccount.getCreditLimit().toString());
			} else {
				mField5.setText("$ 0");
			}
			mField5Label.setText(getString(R.string.label_account_credit_limit));
			
		} else if (mSelectedAccountTypeName.equals("Loans") || (mSelectedAccountTypeName.equals("Mortgage"))) {
			mField3Label.setVisibility(View.VISIBLE);
			mField3.setVisibility(View.VISIBLE);
			
			mField4Label.setVisibility(View.VISIBLE);
			mField4.setVisibility(View.VISIBLE);
			
			mField5Label.setVisibility(View.GONE);
			mField5.setVisibility(View.GONE);
			
			//This is to disable the onclicklistener that was created when user selects account type "Property"
			mField3.setOnClickListener(null);
		
			if (mBankAccount.getInterestRate() != null) {
				mField3.setText("% " + mBankAccount.getInterestRate().toString());
			} else {
				mField3.setText("% 0");
			}
			mField3Label.setText(getString(R.string.label_account_interest_rate));
			
			if (mBankAccount.getBeginningBalance() != null) {
				mField4.setText("$ " + mBankAccount.getBeginningBalance().toString());
			} else {
				mField4.setText("$ 0");
			}
			mField4Label.setText(getString(R.string.label_account_original_balance));
			
		} else if (mSelectedAccountTypeName.equals("Investments") || (mSelectedAccountTypeName.equals("Line of Credit"))) {
			
			mField3Label.setVisibility(View.VISIBLE);
			mField3.setVisibility(View.VISIBLE);
			
			mField4Label.setVisibility(View.GONE);
			mField4.setVisibility(View.GONE);
			
			mField5Label.setVisibility(View.GONE);
			mField5.setVisibility(View.GONE);
			
			//This is to disable the onclicklistener that was created when user selects account type "Property"
			mField3.setOnClickListener(null);
			
			if (mBankAccount.getInterestRate() != null) {
				mField3.setText("% " + mBankAccount.getInterestRate().toString());
			} else {
				mField3.setText("% 0");
			}
			mField3Label.setText(getString(R.string.label_account_interest_rate));
			
		} else if (mSelectedAccountTypeName.equals("Property")) {			
			mField3Label.setVisibility(View.VISIBLE);
			mField3.setVisibility(View.VISIBLE);
			
			if (mBankAccount.getPropertyType() != null) {
				for (AccountType propertyTypes : mFilteredPropertyTypes) {
					String propertyId = propertyTypes.getAccountTypeId().replace(".", "");
					if (mBankAccount.getPropertyType().intValue() == Integer.valueOf(propertyId)) {
						mField3.setText(propertyTypes.getAccountTypeName());
					}
				}
				
			} else {
				mField3.setText("");
			}
			mField3Label.setText(getString(R.string.label_account_property_type));
			
			mField4Label.setVisibility(View.GONE);
			mField4.setVisibility(View.GONE);
			
			mField5Label.setVisibility(View.GONE);
			mField5.setVisibility(View.GONE);
			
			mField3.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ArrayAdapter<AccountType> listAdapter;   
					
	                final View inflatedView = inflater.inflate(R.layout.tablet_account_type_settings, null);
	                final ListView accountTypesListView = (ListView)inflatedView.findViewById(R.id.account_type_settings_list);
	                               
	                listAdapter = new AccountSettingsTypesAdapter(getActivity(), R.layout.tablet_account_type_settings_list_item, mFilteredPropertyTypes);
	                
	                accountTypesListView.setAdapter(listAdapter);
	                
	                mSlidingView = new SlidingView(getActivity(), 0, 0, (ViewGroup)mRoot, inflatedView, SlideFrom.RIGHT, v);
			
					accountTypesListView.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								mSlidingView.dismiss();
								((DropDownTabletActivity)getActivity()).animateLabelsReverse();
								
								AccountType mSelectedPropertyType = ((AccountType)accountTypesListView.getItemAtPosition(position));
								
								mSelectedPropertyTypeName = mSelectedPropertyType.getAccountTypeName();
								setupSecondaryOptions();
								mField3.setText(mSelectedPropertyType.getAccountTypeName());
							};
					});
				}
			});
			
			
		} else {
			mField3Label.setVisibility(View.GONE);
			mField3.setVisibility(View.GONE);
			
			mField4Label.setVisibility(View.GONE);
			mField4.setVisibility(View.GONE);
			
			mField5Label.setVisibility(View.GONE);
			mField5.setVisibility(View.GONE);
		}
		
	}
	
	private void setupOnClickListeners() {
		
		((DropDownTabletActivity)getActivity()).backArrowAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSlidingView != null) {
					if (mSlidingView.viewIsVisible()) {
						mSlidingView.dismiss();
						((DropDownTabletActivity)getActivity()).animateLabelsReverse();
					}
				}
			}
		});
		
		mField2.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(final View v) {
				ArrayAdapter<AccountType> listAdapter;   
				UiUtils.hideKeyboard(mActivity, v);
				
				((DropDownTabletActivity)getActivity()).animateLabelsForward(mActivity.getResources().getString(R.string.label_account_type), true);
				
				
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View inflatedView = inflater.inflate(R.layout.tablet_account_type_settings, null);
                final ListView accountTypesListView = (ListView)inflatedView.findViewById(R.id.account_type_settings_list);
                               
                listAdapter = new AccountSettingsTypesAdapter(getActivity(), R.layout.tablet_account_type_settings_list_item, mFilteredAccountTypes);
                
                accountTypesListView.setAdapter(listAdapter);
                
                mSlidingView = new SlidingView(getActivity(), 0, 0, (ViewGroup)mRoot, inflatedView, SlideFrom.RIGHT, v);
		
				accountTypesListView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							mSlidingView.dismiss();
							((DropDownTabletActivity)getActivity()).animateLabelsReverse();
							
							mSelectedAccountType = ((AccountType)accountTypesListView.getItemAtPosition(position));
							
							mSelectedAccountTypeName = mSelectedAccountType.getAccountTypeName();
							setupSecondaryOptions();
							mField2.setText(mSelectedAccountType.getAccountTypeName());
						};
				});
			}
		});
		
		mSave.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				setBankAccountValues();						
				mBankAccount.updateSingle();
				
        		//start the sync
        		Intent intent = new Intent(getActivity(), SyncService.class);
        		getActivity().startService(intent);
        		((DropDownTabletActivity)getActivity()).dismissDropdown();
			}

		});
	}

	
	private void setBankAccountValues() {
		//If mSelectedAccountType is null, that means a new account type wasn't selected. No need to save.
		if (mSelectedAccountType != null) {
			mBankAccount.setAccountType(mSelectedAccountType);
		}
		
		if (mSelectedAccountTypeName.equals("Credit Card")) {
			String interestRate = mField3.getText().toString().replace("% ", "");
			String creditLimit = mField5.getText().toString().replace("$ ", "");
			mBankAccount.setInterestRate(Double.valueOf(interestRate));
			mBankAccount.setDueDay(Integer.valueOf(mField4.getText().toString()));
			mBankAccount.setCreditLimit(Double.valueOf(creditLimit));					
			mBankAccount.setBeginningBalance(0.0);
			mBankAccount.setPropertyType(0);
		} else if (mSelectedAccountTypeName.equals("Loans") || (mSelectedAccountTypeName.equals("Mortgage"))) {
			String interestRate = mField3.getText().toString().replace("% ", "");
			String beginingBalance = mField4.getText().toString().replace("$ ", "");
			mBankAccount.setInterestRate(Double.valueOf(interestRate));
			mBankAccount.setBeginningBalance(Double.valueOf(beginingBalance));					
			mBankAccount.setDueDay(0);
			mBankAccount.setCreditLimit(0.0);
			mBankAccount.setPropertyType(0);
		} else if (mSelectedAccountTypeName.equals("Investments") || (mSelectedAccountTypeName.equals("Line of Credit"))) {
			String interestRate = mField3.getText().toString().replace("% ", "");
			mBankAccount.setInterestRate(Double.valueOf(interestRate));	
			mBankAccount.setDueDay(0);
			mBankAccount.setCreditLimit(0.0);
			mBankAccount.setBeginningBalance(0.0);
			mBankAccount.setPropertyType(0);
			
		} else if (mSelectedAccountTypeName.equals("Property")) {	
			for (AccountType accountType : mFilteredPropertyTypes) {
				if (accountType.getAccountTypeName().equals(mField3.getText().toString())) {
					String typeID = accountType.getAccountTypeId().replace(".", "");
					
					mBankAccount.setPropertyType(Integer.valueOf(typeID));
					mBankAccount.setInterestRate(0.0);
					mBankAccount.setDueDay(0);
					mBankAccount.setCreditLimit(0.0);
					mBankAccount.setBeginningBalance(0.0);
				}
			}
		}
	}
}