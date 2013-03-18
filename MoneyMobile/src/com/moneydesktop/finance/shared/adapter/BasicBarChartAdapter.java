package com.moneydesktop.finance.shared.adapter;

import android.content.Context;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.views.BarView;

import java.util.ArrayList;

public class BasicBarChartAdapter extends BaseBarChartAdapter {
    ArrayList<BarViewModel> mBarModelList;
    private Context mContext;

    public BasicBarChartAdapter(ArrayList<BarViewModel> l) {
        mBarModelList = l;
    }

    private DataSetChangeListener mDataSetChangeListener;

    @Override
    public int getCount() {
        return mBarModelList.size();
    }

    public void setNewList(ArrayList<BarViewModel> l) {
        mBarModelList = l;
        notifyDataSetChange();
    }

    public void addBar(BarViewModel m) {
        mBarModelList.add(m);
        notifyDataSetChange();
    }

    @Override
    public Object getItem(int arg0) {
        return mBarModelList.get(arg0);
    }

    @Override
    public void getView(int position, BarView convertView) {
        convertView.setScaleAmount((float) mBarModelList.get(position).mMaxAmount);
        convertView.setTime((long) mBarModelList.get(position).mDate);
        convertView.setLabelText(mBarModelList.get(position).mLabelText);
        convertView.setPopupText(mBarModelList.get(position).mPopupText);
        convertView.setAmountAnimated((float) mBarModelList.get(position).mAmount);

    }

}