package com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.barchart.BarViewTwo;
import com.moneydesktop.finance.views.barchart.BaseBarAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

    private List<BarViewModel> mData;
    private float mMax = 0f;
    private int mMinHeight = 0;

    private int mBarColor = Color.BLUE;

    public TransactionChartAdapter(Context context) {
        super(context);

        mMinHeight = (int) UiUtils.getDynamicPixels(context, MIN_HEIGHT);

        mBarColor = getColor(R.color.gray4);

        mData = new ArrayList<BarViewModel>();
    }

    public void reverse() {
        Collections.reverse(mData);
        notifyDataSetChanged();
    }

    public void reverse2() {
        Collections.reverse(mData);
        notifyDataSetInvalidated();
    }

    private void findMax() {

        for (BarViewModel bar : mData) {

            if (bar.getAmount() > mMax) {
                mMax = bar.getAmount();
            }
        }
    }

    @Override
    public View configureBarView(BarViewTwo barView, int position) {

        BarViewModel model = getBarModel(position);

        barView.setMinBarHeight(mMinHeight);

        barView.setBarAmount(model.getAmount());
        barView.setBarColor(model.getColor());

        barView.setLabelText(model.getLabelText());
        barView.setLabelTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        barView.setLabelTextSize(Fonts.getFontSize(16));
        barView.setLabelTextColor(mBarColor);

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

    public void showDaily() {

        String popupFormat = "M/d/yyyy";
        String labelFormat = "d";

        mData.clear();

        List<Transactions> data = Transactions.getDailyExpenseTotals(30);

        BarViewModel temp;

        for (Transactions expense : data) {

            temp = new BarViewModel(mData.size(),
                    formatDate(labelFormat, expense.getDate()),
                    formatDate(popupFormat, expense.getDate()),
                    expense.getAmount(),
                    expense.getDate());

            mData.add(temp);
        }

        findMax();
        notifyDataSetInvalidated();
    }

    private String formatDate(String format, Date date) {

        SimpleDateFormat mFormat = new SimpleDateFormat(format);

        return mFormat.format(date);
    }
}
