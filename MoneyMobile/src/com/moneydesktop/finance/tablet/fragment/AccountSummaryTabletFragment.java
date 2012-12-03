package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.views.CircleNavView;

public class AccountSummaryTabletFragment extends BaseTabletFragment{

	Button mLaunchNav;
	CircleNavView mCircleNav;
	
	
	public static AccountSummaryTabletFragment newInstance(int position) {
		
		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.tablet_account_summary, null);
		mLaunchNav = (Button)root.findViewById(R.id.view_nav_button);
		mCircleNav = (CircleNavView)activity.findViewById(R.id.circle_tablet_nav);
		
		
		setupView();
		
		return root;
	}
	
	private void setupView() {

      mLaunchNav.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {  
              mCircleNav.setVisibility(View.VISIBLE);
			}
		}); 
		
	}
	

	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
