package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
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
import com.moneydesktop.finance.shared.adapter.SelectPropertyTypesAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class AddAccountPropertyTypesHandsetFragment extends BaseFragment{

	private ListView mAccountTypesList;

    private AccountType mSelectedAccountType;
	
	private QueryProperty mAccountTypeNotWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
	private QueryProperty mAccountTypeAnd = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.ParentAccountTypeId, "= ?");
	private QueryProperty mOrderBy = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);

    public void setSelectedAccountType(AccountType mSelectedAccountType) {
        this.mSelectedAccountType = mSelectedAccountType;
    }
	
	@Override
	public FragmentType getType() {
		return null;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.add_account_type_of_property).toUpperCase();
	}
    
	@Override
	public boolean onBackPressed() {
		return false;
	}

	public static AddAccountPropertyTypesHandsetFragment newInstance(AccountType accountType) {
		
		AddAccountPropertyTypesHandsetFragment frag = new AddAccountPropertyTypesHandsetFragment();
		frag.setSelectedAccountType(accountType);
		
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
		.and().where(mAccountTypeAnd, mSelectedAccountType.getId().toString())
		.orderBy(mOrderBy, false);
		
		allAccountTypes = accountTypeDao.queryRaw(query.toString(), query.getSelectionArgs());
		
		mAccountTypesList.setAdapter(new SelectPropertyTypesAdapter(mActivity, R.layout.handset_select_account_property_types_item, allAccountTypes));
		
		mAccountTypesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				showManualAccountSaveFragment(position);
			}
		});
		
		
	}

	private void showManualAccountSaveFragment(int position) {
		mSelectedAccountType = ((AccountType)mAccountTypesList.getItemAtPosition(position));
		
		AccountTypesManualSaveHandsetFragment frag = AccountTypesManualSaveHandsetFragment.newInstance(mSelectedAccountType);
        mActivity.pushFragment(getId(), frag);
	}
	
}