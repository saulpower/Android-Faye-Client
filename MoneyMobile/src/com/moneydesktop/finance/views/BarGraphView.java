package com.moneydesktop.finance.views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.views.BaseBarChartAdapter.DataSetChangeListener;

import java.util.ArrayList;
public class BarGraphView extends RelativeLayout implements DataSetChangeListener {
    LinearLayout mBarContainer;
    int mMargin;
    int mBColor;
    BaseBarChartAdapter mAdapter;
    boolean mLabel;
    double mGraphMax;
    
    public BarGraphView(Context context, double max, BaseBarChartAdapter adapter) {
        super(context);
        mBarContainer = new LinearLayout(getContext());
        mMargin = 1;
        mGraphMax = max;
        mBColor = getResources().getColor(R.color.gray3);
        mLabel = false;
        LayoutParams l = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        mBarContainer.setLayoutParams(l);
        mBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(mBarContainer);
        mAdapter = adapter;
        mAdapter.setDataSetChangeListener(this);
        dataSetDidChange();
        // TODO Auto-generated constructor stub
    }

    public void changeBarValue(int b, double v, boolean animate){
        if(animate){
        ((BarView) mBarContainer.getChildAt(b)).setAmountAnimated((float)v);
        }
        else{
            ((BarView) mBarContainer.getChildAt(b)).setAmount((float)v);
        }
    }
    public void changeBarColor(int b, int c, boolean animate){
        if(animate){
        ((BarView) mBarContainer.getChildAt(b)).setColorAnimated(c);
        }
        else{
            ((BarView) mBarContainer.getChildAt(b)).setBarColor(c);
        }
    }
    public void add(BarView b){
        mBarContainer.addView(b);
        layoutBars();
    }
    public void add(double value){
        BarView b = new BarView(getContext(),value,mGraphMax);
        b.setBarColor(mBColor);
        b.setColorAnimated(mBColor);
        mBarContainer.addView(b);
        layoutBars();
    }
    public void add(double value, String labelText){
        BarView b = new BarView(getContext(),labelText,value,mGraphMax);
        b.setBarColor(mBColor);
        mBarContainer.addView(b);
        layoutBars();
    }
    public void setLabel(boolean label){
        mLabel = label;
        layoutBars();
    }
    public void setMargin(int m){
        mMargin = m;
        layoutBars();
    }
    public void changeBarColor(int c){
        mBColor = c;
        for(int i = 0; i < mBarContainer.getChildCount(); i++){
            ((BarView) mBarContainer.getChildAt(i)).setBarColor(mBColor);
        }
        layoutBars();
    }
    public void removeBar(int b){
        mBarContainer.removeViewAt(b);
        layoutBars();
    }
    private void layoutBars(){
        float weight = (float)1/(float)(mBarContainer.getChildCount()+1);
        for(int i = 0; i < mBarContainer.getChildCount(); i++){
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(0,LayoutParams.FILL_PARENT,weight);
            layout.setMargins(mMargin, 0, mMargin, 0);          
            mBarContainer.getChildAt(i).setLayoutParams(layout);
            ((BarView) mBarContainer.getChildAt(i)).showLabel(mLabel);
        }
        invalidate();
    }

    @Override
    public void dataSetDidChange() {
        if(mBarContainer.getChildCount() == 0){
            for(int i = 0; i < mAdapter.getCount(); i++){
                mBarContainer.addView(new BarView(getContext()));
            }
        }
        else if(mAdapter.getCount() < mBarContainer.getChildCount()){
                mBarContainer.removeViews(0, (mBarContainer.getChildCount()-mAdapter.getCount()));
        }
        else if(mAdapter.getCount() > mBarContainer.getChildCount()){
            for(int i = 0; i < (mAdapter.getCount() - mBarContainer.getChildCount()); i++){
                mBarContainer.addView(new BarView(getContext()));
            }
        }
        for(int i = 0; i < mAdapter.getCount(); i++){
            mAdapter.getView(i,(BarView)mBarContainer.getChildAt(i));
        }
        layoutBars();                
    }
}
