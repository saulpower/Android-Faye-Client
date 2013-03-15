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
import android.widget.TextView;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Institution;
import com.moneydesktop.finance.database.InstitutionDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.adapter.AddNewInstitutionAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.LabelEditText;

import java.util.List;

public class AccountInstitutionListHandsetFragment extends BaseFragment{

	private static AccountInstitutionListHandsetFragment mCurrentFragment;

	private TextView mSearchIcon;
	private LabelEditText mSearchField;
	private ListView mInstitutionList;
	private AddNewInstitutionAdapter mAdapter;
	private AccountOptionsCredentialsHandsetFragment mConnectAccountFragment;
	
	private QueryProperty mWherePopularity = new QueryProperty(InstitutionDao.TABLENAME, InstitutionDao.Properties.Popularity, "!= ?");
	
	@Override
	public FragmentType getType() {
		return null;
	}

	@Override
	public String getFragmentTitle() {
		return null;
	}

    @Override
    public void isShowing(boolean fromBackstack) {

        if (mActivity != null) {
            mActivity.updateNavBar(getFragmentTitle(), true);
        }
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

	public static AccountInstitutionListHandsetFragment newInstance() {
		
		AccountInstitutionListHandsetFragment frag = new AccountInstitutionListHandsetFragment();
		mCurrentFragment = frag;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_add_account_select_institution, null);
		
		mSearchIcon = (TextView)mRoot.findViewById(R.id.handset_search_institution_img);
		mSearchField = (LabelEditText)mRoot.findViewById(R.id.handset_search_institution_box);
		mInstitutionList = (ListView)mRoot.findViewById(R.id.handset_search_institution_list);
		
		setupFonts();
		setupView();
		
		return mRoot;
	}

	private void setupFonts() {
		Fonts.applyGlyphFont(mSearchIcon, 20);
		Fonts.applySecondaryItalicFont(mSearchField, 18);
	}
	
	private void setupView() {
		final InstitutionDao dao = ApplicationContext.getDaoSession().getInstitutionDao();
		
		PowerQuery powerQuery = new PowerQuery(dao);
		powerQuery.where(mWherePopularity, "0");
		List<Institution> institutions = dao.queryRaw(powerQuery.toString(), powerQuery.getSelectionArgs());
        
		mAdapter = new AddNewInstitutionAdapter(mActivity, R.layout.tablet_add_bank_institution_list_item, institutions, mSearchField, mInstitutionList);
		mInstitutionList.setAdapter(mAdapter);
		mAdapter.initializeData();
		
		mInstitutionList.setOnItemClickListener(new OnItemClickListener() 
		{
		    @Override
		    public void onItemClick(AdapterView<?> a, View v,int position, long id) 
		    {
		    	loadConnectScreenFragment(a, position);
		    }
		});
	}
	
	private AccountOptionsCredentialsHandsetFragment getConnectScreenFragment(Institution selectedInstitution) {

		mConnectAccountFragment = AccountOptionsCredentialsHandsetFragment.newInstance(selectedInstitution);
		
		return mConnectAccountFragment;
	}

	private void loadConnectScreenFragment(AdapterView<?> a, int position) {
		Institution selectedInstitution = (Institution)a.getItemAtPosition(position);
		
		AccountOptionsCredentialsHandsetFragment frag = getConnectScreenFragment(selectedInstitution);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(mCurrentFragment.getId(), frag);
		ft.addToBackStack(null);
		ft.commit();
	}
	
}
