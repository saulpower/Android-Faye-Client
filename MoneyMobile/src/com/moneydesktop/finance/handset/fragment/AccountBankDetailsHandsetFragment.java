
package com.moneydesktop.finance.handset.fragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.handset.adapter.AccountExclusionsAdapter;
import com.moneydesktop.finance.model.EventMessage.MenuEvent;
import com.moneydesktop.finance.shared.adapter.SelectAccountTypesAdapter;
import com.moneydesktop.finance.shared.adapter.SelectPropertyTypesAdapter;
import com.moneydesktop.finance.shared.fragment.FixBankFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.LabelEditText;
import com.moneydesktop.finance.views.LineView;

import de.greenrobot.event.EventBus;

public class AccountBankDetailsHandsetFragment extends FixBankFragment{
	
	private static BankAccount mBankAccount;
	private ImageView mLogo;
	private LabelEditText mBalance, mAccountType, mExclusions, mAccountName;
	private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();	
	private AccountTypeDao mAccountTypeDAO;
	private List<AccountType> mFilteredAccountTypes;
	private List<AccountType> mFilteredPropertyTypes;	
	private LabelEditText optionalField1;
	private LabelEditText optionalField2;
	private LabelEditText optionalField3;
	private AccountType mSelectedAccountType;
	private String mBankAccountTypeName;
	private LineView mLine4, mLine5, mLine6;
	private TextView mSaveExclusions;
	private QueryProperty mAccountTypeNotWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
	private QueryProperty mAccountTypeAnd = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.ParentAccountTypeId, "= ?");
	private QueryProperty mOrderBy = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);
	
	private ListView mAccountTypesList;
	private ListView mAccountPropertyTypesList;
	private ListView mAccountExclusions;
	
	private ViewFlipper mFlipper; 
		
	@Override
	public FragmentType getType() {
		return null;
	}
	
	@Override
	public String getFragmentTitle() {
		return getString(R.string.label_account_detail);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
    }
    
	@Override
	public boolean onBackPressed() {
		return false;
	}
	
	public static AccountBankDetailsHandsetFragment newInstance(BankAccount bankAccount) {
		
		AccountBankDetailsHandsetFragment frag = new AccountBankDetailsHandsetFragment();
		mBankAccount = bankAccount;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_account_details_view, null);
		setupMenuItems();
		mFlipper = (ViewFlipper)mRoot.findViewById(R.id.handset_account_details_flipper);
		
		mLogo = (ImageView)mRoot.findViewById(R.id.handset_account_details_bank_logo);
		mAccountName = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_account_name);
		mBalance = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_balance);
		mAccountType = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_type);
		optionalField1 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_optional1);
		optionalField2 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_optional2);
		optionalField3 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_optional3);
		mExclusions = (LabelEditText)mRoot.findViewById(R.id.handset_bank_details_exclusions);
		mAccountTypesList = (ListView)mRoot.findViewById(R.id.handset_account_types_list);
		mAccountPropertyTypesList = (ListView)mRoot.findViewById(R.id.handset_account_property_types_list);
		mSaveExclusions = (TextView)mRoot.findViewById(R.id.handset_save_exclusions);
		mAccountExclusions = (ListView)mRoot.findViewById(R.id.handset_exclusions_list);

		mLine4 = (LineView)mRoot.findViewById(R.id.view_breaker4);
		mLine5 = (LineView)mRoot.findViewById(R.id.view_breaker5);
		mLine6 = (LineView)mRoot.findViewById(R.id.view_breaker6);
	
		Fonts.applyPrimaryBoldFont(mAccountName, 14);
		Fonts.applyPrimaryBoldFont(mBalance, 24);
		Fonts.applyPrimaryBoldFont(mAccountType, 14);
		Fonts.applyPrimaryBoldFont(optionalField1, 14);
		Fonts.applyPrimaryBoldFont(optionalField2, 14);
		Fonts.applyPrimaryBoldFont(optionalField3, 14);
		Fonts.applyPrimaryBoldFont(mExclusions, 14);			
		Fonts.applyPrimaryBoldFont(mSaveExclusions, 14);
		
		mBankAccountTypeName = mBankAccount.getAccountType().getAccountTypeName();
		
		getAccountTypesLists();
		setupView();
		setupSecondaryOptions();
		setupOnClickListeners();
		
		return mRoot;
	}

	private void setupView() {
		
        String logoId = mBankAccount.getBank().getBankId();
        
        if (mBankAccount.getBank().getInstitution() != null) {
            logoId = mBankAccount.getBank().getInstitution().getInstitutionId();
        }
		
		BankLogoManager.getBankImage(mLogo, logoId);		
		
		mAccountName.setText(mBankAccount.getBank().getBankName());
		mAccountName.setLabelText(mBankAccount.getAccountName());
		mBalance.setText(mFormatter.format(mBankAccount.getBalance()));
		mAccountType.setText(mBankAccount.getAccountType().getAccountTypeName());
		
		mBalance.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Animation shakeAnimation = AnimationFactory.createShakeAnimation(mActivity);
				v.startAnimation(shakeAnimation);
				return true;
			}
		});
		
		if (mBankAccount.getExclusionFlags() > 0) {
			mExclusions.setText(getString(R.string.label_exclusions_description));
		}
		
		setupExclusionScreen();
		
		preloadAccountTypesList();
	}

	private void setupExclusionScreen() {
		
		List<AccountExclusionFlags> exclusionFlags = BankAccount.getExclusionsForAccount(mBankAccount);
		
		mAccountExclusions.setAdapter(new AccountExclusionsAdapter(mActivity, R.layout.handset_account_details_exclusions_item, mActivity.getResources().getStringArray(R.array.account_exclusions_handset), exclusionFlags, mBankAccount));
		
		mAccountExclusions.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
				CheckBox checkbox = (CheckBox)v.findViewById(R.id.handset_exclude_checkbox);
				TextView title = (TextView)v.findViewById(R.id.handset_exclude_txt);
				updateCheckedStatus(checkbox, title);
			}
		});
		
		mSaveExclusions.setText(getString(R.string.save));
		
		mSaveExclusions.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBankAccount.updateSingle();
				Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_left);
				Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_right);
				mFlipper.setInAnimation(in);
				mFlipper.setOutAnimation(out);
				mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view1)));
			}
		});		
	}
		
	private void updateCheckedStatus(CheckBox checkbox, TextView txtTitle) {
		
		int flagCount = mBankAccount.getExclusionFlags();
		if (checkbox.isChecked()) {
			checkbox.setChecked(false);
			
			if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_transaction_lists))) {			
				flagCount = flagCount - AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST.index();		
				
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_reports_charts))) {
				flagCount = flagCount - AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS.index();
				
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_account_listings))) {
				flagCount = flagCount - AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST.index();
				
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_budgets))) {
				flagCount = flagCount - AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS.index();
				
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_transfers_in))) {
				flagCount = flagCount - AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME.index();
				
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_transfers_out))) {
				flagCount = flagCount - AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES.index();
			}

		} else {
			checkbox.setChecked(true);
			
			if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_transaction_lists))) {			
				flagCount = flagCount + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST.index();			
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_reports_charts))) {
				flagCount = flagCount + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS.index();
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_account_listings))) {
				flagCount = flagCount + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST.index();
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_budgets))) {
				flagCount = flagCount + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS.index();
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_transfers_in))) {
				flagCount = flagCount + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME.index();
			} else if (txtTitle.getText().equals(getString(R.string.handset_account_exclusion_from_transfers_out))) {
				flagCount = flagCount + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES.index();
			}
		}
		
		mBankAccount.setExclusionFlags(flagCount);
	}
	
	private void preloadAccountTypesList() {
		//get List of account types
		AccountTypeDao accountTypeDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		
	    List<AccountType> allAccountTypes = new ArrayList<AccountType>();
	    		
	    //Get all account types in alphabetical order. Removing the "Unknown" Type as well as property types. 
		PowerQuery query = new PowerQuery(accountTypeDao);	
		query.where(mAccountTypeNotWhere, "Unknown")
		.and().where(mAccountTypeAnd, "0")
		.orderBy(mOrderBy, false);
		
		allAccountTypes = accountTypeDao.queryRaw(query.toString(), query.getSelectionArgs());
		
		mAccountTypesList.setAdapter(new SelectAccountTypesAdapter(mActivity, R.layout.handset_select_account_types_item, allAccountTypes));
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
		
		if (mBankAccountTypeName.equals(Constant.CREDIT_CARD)) {
			bankDetailsForCreditCard();
			
		} else if (mBankAccountTypeName.equals(Constant.LOANS) || (mBankAccountTypeName.equals(Constant.MORTGAGE))) {
			bankDetailsForLoanAndMortgage();
			
		} else if (mBankAccountTypeName.equals(Constant.INVESTMENTS) || (mBankAccountTypeName.equals(Constant.LINE_OF_CREDIT))) {
			bankDetailsForInvestmentsAndLOC();
			
		} else if (mBankAccountTypeName.equals(Constant.PROPERTY)) {
			bankDetailsForProperty();
			
		} else {
			bankDetailsHideAllOptional();
		}		
	}

	private void bankDetailsHideAllOptional() {
		optionalField1.setVisibility(View.GONE);
		optionalField2.setVisibility(View.GONE);
		optionalField3.setVisibility(View.GONE);
		
		mLine4.setVisibility(View.GONE);
		mLine5.setVisibility(View.GONE);
		mLine6.setVisibility(View.GONE);
	}

	private void bankDetailsForProperty() {
		optionalField1.setFocusable(false);
		 if (mBankAccount.getSubAccountType() != null) {
			 optionalField1.setText(mBankAccount.getSubAccountType().getAccountTypeName());
			 optionalField1.setLabelText(getString(R.string.label_account_property_type));
		} else if (mSelectedAccountType != null) {
			optionalField1.setText(mSelectedAccountType.getAccountTypeName());
			optionalField1.setLabelText(getString(R.string.label_account_property_type));
		} else {
			optionalField1.setLabelText(getString(R.string.label_account_property_type));
			optionalField1.setText("");
		}
		 		
		optionalField1.setVisibility(View.VISIBLE);
		optionalField2.setVisibility(View.GONE);
		optionalField3.setVisibility(View.GONE);
		
		mLine4.setVisibility(View.VISIBLE);
		mLine5.setVisibility(View.GONE);
		mLine6.setVisibility(View.GONE);
	}

	private void bankDetailsForInvestmentsAndLOC() {
		optionalField1.setVisibility(View.VISIBLE);
		optionalField2.setVisibility(View.GONE);
		optionalField3.setVisibility(View.GONE);
		
		mLine4.setVisibility(View.VISIBLE);
		mLine5.setVisibility(View.GONE);
		mLine6.setVisibility(View.GONE);
		
		//This is to disable the onclicklistener that was created when user selects account type "Property"
		optionalField1.setOnClickListener(null);
		
		if (mBankAccount.getInterestRate() != null) {
			optionalField1.setText("% " + mBankAccount.getInterestRate().toString());
		} else {
			optionalField1.setText("% 0");
		}
		optionalField1.setLabelText(getString(R.string.label_account_interest_rate));
	}

	private void bankDetailsForLoanAndMortgage() {
		optionalField1.setVisibility(View.VISIBLE);
		optionalField2.setVisibility(View.VISIBLE);
		optionalField3.setVisibility(View.GONE);
		
		mLine4.setVisibility(View.VISIBLE);
		mLine5.setVisibility(View.VISIBLE);
		mLine6.setVisibility(View.GONE);
		
		//This is to disable the onclicklistener that was created when user selects account type "Property"
		optionalField1.setOnClickListener(null);

		if (mBankAccount.getInterestRate() != null) {
			optionalField1.setText("% " + mBankAccount.getInterestRate().toString());
		} else {
			optionalField1.setText("% 0");
		}
		optionalField1.setLabelText(getString(R.string.label_account_interest_rate));
		
		if (mBankAccount.getBeginningBalance() != null) {
			optionalField2.setText("$ " + mBankAccount.getBeginningBalance().toString());
		} else {
			optionalField2.setText("$ 0");
		}
		optionalField2.setLabelText(getString(R.string.label_account_original_balance));
	}

	private void bankDetailsForCreditCard() {
		optionalField1.setVisibility(View.VISIBLE);
		optionalField2.setVisibility(View.VISIBLE);
		optionalField3.setVisibility(View.VISIBLE);
		mLine4.setVisibility(View.VISIBLE);
		mLine5.setVisibility(View.VISIBLE);
		mLine6.setVisibility(View.VISIBLE);
		
		//This is to disable the onclicklistener that was created when user selects account type "Property"
		optionalField1.setOnClickListener(null);
		
		if (mBankAccount.getInterestRate() != null) {
			optionalField1.setText(mBankAccount.getInterestRate().toString() + " %");
		} else {
			optionalField1.setText("0 %");
		}
		optionalField1.setLabelText(getString(R.string.label_account_interest_rate));
		
		if (mBankAccount.getDueDay() != null) {
			optionalField2.setText(mBankAccount.getDueDay().toString());
		} else {
			optionalField2.setText("0");
		}
		optionalField2.setLabelText(getString(R.string.label_account_due_day));
		
		if (mBankAccount.getCreditLimit() != null) {
			optionalField3.setText("$ " + mBankAccount.getCreditLimit().toString());
		} else {
			optionalField3.setText("$ 0");
		}
		optionalField3.setLabelText(getString(R.string.label_account_credit_limit));
	}
	
	private void setupOnClickListeners() {
		
		mAccountType.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				UiUtils.hideKeyboard(mActivity, v);
				
				Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
				Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
				mFlipper.setInAnimation(in);
				mFlipper.setOutAnimation(out);
				mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view2)));
				
				mAccountTypesList.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
							accountTypeSelectionListener(position);
						}
				});
				return true;
			}
		});	
		
		mExclusions.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				UiUtils.hideKeyboard(mActivity, v);
				
				Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
				Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
				mFlipper.setInAnimation(in);
				mFlipper.setOutAnimation(out);
				
				mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view4)));
				
				return true;
			}
		});
	}
	
	private void loadPropertyTypesList() {
		//get List of account types
		AccountTypeDao accountTypeDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		
		List<AccountType> allAccountTypes = new ArrayList<AccountType>();
				
		//Get all account types in alphabetical order. Removing the "Unknown" Type as well as property types. 
		PowerQuery query = new PowerQuery(accountTypeDao);	
		query.where(mAccountTypeNotWhere, "Unknown")
		.and().where(mAccountTypeAnd, mSelectedAccountType.getId().toString())
		.orderBy(mOrderBy, false);
		
		allAccountTypes = accountTypeDao.queryRaw(query.toString(), query.getSelectionArgs());
		
		mAccountPropertyTypesList.setAdapter(new SelectPropertyTypesAdapter(mActivity, R.layout.handset_select_account_property_types_item, allAccountTypes));
		
		mAccountPropertyTypesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				mSelectedAccountType = ((AccountType)mAccountPropertyTypesList.getItemAtPosition(position));
				setupSecondaryOptions();
				
				mAccountType.setText(mBankAccountTypeName);
				
				Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_left);
				Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_right);
				mFlipper.setInAnimation(in);
				mFlipper.setOutAnimation(out);
				mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view1)));
				
			}
		});
		
	};
		
	private void setBankAccountValues() {
		//If mSelectedAccountType is null, that means a new account type wasn't selected. No need to save.
		if (mSelectedAccountType != null) {
			mBankAccount.setAccountType(mSelectedAccountType);
		}
		
		if (mBankAccountTypeName.equals(Constant.CREDIT_CARD)) {
			String interestRate = optionalField1.getText().toString().replace(" %", "");
			String creditLimit = optionalField3.getText().toString().replace("$ ", "");
			mBankAccount.setInterestRate(Double.valueOf(interestRate));
			mBankAccount.setDueDay(Integer.valueOf(optionalField2.getText().toString()));
			mBankAccount.setCreditLimit(Double.valueOf(creditLimit));					
			mBankAccount.setBeginningBalance(0.0);
			mBankAccount.setPropertyType(0);
		} else if (mBankAccountTypeName.equals(Constant.LOANS) || (mBankAccountTypeName.equals(Constant.MORTGAGE))) {
			String interestRate = optionalField1.getText().toString().replace(" %", "");
			String beginingBalance = optionalField2.getText().toString().replace("$ ", "");
			mBankAccount.setInterestRate(Double.valueOf(interestRate));
			mBankAccount.setBeginningBalance(Double.valueOf(beginingBalance));					
			mBankAccount.setDueDay(0);
			mBankAccount.setCreditLimit(0.0);
			mBankAccount.setPropertyType(0);
		} else if (mBankAccountTypeName.equals(Constant.INVESTMENTS) || (mBankAccountTypeName.equals(Constant.LINE_OF_CREDIT))) {
			String interestRate = optionalField1.getText().toString().replace(" %", "");
			mBankAccount.setInterestRate(Double.valueOf(interestRate));	
			mBankAccount.setDueDay(0);
			mBankAccount.setCreditLimit(0.0);
			mBankAccount.setBeginningBalance(0.0);
			mBankAccount.setPropertyType(0);
			
		} else if (mBankAccountTypeName.equals(Constant.PROPERTY)) {	
			for (AccountType accountType : mFilteredPropertyTypes) {
				if (accountType.getAccountTypeName().equals(optionalField1.getText().toString())) {
					
					String[] splitID = accountType.getAccountTypeId().split("\\.");
					Integer typeID = Integer.valueOf(splitID[1]);
					
					mBankAccount.setPropertyType(typeID);
					mBankAccount.setInterestRate(0.0);
					mBankAccount.setDueDay(0);
					mBankAccount.setCreditLimit(0.0);
					mBankAccount.setBeginningBalance(0.0);
				}
			}
		}
	}
	
    private void setupMenuItems() {

		List<Pair<Integer, List<int[]>>> data = new ArrayList<Pair<Integer, List<int[]>>>();

    	List<int[]> items = new ArrayList<int[]>();
    	items.add(new int[] {R.string.nav_icon_menu_left, R.string.label_account_details_view_transactions});
    	items.add(new int[] {R.string.nav_icon_trash, R.string.label_account_details_delete_account});
    	
    	data.add(new Pair<Integer, List<int[]>>(R.string.label_account_details_menu, items));
    	
    	mActivity.addMenuItems(data);
    	mActivity.setMenuFragment(FragmentType.ACCOUNT_SETTINGS);
    }
    
	public void onEvent(MenuEvent event) {
		if (event.getFragmentType().equals(FragmentType.ACCOUNT_SETTINGS)) {
		    switch (event.getChildPosition()) {
			    case 0:
			    	//View transactions
			        ((DashboardHandsetActivity)mActivity).getMenuDrawer().closeMenu();
			    	
			        TransactionsHandsetFragment frag = getViewTransactionFragment();
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
					ft.replace(R.id.accounts_fragment, frag);
					ft.addToBackStack(null);
					ft.commit();
			    	break;
			    case 1:
			    	//delete account
			    	((DashboardHandsetActivity)mActivity).getMenuDrawer().closeMenu();
			    	mBankAccount.softDeleteSingle();
			    	mActivity.popMenuView();
			    	mActivity.popBackStack();
			    	
			    	SyncEngine.sharedInstance().beginSync();
			    	break;
		    }
		}
	}
	
	private TransactionsHandsetFragment getViewTransactionFragment() {
		
		Intent intent = new Intent();
		intent.putExtra(Constant.EXTRA_ACCOUNT_ID, mBankAccount.getAccountId());
		
		return TransactionsHandsetFragment.newInstance(intent);
	}

	private void accountTypeSelectionListener(int position) {
		mSelectedAccountType = ((AccountType)mAccountTypesList.getItemAtPosition(position));
		
		mBankAccountTypeName = mSelectedAccountType.getAccountTypeName();

		if (!mBankAccountTypeName.toLowerCase().equals("property")) {
			setupSecondaryOptions();
			
			mAccountType.setText(mSelectedAccountType.getAccountTypeName());
			
			Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_left);
			Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_right);
			mFlipper.setInAnimation(in);
			mFlipper.setOutAnimation(out);
			mFlipper.showPrevious();
		} else {
			loadPropertyTypesList();
			
			Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
			Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
			mFlipper.setInAnimation(in);
			mFlipper.setOutAnimation(out);
			mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view3)));
		}
	}

}