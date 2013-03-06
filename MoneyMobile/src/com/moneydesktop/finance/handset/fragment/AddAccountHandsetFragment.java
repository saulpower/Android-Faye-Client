package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.fragment.BaseFragment;

public class AddAccountHandsetFragment extends BaseFragment{

	private static AddAccountHandsetFragment mCurrentFragment;
	private LinearLayout mLinkedContainer, mManualContainer;
	private AccountInstitutionListHandsetFragment mInstitutionFragment;
	private AccountTypesListHandsetFragment mAccountTypesFragment;
	
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

	public static AddAccountHandsetFragment newInstance() {
		
		AddAccountHandsetFragment frag = new AddAccountHandsetFragment();
		mCurrentFragment = frag;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_add_account, null);
		
		mLinkedContainer = (LinearLayout)mRoot.findViewById(R.id.handset_add_account_linked_container);
		mManualContainer = (LinearLayout)mRoot.findViewById(R.id.handset_add_account_manual_container);	

		setupView();
		
		return mRoot;
	}

	private void setupView() {
		
		mLinkedContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showInstitutionListFragment();	
			}
		});
		
		mManualContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showAccountTypesFragment();	
			}
		});
	}
	
	private AccountInstitutionListHandsetFragment getLinkedFragment() {

		if (mInstitutionFragment == null) {
			mInstitutionFragment = AccountInstitutionListHandsetFragment.newInstance();
		}
		
		return mInstitutionFragment;
	}
	
	private AccountTypesListHandsetFragment getManualFragment() {

		if (mAccountTypesFragment == null) {
			mAccountTypesFragment = AccountTypesListHandsetFragment.newInstance();
		}
		
		return mAccountTypesFragment;
	}

	private void showAccountTypesFragment() {
		AccountTypesListHandsetFragment frag = getManualFragment();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(mCurrentFragment.getId(), frag);
		ft.addToBackStack(null);
		ft.commit();
	}


	private void showInstitutionListFragment() {
		AccountInstitutionListHandsetFragment frag = getLinkedFragment();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(mCurrentFragment.getId(), frag);
		ft.addToBackStack(null);
		ft.commit();
	}
}