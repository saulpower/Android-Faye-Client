package com.moneydesktop.finance.tablet.fragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.SlideFrom;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.adapter.AccountSettingsTypesAdapter;
import com.moneydesktop.finance.util.EmailUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.SettingButton;
import com.moneydesktop.finance.views.SlidingView;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class ShowHideDataTabletFragment extends BaseFragment {
	
	private static String mAccountId;
	
	private Button mSaveChanges;
	private static BankAccount mBankAccount;
	private TextView mAccountName, mBankName, mAccountSum, mBankRefreshStatus, mExcludeFromIncomeTxt, mExcludeFromExpensesTxt;
	private ImageView mBankLogo;
	private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
	private CheckBox mExcludeFromIncomeCheckbox, mExcludeFromExpensesCheckbox;
	
	@Override
 	public String getFragmentTitle() {
		return getString(R.string.show_hide_label);
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}
		
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        mActivity.onFragmentAttached(this);
	}

	public static ShowHideDataTabletFragment newInstance(Intent intent) {
		
		ShowHideDataTabletFragment fragment = new ShowHideDataTabletFragment();
        
		mAccountId = intent.getExtras().getString(Constant.KEY_BANK_ACCOUNT_ID);
			
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_show_hide_data, null);
  
        BankAccountDao bankAccountDAO = ApplicationContext.getDaoSession().getBankAccountDao();
        mBankAccount = bankAccountDAO.load(Long.valueOf(mAccountId.hashCode()));
   
        mAccountName = (TextView)mRoot.findViewById(R.id.tablet_account_linear_summary_account_name);
        mBankName = (TextView)mRoot.findViewById(R.id.tablet_account_linear_summary_bank_name);
        mAccountSum = (TextView)mRoot.findViewById(R.id.tablet_account_linear_summary_account_sum);
        mBankRefreshStatus = (TextView)mRoot.findViewById(R.id.tablet_account_linear_summary_refresh_status);
        mExcludeFromIncomeTxt = (TextView)mRoot.findViewById(R.id.exclude_transfers_from_income_txt);
        mExcludeFromExpensesTxt = (TextView)mRoot.findViewById(R.id.exclude_transfers_from_expenses_txt);
        mExcludeFromIncomeCheckbox = (CheckBox)mRoot.findViewById(R.id.exclude_transfers_from_income);
        mExcludeFromExpensesCheckbox = (CheckBox)mRoot.findViewById(R.id.exclude_transfers_from_expense);
        mBankLogo = (ImageView)mRoot.findViewById(R.id.tablet_account_linear_summary_logo);
        mSaveChanges = (Button) mRoot.findViewById(R.id.tablet_account_linear_summary_save_button);
       
        setupView();
       
        return mRoot;
    }

	private void setupView() {
		
		if (mBankAccount.getBank().getInstitution() == null) {
			BankLogoManager.getBankImage(mBankLogo, mBankAccount.getBank().getBankId());
		} else {
			BankLogoManager.getBankImage(mBankLogo, mBankAccount.getBank().getInstitution().getInstitutionId());			
		}
		
		mAccountName.setText(mBankAccount.getAccountName());
		mBankName.setText(mBankAccount.getBank().getBankName());
		mAccountSum.setText(mBankAccount.getBalance() == null ? "" : mFormatter.format(mBankAccount.getBalance()));
		mBankRefreshStatus.setText("refreshStatus");
		mExcludeFromIncomeTxt.setText(getString(R.string.show_hide_exclude_transfers_from_income));
		mExcludeFromExpensesTxt.setText(getString(R.string.show_hide_exclude_transfers_from_expenses));
        mSaveChanges.setText(getString(R.string.save_changes));
        
        Fonts.applyPrimaryBoldFont(mAccountName, 12);
        Fonts.applyPrimaryBoldFont(mBankName, 10);
        Fonts.applyPrimaryBoldFont(mAccountSum, 14);
        Fonts.applySecondaryItalicFont(mBankRefreshStatus, 8);
        Fonts.applyPrimaryBoldFont(mExcludeFromIncomeTxt, 12);
        Fonts.applyPrimaryBoldFont(mExcludeFromExpensesTxt, 12);        
        Fonts.applyPrimaryBoldFont(mSaveChanges, 18);
        
                
        List<AccountExclusionFlags> exclusionFlags = BankAccount.getExclusionsForAccount(mBankAccount);
		
		for (AccountExclusionFlags flag : exclusionFlags) {
			if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ALL)) {
				mExcludeFromExpensesCheckbox.setChecked(true);
				mExcludeFromIncomeCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES)) {
				mExcludeFromExpensesCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME)) {
				mExcludeFromIncomeCheckbox.setChecked(true);
			}
		}
		
        setupOnClickListeners();		
	}



	private void setupOnClickListeners() {
		
		mSaveChanges.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
	
				if (mExcludeFromIncomeCheckbox.isChecked()) {

					mBankAccount.setExclusionFlags(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME.index());
				}
				
				DataController.save();
			}

		});
	}

	@Override
	public FragmentType getType() {
		// TODO Auto-generated method stub
		return null;
	}

}