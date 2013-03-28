package com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums;
import com.moneydesktop.finance.data.Reports;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.util.DateUtil;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.barchart.BarView;
import com.moneydesktop.finance.views.barchart.BarViewModel;
import com.moneydesktop.finance.views.barchart.BaseBarAdapter;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionChartAdapter extends BaseBarAdapter {

    public final String TAG = this.getClass().getSimpleName();

    protected static final int MIN_HEIGHT = 2;

    protected Enums.TransactionsReport mCurrentReport = Enums.TransactionsReport.MONTHLY;
    protected Date mCurrentDate;
    protected int mCurrentCount;

    protected List<BarViewModel> mData;
    protected float mMax = 0f;
    protected int mMinHeight = 0;

    protected ColorStateList mBarColors;
    protected int mBarLabelColor = Color.WHITE;

    public TransactionChartAdapter(Context context) {
        super(context);

        mMinHeight = (int) UiUtils.getDynamicPixels(context, MIN_HEIGHT);
        mBarLabelColor = getColor(R.color.gray5);
        mBarColors = getContext().getResources().getColorStateList(R.drawable.gray5_to_primary_text);
        mData = new ArrayList<BarViewModel>();
    }

    @Override
    public boolean isLongClickable(int position) {
        return mCurrentReport != Enums.TransactionsReport.DAILY && getBarModel(position).getAmount() != 0;
    }

    public Enums.TransactionsReport getCurrentReport() {
        return mCurrentReport;
    }

    /**
     * Find the maximum value in the data set
     */
    protected void findMax() {

        // We put a max of 1 so our max can never be zero
        mMax = 1f;

        for (BarViewModel bar : mData) {

            if (bar.getAmount() > mMax) {
                mMax = bar.getAmount();
            }
        }
    }

    @Override
    public View configureBarView(BarView barView, int position) {

        BarViewModel model = getBarModel(position);

        barView.setMinBarHeight(mMinHeight);

        barView.setBarAmount(model.getAmount());
        barView.setBarColors(model.getColors());

        barView.setLabelText(model.getLabelText());
        barView.setLabelTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        barView.setLabelTextSize(Fonts.getPaintFontSize(12));
        barView.setLabelTextColor(mBarLabelColor);

        return barView;
    }

    @Override
    public float getMaxAmount() {
        return mMax;
    }

    @Override
    public BarViewModel getBarModel(int position) {
        return mData.get(position);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mData.size();
    }

    /**
     * Drill down to the next level report (i.e. Quarterly -> Monthly) based
     * on the currently selected date.
     */
    public void drillDown() {

        Date date = getBarModel(getBarChart().getSelection()).getDate();
        Calendar calendar = DateUtils.toCalendar(date);

        switch (mCurrentReport) {

            case YEARLY:
                mCurrentReport = Enums.TransactionsReport.QUARTERLY;
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                showQuarterly(false, false, calendar.getTime());
                break;

            case QUARTERLY:
                mCurrentReport = Enums.TransactionsReport.MONTHLY;
                int quarter = DateUtil.getQuarterNumber(calendar);
                calendar.set(Calendar.MONTH, DateUtil.getQuarterStartMonth(quarter));
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, 2);
                showMonthly(false, false, calendar.getTime(), 3);
                break;

            case MONTHLY:
                mCurrentReport = Enums.TransactionsReport.DAILY;
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                showDaily(false, false, calendar.getTime(), calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1);
                break;
        }
    }

    /**
     * Provides the amount of rows and columns for the
     * drill down animation
     *
     * @return An int array of [rows, columns]
     */
    public int[] getDrillDownMatrix() {

        switch (mCurrentReport) {
            case YEARLY:
                return new int[] {2, 2};
            case QUARTERLY:
                return new int[] {3, 2};
            case MONTHLY:
                return new int[] {3, 11};
            default:
                return null;
        }
    }

    /**
     * Select a {@link Enums.TransactionsReport} to display data for
     * that report.
     *
     * @param report {@link Enums.TransactionsReport}
     */
    public void selectReport(Enums.TransactionsReport report) {

        if (getBarChart().isAnimating() || mCurrentReport == report) return;

        mCurrentReport = report;
        updateAdapter(true, true);
    }

    /**
     * Refresh the data for the currently selected report
     */
    public void refreshAdapterData() {
        updateAdapter(false, false);
    }

    private void updateAdapter(boolean invalidate, boolean useDefaults) {

        switch (mCurrentReport) {
            case DAILY:
                showDaily(invalidate, useDefaults);
                break;
            case MONTHLY:
                showMonthly(invalidate, useDefaults);
                break;
            case QUARTERLY:
                showQuarterly(invalidate, useDefaults);
                break;
            case YEARLY:
                showYearly(invalidate);
                break;
        }
    }

    public void showDaily(boolean invalidate, boolean useDefaults) {

        if (useDefaults) {
            mCurrentDate = new Date();
            mCurrentCount = 30;
        }

        showDaily(invalidate, true, mCurrentDate, mCurrentCount);
    }

    private void showDaily(boolean invalidate, boolean report, Date date, int days) {

        mCurrentDate = date;
        mCurrentCount = days;

        String popupFormat = "M/d/yyyy";
        String labelFormat = "d";

        List<Transactions> data = Reports.getDailyExpenseTotals(date, days);

        updateData(popupFormat, labelFormat, data, invalidate, report);
    }

    public void showMonthly(boolean invalidate, boolean useDefaults) {

        if (useDefaults) {
            mCurrentDate = new Date();
            mCurrentCount = 6;
        }

        showMonthly(invalidate, true, mCurrentDate, mCurrentCount);
    }

    private void showMonthly(boolean invalidate, boolean report, Date date, int months) {

        mCurrentDate = date;
        mCurrentCount = months;

        String format = "MMM yyyy";

        List<Transactions> data = Reports.getMonthlyExpenseTotals(date, months);

        updateData(format, format, data, invalidate, report);
    }

    public void showQuarterly(boolean invalidate, boolean useDefaults) {

        if (useDefaults) {
            mCurrentDate = new Date();
        }

        showQuarterly(invalidate, true, mCurrentDate);
    }

    private void showQuarterly(boolean invalidate, boolean report, Date date) {

        mCurrentDate = date;

        String format = "yyyy";

        List<Transactions> data = Reports.getQuarterlyExpenseTotals(date, 4);

        updateData(format, format, data, invalidate, report);
    }

    public void showYearly(boolean invalidate) {
        showYearly(invalidate, true);
    }

    private void showYearly(boolean invalidate, boolean report) {

        mCurrentDate = null;

        String format = "yyyy";

        List<Transactions> data = Reports.getYearlyExpenseTotals(2);

        updateData(format, format, data, invalidate, report);
    }

    /**
     * Take the data for the report and put it into a {@link BarViewModel} to aid in displaying
     * it in the bar chart view.
     *
     * @param popupFormat The date format for the popup view
     * @param labelFormat The date format for the label on the {@link BarView}
     * @param data The data for the given report
     * @param invalidate Should the adapter notify a data set invalidate or change
     * @param report Whether to report a data change at this time
     */
    protected void updateData(String popupFormat, String labelFormat, List<Transactions> data, boolean invalidate, boolean report) {

        mData.clear();

        BarViewModel temp;

        for (Transactions expense : data) {

            temp = new BarViewModel(mBarColors,
                    formatDate(labelFormat, expense.getDate(), expense.getQuarterNumber()).toUpperCase(),
                    formatDate(popupFormat, expense.getDate(), expense.getQuarterNumber()).toUpperCase(),
                    expense.getAmount(),
                    expense.getDate());

            mData.add(temp);
        }

        findMax();

        if (!report) return;

        if (invalidate) {
            notifyDataSetInvalidated();
        } else {
            notifyDataSetChanged();
        }
    }

    protected String formatDate(String format, Date date, int quarter) {

        if (quarter != -1) {
            format = String.format("'%s:' '%d,' %s", getString(R.string.quarter), quarter, format);
        }

        SimpleDateFormat mFormat = new SimpleDateFormat(format);

        return mFormat.format(date);
    }
}
