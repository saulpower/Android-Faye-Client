
package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

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

        mRoot = inflater.inflate(R.layout.handset_transaction_summary_view, null);
        setupView(mRoot);

        return mRoot;
    }

    // TODO: fill with actual data, stubbing for now.
    private void setupView(View v) {
        TextView mainLabel = (TextView) v.findViewById(R.id.label_transactions_view);
        Fonts.applySecondaryItalicFont(mainLabel, 16);

        TextView incomeBalanceIcon = (TextView) v.findViewById(R.id.income_balance_icon);
        Fonts.applyGlyphFont(incomeBalanceIcon, 28);
        TextView incomeBalance = (TextView) v.findViewById(R.id.income_balance);
        Fonts.applyPrimarySemiBoldFont(incomeBalance, 28);
        TextView incomeBalanceLabel = (TextView) v.findViewById(R.id.income_balance_label);
        Fonts.applySecondaryItalicFont(incomeBalanceLabel, 12);

        TextView expenseBalanceIcon = (TextView) v.findViewById(R.id.expense_balance_icon);
        Fonts.applyGlyphFont(expenseBalanceIcon, 28);
        TextView expenseBalance = (TextView) v.findViewById(R.id.expense_balance);
        Fonts.applyPrimarySemiBoldFont(expenseBalance, 28);
        TextView expenseBalanceLabel = (TextView) v.findViewById(R.id.expense_balance_label);
        Fonts.applySecondaryItalicFont(expenseBalanceLabel, 12);

        TextView dailySpendingText = (TextView) v.findViewById(R.id.daily_spending_text);
        Fonts.applySecondaryItalicFont(dailySpendingText, 12);

        TextView newTransactionsNumber = (TextView) v.findViewById(R.id.new_transactions_number);
        Fonts.applyPrimarySemiBoldFont(newTransactionsNumber, 28);
        TextView newTransactionsLabel = (TextView) v.findViewById(R.id.new_transactions_label);
        Fonts.applySecondaryItalicFont(newTransactionsLabel, 12);

        TextView topCategoryIcon = (TextView) v.findViewById(R.id.top_category_icon);
        Fonts.applyPrimarySemiBoldFont(topCategoryIcon, 28);
        TextView topCategoryText = (TextView) v.findViewById(R.id.top_category_text);
        Fonts.applySecondaryItalicFont(topCategoryText, 12);

        TextView uncategorizedTransactionIcon = (TextView) v
                .findViewById(R.id.uncategorized_transaction_icon);
        Fonts.applyPrimarySemiBoldFont(uncategorizedTransactionIcon, 48);

        TextView uncategorizedTransactionText = (TextView) v
                .findViewById(R.id.uncategorized_transaction_text);
        Fonts.applyPrimaryFont(uncategorizedTransactionText, 14);

        // Stub Data
        incomeBalance.setText("$0.00");
        expenseBalance.setText("$0.00");
        newTransactionsNumber.setText("0");
        topCategoryIcon.setText("?");
        uncategorizedTransactionText.setText("You have no uncategorized transactions.");

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
