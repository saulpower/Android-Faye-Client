
package main.java.com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.database.Transactions;
import main.java.com.moneydesktop.finance.handset.adapter.TransactionChartHandsetAdapter;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.shared.fragment.GrowFragment;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.views.barchart.BarChartView;
import de.greenrobot.event.EventBus;

import java.text.NumberFormat;

public class TransactionSummaryHandsetFragment extends GrowFragment {

    private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();

    private BarChartView mBarChart;
    private TransactionChartHandsetAdapter mAdapter;

    private TextView mMainLabel;
    private TextView mIncomeBalanceIcon;
    private TextView mIncomeBalance;
    private TextView mIncomeBalanceLabel;
    private TextView mExpenseBalanceIcon;
    private TextView mExpenseBalance;
    private TextView mExpenseBalanceLabel;
    private TextView mDailySpendingText;
    private TextView mNewTransactionsNumber;
    private TextView mNewTransactionsLabel;
    private TextView mTopCategoryIcon;
    private TextView mTopCategoryText;
    private TextView mUncategorizedTransactionIcon;
    private TextView mUncategorizedTransactionText;

    public static TransactionSummaryHandsetFragment getInstance(int position) {

        TransactionSummaryHandsetFragment fragment = new TransactionSummaryHandsetFragment();

        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void onEvent(EventMessage.SyncEvent event) {

        if (event.isFinished() && !mBarChart.isTransitioning() && !mBarChart.isUpdating()) {
            mAdapter.refreshAdapterData();
        }
    }

    @Override
    public FragmentType getType() {
        return FragmentType.TRANSACTION_SUMMARY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.handset_transaction_summary_view, null);

        setupView();
        applyFonts();

        setupBarGraphView();

        calculateSummaries();

        mRoot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                 mActivity.showFragment(FragmentType.TRANSACTIONS, true);
            }

        });

        return mRoot;
    }

    private void setupView() {

        mBarChart = (BarChartView) mRoot.findViewById(R.id.daily_spending_graph);

        mMainLabel = (TextView) mRoot.findViewById(R.id.label_transactions_view);
        mIncomeBalanceIcon = (TextView) mRoot.findViewById(R.id.income_balance_icon);
        mIncomeBalance = (TextView) mRoot.findViewById(R.id.income_balance);
        mIncomeBalanceLabel = (TextView) mRoot.findViewById(R.id.income_balance_label);
        mExpenseBalanceIcon = (TextView) mRoot.findViewById(R.id.expense_balance_icon);
        mExpenseBalance = (TextView) mRoot.findViewById(R.id.expense_balance);
        mExpenseBalanceLabel = (TextView) mRoot.findViewById(R.id.expense_balance_label);
        mDailySpendingText = (TextView) mRoot.findViewById(R.id.daily_spending_text);
        mNewTransactionsNumber = (TextView) mRoot.findViewById(R.id.new_transactions_number);
        mNewTransactionsLabel = (TextView) mRoot.findViewById(R.id.new_transactions_label);
        mTopCategoryIcon = (TextView) mRoot.findViewById(R.id.top_category_icon);
        mTopCategoryText = (TextView) mRoot.findViewById(R.id.top_category_text);
        mUncategorizedTransactionIcon = (TextView) mRoot.findViewById(R.id.uncategorized_transaction_icon);
        mUncategorizedTransactionText = (TextView) mRoot.findViewById(R.id.uncategorized_transaction_text);
    }

    private void applyFonts() {

        Fonts.applySecondaryItalicFont(mMainLabel, 8);
        Fonts.applyGlyphFont(mIncomeBalanceIcon, 20);
        Fonts.applyPrimarySemiBoldFont(mIncomeBalance, 20);
        Fonts.applySecondaryItalicFont(mIncomeBalanceLabel, 8);
        Fonts.applyGlyphFont(mExpenseBalanceIcon, 20);
        Fonts.applyPrimarySemiBoldFont(mExpenseBalance, 20);
        Fonts.applySecondaryItalicFont(mExpenseBalanceLabel, 8);
        Fonts.applySecondaryItalicFont(mDailySpendingText, 8);
        Fonts.applyPrimarySemiBoldFont(mNewTransactionsNumber, 20);
        Fonts.applySecondaryItalicFont(mNewTransactionsLabel, 8);
        Fonts.applyGlyphFont(mTopCategoryIcon, 20);
        Fonts.applySecondaryItalicFont(mTopCategoryText, 8);
        Fonts.applyPrimarySemiBoldFont(mUncategorizedTransactionIcon, 28);
        Fonts.applyPrimaryFont(mUncategorizedTransactionText, 8);
    }

    private void calculateSummaries() {

        mTopCategoryIcon.setText(Transactions.topCategory());

        mIncomeBalance.setText(mFormatter.format(Math.abs(Transactions.getIncomeTotal())));
        mExpenseBalance.setText(mFormatter.format(Math.abs(Transactions.getExpensesTotal())));
        mNewTransactionsNumber.setText(Integer.toString(Transactions.getProcessedTransactions()));
        mUncategorizedTransactionText.setText(getResources().getString(R.string.text_you_have) + " " +
                +Transactions.getUncategorizedTransactions() + " " + getResources().getString(R.string.text_uncat_trans));
    }

    private void setupBarGraphView() {

        mAdapter = new TransactionChartHandsetAdapter(mActivity);
        mBarChart.setAdapter(mAdapter);
        mBarChart.setShowPopup(false);
        mBarChart.setEnabled(false);

        mAdapter.selectReport(Enums.TransactionsReport.DAILY);
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
