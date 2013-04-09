package main.java.com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums;
import main.java.com.moneydesktop.finance.data.Reports;
import main.java.com.moneydesktop.finance.database.BankAccountBalance;
import main.java.com.moneydesktop.finance.database.Transactions;
import main.java.com.moneydesktop.finance.util.DateUtil;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.barchart.BarView;
import main.java.com.moneydesktop.finance.views.barchart.BarViewModel;
import main.java.com.moneydesktop.finance.views.barchart.BaseBarAdapter;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kentandersen
 * Date: 3/21/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccountBalanceAdapter extends BaseBarAdapter {

    public final String TAG = this.getClass().getSimpleName();

    protected static final int MIN_HEIGHT = 2;
    protected String mBankAccountId;

    protected Enums.AccountBalanceReport mCurrentReport;
    protected Date mCurrentDate;
    protected int mCurrentCount;

    protected List<BarViewModel> mData;
    protected float mMax = 0f;
    protected int mMinHeight = 0;

    protected ColorStateList mBarColors;
    protected int mBarLabelColor = Color.WHITE;

    public AccountBalanceAdapter(Context context, String bankAccountID) {
        super(context);
        mMinHeight = (int) UiUtils.getDynamicPixels(context, MIN_HEIGHT);
        mBarLabelColor = getColor(R.color.gray5);
        mBarColors = getContext().getResources().getColorStateList(R.drawable.gray5_to_primary_text);
        mData = new ArrayList<BarViewModel>();
        mBankAccountId = bankAccountID;
    }

    @Override
    public boolean isLongClickable(int position) {
        return mCurrentReport != Enums.AccountBalanceReport.DAILY && getBarModel(position).getAmount() != 0;
    }

    public Enums.AccountBalanceReport getCurrentReport() {
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
     * Select a {@link Enums.TransactionsReport} to display data for
     * that report.
     *
     * @param report {@link Enums.TransactionsReport}
     */
    public void selectReport(Enums.AccountBalanceReport report) {

        if (getBarChart().isAnimating() || mCurrentReport == report) return;

        mCurrentReport = report;
        updateAdapter(true, true);
    }

    public void selectReport(Enums.AccountBalanceReport report, final String bankAccountId) {

        if (getBarChart().isAnimating() || mCurrentReport == report) return;

        mCurrentReport = report;
        mBankAccountId = bankAccountId;
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
        }
    }

    public void showDaily(boolean invalidate, boolean useDefaults) {

        if (useDefaults) {
            mCurrentDate = new Date();
            mCurrentCount = 60;
        }

        showDaily(invalidate, true, mCurrentDate, mCurrentCount);
    }

    private void showDaily(boolean invalidate, boolean report, Date date, int days) {

        mCurrentDate = date;
        mCurrentCount = days;

        String popupFormat = "M/d/yyyy";
        String labelFormat = "d";

        List<BankAccountBalance> data = Reports.getDailyBalanceTotalsForBankAccount(date, days, mBankAccountId);

        updateData(popupFormat, labelFormat, data, invalidate, report);
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
    protected void updateData(String popupFormat, String labelFormat, List<BankAccountBalance> data, boolean invalidate, boolean report) {

        mData.clear();

        BarViewModel temp;

        for (BankAccountBalance balances : data) {

            temp = new BarViewModel(mBarColors,
                    formatDate(labelFormat, balances.getDate()).toUpperCase(),
                    formatDate(popupFormat, balances.getDate()).toUpperCase(),
                    balances.getBalance(),
                    balances.getDate());

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

    protected String formatDate(String format, Date date) {

        SimpleDateFormat mFormat = new SimpleDateFormat(format);

        return mFormat.format(date);
    }
}
