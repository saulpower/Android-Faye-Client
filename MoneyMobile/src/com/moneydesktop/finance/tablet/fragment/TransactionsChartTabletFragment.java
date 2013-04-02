package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.adapter.TransactionChartAdapter;
import com.moneydesktop.finance.util.DateRange;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.UpArrowButton;
import com.moneydesktop.finance.views.barchart.BarChartView;
import com.moneydesktop.finance.views.barchart.BarViewModel;
import de.greenrobot.event.EventBus;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionsChartTabletFragment extends SummaryTabletFragment implements BarChartView.OnDataShowingListener, ViewSwitcher.ViewFactory, BarChartView.OnPopupClickListener {

    private static final int TEXT_PADDING = 20;

    private BarChartView mBarChart;
    private TransactionChartAdapter mAdapter;

    private UpArrowButton mDay, mMonth, mQuarter, mYear;
    private TextSwitcher mTitle;

    private int mTextPadding = 0;

    private Handler mHandler;

    public static TransactionsChartTabletFragment newInstance(int position) {

        TransactionsChartTabletFragment frag = new TransactionsChartTabletFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);

        return frag;
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

    /**
     * Creates a new {@link android.view.View} to be added in a
     * {@link android.widget.ViewSwitcher}.
     *
     * @return a {@link android.view.View}
     */
    @Override
    public View makeView() {

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(mTextPadding, 0, 0, 0);

        TextView textView = new TextView(mActivity);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setTextColor(getResources().getColor(R.color.gray7));
        textView.setBackgroundColor(Color.TRANSPARENT);

        Fonts.applySecondaryItalicFont(textView, 12);

        return textView;
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

        mHandler = new Handler();

        mRoot = inflater.inflate(R.layout.tablet_transaction_summary_view, null);

        mTextPadding = (int) (UiUtils.getDynamicPixels(mActivity, TEXT_PADDING) + 0.5f);

        return mRoot;
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        mTitle = (TextSwitcher) mRoot.findViewById(R.id.transactions_title);
        mTitle.setFactory(this);
        mTitle.setInAnimation(mActivity, R.anim.in_left);
        mTitle.setOutAnimation(mActivity, R.anim.out_right_fade);

        mBarChart = (BarChartView) mRoot.findViewById(R.id.bar_chart);
        mDay = (UpArrowButton) mRoot.findViewById(R.id.tablet_transaction_daily_button);
        mMonth = (UpArrowButton) mRoot.findViewById(R.id.tablet_transaction_monthly_button);
        mQuarter = (UpArrowButton) mRoot.findViewById(R.id.tablet_transaction_quarterly_button);
        mYear = (UpArrowButton) mRoot.findViewById(R.id.tablet_transaction_yearly_button);

        mDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReport(v, Enums.TransactionsReport.DAILY);
            }
        });
        mMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReport(v, Enums.TransactionsReport.MONTHLY);
            }
        });
        mQuarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReport(v, Enums.TransactionsReport.QUARTERLY);
            }
        });
        mYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReport(v, Enums.TransactionsReport.YEARLY);
            }
        });

        setupGraph();
    }

    private void setupGraph() {

        mBarChart.setOnDataShowingListener(this);
        mBarChart.setOnPopupClickListener(this);

        mAdapter = new TransactionChartAdapter(mActivity);
        mBarChart.setAdapter(mAdapter);

        selectReport(mDay, Enums.TransactionsReport.DAILY);
        onDataShowing(false);
    }

    private void selectReport(View v, Enums.TransactionsReport report) {

        if (mBarChart.isAnimating() || mAdapter.getCurrentReport() == report) return;

        mAdapter.selectReport(report);
        setSelectedButton(v);
    }

    private void setSelectedButton(View selected) {

        clearButtons();
        selected.setSelected(true);
    }

    private void clearButtons() {
        mDay.setSelected(false);
        mMonth.setSelected(false);
        mQuarter.setSelected(false);
        mYear.setSelected(false);
    }

    @Override
    public void onDataShowing(boolean fromDrillDown) {

        if (fromDrillDown) clearButtons();

        String title = "";

        switch(mAdapter.getCurrentReport()) {
            case DAILY:
                title = getString(R.string.spending_daily);
                mDay.setSelected(true);
                break;
            case MONTHLY:
                title = getString(R.string.spending_monthly);
                mMonth.setSelected(true);
                break;
            case QUARTERLY:
                title = getString(R.string.spending_quarterly);
                mQuarter.setSelected(true);
                break;
            case YEARLY:
                title = getString(R.string.spending_yearly);
                mYear.setSelected(true);
                break;
        }

        TextView text = (TextView) mTitle.getCurrentView();

        if (!text.getText().toString().equals(title)) {
            mTitle.setText(title);
        }
    }

    public void onEvent(final EventMessage.DatabaseSaveEvent event) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (event.didDatabaseChange() && event.getChangedClassesList().contains(Transactions.class) && !mBarChart.isTransitioning() && !mBarChart.isUpdating()) {
                    mAdapter.refreshAdapterData();
                }
            }
        });
    }

    private DateRange getDateRange() {

        DateRange range = new DateRange();

        BarViewModel model = mAdapter.getBarModel(mBarChart.getSelection());

        switch(mAdapter.getCurrentReport()) {
            case DAILY:
                range.setDayRange(model.getDate(), 1);
                break;
            case MONTHLY:
                range.setMonthRange(model.getDate(), 1);
                break;
            case QUARTERLY:
                range.setQuarterRange(model.getDate(), 1);
                break;
            case YEARLY:
                range.setYearRange(model.getDate(), 1);
                break;
        }

        return range;
    }

    private void showTransactions() {

        BarViewModel model = mAdapter.getBarModel(mBarChart.getSelection());

        if (model.getAmount() == 0) return;

        DateRange range = getDateRange();

        Intent i = new Intent(mActivity, DropDownTabletActivity.class);
        i.putExtra(Constant.EXTRA_FRAGMENT, Enums.FragmentType.TRANSACTIONS_PAGE);
        i.putExtra(Constant.EXTRA_TXN_TYPE, Enums.TxFilter.ALL);
        i.putExtra(Constant.EXTRA_START_DATE, range.getStartDate().getTime());
        i.putExtra(Constant.EXTRA_END_DATE, range.getEndDate().getTime());

        mActivity.startActivity(i);
    }

    @Override
    public void onPopupClicked() {
        showTransactions();
    }
}
