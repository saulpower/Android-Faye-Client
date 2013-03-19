package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.GraphBarTouchEvent;
import com.moneydesktop.finance.model.EventMessage.GraphDataZoomEvent;
import com.moneydesktop.finance.shared.adapter.BaseBarChartAdapter;
import com.moneydesktop.finance.shared.adapter.BasicBarChartAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.BarGraphView;
import com.moneydesktop.finance.views.UpArrowButton;
import de.greenrobot.event.EventBus;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TransactionsSummaryTabletFragment extends SummaryTabletFragment {
    BarGraphView mGraph;
    BasicBarChartAdapter mAdapter;
    TextView mFragmentLabel;
    int mCurrentStatus;
    UpArrowButton mDaily;
    UpArrowButton mMonthly;
    UpArrowButton mQuarterly;
    UpArrowButton mYearly;
    boolean mTransitioning;
    public static TransactionsSummaryTabletFragment newInstance(int position) {

        TransactionsSummaryTabletFragment frag = new TransactionsSummaryTabletFragment();

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
    public FragmentType getType() {
        return FragmentType.TRANSACTION_SUMMARY;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
        this.mActivity.onFragmentAttached(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater
                .inflate(R.layout.tablet_transaction_summary_view, null);

        setupViews();
        configureView();
        mTransitioning = false;
        mFragmentLabel = (TextView) mRoot
                .findViewById(R.id.tablet_transaction_summary_title);
        Fonts.applySecondaryItalicFont(mFragmentLabel, 12);
        mGraph = (BarGraphView) mRoot
                .findViewById(R.id.tablet_transaction_summary_graph);
        mAdapter = new BasicBarChartAdapter(new ArrayList());
        mGraph.setAdapter(mAdapter);
        mDaily = (UpArrowButton) mRoot
                .findViewById(R.id.tablet_transaction_daily_button);
        mDaily.setBackgroundColor(getResources().getColor(R.color.gray2));
        mDaily.setTextColor(getResources().getColor(R.color.white));
        Fonts.applyPrimaryFont(mDaily, 14);

        mDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTransitioning){
                    setGraphViewDaily(new Date());
                }
            }

        });

        mMonthly = (UpArrowButton) mRoot
                .findViewById(R.id.tablet_transaction_monthly_button);
        mMonthly.setBackgroundColor(getResources().getColor(
                R.color.primaryColor));
        mMonthly.setTextColor(getResources().getColor(R.color.white));
        Fonts.applyPrimaryFont(mMonthly, 14);

        mMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTransitioning){
                    setGraphViewMonthly(new Date());
                }
            }

        });

        mQuarterly = (UpArrowButton) mRoot
                .findViewById(R.id.tablet_transaction_quarterly_button);
        mQuarterly.setBackgroundColor(getResources().getColor(R.color.gray2));
        mQuarterly.setTextColor(getResources().getColor(R.color.white));
        Fonts.applyPrimaryFont(mQuarterly, 14);

        mQuarterly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTransitioning){
                    setGraphViewQuarterly(new Date());
                }
            }

        });

        mYearly = (UpArrowButton) mRoot
                .findViewById(R.id.tablet_transaction_yearly_button);
        mYearly.setBackgroundColor(getResources().getColor(R.color.gray2));
        mYearly.setTextColor(getResources().getColor(R.color.white));
        Fonts.applyPrimaryFont(mYearly, 14);
        mYearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTransitioning){
                    setGraphViewYearly(new Date());
                }
            }

        });
        setGraphViewMonthly(new Date());
        return mRoot;
    }

    public void onEvent(GraphBarTouchEvent event) {
        mGraph.handleTransactionsTouch(event.getBar());
    }

    public void onEvent(GraphDataZoomEvent event) {
        Calendar c = Calendar.getInstance();
        switch (mCurrentStatus) {
            case Constant.DAILY_VIEW:
                break;
            case Constant.MONTHLY_VIEW:
                mGraph.dismissPopup();
                c.setTimeInMillis(event.getDate());
                c.set(c.DAY_OF_MONTH, c.getActualMaximum(c.DAY_OF_MONTH));
                this.setGraphViewDaily(c.getTime());
                break;
            case Constant.QUARTERLY_VIEW:
                mGraph.dismissPopup();
                this.setGraphViewMonthly(new Date(event.getDate()));
                break;
            case Constant.YEARLY_VIEW:
                mGraph.dismissPopup();

                c.setTimeInMillis(event.getDate());
                Calendar now = Calendar.getInstance();
                if (c.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                    this.setGraphViewQuarterly(new Date(event.getDate()));
                } else {
                    c.set(Calendar.DAY_OF_YEAR,
                            c.getActualMaximum(Calendar.DAY_OF_YEAR));
                    this.setGraphViewQuarterly(c.getTime());
                }
                break;
        }
    }
    public void onEvent(EventMessage.DatabaseSaveEvent event){
        if(event.getChangedClassesList().contains(Transactions.class)){
            Log.d("databaseRefresh","Calling databaseSaveEvent for transactions");
        }
    }
    /**
     * Changes all of the button colors back to gray2.
     */
    private void clearButtonColors() {
        mDaily.setBackgroundColor(getResources().getColor(R.color.gray2));
        mDaily.showArrow(false);
        mMonthly.setBackgroundColor(getResources().getColor(R.color.gray2));
        mMonthly.showArrow(false);
        mQuarterly.setBackgroundColor(getResources().getColor(R.color.gray2));
        mQuarterly.showArrow(false);
        mYearly.setBackgroundColor(getResources().getColor(R.color.gray2));
        mYearly.showArrow(false);
    }
    /**
     * Displays the expense transaction totals for the last 30 days of transactions
     *
     * @param  end the Date to count back from
     */
    private void setGraphViewDaily(Date end) {
        clearButtonColors();
        mDaily.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        mDaily.showArrow(true);
        setLabelText(getResources().getString(
                R.string.spending_daily));
        List<Double[]> data = Transactions.get30DayExpenseTotals(end);
        double max = 0;
        for (int counter = 0; counter < data.size(); counter++) {
            if (data.get(counter)[1] > max) {
                max = data.get(counter)[1];
            }
        }
        mCurrentStatus = Constant.DAILY_VIEW;
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat formatPopup = new SimpleDateFormat("M/d/yyyy");
        SimpleDateFormat formatLabel = new SimpleDateFormat("d");
        for (int c = 0; c < data.size(); c++) {
            StringBuffer label = new StringBuffer();
            label = formatLabel
                    .format(data.get(c)[0], label, new FieldPosition(0));
            StringBuffer popup = new StringBuffer();
            popup = formatPopup.format(data.get(c)[0], popup, new FieldPosition(0));
            double time = data.get(c)[0];
            barList.add(new BarViewModel(label.toString(), popup.toString(), data.get(c)[1],
                    max, (long) time));
        }
        mAdapter.setNewList(barList);
        mGraph.setMargin((int) UiUtils.getDynamicPixels(getActivity(), 3));
        mGraph.setMax(max);
        mGraph.setLabel(true);
        mTransitioning = true;
        final Handler handler = new Handler();
        if(mGraph.getBarCount() > 0)  {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mGraph.handleTransactionsTouch(mGraph.getBar(mGraph.getBarCount() - 1));
                    mTransitioning = false;
                }

            }, 1100);
        }
    }

    private void setGraphViewMonthly(Date end) {
        clearButtonColors();
        mMonthly.showArrow(true);
        mMonthly.setBackgroundColor(getResources().getColor(
                R.color.primaryColor));
        setLabelText(getResources().getString(
                R.string.spending_monthly));
        List<Double[]> data = Transactions.getMonthlyExpenseTotals(end);
        double max = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[1] > max) {
                max = data.get(i)[1];
            }
        }
        mCurrentStatus = Constant.MONTHLY_VIEW;
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("MMM yyyy");
        for (int c = 0; c < data.size(); c++) {
            StringBuffer date = new StringBuffer();
            date = format
                    .format(data.get(c)[0], date, new FieldPosition(0));
            double time = data.get(c)[0];
            barList.add(new BarViewModel(date.toString().toUpperCase(), date.toString().toUpperCase(),
                    data.get(c)[1], max, (long) time));
        }
        mAdapter.setNewList(barList);
        mGraph.setMax(max);
        mGraph.setLabel(true);
        mGraph.setMargin((int) UiUtils.getDynamicPixels(getActivity(), 3));
        final Handler handler = new Handler();
        mTransitioning = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mGraph.getBarCount() > 0) {
                    mGraph.handleTransactionsTouch(mGraph.getBar(mGraph.getBarCount() - 1));
                }
                mTransitioning = false;
            }
        }, 1000);

    }
    /**
     * Returns the expense transaction totals for the last 4 quarters of transactions
     *
     * @param  end the Date to count back from
     */
    private void setGraphViewQuarterly(Date end) {
        clearButtonColors();
        mQuarterly.showArrow(true);
        mQuarterly.setBackgroundColor(getResources().getColor(
                R.color.primaryColor));
        setLabelText(getResources().getString(
                R.string.spending_quarterly));
        List<Double[]> data = Transactions.getQuarterlyExpenseTotals(end);
        double max = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[1] > max) {
                max = data.get(i)[1];
            }
        }
        mCurrentStatus = Constant.QUARTERLY_VIEW;
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        for (int c = 0; c < data.size(); c++) {
            StringBuffer date = new StringBuffer();
            date = format
                    .format(data.get(c)[0], date, new FieldPosition(0));
            int quarter = (int) (double) data.get(c)[2];
            String addVal = getResources().getString(R.string.quarter)
                    + ": " + Integer.toString(quarter) + ", "
                    + date.toString();
            double time = data.get(c)[0];
            barList.add(new BarViewModel(addVal, addVal, data.get(c)[1], max,
                    (long) time));
        }
        mAdapter.setNewList(barList);
        mGraph.setMax(max);
        mGraph.setLabel(true);
        mGraph.setMargin((int) UiUtils.getDynamicPixels(getActivity(), 3));
        if(mGraph.getBarCount() > 0)  {
            final Handler handler = new Handler();
            mTransitioning = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTransitioning = false;
                    //The quarterly view takes a bit more time to initialize because of the multiple DB lookups,
                    //so we're giving it a little extra time.
                    mGraph.handleTransactionsTouch(mGraph.getBar(mGraph.getBarCount() - 1));
                }

            }, 1100);
        }
    }
    /**
     * Returns the expense transaction totals for the last 2 years of transactions
     *
     * @param  end the Date to count back from
     */
    private void setGraphViewYearly(Date end) {
        clearButtonColors();
        mYearly.showArrow(true);
        mYearly.setBackgroundColor(getResources()
                .getColor(R.color.primaryColor));
        setLabelText(getResources().getString(
                R.string.spending_yearly));
        List<Double[]> data = Transactions.getYearlyExpenseTotals(end);
        double max = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[1] > max) {
                max = data.get(i)[1];
            }
        }
        mCurrentStatus = Constant.YEARLY_VIEW;
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        for (int c = 0; c < data.size(); c++) {
            if (data.get(c)[1] > 0) {
                StringBuffer date = new StringBuffer();
                date = format
                        .format(data.get(c)[0], date, new FieldPosition(0));
                double time = data.get(c)[0];
                barList.add(new BarViewModel(date.toString(), date.toString(), data.get(c)[1],
                        max, (long) time));
            }
        }
        mAdapter.setNewList(barList);
        mGraph.setMax(max);
        mGraph.setLabel(true);
        final Handler handler = new Handler();
        if(mGraph.getBarCount() > 0)  {
            mTransitioning = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGraph.handleTransactionsTouch(mGraph.getBar(mGraph.getBarCount() - 1));
                    mTransitioning = false;
                }}, 1000);
        }
    }
    /**
     * Animates a change in the label at the top of the view.
     *
     * @param  newText the string that the label text should be changed to.
     */
    private void setLabelText(final String newText) {
        Animation outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.out_right_fade);
        final Animation inAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.in_left);
        outAnim.setFillAfter(true);
        final Handler handler = new Handler();
        mFragmentLabel.startAnimation(outAnim);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFragmentLabel.setText(newText);
                mFragmentLabel.setVisibility(View.VISIBLE);
                mFragmentLabel.startAnimation(inAnim);
            }}, 600);
    }

}