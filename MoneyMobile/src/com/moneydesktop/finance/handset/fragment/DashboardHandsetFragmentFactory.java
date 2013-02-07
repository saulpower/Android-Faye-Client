package com.moneydesktop.finance.handset.fragment;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.data.Enums.FragmentType;

public class DashboardHandsetFragmentFactory {

    public static BaseFragment getInstance(int position) {
        
        switch (position) {
            case 0:
                return AccountSummaryHandsetFragment.getInstance(FragmentType.ACCOUNT_SUMMARY);
//            case 1:
//                return SpendingSummaryFragment.getInstance(FragmentType.SPENDING_SUMMARY);
//            case 2:
//                return BudgetSummaryHandsetFragment.getInstance(FragmentType.ACCOUNT_SUMMARY);
            case 1:
                return TransactionSummaryHandsetFragment.getInstance(FragmentType.TRANSACTION_SUMMARY);
            case 2:
                return SettingsHandsetFragment.getInstance(FragmentType.SETTINGS);
        }
        
        return null;
    }
}
