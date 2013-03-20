package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.fragment.BaseFragment;

public class AddAccountHandsetFragment extends BaseFragment{

	private static AddAccountHandsetFragment mCurrentFragment;
	private LinearLayout mLinkedContainer, mManualContainer;
	
	@Override
	public FragmentType getType() {
		return null;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.add_account_label_handset).toUpperCase();
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

        mManualContainer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showAccountTypesFragment();
            }
        });

        if (User.getCurrentUser() == null || !User.getCurrentUser().getCanSync()) {
            mLinkedContainer.setVisibility(View.GONE);
            return;
        }

		mLinkedContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showInstitutionListFragment();	
			}
		});
	}

	private void showAccountTypesFragment() {
		AccountTypesListHandsetFragment frag = AccountTypesListHandsetFragment.newInstance();
        mActivity.pushFragment(mCurrentFragment.getId(), frag);
	}


	private void showInstitutionListFragment() {
		AccountInstitutionListHandsetFragment frag = AccountInstitutionListHandsetFragment.newInstance();
        mActivity.pushFragment(mCurrentFragment.getId(), frag);
	}
}