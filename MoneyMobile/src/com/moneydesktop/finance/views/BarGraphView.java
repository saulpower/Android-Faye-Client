package com.moneydesktop.finance.views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;

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
        mBColor = getResources().getColor(R.color.gray3);
        mLabel = false;
        LayoutParams l = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        mBarContainer.setLayoutParams(l);
        mBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(mBarContainer);
        // TODO Auto-generated constructor stub
    }

    public void changeBarValue(int b, double v, boolean animate){
        if(animate){
        mViews.get(b).setAmountAnimated((float)v);
        }
        else{
            mViews.get(b).setAmount((float)v);
        }
    }
    public void changeBarColor(int b, int c, boolean animate){
        if(animate){
        mViews.get(b).setColorAnimated(c);
        }
        else{
            ((BarView) mBarContainer.getChildAt(b)).setBarColor(c);
        }
    }
    public void add(BarView b){
        mViews.add(b);
        mBarContainer.addView(b);
        layoutBars();
    }
    public void add(double value){
        BarView b = new BarView(getContext(),value,mGraphMax);
        b.setBarColor(mBColor);
        b.setColorAnimated(mBColor);
        mViews.add(b);
        mBarContainer.addView(b);
        layoutBars();
    }
    public void add(double value, String labelText){
        BarView b = new BarView(getContext(),labelText,value,mGraphMax);
        b.setBarColor(mBColor);
        mViews.add(b);
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
}
