package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.shared.fragment.TransactionsFragment;
import com.moneydesktop.finance.tablet.adapter.AccountBalanceAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.barchart.BarChartView;
import com.moneydesktop.finance.views.barchart.BarViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BankAccountBalanceHistoryFragment extends TransactionsFragment implements BarChartView.OnPopupClickListener {

	private BankAccount mBankAccount;
    private BarChartView mBarChart;
    private AccountBalanceAdapter mChartAdapter;
    private TextView mMessage;
    protected QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);

    @Override
	public String getFragmentTitle() {
		return mBankAccount.getAccountName() + " - Historical Balances";
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity.onFragmentAttached(this);
	}

    @Override
    protected Date getStartDate() {
        Calendar cal = new GregorianCalendar();
        Date today = new Date();
        cal.setTime(today);
        cal.add(Calendar.DATE, -60);
        Date startDate = cal.getTime();
        return startDate;
    }

    @Override
    protected Date getEndDate() {
        Date today = new Date();
        return today;
    }

    @Override
    protected Object getChildInstance() {
        return this;
    }

    @Override
	public boolean onBackPressed() {
		return false;
	}
	
	public static BankAccountBalanceHistoryFragment newInstance(Intent intent) {

        BankAccountBalanceHistoryFragment fragment = new BankAccountBalanceHistoryFragment();

        Long accountId = intent.getExtras().getLong(Constant.EXTRA_ACCOUNT_ID);
		
        BankAccountDao bankAccountDAO = ApplicationContext.getDaoSession().getBankAccountDao();
        fragment.setbankAccount(bankAccountDAO.load(accountId));
		
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
	}

    public void setbankAccount(BankAccount bankAccount) {
        mBankAccount = bankAccount;
    }

    @Override
    public void isShowing() {
        super.isShowing();

        if (mLoaded) {
            mAdapter.refreshCurrentSelection();
        }
    }

    @Override
    public void isHiding() {
        mTransactionsList.setSelection(0);
    }

    @Override
    protected void setupView() {
        super.setupView();

        mTransactionsList.setPinnedHeaderView(mActivity.getLayoutInflater().inflate(R.layout.handset_item_transaction_header, mTransactionsList, false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_bank_account_balance_history, null);
        mBarChart = (BarChartView) mRoot.findViewById(R.id.tablet_bank_account_balance_chart);
        mMessage = (TextView) mRoot.findViewById(R.id.no_historical_data_available);

        mBankAccountID = mBankAccount.getId().toString();

        mIsSearchEnabled = false;

        setupGraph();

        return mRoot;
    }

    private void setupGraph() {

        mBarChart.setOnPopupClickListener(this);

        mChartAdapter = new AccountBalanceAdapter(mActivity, mBankAccountID);
        mBarChart.setAdapter(mChartAdapter);

        selectReport(Enums.AccountBalanceReport.DAILY);

        mBarChart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BarViewModel barModel = mChartAdapter.getBarModel(position);
                Date selectedDate = barModel.getDate();

                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                String barDate = formatter.format(selectedDate);

                //We don't have easy access to know what transaction position corresponds to the barview position, so we had to do some nested looping to find matches on dates.
                for (int i = 0; i < mAdapter.getSections().length; i++) {
                    String titleText = (String)(mAdapter.getSections()[i]);
                    if (titleText.equals(barDate)) {

                        for (int x = 0; x < mAdapter.getCount(); x++) {

                            Transactions transactions = mAdapter.getItem(x);

                            Date transDate = transactions.getDate();
                            String transactionDate = formatter.format(transDate);

                            if (transactionDate.equals(barDate)) {
                                mTransactionsList.smoothScrollToPosition(x);
                                break;
                            }

                        }
                    }
                }
            }
        });

        if (mChartAdapter.getCount() == 0) {
            mMessage.setVisibility(View.VISIBLE);

            Fonts.applySecondaryItalicFont(mMessage, 18);

            mBarChart.setVisibility(View.INVISIBLE);
        }
    }

    private void selectReport(Enums.AccountBalanceReport report) {

        if (mBarChart.isAnimating() || mChartAdapter.getCurrentReport() == report) return;

        mChartAdapter.selectReport(report, mBankAccountID);
    }


	@Override
	public FragmentType getType() {
		return null;
	}

    @Override
    public void onPopupClicked() {
    }

    @Override
    public void dataLoaded(boolean invalidate) {
        mLoaded = true;
        mAdapter.applyNewData();
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//    }
}
