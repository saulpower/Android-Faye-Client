
package com.moneydesktop.finance.tablet.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.activity.IntroBaseActivity;
import com.moneydesktop.finance.tablet.fragment.IntroTabletFragment;
import com.moneydesktop.finance.util.Fonts;

public class IntroTabletActivity extends IntroBaseActivity {

    @Override
    protected void applyFonts() {
        Fonts.applyPrimaryFont(mLoadingMessage, 24);
        Fonts.applyPrimarySemiBoldFont(mStartButton, 18);
    }

    @Override
    protected FragmentPagerAdapter getAdapter() {
        return new MyAdapter(getSupportFragmentManager());
    }

    @Override
    protected Intent getDashboardIntent() {
        return new Intent(this, DashboardTabletActivity.class);
    }

    @Override
    protected int getContentResource() {
        return R.layout.tablet_intro_view;
    }
    
    private void saveBankExclusions() {
		BankAccountDao dao = ApplicationContext.getDaoSession().getBankAccountDao();
		List<BankAccount> bankAccountList = dao.loadAll();
		    
		ArrayList<String> transactionsList = new ArrayList<String>();
		ArrayList<String> reports = new ArrayList<String> ();
		ArrayList<String> accountList = new ArrayList<String> ();
		ArrayList<String> budgets = new ArrayList<String> ();
		ArrayList<String> transfersFromIncome = new ArrayList<String> ();
		ArrayList<String> transfersFromExpenses = new ArrayList<String> ();
		ArrayList<String> all = new ArrayList<String> ();
		
		
		for (BankAccount bankAccount : bankAccountList) {
			List<AccountExclusionFlags> exclusionListForAccount = BankAccount.getExclusionsForAccount(bankAccount);
			
			for (AccountExclusionFlags exclusions : exclusionListForAccount) {
				
				if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ALL) {
					all.add(String.valueOf(bankAccount.getBankAccountId()));
					
				} else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES) {
					transfersFromExpenses.add(String.valueOf(bankAccount.getBankAccountId()));
					
				} else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME) {
					transfersFromIncome.add(String.valueOf(bankAccount.getBankAccountId()));
					
				} else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS) {
					budgets.add(String.valueOf(bankAccount.getBankAccountId()));
					
				} else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST) {
					accountList.add(String.valueOf(bankAccount.getBankAccountId()));
					
				} else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS) {
					reports.add(String.valueOf(bankAccount.getBankAccountId()));
					
				} else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST) {
					transactionsList.add(String.valueOf(bankAccount.getBankAccountId()));
				}
			}
		}
		
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_ALL, Util.serializeObject(all).toString());
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_TRANSFERS_FROM_EXPENSES, Util.serializeObject(transfersFromExpenses).toString());
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_TRANSFERS_FROM_INCOME, Util.serializeObject(transfersFromIncome).toString());
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_BUDGETS, Util.serializeObject(budgets).toString());
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_ACCOUNTS_LIST, Util.serializeObject(accountList).toString());
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_REPORTS, Util.serializeObject(reports).toString());
		Preferences.saveString(Constant.PREFS_EXCLUSIONS_TRANSACTIONS_LIST, Util.serializeObject(transactionsList).toString());
		
	}

    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new IntroTabletFragment(R.drawable.tablet_tips1);
                case 1:
                    return new IntroTabletFragment(R.drawable.tablet_tips2);
                case 2:
                    return new IntroTabletFragment(R.drawable.tablet_tips3);
                case 3:
                    return new IntroTabletFragment(R.drawable.tablet_tips4);
                default:
                    return null;
            }
        }
    }
}
