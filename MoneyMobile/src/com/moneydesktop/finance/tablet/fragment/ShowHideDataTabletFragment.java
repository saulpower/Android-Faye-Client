package com.moneydesktop.finance.tablet.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.Fonts;

import java.text.NumberFormat;
import java.util.List;

@TargetApi(11)
public class ShowHideDataTabletFragment extends BaseFragment {
	
	private static String mAccountId;
	
	private Button mSaveChanges;
	private static BankAccount mBankAccount;
	private TextView mAccountName, mBankName, mAccountSum, mBankRefreshStatus, mExcludeFromIncomeTxt, mExcludeFromExpensesTxt, mExcludeTransactionsFromListsTxt, mExcludeFromReportsTxt, mExcludeFromAccountSummaryTxt, mExcludeFromBudgetsTxt;
	private ImageView mBankLogo;
	private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
	private CheckBox mExcludeFromIncomeCheckbox, mExcludeFromExpensesCheckbox, mExcludeTransactionsFromListsCheckbox, mExcludeFromReportsCheckbox, mExcludeFromAccountSummaryCheckbox, mExcludeFromBudgetsCheckbox;
	
	@Override
 	public String getFragmentTitle() {
		return getString(R.string.show_hide_label);
	}

	@Override
	public boolean onBackPressed() {
		return false;
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
        
        
        mExcludeTransactionsFromListsTxt = (TextView)mRoot.findViewById(R.id.exclude_transactions_from_list_txt);
        mExcludeFromReportsTxt = (TextView)mRoot.findViewById(R.id.exclude_from_reports_txt);
        mExcludeFromAccountSummaryTxt = (TextView)mRoot.findViewById(R.id.exclude_from_account_summary_txt);
        mExcludeFromBudgetsTxt = (TextView)mRoot.findViewById(R.id.exclude_from_budgets_txt);
        mExcludeFromIncomeTxt = (TextView)mRoot.findViewById(R.id.exclude_transfers_from_income_txt);
        mExcludeFromExpensesTxt = (TextView)mRoot.findViewById(R.id.exclude_transfers_from_expenses_txt);
        
        
        mExcludeTransactionsFromListsCheckbox = (CheckBox)mRoot.findViewById(R.id.exclude_transactions_from_lists);
        mExcludeFromReportsCheckbox = (CheckBox)mRoot.findViewById(R.id.exclude_from_reports);
        mExcludeFromAccountSummaryCheckbox = (CheckBox)mRoot.findViewById(R.id.exclude_from_account_summary);
        mExcludeFromBudgetsCheckbox = (CheckBox)mRoot.findViewById(R.id.exclude_from_budgets);
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
		mExcludeTransactionsFromListsTxt.setText(getString(R.string.show_hide_exclude_transactions_from_lists));
		mExcludeFromReportsTxt.setText(getString(R.string.show_hide_exclude_from_reports));
		mExcludeFromAccountSummaryTxt.setText(getString(R.string.show_hide_exclude_from_account_summary));
		mExcludeFromBudgetsTxt.setText(getString(R.string.show_hide_exclude_from_budgets));
		mExcludeFromIncomeTxt.setText(getString(R.string.show_hide_exclude_transfers_from_income));
		mExcludeFromExpensesTxt.setText(getString(R.string.show_hide_exclude_transfers_from_expenses));
        mSaveChanges.setText(getString(R.string.save_changes));
        
        Fonts.applyPrimaryBoldFont(mAccountName, 12);
        Fonts.applyPrimaryBoldFont(mBankName, 10);
        Fonts.applyPrimaryBoldFont(mAccountSum, 14);
        Fonts.applySecondaryItalicFont(mBankRefreshStatus, 8);      
        Fonts.applyPrimaryBoldFont(mExcludeTransactionsFromListsTxt, 12);
        Fonts.applyPrimaryBoldFont(mExcludeFromReportsTxt, 12);
        Fonts.applyPrimaryBoldFont(mExcludeFromAccountSummaryTxt, 12);
        Fonts.applyPrimaryBoldFont(mExcludeFromBudgetsTxt, 12);
        Fonts.applyPrimaryBoldFont(mExcludeFromIncomeTxt, 12);
        Fonts.applyPrimaryBoldFont(mExcludeFromExpensesTxt, 12);        
        Fonts.applyPrimaryBoldFont(mSaveChanges, 18);
        
                
        List<AccountExclusionFlags> exclusionFlags = BankAccount.getExclusionsForAccount(mBankAccount);
		
		for (AccountExclusionFlags flag : exclusionFlags) {

			if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES)) {
				mExcludeFromExpensesCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME)) {
				mExcludeFromIncomeCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST)) {
				mExcludeTransactionsFromListsCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS)) {
				mExcludeFromReportsCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST)) {
				mExcludeFromAccountSummaryCheckbox.setChecked(true);
			} else if (flag.equals(AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS)) {
				mExcludeFromBudgetsCheckbox.setChecked(true);
			}
		}
		
        setupOnClickListeners();		
	}

	private void setupOnClickListeners() {
		
		mSaveChanges.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
	
				int flag = 0;
				if (mExcludeFromExpensesCheckbox.isChecked()) {
					flag = flag + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES.index();
				}
				if (mExcludeFromIncomeCheckbox.isChecked()) {
					flag = flag + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME.index();
				}
				if (mExcludeTransactionsFromListsCheckbox.isChecked()) {
					flag = flag + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST.index();
				}
				if (mExcludeFromReportsCheckbox.isChecked()) {
					flag = flag + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS.index();
				}
				if (mExcludeFromAccountSummaryCheckbox.isChecked()) {
					flag = flag + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST.index();
				}
				if (mExcludeFromBudgetsCheckbox.isChecked()) {
					flag = flag + AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS.index();
				}
				
				mBankAccount.setExclusionFlags(flag);
				mBankAccount.updateSingle();
				((DropDownTabletActivity)mActivity).dismissDropdown();
			}

		});
	}

	@Override
	public FragmentType getType() {
		// TODO Auto-generated method stub
		return null;
	}

}