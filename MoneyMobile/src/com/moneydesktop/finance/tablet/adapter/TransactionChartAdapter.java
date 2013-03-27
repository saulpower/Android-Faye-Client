package com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.views.barchart.BarViewModel;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.barchart.BarView;
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

    private static final int MIN_HEIGHT = 2;

    private Enums.TransactionsReport mCurrentReport = Enums.TransactionsReport.MONTHLY;

    private List<BarViewModel> mData;
    private float mMax = 0f;
    private int mMinHeight = 0;

    private ColorStateList mBarColors;
    private int mBarLabelColor = Color.BLUE;

    public TransactionChartAdapter(Context context) {
        super(context);

        mMinHeight = (int) UiUtils.getDynamicPixels(context, MIN_HEIGHT);
        mBarLabelColor = getColor(R.color.gray5);
        mBarColors = getContext().getResources().getColorStateList(R.drawable.gray5_to_primary_text);
        mData = new ArrayList<BarViewModel>();
    }

    public Enums.TransactionsReport getCurrentReport() {
        return mCurrentReport;
    }

    /**
     * Find the maximum value in the data set
     */
    private void findMax() {

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
                showMonthly(false, false, date);
                break;
            case MONTHLY:
                mCurrentReport = Enums.TransactionsReport.DAILY;
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                showDaily(false, false, calendar.getTime());
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
        updateAdapter(true);
    }

    /**
     * Refresh the data for the currently selected report
     */
    public void refreshAdapterData() {
        updateAdapter(false);
    }

    private void updateAdapter(boolean invalidate) {

        switch (mCurrentReport) {
            case DAILY:
                showDaily(invalidate);
                break;
            case MONTHLY:
                showMonthly(invalidate);
                break;
            case QUARTERLY:
                showQuarterly(invalidate);
                break;
            case YEARLY:
                showYearly(invalidate);
                break;
        }
    }

    public void showDaily(boolean invalidate) {
        showDaily(invalidate, true, new Date());
    }

    private void showDaily(boolean invalidate, boolean report, Date date) {

        String popupFormat = "M/d/yyyy";
        String labelFormat = "d";

        List<Transactions> data = Transactions.getDailyExpenseTotals(date, 30);

        updateData(popupFormat, labelFormat, data, invalidate, report);
    }

    public void showMonthly(boolean invalidate) {
        showMonthly(invalidate, true, new Date());
    }

    private void showMonthly(boolean invalidate, boolean report, Date date) {

        String format = "MMM yyyy";

        List<Transactions> data = Transactions.getMonthlyExpenseTotals(date, 6);

        updateData(format, format, data, invalidate, report);
    }

    public void showQuarterly(boolean invalidate) {
        showQuarterly(invalidate, true, new Date());
    }

    private void showQuarterly(boolean invalidate, boolean report, Date date) {

        String format = "yyyy";

        List<Transactions> data = Transactions.getQuarterlyExpenseTotals(date, 4);

        updateData(format, format, data, invalidate, report);
    }

    public void showYearly(boolean invalidate) {
        showYearly(invalidate, true);
    }

    private void showYearly(boolean invalidate, boolean report) {

        String format = "yyyy";

        List<Transactions> data = Transactions.getYearlyExpenseTotals(2);

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
    private void updateData(String popupFormat, String labelFormat, List<Transactions> data, boolean invalidate, boolean report) {

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

    private String formatDate(String format, Date date, int quarter) {

        if (quarter != -1) {
            format = String.format("'%s:' '%d,' %s", getString(R.string.quarter), quarter, format);
        }

        SimpleDateFormat mFormat = new SimpleDateFormat(format);

        return mFormat.format(date);
    }
}
