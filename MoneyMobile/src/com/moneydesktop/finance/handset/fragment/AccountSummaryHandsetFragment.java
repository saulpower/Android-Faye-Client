
package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;

import java.util.List;

public class AccountSummaryHandsetFragment extends BaseFragment {

    private static AccountSummaryHandsetFragment sFragment;

    public static AccountSummaryHandsetFragment getInstance(int position) {

        if (sFragment != null) {
            return sFragment;
        }

        sFragment = new AccountSummaryHandsetFragment();
        sFragment.setPosition(position);
        sFragment.setRetainInstance(true);

        Bundle args = new Bundle();
        sFragment.setArguments(args);

        return sFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        BankAccountDao banks = ApplicationContext.getDaoSession().getBankAccountDao();
        List<BankAccount> bankList = banks.loadAll();

        mRoot = inflater.inflate(R.layout.handset_account_summary_view, null);
        setupView();

        return mRoot;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setupView() {

    }

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
