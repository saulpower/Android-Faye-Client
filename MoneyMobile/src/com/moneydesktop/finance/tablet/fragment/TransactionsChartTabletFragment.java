package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.tablet.adapter.TransactionChartAdapter;
import com.moneydesktop.finance.views.UpArrowButton;
import com.moneydesktop.finance.views.barchart.BarChartView;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionsChartTabletFragment extends SummaryTabletFragment {

    private BarChartView mGraph;
    private TransactionChartAdapter mAdapter;

    private ArrayList<BarViewModel> mBarList;

    private UpArrowButton mDay;

    public static TransactionsChartTabletFragment newInstance(int position) {

        TransactionsChartTabletFragment frag = new TransactionsChartTabletFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public String getTitleText() {
        return getString(R.string.title_fragment_transaction_summary);
    }

    @Override
    public Enums.FragmentType getType() {
        return Enums.FragmentType.TRANSACTION_SUMMARY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.tablet_transaction_summary_view, null);

        setupViews();
        configureView();

        loadDaily();

        return mRoot;
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        mGraph = (BarChartView) mRoot.findViewById(R.id.bar_chart);
        mDay = (UpArrowButton) mRoot.findViewById(R.id.tablet_transaction_daily_button);

        mDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.reverse();
            }
        });
    }

    private void loadDaily() {

        mBarList = new ArrayList<BarViewModel>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        List<Double[]> data = Transactions.get30DayExpenseTotals(calendar.getTime());

        for (Double[] item : data) {
            mBarList.add(new BarViewModel(item[1]));
        }

        mAdapter = new TransactionChartAdapter(mActivity, mBarList);
        mGraph.setAdapter(mAdapter);
    }
}
