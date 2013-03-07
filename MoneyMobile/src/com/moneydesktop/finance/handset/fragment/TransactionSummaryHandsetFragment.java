
package com.moneydesktop.finance.handset.fragment;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.shared.fragment.GrowFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.BarGraphView;
import com.moneydesktop.finance.views.BarView;

public class TransactionSummaryHandsetFragment extends GrowFragment {
	
    private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();

    public static TransactionSummaryHandsetFragment getInstance(int position) {


    	TransactionSummaryHandsetFragment fragment = new TransactionSummaryHandsetFragment();

        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

	@Override
	public FragmentType getType() {
		return FragmentType.TRANSACTION_SUMMARY;
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
                 mActivity.showFragment(FragmentType.TRANSACTIONS, true);
            }
            
        });
        return mRoot;
    }

    private void setupView(List<Transactions> transList, View v) {
        TextView mainLabel = (TextView) v.findViewById(R.id.label_transactions_view);
        Fonts.applySecondaryItalicFont(mainLabel, 8);

        TextView incomeBalanceIcon = (TextView) v.findViewById(R.id.income_balance_icon);
        Fonts.applyGlyphFont(incomeBalanceIcon, 20);
        TextView incomeBalance = (TextView) v.findViewById(R.id.income_balance);
        Fonts.applyPrimarySemiBoldFont(incomeBalance, 20);
        TextView incomeBalanceLabel = (TextView) v.findViewById(R.id.income_balance_label);
        Fonts.applySecondaryItalicFont(incomeBalanceLabel, 8);

        TextView expenseBalanceIcon = (TextView) v.findViewById(R.id.expense_balance_icon);
        Fonts.applyGlyphFont(expenseBalanceIcon, 20);
        TextView expenseBalance = (TextView) v.findViewById(R.id.expense_balance);
        Fonts.applyPrimarySemiBoldFont(expenseBalance, 20);
        TextView expenseBalanceLabel = (TextView) v.findViewById(R.id.expense_balance_label);
        Fonts.applySecondaryItalicFont(expenseBalanceLabel, 8);

        TextView dailySpendingText = (TextView) v.findViewById(R.id.daily_spending_text);
        Fonts.applySecondaryItalicFont(dailySpendingText, 8);

        TextView newTransactionsNumber = (TextView) v.findViewById(R.id.new_transactions_number);
        Fonts.applyPrimarySemiBoldFont(newTransactionsNumber, 20);
        TextView newTransactionsLabel = (TextView) v.findViewById(R.id.new_transactions_label);
        Fonts.applySecondaryItalicFont(newTransactionsLabel, 8);

        TextView topCategoryIcon = (TextView) v.findViewById(R.id.top_category_icon);
        topCategoryIcon.setText(Transactions.topCategory());
        Fonts.applyGlyphFont(topCategoryIcon, 20);
        TextView topCategoryText = (TextView) v.findViewById(R.id.top_category_text);
        Fonts.applySecondaryItalicFont(topCategoryText, 8);

        TextView uncategorizedTransactionIcon = (TextView) v
                .findViewById(R.id.uncategorized_transaction_icon);
        Fonts.applyPrimarySemiBoldFont(uncategorizedTransactionIcon, 28);

        TextView uncategorizedTransactionText = (TextView) v
                .findViewById(R.id.uncategorized_transaction_text);
        Fonts.applyPrimaryFont(uncategorizedTransactionText, 8);
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
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -30);
        BarGraphView l = (BarGraphView) v.findViewById(R.id.daily_spending_graph);
        
        for(int c = 0; c < data.size(); c++){
                if( data.get(c)[1] > 0){
                    BarView b = new BarView(getActivity(), Integer.toString(data.get(c)[0].intValue()),
                            data.get(c)[1], max);
                    LayoutParams layout = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                    b.setLayoutParams(layout);
                    l.addView(b);
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
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
