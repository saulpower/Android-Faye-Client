
package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.util.Fonts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransactionSummaryHandsetFragment extends BaseFragment {

    private static TransactionSummaryHandsetFragment sFragment;

    public static TransactionSummaryHandsetFragment getInstance(int position) {

        if (sFragment != null) {
            return sFragment;
        }

        sFragment = new TransactionSummaryHandsetFragment();
        sFragment.setPosition(position);
        sFragment.setRetainInstance(true);

        Bundle args = new Bundle();
        sFragment.setArguments(args);

        return sFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        TransactionsDao transactions = ApplicationContext.getDaoSession().getTransactionsDao();
        CategoryDao categories = ApplicationContext.getDaoSession().getCategoryDao();
        List<Category> catList = categories.loadAll();
        List<Transactions> transList = transactions.loadAll();
        mRoot = inflater.inflate(R.layout.handset_transaction_summary_view, null);
        setupView(transList, catList, mRoot);

        return mRoot;
    }

    // TODO: fill with actual data, stubbing for now.
    private void setupView(List<Transactions> transList, List<Category> catList, View v) {
        TextView mainLabel = (TextView) v.findViewById(R.id.label_transactions_view);
        HashMap transInfo = parseTransactions(transList);
        Fonts.applySecondaryItalicFont(mainLabel, 16);

        TextView incomeBalanceIcon = (TextView) v.findViewById(R.id.income_balance_icon);
        Fonts.applyGlyphFont(incomeBalanceIcon, 28);
        TextView incomeBalance = (TextView) v.findViewById(R.id.income_balance);
        Fonts.applyPrimarySemiBoldFont(incomeBalance, 28);
        TextView incomeBalanceLabel = (TextView) v.findViewById(R.id.income_balance_label);
        Fonts.applySecondaryItalicFont(incomeBalanceLabel, 14);

        TextView expenseBalanceIcon = (TextView) v.findViewById(R.id.expense_balance_icon);
        Fonts.applyGlyphFont(expenseBalanceIcon, 28);
        TextView expenseBalance = (TextView) v.findViewById(R.id.expense_balance);
        Fonts.applyPrimarySemiBoldFont(expenseBalance, 28);
        TextView expenseBalanceLabel = (TextView) v.findViewById(R.id.expense_balance_label);
        Fonts.applySecondaryItalicFont(expenseBalanceLabel, 14);

        TextView dailySpendingText = (TextView) v.findViewById(R.id.daily_spending_text);
        Fonts.applySecondaryItalicFont(dailySpendingText, 14);

        TextView newTransactionsNumber = (TextView) v.findViewById(R.id.new_transactions_number);
        Fonts.applyPrimarySemiBoldFont(newTransactionsNumber, 28);
        TextView newTransactionsLabel = (TextView) v.findViewById(R.id.new_transactions_label);
        Fonts.applySecondaryItalicFont(newTransactionsLabel, 14);

        TextView topCategoryIcon = (TextView) v.findViewById(R.id.top_category_icon);
        topCategoryIcon.setText(Transactions.topCategory());
        Fonts.applyGlyphFont(topCategoryIcon, 28);
        TextView topCategoryText = (TextView) v.findViewById(R.id.top_category_text);
        Fonts.applySecondaryItalicFont(topCategoryText, 14);

        TextView uncategorizedTransactionIcon = (TextView) v
                .findViewById(R.id.uncategorized_transaction_icon);
        Fonts.applyPrimarySemiBoldFont(uncategorizedTransactionIcon, 48);

        TextView uncategorizedTransactionText = (TextView) v
                .findViewById(R.id.uncategorized_transaction_text);
        Fonts.applyPrimaryFont(uncategorizedTransactionText, 16);

        // Stub Data
        incomeBalance.setText("$" + transInfo.get("income").toString());
        expenseBalance.setText("$" + transInfo.get("expenses").toString());
        newTransactionsNumber.setText(transInfo.get("new_transactions").toString());
        uncategorizedTransactionText.setText("You have no uncategorized transactions.");

    }

    private HashMap<String, Object> parseTransactions(List<Transactions> l) {
        HashMap<String, Object> t = new HashMap<String, Object>();
        t.put("uncat_transactions", Integer.valueOf(0));
        t.put("new_transactions", Integer.valueOf(0));
        t.put("income", Double.valueOf(0));
        t.put("expenses", Double.valueOf(0));
        List<Category> catList = new ArrayList<Category>();
        for (int i = 0; i < l.size(); i++) {
            catList.add(l.get(i).getCategory());
        }
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).isIncome()) {
                t.put("income", (Double) t.get("income") + l.get(i).getAmount());
            }
            else {
                t.put("expenses", (Double) t.get("expenses") + l.get(i).getAmount());
            }
            if (l.get(i).isNew()) {
                t.put("new_transactions", (Integer) t.get("cash_accounts") + 1);
            }
        }
        HashMap<String, Object> c = new HashMap<String, Object>();
        for (int i = 0; i < l.size(); i++) {

        }
        return t;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
