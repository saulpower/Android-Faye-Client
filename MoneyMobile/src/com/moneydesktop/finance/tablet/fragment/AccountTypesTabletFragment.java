package com.moneydesktop.finance.tablet.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AccountTypesAdapter;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;

public class AccountTypesTabletFragment extends BaseTabletFragment {
    private ExpandableListView mExpandableListView;
    private static SlidingDrawerRightSide mRightDrawer;
    private View mFooter;
	
	public static AccountTypesTabletFragment newInstance(int position) {	
		AccountTypesTabletFragment frag = new AccountTypesTabletFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.activity_account_types, null);
		mFooter = inflater.inflate(R.layout.account_type_list_footer, null);
		mExpandableListView = (ExpandableListView)root.findViewById(R.id.accounts_expandable_list_view);
		mRightDrawer = (SlidingDrawerRightSide)root.findViewById(R.id.account_slider);
		setupView();
		
		return root;
	}
	
	private void setupView() {
		final LinearLayout panelLayoutHolder = (LinearLayout)root.findViewById(R.id.panel_layout_holder);
        mExpandableListView.setGroupIndicator(null);
        
        List<AccountType> accountTypes = ApplicationContext.getDaoSession().getAccountTypeDao().loadAll();
        List<AccountType> accountTypesFiltered = new ArrayList<AccountType>();
        
        for (AccountType type : accountTypes) {  //This could possibly be optimized by throwing a "where" in the query builder
        	if (!type.getBankAccounts().isEmpty()) {
        		accountTypesFiltered.add(type);
        	}
        }
        
        if (!accountTypesFiltered.isEmpty()) {        	
        	mExpandableListView.addFooterView(mFooter);
        	mExpandableListView.setAdapter(new AccountTypesAdapter(accountTypesFiltered, activity, mExpandableListView));
        } else {
        	Toast.makeText(activity, "No Accounts types that have bank accounts...show empty state", Toast.LENGTH_SHORT).show();
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
        layoutParams.width = UiUtils.getMinimumPanalWidth(activity);
        panelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams, activity);
        initializeDrawer(panelLayoutHolder);
	}
	
	
	private void initializeDrawer (LinearLayout panelLayoutHolder) {
		List<Bank> banksList = ApplicationContext.getDaoSession().getBankDao().loadAll();
    	
    	panelLayoutHolder.addView(getPanelHeader());

        //For every bank that is attached, add it to the Drawer
        for (Bank bank : banksList) {
            //create the view to be attached to Drawer
        	panelLayoutHolder.addView(populateDrawerView(bank));
        }
    }

    private View getPanelHeader() {
    	LayoutInflater layoutInflater = activity.getLayoutInflater();
    	final View headerView = layoutInflater.inflate(R.layout.tablet_panel_header, null); 
		return headerView;
	}

	private View populateDrawerView (final Bank bank) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View bankTypeAccountView = layoutInflater.inflate(R.layout.bank_account, null);
        ImageView bankImage = (ImageView)bankTypeAccountView.findViewById(R.id.bank_account_image);
        TextView bankName = (TextView)bankTypeAccountView.findViewById(R.id.account_bank_name);
        String logoid = bank.getLogoId();
        
       // bankImage.setBackgroundResource(R.drawable.bank1);
        bankName.setText(bank.getBankName());

        return bankTypeAccountView;
    }


    public static SlidingDrawerRightSide setupDrawer (final ViewGroup.LayoutParams layoutParams, Activity activity) {
        final ViewGroup.LayoutParams drawerLayoutParams = mRightDrawer.getLayoutParams();

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        int width = bitmap.getWidth();
        bitmap.recycle();

        drawerLayoutParams.width = layoutParams.width + width;
        drawerLayoutParams.height = UiUtils.getScreenHeight(activity) ;
        mRightDrawer.setLayoutParams(drawerLayoutParams);

        return mRightDrawer;
    }
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

}
