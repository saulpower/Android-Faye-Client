package com.moneydesktop.finance.views.barchart;

import com.moneydesktop.finance.views.BarView;

abstract public class BaseBarChartAdapter {
    public interface DataSetChangeListener {
        public void dataSetDidChange();
    }

    private DataSetChangeListener mDataSetChangeListener;

    public DataSetChangeListener getDataSetChangeListener() {
        return mDataSetChangeListener;
    }

    public void setDataSetChangeListener(DataSetChangeListener mDataSetChangeListener) {
        this.mDataSetChangeListener = mDataSetChangeListener;
    }

    public void notifyDataSetChange() {
        if (mDataSetChangeListener != null) {
            mDataSetChangeListener.dataSetDidChange();
        }
    }

    public abstract int getCount();

    public abstract Object getItem(int arg0);

    public abstract void getView(int position, BarView convertView);
}