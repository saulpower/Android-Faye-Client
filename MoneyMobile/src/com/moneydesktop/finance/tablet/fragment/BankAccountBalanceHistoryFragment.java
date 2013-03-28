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
import com.moneydesktop.finance.views.barchart.BarChartView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BankAccountBalanceHistoryFragment extends TransactionsFragment implements BarChartView.OnPopupClickListener, AdapterView.OnItemClickListener {

	private static BankAccount mBankAccount;
    private BarChartView mBarChart;
    private AccountBalanceAdapter mChartAdapter;
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
        cal.add(Calendar.DATE, -30);
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

        String accountId = intent.getExtras().getString(Constant.EXTRA_ACCOUNT_ID);
		
        BankAccountDao bankAccountDAO = ApplicationContext.getDaoSession().getBankAccountDao();
        mBankAccount = bankAccountDAO.load(Long.valueOf(accountId.hashCode()));
		
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
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
        mTransactionsList.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_bank_account_balance_history, null);
        mBarChart = (BarChartView) mRoot.findViewById(R.id.tablet_bank_account_balance_chart);

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

                //NOTE: Since one object is in ASC order, and the other DESC order, we have to reverse to position clicked in order to get the transaction list to animate in the correct direction
                mTransactionsList.smoothScrollToPosition(mBarChart.getAdapter().getCount() - position);
            }
        });
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
        Toast.makeText(mActivity, "popup clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void dataLoaded(boolean invalidate) {
        mLoaded = true;
        mAdapter.applyNewData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(mActivity, "transaction item clicked", Toast.LENGTH_SHORT).show();
       // ((TransactionsAdapter)parent).get

        mBarChart.setSelection(position);
    }
}
