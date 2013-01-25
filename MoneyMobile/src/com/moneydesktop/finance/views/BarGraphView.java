package com.moneydesktop.finance.views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class BarGraphView extends RelativeLayout {
    ArrayList<BarView> mViews;
    LinearLayout mBarContainer;
    int mMargin;
    int mBColor;
    boolean mLabel;
    double mGraphMax;
    public BarGraphView(Context context, double max) {
        super(context);
        mViews = new ArrayList<BarView>();
        mBarContainer = new LinearLayout(getContext());
        mMargin = 1;
        mGraphMax = max;
        mLabel = false;
        LayoutParams l = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        mBarContainer.setLayoutParams(l);
        mBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        // TODO Auto-generated constructor stub
    }
    public void add(BarView b){
        mViews.add(b);
        layoutBars();
    }
    public void add(double value){
        BarView b = new BarView(getContext(),value,mGraphMax);
        mViews.add(b);
        layoutBars();
    }
    public void add(double value, String labelText){
        BarView b = new BarView(getContext(),labelText,value,mGraphMax);
        mViews.add(b);
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
        layoutBars();
    }
    private void layoutBars(){
        float weight = 1/mViews.size();
        for(int i = 0; i < mViews.size(); i++){
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,weight);
            layout.setMargins(mMargin, 0, mMargin, 0);
            mViews.get(i).setBarColor(mBColor);
            mViews.get(i).setLayoutParams(layout);
            mViews.get(i).showLabel(mLabel);
        }
        invalidate();
    }
}
