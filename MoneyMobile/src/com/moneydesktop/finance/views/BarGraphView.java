package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.views.BaseBarChartAdapter.DataSetChangeListener;
public class BarGraphView extends RelativeLayout implements DataSetChangeListener {
    LinearLayout mBarContainer;
    int mMargin;
    int mBColor;
    int mSelectedColor;
    int mSelectedBar;
    BaseBarChartAdapter mAdapter;
    boolean mLabel;
    float mFontSize;
    double mGraphMax;
    public BarGraphView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mSelectedBar = -1;
        mBarContainer = new LinearLayout(getContext());
        mMargin = 1;
        mBColor = getResources().getColor(R.color.gray3);
        mSelectedColor = getResources().getColor(R.color.primaryColor);
        mLabel = false;
        LayoutParams l = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        mBarContainer.setLayoutParams(l);
        mBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        this.setOnTouchListener(new OnTouchListener() {
            final Animation bounce = AnimationUtils.loadAnimation(getContext(), R.anim.scale_bounce);
            @Override
            public boolean onTouch(View v, MotionEvent m) {
            	
            	if (((BarGraphView)v).getGraphChildCount() == 0) {
            		return false;
            	}
            	
                int xSize = v.getWidth();
                double xIndex = xSize/((BarGraphView)v).getGraphChildCount();
                int barChosen = (int) (m.getX()/xIndex);
                if(mSelectedBar != -1){
                    BarView oldBar = (BarView) mBarContainer.getChildAt(mSelectedBar);
                    oldBar.setBarColor(mBColor);
                }
                mSelectedBar = barChosen;
                BarView manip = (BarView) mBarContainer.getChildAt(mSelectedBar);
                manip.setBarColor(mSelectedColor);
                manip.startAnimation(bounce);
                BarGraphPopUpView popup = new BarGraphPopUpView(v.getContext());
                popup.mTopLine.setText(manip.getLabel());
                popup.mMidLine.setText(Double.toString(manip.getAmount()));
                popup.mBottomLine.setText(getResources().getString(R.string.button_transactions));
                int pX = (int) (500);
                		//(int) (manip.getLeft()+(manip.getWidth()/2)-popup.getWidth()/2);
                int pY = 500;
                //(int) (manip.getHeight()+10+popup.getHeight());
                PopupWindowAtLocation p = new PopupWindowAtLocation(getContext(), BarGraphView.this, pX, pY, manip, popup);
                return true;
            }

        });
        this.addView(mBarContainer);
    }
    public int getGraphChildCount(){
        return mBarContainer.getChildCount();
    }
    public void setAdapter(BaseBarChartAdapter adapter){
        mAdapter = adapter;
        mAdapter.setDataSetChangeListener(this);
        dataSetDidChange();
    }
    public int getBarColor(){
        return mBColor;
    }
    public void setMax(double max){
        mGraphMax = max;
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
    public void setLabelFontSize(float fontSize){
        mFontSize = fontSize;
        for(int i = 0; i < mBarContainer.getChildCount(); i++){
            ((BarView) mBarContainer.getChildAt(i)).setTextSize(mFontSize);
        }
        layoutBars();
    }
    public void setIndividualBarColor(int b, int c){
        ((BarView) mBarContainer.getChildAt(b)).setBarColor(c);
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
            while(!(mBarContainer.getChildCount() == mAdapter.getCount())){
                mBarContainer.addView(new BarView(getContext()));
            }
        }
        for(int i = 0; i< mBarContainer.getChildCount(); i++){
            ((BarView) mBarContainer.getChildAt(i)).setAmount(0);
        }
        for(int i = 0; i < mAdapter.getCount(); i++){
            mAdapter.getView(i,(BarView)mBarContainer.getChildAt(i));
        }
        layoutBars();                
    }
}
