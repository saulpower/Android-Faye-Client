package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.tablet.adapter.AccountTypesAdapter;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;

import java.util.ArrayList;
import java.util.List;

public class AccountTypesTabletFragment extends BaseTabletFragment {
    private ExpandableListView mExpandableListView;
    private static SlidingDrawerRightSide sRightDrawer;
	
	public static AccountTypesTabletFragment newInstance(int position) {
		
		AccountTypesTabletFragment frag = new AccountTypesTabletFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.mActivity.onFragmentAttached();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.activity_account_types, null);
		mExpandableListView = (ExpandableListView)mRoot.findViewById(R.id.accounts_expandable_list_view);
		sRightDrawer = (SlidingDrawerRightSide)mRoot.findViewById(R.id.account_slider);
		setupView();
		
		return mRoot;
	}
	
	private void setupView() {
		final LinearLayout panelLayoutHolder = (LinearLayout)mRoot.findViewById(R.id.panel_layout_holder);
        mExpandableListView.setGroupIndicator(null);
        
        List<AccountType> accountTypes = ApplicationContext.getDaoSession().getAccountTypeDao().loadAll();
        List<AccountType> accountTypesFiltered = new ArrayList<AccountType>();
        
        
        for (AccountType type : accountTypes) {  //This could possibly be optimized by throwing a "where" in the query builder
        	if (!type.getBankAccounts().isEmpty()) {
        		accountTypesFiltered.add(type);
        	}
        }
        
        if (!accountTypesFiltered.isEmpty()) {
        	mExpandableListView.setAdapter(new AccountTypesAdapter(accountTypesFiltered, mActivity, mExpandableListView));
        } else {
        	Toast.makeText(mActivity, "No Accounts types that have bank accounts...show empty state", Toast.LENGTH_SHORT).show();
        }

        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
	            ((AccountTypesAdapter)mExpandableListView.getExpandableListAdapter()).notifyDataSetChanged();
	            return false;
			}
		});
        
        panelLayoutHolder.setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View view, MotionEvent event) {
				return true;
			}
		});

        
        final ViewGroup.LayoutParams layoutParams = panelLayoutHolder.getLayoutParams();
        layoutParams.width = UiUtils.getMinimumPanalWidth(mActivity);
        panelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams, mActivity);
        initializeDrawer(panelLayoutHolder);
	}
	
	
	private void initializeDrawer (LinearLayout panelLayoutHolder) {
		List<Bank> banksList = ApplicationContext.getDaoSession().getBankDao().loadAll();
    	
    	panelLayoutHolder.addView(getPanelHeader());
    	

    	
    	//TODO: change this so that it doesn't add ACCOUNT, but instead, BANKS
        //For every bank account that is attached, add it to the Drawer
        for (Bank banks : banksList) {
            //create the view to be attached
            //add it to the Drawer
        	panelLayoutHolder.addView(populateDrawerView(banks));
        }
    }

    private View getPanelHeader() {
    	LayoutInflater layoutInflater = mActivity.getLayoutInflater();
    	final View headerView = layoutInflater.inflate(R.layout.tablet_panel_header, null); 
		return headerView;
	}

	private View populateDrawerView (final Bank banks) {
        LayoutInflater layoutInflater = mActivity.getLayoutInflater();
        final View bankTypeAccountView = layoutInflater.inflate(R.layout.bank_account, null);

        //ImageView bankImage = (ImageView) bankAccountView.findViewById(R.id.bank_account_image);

        //set the bank image here...
        //bankImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bankbook));

        return bankTypeAccountView;
    }


    public static SlidingDrawerRightSide setupDrawer (final ViewGroup.LayoutParams layoutParams, Activity activity) {
        final ViewGroup.LayoutParams drawerLayoutParams = sRightDrawer.getLayoutParams();

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        int width = bitmap.getWidth();
        bitmap.recycle();

        drawerLayoutParams.width = layoutParams.width + width;
        drawerLayoutParams.height = UiUtils.getScreenHeight(activity) ;
        sRightDrawer.setLayoutParams(drawerLayoutParams);

        return sRightDrawer;
    }
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
