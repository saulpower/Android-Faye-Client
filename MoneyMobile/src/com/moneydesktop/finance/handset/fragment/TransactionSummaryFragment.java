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

	public static TransactionSummaryFragment newInstance(int position) {
		
		TransactionSummaryFragment frag = new TransactionSummaryFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	Button button;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.handset_transaction_summary_view, null);
		setupView();
		
		return root;
	}
	
	private void setupView() {

		button = (Button) root.findViewById(R.id.button);		
		button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				activity.showFragment(getPosition());
			}
		});
	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
