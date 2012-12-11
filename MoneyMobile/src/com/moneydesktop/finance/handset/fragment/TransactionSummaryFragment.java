package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

public class TransactionSummaryFragment extends BaseFragment {

    private static TransactionSummaryFragment sFragment;
    
	public static TransactionSummaryFragment getInstance(int position) {
		
	    if (sFragment != null) {
	        return sFragment;
	    }
	    
	    sFragment = new TransactionSummaryFragment();
	    sFragment.setPosition(position);
        sFragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	private Button mButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_transaction_summary_view, null);
		setupView();
		
		return mRoot;
	}
	
	private void setupView() {

		mButton = (Button) mRoot.findViewById(R.id.button);		
		mButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				mActivity.showFragment(getPosition());
			}
		});
	}
    
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)  {
        super.onSaveInstanceState(outState);
    }
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
