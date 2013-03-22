package com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.barchart.BarViewTwo;
import com.moneydesktop.finance.views.barchart.BaseBarAdapter;

import java.util.Collections;
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

    public TransactionChartAdapter(Context context, List<BarViewModel> data) {
        super(context);

        mMinHeight = (int) UiUtils.getDynamicPixels(context, MIN_HEIGHT);

        mBarColor = getColor(R.color.gray4);

        mData = data;
        findMax();
    }

    public void reverse() {
        Collections.reverse(mData);
        notifyDataSetChanged();
    }

    private void findMax() {

        for (BarViewModel bar : mData) {

            if (bar.mAmount > mMax) {
                mMax = bar.mAmount;
            }
        }
    }

    @Override
    public View configureBarView(BarViewTwo barView, int position) {

        float amount = getAmount(position);

        barView.setMinBarHeight(mMinHeight);

        barView.setBarAmount(amount);
        barView.setBarColor(mBarColor);

        barView.setLabelText(position + "");
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
    public float getAmount(int position) {

        float amount = mData.get(position).mAmount;

        if (amount < 0f) {
            amount = 0f;
        }

        return amount;
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
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }
}
