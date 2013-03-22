package com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/21/13
 * Time: 2:21 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseBarAdapter extends BaseAdapter {

    private Context mContext;

    public BaseBarAdapter(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public int getColor(int colorResource) {
        return mContext.getResources().getColor(colorResource);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BarViewTwo barView = (BarViewTwo) convertView;

        if (barView == null) {
            barView = new BarViewTwo(mContext);
        }

        barView.setMaxAmount(getMaxAmount());

        return configureBarView(barView, position);
    }

    public abstract View configureBarView(BarViewTwo barView, int position);

    public abstract float getMaxAmount();
    public abstract float getAmount(int position);
}
