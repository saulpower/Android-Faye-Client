package com.moneydesktop.finance.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.moneydesktop.finance.model.BarViewModel;

import java.util.ArrayList;
public class BarChartAdapter extends BaseAdapter{
private Context mContext;
    ArrayList<BarViewModel> mBarModelList;
    public BarChartAdapter(ArrayList<BarViewModel> l, Context c){
        super();
        mContext = c;
        mBarModelList = l;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mBarModelList.size();
    }

    @Override
    public Object getItem(int arg0) {
        
        return mBarModelList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        BarViewModel model = mBarModelList.get(position);
        BarView bar = new BarView(mContext,model.mLabelText,model.mAmount,model.mMaxAmount);
        
        return bar;
    }

}
