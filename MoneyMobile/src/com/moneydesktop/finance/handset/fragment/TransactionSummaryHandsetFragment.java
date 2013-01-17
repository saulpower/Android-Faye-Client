
package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.BarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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

    private void setupView(List<Transactions> transList, List<Category> catList, View v) {
        TextView mainLabel = (TextView) v.findViewById(R.id.label_transactions_view);
        HashMap transInfo = parseTransactions(transList);
        Fonts.applySecondaryItalicFont(mainLabel, 12);

        TextView incomeBalanceIcon = (TextView) v.findViewById(R.id.income_balance_icon);
        Fonts.applyGlyphFont(incomeBalanceIcon, 24);
        TextView incomeBalance = (TextView) v.findViewById(R.id.income_balance);
        Fonts.applyPrimarySemiBoldFont(incomeBalance, 24);
        TextView incomeBalanceLabel = (TextView) v.findViewById(R.id.income_balance_label);
        Fonts.applySecondaryItalicFont(incomeBalanceLabel, 10);

        TextView expenseBalanceIcon = (TextView) v.findViewById(R.id.expense_balance_icon);
        Fonts.applyGlyphFont(expenseBalanceIcon, 24);
        TextView expenseBalance = (TextView) v.findViewById(R.id.expense_balance);
        Fonts.applyPrimarySemiBoldFont(expenseBalance, 24);
        TextView expenseBalanceLabel = (TextView) v.findViewById(R.id.expense_balance_label);
        Fonts.applySecondaryItalicFont(expenseBalanceLabel, 10);

        TextView dailySpendingText = (TextView) v.findViewById(R.id.daily_spending_text);
        Fonts.applySecondaryItalicFont(dailySpendingText, 10);

        TextView newTransactionsNumber = (TextView) v.findViewById(R.id.new_transactions_number);
        Fonts.applyPrimarySemiBoldFont(newTransactionsNumber, 24);
        TextView newTransactionsLabel = (TextView) v.findViewById(R.id.new_transactions_label);
        Fonts.applySecondaryItalicFont(newTransactionsLabel, 10);

        TextView topCategoryIcon = (TextView) v.findViewById(R.id.top_category_icon);
        topCategoryIcon.setText(Transactions.topCategory());
        Fonts.applyGlyphFont(topCategoryIcon, 24);
        TextView topCategoryText = (TextView) v.findViewById(R.id.top_category_text);
        Fonts.applySecondaryItalicFont(topCategoryText, 10);

        TextView uncategorizedTransactionIcon = (TextView) v
                .findViewById(R.id.uncategorized_transaction_icon);
        Fonts.applyPrimarySemiBoldFont(uncategorizedTransactionIcon, 44);

        TextView uncategorizedTransactionText = (TextView) v
                .findViewById(R.id.uncategorized_transaction_text);
        Fonts.applyPrimaryFont(uncategorizedTransactionText, 12);
        setupBarGraphView(v);

        incomeBalance.setText("$" + Transactions.getIncomeTotal().toString());
        expenseBalance.setText("$" + Transactions.getExpensesTotal().toString());
        newTransactionsNumber.setText(Integer.toString(Transactions.getProcessedTransactions()));
        uncategorizedTransactionText.setText("You have "
                + Transactions.getUncategorizedTransactions() + " uncategorized transactions.");

    }

    private void setupBarGraphView(View v) {
        HashMap<Date, Double> data = Transactions.get30DayExpenseTotals();
        int widthscale = data.size();
        double heightscale = Collections.max(data.values());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -30);
        LinearLayout l = (LinearLayout) v.findViewById(R.id.daily_spending_graph);
        for (int i = 0; i < 30; i++) {
            if (data.get(cal.getTime()) != null) {
                BarView b = new BarView(getActivity(), Integer.toString(cal
                        .get(Calendar.DAY_OF_MONTH)),
                        data.get(cal.getTime()), heightscale);
                LayoutParams layout = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                b.setLayoutParams(layout);
                l.addView(b);
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
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
