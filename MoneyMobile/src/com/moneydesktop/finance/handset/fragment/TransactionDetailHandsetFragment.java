package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.shared.TransactionDetailBaseFragment;

public class TransactionDetailHandsetFragment extends TransactionDetailBaseFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	public static TransactionDetailHandsetFragment newInstance(long guid) {
		
		TransactionDetailHandsetFragment frag = new TransactionDetailHandsetFragment();
		
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_GUID, guid);
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        this.mActivity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_transaction_detail_view, null);
        initializeView();
		
		getTransaction();
		loadTransaction();
		
		return mRoot;
	}
}
