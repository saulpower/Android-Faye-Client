
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
import com.moneydesktop.finance.views.AccountBalanceItemView;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

public class AccountSummaryHandsetFragment extends BaseFragment {

    private static AccountSummaryHandsetFragment sFragment;
    private HashMap mAccountInfo;

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
        List<BankAccount> mBankList = banks.loadAll();

        mRoot = inflater.inflate(R.layout.account_summary_fragment_view, (ViewGroup) mRoot);
        setupView(mBankList, mRoot);

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

    private void setupView(List<BankAccount> bankList, View v) {
        mAccountInfo = getAccountValues(bankList);

        AccountBalanceItemView cashAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_cash);
        cashAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("cash_accounts")));
        cashAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo.get("cash_amount")));

        AccountBalanceItemView checkingAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_checking);
        checkingAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("checking_accounts")));
        checkingAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo
                .get("checking_amount")));

        AccountBalanceItemView invAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_inv);
        invAccounts
                .setAccountStatus(makeAccountsString((Integer) mAccountInfo.get("inv_accounts")));
        invAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo.get("inv_amount")));

        AccountBalanceItemView propAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_prop);
        propAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("prop_accounts")));
        propAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo.get("prop_amount")));

        AccountBalanceItemView savingAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_saving);
        savingAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("saving_accounts")));
        savingAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo
                .get("saving_amount")));

        AccountBalanceItemView ccAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_cc);
        ccAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo.get("cc_accounts")));
        ccAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo.get("cc_amount")));

        AccountBalanceItemView locAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_loc);
        locAccounts
                .setAccountStatus(makeAccountsString((Integer) mAccountInfo.get("loc_accounts")));
        locAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo.get("loc_amount")));

        AccountBalanceItemView loansAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_loans);
        loansAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("loans_accounts")));
        loansAccounts
                .setAccountAmount(makeTotalsString((Float) mAccountInfo.get("loans_amount")));

        AccountBalanceItemView mortAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_mort);
        mortAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("mort_accounts")));
        mortAccounts.setAccountAmount(makeTotalsString((Float) mAccountInfo.get("mort_amount")));

    }

    private String makeAccountsString(Integer a) {
        if (a == 0) {
            return "No accounts";
        }
        else if (a == 1) {
            return "1 account";
        }
        else {
            return a.toString() + " accounts";
        }
    }

    private String makeTotalsString(float m) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(m);
    }

    private HashMap getAccountValues(List<BankAccount> bankList) {
        HashMap accountTypes = new HashMap();
        // initializing fields so we don't run into NPEs later
        accountTypes.put("cash_accounts", new Integer(0));
        accountTypes.put("cash_amounts", new Float(0));
        accountTypes.put("checking_accounts", new Integer(0));
        accountTypes.put("checking_amounts", new Float(0));
        accountTypes.put("inv_accounts", new Integer(0));
        accountTypes.put("inv_amounts", new Float(0));
        accountTypes.put("prop_accounts", new Integer(0));
        accountTypes.put("prop_amounts", new Float(0));
        accountTypes.put("saving_accounts", new Integer(0));
        accountTypes.put("saving_amounts", new Float(0));
        accountTypes.put("cc_accounts", new Integer(0));
        accountTypes.put("cc_amounts", new Float(0));
        accountTypes.put("loc_accounts", new Integer(0));
        accountTypes.put("loc_amounts", new Float(0));
        accountTypes.put("loans_accounts", new Integer(0));
        accountTypes.put("loans_amounts", new Float(0));
        accountTypes.put("mort_accounts", new Integer(0));
        accountTypes.put("mort_amounts", new Float(0));
        for (int i = 0; i < bankList.size(); i++) {
            if (bankList.get(i).getAccountTypeId() == 1) {
                accountTypes.put("checking_accounts",
                        (Integer) accountTypes.get("checking_accounts") + 1);
                accountTypes.put("checking_amounts", (Float) accountTypes.get("checking_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 2) {
                accountTypes.put("saving_accounts",
                        (Integer) accountTypes.get("saving_accounts") + 1);
                accountTypes.put("saving_amounts", (Float) accountTypes.get("saving_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 3) {
                accountTypes
                        .put("loans_accounts", (Integer) accountTypes.get("loans_accounts") + 1);
                accountTypes.put("loans_amounts", (Float) accountTypes.get("loans_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 4) {
                accountTypes.put("cc_accounts", (Integer) accountTypes.get("cc_accounts") + 1);
                accountTypes.put("cc_amounts", (Float) accountTypes.get("cc_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 5) {
                accountTypes.put("inv_accounts", (Integer) accountTypes.get("inv_accounts") + 1);
                accountTypes.put("inv_amounts", (Float) accountTypes.get("inv_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 6) {
                accountTypes.put("loc_accounts", (Integer) accountTypes.get("loc_accounts") + 1);
                accountTypes.put("loc_amounts", (Float) accountTypes.get("loc_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 7) {
                accountTypes.put("mort_accounts", (Integer) accountTypes.get("mort_accounts") + 1);
                accountTypes.put("mort_amounts", (Float) accountTypes.get("mort_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 8) {
                accountTypes.put("prop_accounts", (Integer) accountTypes.get("prop_accounts") + 1);
                accountTypes.put("prop_amounts", (Float) accountTypes.get("prop_amounts")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 9) {
                accountTypes.put("cash_accounts", (Integer) accountTypes.get("cash_accounts") + 1);
                accountTypes.put("cash_amounts", (Float) accountTypes.get("cash_amounts")
                        + bankList.get(i).getBalance());
            }
        }

        return accountTypes;
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
