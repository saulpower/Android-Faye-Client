package com.moneydesktop.finance.tablet.activity;


import java.util.ArrayList;
import java.util.List;

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
import android.widget.TextView;
import android.widget.Toast;

public class AccountSummaryTabletActivity extends BaseActivity {

    private ExpandableListView mExpandableListView;

    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_types);
        
        UiUtils.setupTitleBar(this, getResources().getString(R.string.account_types_title), null, true, false, R.drawable.tablet_button_plus, R.drawable.tablet_button_plus, R.drawable.tablet_button_plus, 0);

        mExpandableListView = (ExpandableListView) findViewById(R.id.accounts_expandable_list_view);
        final LinearLayout panelLayoutHolder = (LinearLayout) findViewById(R.id.panel_layout_holder);
        mExpandableListView.setGroupIndicator(null);
        
        List<AccountType> accountTypes = ApplicationContext.getDaoSession().getAccountTypeDao().loadAll();
        List<AccountType> accountTypesFiltered = new ArrayList<AccountType>();
        
        
        for (AccountType type : accountTypes) {  //This could possibly be optimized by throwing a "where" in the query builder
        	if (!type.getBankAccounts().isEmpty()) {
        		accountTypesFiltered.add(type);
        	}
        }
        
        if (!accountTypesFiltered.isEmpty()) {
        	mExpandableListView.setAdapter(new AccountTypesAdapter(accountTypesFiltered, this, mExpandableListView));
        } else {
        	Toast.makeText(this, "No Accounts types that have bank accounts...show empty state", Toast.LENGTH_SHORT).show();
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
        layoutParams.width = UiUtils.getMinimumPanalWidth(AccountSummaryTabletActivity.this);
        panelLayoutHolder.setLayoutParams(layoutParams);

        setupDrawer(layoutParams, this);
        initializeDrawer(panelLayoutHolder);
    }

	private void initializeDrawer (final LinearLayout panelLayoutHolder) {
    	
    	List<BankAccount> bankAccounts = ApplicationContext.getDaoSession().getBankAccountDao().loadAll();
        //For every bank account that is attached, add it to the Drawer
        for (BankAccount bankAccount : bankAccounts) {
            //create the view to be attached
            //add it to the Drawer
            panelLayoutHolder.addView(populateDrawerView(bankAccount));
        }
    }

    private View populateDrawerView (final BankAccount bankAccount) {
        LayoutInflater layoutInflater = getLayoutInflater();
        final View bankAccountView = layoutInflater.inflate(R.layout.bank_account, null);

        //ImageView bankImage = (ImageView) bankAccountView.findViewById(R.id.bank_account_image);

        //set the bank image here...
        //bankImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.tablet_accounts_bankbook));

        return bankAccountView;
    }


    public static SlidingDrawerRightSide setupDrawer (final ViewGroup.LayoutParams layoutParams, Activity activity) {
        final SlidingDrawerRightSide drawer = (SlidingDrawerRightSide) activity.findViewById(R.id.account_slider);
        final ViewGroup.LayoutParams drawerLayoutParams = drawer.getLayoutParams();

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        int width = bitmap.getWidth();
        bitmap.recycle();

        drawerLayoutParams.width = layoutParams.width + width;
        drawer.setLayoutParams(drawerLayoutParams);

        return drawer;
    }

	@Override
	public String getActivityTitle() {
		return null;
	}
}