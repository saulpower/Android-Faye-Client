package com.moneydesktop.finance.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.model.BarViewModel;

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
    public void setNewList(ArrayList<BarViewModel> l){
        mBarModelList = l;    
        notifyDataSetChange();
    }
    public void addBar(BarViewModel m){
        mBarModelList.add(m);
        notifyDataSetChange();
    }
    @Override
    public Object getItem(int arg0) {       
        return mBarModelList.get(arg0);
    }

    @Override
    public void getView(int position, BarView convertView) {
        // TODO Auto-generated method stub
        convertView.setScaleAmount((float) mBarModelList.get(position).mMaxAmount);
        convertView.setAmountAnimated((float) mBarModelList.get(position).mAmount);
        convertView.setLabelText(mBarModelList.get(position).mLabelText);
    }

}
