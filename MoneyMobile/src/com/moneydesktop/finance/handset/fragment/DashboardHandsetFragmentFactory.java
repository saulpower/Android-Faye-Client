package com.moneydesktop.finance.handset.fragment;

import com.moneydesktop.finance.BaseFragment;

public class DashboardHandsetFragmentFactory {

    public static BaseFragment getInstance(int position) {
        
        switch (position) {
            case 0:
                return AccountSummaryHandsetFragment.getInstance(0);
            case 1:
                return SpendingSummaryFragment.getInstance(1);
            case 2:
                return BudgetSummaryHandsetFragment.getInstance(2);
            case 3:
                return TransactionSummaryHandsetFragment.getInstance(3);
            case 4:
                return SettingsHandsetFragment.getInstance(4);
            case 5:
                return AccountTypesHandsetFragment.getInstance(5);
        }
        
        return null;
    }
}
