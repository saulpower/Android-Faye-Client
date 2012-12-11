package com.moneydesktop.finance.handset.fragment;

import com.moneydesktop.finance.BaseFragment;

public class DashboardFragmentFactory {

    public static BaseFragment getInstance(int position) {
        
        switch (position) {
            case 0:
                return AccountSummaryFragment.getInstance(0);
            case 1:
                return SpendingSummaryFragment.getInstance(1);
            case 2:
                return BudgetSummaryFragment.getInstance(2);
            case 3:
                return TransactionSummaryFragment.getInstance(3);
            case 4:
                return SettingsFragment.getInstance(4);
        }
        
        return null;
    }
}
