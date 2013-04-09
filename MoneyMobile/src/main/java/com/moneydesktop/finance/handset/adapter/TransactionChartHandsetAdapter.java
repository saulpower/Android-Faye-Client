package main.java.com.moneydesktop.finance.handset.adapter;

import android.content.Context;
import android.view.View;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.database.Transactions;
import main.java.com.moneydesktop.finance.tablet.adapter.TransactionChartAdapter;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.views.barchart.BarView;
import main.java.com.moneydesktop.finance.views.barchart.BarViewModel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionChartHandsetAdapter extends TransactionChartAdapter {

    public final String TAG = this.getClass().getSimpleName();

    public TransactionChartHandsetAdapter(Context context) {
        super(context);

        mBarLabelColor = getColor(R.color.gray3);
        mBarColors = getContext().getResources().getColorStateList(R.drawable.gray1);
    }

    @Override
    public boolean isLongClickable(int position) {
        return false;
    }

    @Override
    public View configureBarView(BarView barView, int position) {

        BarViewModel model = getBarModel(position);

        barView.setMinBarHeight(mMinHeight);

        barView.setBarAmount(model.getAmount());
        barView.setBarColors(model.getColors());

        barView.setLabelText(model.getLabelText());
        barView.setLabelTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        barView.setLabelTextSize(Fonts.getPaintFontSize(6));
        barView.setLabelTextColor(mBarLabelColor);

        return barView;
    }

    /**
     * Take the data for the report and put it into a {@link main.java.com.moneydesktop.finance.views.barchart.BarViewModel} to aid in displaying
     * it in the bar chart view.
     *
     * @param popupFormat The date format for the popup view
     * @param labelFormat The date format for the label on the {@link main.java.com.moneydesktop.finance.views.barchart.BarView}
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
                    "",
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
}
