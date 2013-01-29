
package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AccountBalanceItemView;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

public class AccountSummaryHandsetFragment extends BaseFragment {
    private HashMap<String, Object> mAccountInfo;

    public static AccountSummaryHandsetFragment getInstance(FragmentType type) {

        AccountSummaryHandsetFragment fragment = new AccountSummaryHandsetFragment();
        fragment.setType(type);
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        BankAccountDao banks = ApplicationContext.getDaoSession().getBankAccountDao();
        List<BankAccount> mBankList = banks.loadAll();
        mRoot = inflater.inflate(R.layout.handset_account_summary_fragment_view, (ViewGroup) mRoot);
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
        TextView label = (TextView) v.findViewById(R.id.label_balance_view);
        label.setText(getResources().getString(R.string.label_balances));
        Fonts.applySecondaryItalicFont(label, 12);
        AccountBalanceItemView cashAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_cash);
        cashAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo

                .get("cash_accounts")));
        cashAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo.get("cash_amount")));

        AccountBalanceItemView checkingAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_checking);
        checkingAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("checking_accounts")));
        checkingAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo
                .get("checking_amount")));

        AccountBalanceItemView invAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_inv);
        invAccounts
                .setAccountStatus(makeAccountsString((Integer) mAccountInfo.get("inv_accounts")));
        invAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo.get("inv_amount")));

        AccountBalanceItemView propAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_prop);
        propAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("prop_accounts")));
        propAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo.get("prop_amount")));

        AccountBalanceItemView savingAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_saving);
        savingAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("saving_accounts")));
        savingAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo
                .get("saving_amount")));

        AccountBalanceItemView ccAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_cc);
        ccAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo.get("cc_accounts")));
        ccAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo.get("cc_amount")));

        AccountBalanceItemView locAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_loc);
        locAccounts
                .setAccountStatus(makeAccountsString((Integer) mAccountInfo.get("loc_accounts")));
        locAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo.get("loc_amount")));

        AccountBalanceItemView loansAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_loans);
        loansAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("loans_accounts")));
        loansAccounts
                .setAccountAmount(makeTotalsString((Double) mAccountInfo.get("loans_amount")));

        AccountBalanceItemView mortAccounts = (AccountBalanceItemView) v
                .findViewById(R.id.account_balance_mort);
        mortAccounts.setAccountStatus(makeAccountsString((Integer) mAccountInfo
                .get("mort_accounts")));
        mortAccounts.setAccountAmount(makeTotalsString((Double) mAccountInfo.get("mort_amount")));

    }

    private String makeAccountsString(Integer a) {
        if (a == 0 || a == null) {
            return "No accounts";
        }
        else if (a == 1) {
            return "1 account";
        }
        else {
            return a.toString() + " accounts";
        }
    }

    private String makeTotalsString(Double m) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(m);
    }

    private HashMap<String, Object> initHashMap() {
        // initializing fields so we don't run into NPEs later
        HashMap<String, Object> accountTypes = new HashMap<String, Object>();
        accountTypes.put("cash_accounts", Integer.valueOf(0));
        accountTypes.put("cash_amount", Double.valueOf(0));
        accountTypes.put("checking_accounts", Integer.valueOf(0));
        accountTypes.put("checking_amount", Double.valueOf(0));
        accountTypes.put("inv_accounts", Integer.valueOf(0));
        accountTypes.put("inv_amount", Double.valueOf(0));
        accountTypes.put("prop_accounts", Integer.valueOf(0));
        accountTypes.put("prop_amount", Double.valueOf(0));
        accountTypes.put("saving_accounts", Integer.valueOf(0));
        accountTypes.put("saving_amount", Double.valueOf(0));
        accountTypes.put("cc_accounts", Integer.valueOf(0));
        accountTypes.put("cc_amount", Double.valueOf(0));
        accountTypes.put("loc_accounts", Integer.valueOf(0));
        accountTypes.put("loc_amount", Double.valueOf(0));
        accountTypes.put("loans_accounts", Integer.valueOf(0));
        accountTypes.put("loans_amount", Double.valueOf(0));
        accountTypes.put("mort_accounts", Integer.valueOf(0));
        accountTypes.put("mort_amount", Double.valueOf(0));
        return accountTypes;
    }

    private HashMap<String, Object> getAccountValues(List<BankAccount> bankList) {
        HashMap<String, Object> accountTypes = initHashMap();
        for (int i = 0; i < bankList.size(); i++) {
            if (bankList.get(i).getAccountTypeId() == 1 || bankList.get(i).getAccountTypeId() == 49) {
                accountTypes.put("checking_accounts",
                        (Integer) accountTypes.get("checking_accounts") + 1);
                accountTypes.put("checking_amount", (Double) accountTypes.get("checking_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 2
                    || bankList.get(i).getAccountTypeId() == 50) {
                accountTypes.put("saving_accounts",
                        (Integer) accountTypes.get("saving_accounts") + 1);
                accountTypes.put("saving_amount", (Double) accountTypes.get("saving_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 3) {
                accountTypes
                        .put("loans_accounts", (Integer) accountTypes.get("loans_accounts") + 1);
                accountTypes.put("loans_amount", (Double) accountTypes.get("loans_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 4
                    || bankList.get(i).getAccountTypeId() == 52) {
                accountTypes.put("cc_accounts", (Integer) accountTypes.get("cc_accounts") + 1);
                accountTypes.put("cc_amount", (Double) accountTypes.get("cc_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 5) {
                accountTypes.put("inv_accounts", (Integer) accountTypes.get("inv_accounts") + 1);
                accountTypes.put("inv_amount", (Double) accountTypes.get("inv_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 6
                    || bankList.get(i).getAccountTypeId() == 54) {
                accountTypes.put("loc_accounts", (Integer) accountTypes.get("loc_accounts") + 1);
                accountTypes.put("loc_amount", (Double) accountTypes.get("loc_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 7
                    || bankList.get(i).getAccountTypeId() == 55) {
                accountTypes.put("mort_accounts", (Integer) accountTypes.get("mort_accounts") + 1);
                accountTypes.put("mort_amount", (Double) accountTypes.get("mort_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 8
                    || bankList.get(i).getAccountTypeId() == 56) {
                accountTypes.put("prop_accounts", (Integer) accountTypes.get("prop_accounts") + 1);
                accountTypes.put("prop_amount", (Double) accountTypes.get("prop_amount")
                        + bankList.get(i).getBalance());
            }
            else if (bankList.get(i).getAccountTypeId() == 9
                    || bankList.get(i).getAccountTypeId() == 48) {
                accountTypes.put("cash_accounts", (Integer) accountTypes.get("cash_accounts") + 1);
                accountTypes.put("cash_amount", (Double) accountTypes.get("cash_amount")
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
