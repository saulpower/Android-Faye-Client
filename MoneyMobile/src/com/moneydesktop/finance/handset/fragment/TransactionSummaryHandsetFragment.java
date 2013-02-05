
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
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.BarGraphView;
import com.moneydesktop.finance.views.BarView;
import com.moneydesktop.finance.views.BasicBarChartAdapter;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TransactionSummaryHandsetFragment extends BaseFragment {
    private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
    private static TransactionSummaryHandsetFragment sFragment;

    public static TransactionSummaryHandsetFragment getInstance(FragmentType type) {

        if (sFragment != null) {
            return sFragment;
        }

        sFragment = new TransactionSummaryHandsetFragment();
        sFragment.setType(type);
        sFragment.setRetainInstance(true);

        Bundle args = new Bundle();
        sFragment.setArguments(args);

        return sFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        TransactionsDao transactions = ApplicationContext.getDaoSession().getTransactionsDao();
        List<Transactions> transList = transactions.loadAll();
        mRoot = inflater.inflate(R.layout.handset_transaction_summary_view, null);
        setupView(transList, mRoot);
        mRoot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                
                 mActivity.showFragment(FragmentType.TRANSACTIONS);
                
            }
            
        });
        return mRoot;
    }

    private void setupView(List<Transactions> transList, View v) {
        TextView mainLabel = (TextView) v.findViewById(R.id.label_transactions_view);
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
        Fonts.applyPrimarySemiBoldFont(uncategorizedTransactionIcon, 38);

        TextView uncategorizedTransactionText = (TextView) v
                .findViewById(R.id.uncategorized_transaction_text);
        Fonts.applyPrimaryFont(uncategorizedTransactionText, 12);
        setupBarGraphView(v);

        incomeBalance.setText(mFormatter.format(Math.abs(Transactions.getIncomeTotal())));
        expenseBalance.setText(mFormatter.format(Math.abs(Transactions.getExpensesTotal())));
        newTransactionsNumber.setText(Integer.toString(Transactions.getProcessedTransactions()));
        uncategorizedTransactionText.setText(getResources().getString(R.string.text_you_have) + " " +
                + Transactions.getUncategorizedTransactions() + " " + getResources().getString(R.string.text_uncat_trans));

    }

    private void setupBarGraphView(View v) {
        List<Double[]> data = Transactions.get30DayExpenseTotals(new Date());
        double max = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i)[1] > max){
                max = data.get(i)[1];
            }
        }

        BarGraphView l = (BarGraphView) v.findViewById(R.id.daily_spending_graph);
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("dd");
        for(int c = 0; c < data.size(); c++){
                if( data.get(c)[1] > 0){
                    StringBuffer date = new StringBuffer();
                    date = format.format(data.get(c)[0],date,new FieldPosition(0));
                    barList.add(new BarViewModel(date.toString(),data.get(c)[1],max));
                }              
            }

            BasicBarChartAdapter adapter = new BasicBarChartAdapter(barList);
            l.setMax(max);
            l.setAdapter(adapter);
            l.setLabel(true);
            l.setLabelFontSize(8);
            
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
