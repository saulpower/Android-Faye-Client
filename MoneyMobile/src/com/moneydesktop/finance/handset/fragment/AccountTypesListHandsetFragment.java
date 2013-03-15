package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.shared.adapter.SelectAccountTypesAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class AccountTypesListHandsetFragment extends BaseFragment{

	private static AccountTypesListHandsetFragment mCurrentFragment;
	private ListView mAccountTypesList;
	private AccountType mSelectedAccountType;
	private AddAccountPropertyTypesHandsetFragment mPropertyTypesFragment;
	private AccountTypesManualSaveHandsetFragment mSaveManualBankFragment;
	
	private QueryProperty mAccountTypeNotWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
	private QueryProperty mAccountTypeAnd = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.ParentAccountTypeId, "= ?");
	private QueryProperty mOrderBy = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);
	
	
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
    public void isShowing(boolean fromBackstack) {

        if (mActivity != null) {
            mActivity.updateNavBar(getFragmentTitle(), true);
        }
    }
    
	@Override
	public boolean onBackPressed() {
		return false;
	}

	public static AccountTypesListHandsetFragment newInstance() {
		
		AccountTypesListHandsetFragment frag = new AccountTypesListHandsetFragment();
		mCurrentFragment = frag;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_account_types_list_view, null);
		
		mAccountTypesList = (ListView)mRoot.findViewById(R.id.handset_account_types_list);

		setupView();
		
		return mRoot;
	}

	private void setupView() {
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
		
		mAccountTypesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	
				showPropertyTypesOrSaveManualBank(position);
			}
		});	
	}
		
	private AddAccountPropertyTypesHandsetFragment getPropertyTypesFragment(AccountType selectedAccountType) {

		if (mPropertyTypesFragment == null) {
			mPropertyTypesFragment = AddAccountPropertyTypesHandsetFragment.newInstance(selectedAccountType);
		}
		
		return mPropertyTypesFragment;
	}
	
	private AccountTypesManualSaveHandsetFragment getSaveManualBankFragment(AccountType accountType) {
		mSaveManualBankFragment = AccountTypesManualSaveHandsetFragment.newInstance(accountType);
		return mSaveManualBankFragment;
	}

	private void showPropertyTypesOrSaveManualBank(int position) {
		mSelectedAccountType = ((AccountType)mAccountTypesList.getItemAtPosition(position));
		
		if (mSelectedAccountType.getAccountTypeName().toLowerCase().equals("property")) {
			
			AddAccountPropertyTypesHandsetFragment frag = getPropertyTypesFragment(mSelectedAccountType);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
			ft.replace(mCurrentFragment.getId(), frag);
			ft.addToBackStack(null);
			ft.commit();
		} else {
			AccountTypesManualSaveHandsetFragment frag = getSaveManualBankFragment(mSelectedAccountType);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
			ft.replace(mCurrentFragment.getId(), frag);
			ft.addToBackStack(null);
			ft.commit();
		}
	}
	
}