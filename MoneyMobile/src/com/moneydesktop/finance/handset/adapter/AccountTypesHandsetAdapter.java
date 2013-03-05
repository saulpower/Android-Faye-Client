package com.moneydesktop.finance.handset.adapter;

import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.handset.fragment.AccountBankDetailsHandsetFragment;
import com.moneydesktop.finance.shared.activity.BaseActivity;
import com.moneydesktop.finance.shared.adapter.UltimateAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.BarGraphView;
import com.moneydesktop.finance.views.BarView;
import com.moneydesktop.finance.views.UltimateListView;


public class AccountTypesHandsetAdapter extends UltimateAdapter implements OnGroupExpandListener, OnGroupCollapseListener{

    private List<AccountType> mAccountTypesFiltered;
    private Context mContext;
    private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
    private UltimateListView mAccountListView;
    
    public AccountTypesHandsetAdapter(Context context, List<AccountType> accountTypesFiltered, UltimateListView accountsListView) {
    	
    	mAccountTypesFiltered = accountTypesFiltered;   
        mContext = context;
        mAccountListView = accountsListView;
        
        mAccountListView.setOnGroupExpandListener(this);
        mAccountListView.setOnGroupCollapseListener(this); 
    }
   

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mAccountTypesFiltered.get(groupPosition).getBankAccounts().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
    	int response;
    	if (mAccountTypesFiltered.size() == 0 || mAccountTypesFiltered == null) {
    		response = 0;
    	} else {
    		response = mAccountTypesFiltered.get(groupPosition).getBankAccounts().size();    		
    	}
    	return response;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mAccountTypesFiltered.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
    	int response;
    	if (mAccountTypesFiltered != null) {
    		response = mAccountTypesFiltered.size();    		
    	} else {
    		response = 0;
    	}
    	return response;
        
    }

	@Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getSectionView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        
        View cell = convertView;
    	cell = ((BaseActivity)mContext).getLayoutInflater().inflate(R.layout.handset_account_type_details_header, parent, false);
        if (mAccountTypesFiltered != null) {
	        if (mAccountTypesFiltered.size() > 0) {
	        	
		        
		        TextView accountTypeName = (TextView)cell.findViewById(R.id.handset_account_type_name);
		        TextView accountTypeSum = (TextView)cell.findViewById(R.id.handset_account_type_sum);
		        
		        accountTypeName.setText(mAccountTypesFiltered.get(groupPosition).getAccountTypeName()); //get the account name (Checking, savings, etc)
		        double accountTypeValue = 0;
		        
		        for (BankAccount bankAccount : mAccountTypesFiltered.get(groupPosition).getBankAccounts()) {
		            accountTypeValue = accountTypeValue + bankAccount.getBalance();
		        }
		        
		        String formatedSum = NumberFormat.getCurrencyInstance().format(accountTypeValue);
		        
		        accountTypeSum.setText(formatedSum);
	        }
        }
        
        
        return cell;
        
    }
    
    @Override
    public View getItemView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View cell = convertView;
        
        cell = ((BaseActivity)mContext).getLayoutInflater().inflate(R.layout.account_type_child_handset, parent, false);
        
        List<BankAccount> bankAccounts = mAccountTypesFiltered.get(groupPosition).getBankAccounts();
        BankAccount bankAccount = bankAccounts.get(childPosition);
        
        if (!bankAccount.isDeleted()) {
	        LinearLayout bankAccountContainer = (LinearLayout) cell.findViewById(R.id.account_type_bank_container_handset);
	        
	     //   LinearLayout barLineView = (LinearLayout) cell.findViewById(R.id.account_types_handset_barview_container);
	               
	        final View view = createChildView();
	        
	        ImageView bankLogo = (ImageView)view.findViewById(R.id.handset_account_types_bank_icon);
	        BankLogoManager.getBankImage(bankLogo, bankAccount.getInstitutionId());
	        TextView accountName = (TextView)view.findViewById(R.id.handset_sub_account_type_name);
	        TextView accountSum = (TextView)view.findViewById(R.id.handset_sub_account_type_sum);
	        
	        Fonts.applyPrimarySemiBoldFont(accountName, 10);
	        Fonts.applyPrimarySemiBoldFont(accountSum, 10);
	        
	        accountName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
	        accountName.setText(bankAccount.getAccountName() == null ? "" : bankAccount.getAccountName());
	        accountSum.setText(bankAccount.getBalance() == null ? "" : mFormatter.format(bankAccount.getBalance()));
	     
	        bankAccountContainer.addView(view);
        }

                
        return cell;
    }
    
    @Override
    public void configureHeader(View header, int section) {
        TextView sectionHeader = (TextView)header.findViewById(R.id.handset_account_type_name);
        TextView sectionSum = (TextView)header.findViewById(R.id.handset_account_type_sum);
        
        AccountType accountType = (AccountType)getGroup(section);
        
        sectionHeader.setText(accountType.getAccountTypeName());
        
        double accountTypeValue = 0;
        for (BankAccount bankAccount : accountType.getBankAccounts()) {
            accountTypeValue = accountTypeValue + bankAccount.getBalance();
        }
        
        String formatedSum = NumberFormat.getCurrencyInstance().format(accountTypeValue);
        
        sectionSum.setText(formatedSum);
    }
    
    
    private View createChildView () {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.handset_account_type_details_footer, null);
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        mAccountListView.expandGroup(groupPosition);   
    }

	@Override
	protected void loadSection(int section) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isSectionLoadable(int section) {
		// TODO Auto-generated method stub
		return false;
	}

}