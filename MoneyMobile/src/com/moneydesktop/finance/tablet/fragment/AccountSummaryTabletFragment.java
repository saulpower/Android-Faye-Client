package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.views.NavBarButtons;

import java.util.ArrayList;

public class AccountSummaryTabletFragment extends BaseFragment {

	Button mLaunchNav;
	
	public static AccountSummaryTabletFragment newInstance(FragmentType type) {
		
		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();
		frag.setType(type);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_account_summary, null);
		mLaunchNav = (Button)mRoot.findViewById(R.id.view_nav_button);
		
		setupTitleBar();
		
		return mRoot;
	}
    
    private void setupTitleBar() {
        
        String[] icons = getResources().getStringArray(R.array.account_summary_title_bar_icons);
        
        ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
        
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "help", Toast.LENGTH_LONG).show();
            }
        });
        
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "email", Toast.LENGTH_LONG).show();
            }
        });
       
        onClickListeners.add(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "print", Toast.LENGTH_LONG).show();
            }
        });
        
        new NavBarButtons(mActivity, icons, onClickListeners);
    }


	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_accounts);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }


}
