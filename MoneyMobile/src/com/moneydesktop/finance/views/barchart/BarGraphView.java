package com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.views.barchart.BaseBarChartAdapter.DataSetChangeListener;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.BarView;
import com.moneydesktop.finance.views.PopupWindowAtLocation;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.NumberFormat;
import java.util.Date;

public class BarGraphView extends RelativeLayout implements DataSetChangeListener {

    LinearLayout mBarContainer;
    int mMargin;
    int mBColor;
    int mSelectedColor;
    Rect mLabelBg;
    Paint mLabelBgPaint;
    PopupWindowAtLocation mPopup;
    BaseBarChartAdapter mAdapter;
    boolean mLabel;
    float mFontSize;
    double mGraphMax;
    private BarView mSelectedBar;

    public BarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBarContainer = new LinearLayout(getContext());
        mMargin = 1;
        mBColor = getResources().getColor(R.color.gray4);
        mLabelBg = new Rect(this.getLeft(), (int) (this.getBottom() + (this.getTop() * .10)), this.getRight(), this.getBottom());
        mLabelBgPaint = new Paint();
        mLabelBgPaint.setColor(getResources().getColor(R.color.white));
        mLabelBgPaint.setStyle(Style.FILL);

        mSelectedColor = getResources().getColor(R.color.primaryColor);
        mLabel = false;
        LayoutParams l = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mBarContainer.setLayoutParams(l);
        mBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(mBarContainer);
    }

    @Override
    public void dataSetDidChange() {
        if (mPopup != null) {
            mPopup.fadeOutTransparency();
        }
        mSelectedBar = null;
        for (int i = 0; i < mBarContainer.getChildCount(); i++) {
            ((BarView) mBarContainer.getChildAt(i)).setAmountAnimated(0);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBarContainer.setVisibility(INVISIBLE);
            }
        }, 600);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBarContainer.setVisibility(INVISIBLE);
                mBarContainer.setVisibility(VISIBLE);
                if (mBarContainer.getChildCount() == 0) {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        mBarContainer.addView(new BarView(getContext()));
                    }
                } else if (mAdapter.getCount() < mBarContainer.getChildCount()) {
                    mBarContainer.removeViews(0,
                            (mBarContainer.getChildCount() - mAdapter.getCount()));
                } else if (mAdapter.getCount() > mBarContainer.getChildCount()) {
                    while (!(mBarContainer.getChildCount() == mAdapter.getCount())) {
                        mBarContainer.addView(new BarView(getContext()));
                    }
                }

                for (int i = 0; i < mAdapter.getCount(); i++) {
                    mAdapter.getView(i, (BarView) mBarContainer.getChildAt(i));
                }

                if (mPopup != null) {
                    mPopup.fadeOutTransparency();
                }
                layoutBars();
            }
        }, 650);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLabel == true) {
            mLabelBg.set(this.getLeft() - 50, (int) (this.getBottom() - (this.getHeight() * .26)), this.getRight(), this.getBottom());
            canvas.drawRect(mLabelBg, mLabelBgPaint);
        }
    }

    public void dismissPopup() {
        mPopup.setVisibility(GONE);
    }

    public boolean handleTransactionsTouch(BarView touchedBar) {
        final Animation bounce = AnimationUtils.loadAnimation(getContext(),
                R.anim.scale_bounce);
        bounce.setDuration(100);
        if (this.getGraphChildCount() == 0) {
            return true;
        }
        changeBarColor(mBColor);
        Boolean sameBar = false;
        if(mSelectedBar == touchedBar){
             sameBar = true;
        }
        mSelectedBar = touchedBar;
        mSelectedBar.setBarColor(mSelectedColor);
        mSelectedBar.startAnimation(bounce);
        if(!sameBar){
            final int index = mBarContainer.indexOfChild(mSelectedBar);
            int pX = (int) ((mSelectedBar.getLeft() + mSelectedBar.getWidth() / 2) - ((UiUtils
                    .convertDpToPixel(240, getContext()) / 2)));
            int pY = (int) (barActualHeight(mSelectedBar) - (UiUtils.convertDpToPixel(110,
                    getContext())));
            if(touchedBar.getRect() != null){
            BarGraphPopUpView popup = new BarGraphPopUpView(getContext(), touchedBar.getLeft() + ((touchedBar.getRect().left + touchedBar.getRect().right) / 2) - 20, touchedBar.getRect().top - 50);

            popup.mTopLine.setText(mSelectedBar.getPopupText());
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            String amountString = formatter.format(mSelectedBar.getAmount());
            popup.mMidLine.setText(amountString);
            popup.mBottomLine.setText(getResources().getString(
                    R.string.button_transactions));
            if (mPopup != null) {
                mPopup.fadeOutTransparency();
            }
            mPopup = new PopupWindowAtLocation(getContext(), BarGraphView.this, pX,
                    pY, mSelectedBar, popup);
            mPopup.setVisibility(VISIBLE);
            popup.mLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) { // View All transactions

                    Intent clickIntent = new Intent(getContext(),
                            DropDownTabletActivity.class);
                    clickIntent.putExtra(Constant.EXTRA_FRAGMENT,
                            FragmentType.TRANSACTIONS_PAGE);
                    clickIntent.putExtra(Constant.EXTRA_TXN_TYPE, TxFilter.ALL);
                    clickIntent.putExtra(Constant.EXTRA_START_TIME, mSelectedBar.getTime());

                    if (index + 1 >= mBarContainer.getChildCount()) {
                        clickIntent.putExtra(Constant.EXTRA_END_TIME, new Date());
                    } else {
                        clickIntent.putExtra(Constant.EXTRA_END_TIME,
                                ((BarView) mBarContainer.getChildAt(index + 1))
                                        .getTime());
                    }

                    getContext().startActivity(clickIntent);
                }
            });
            }
        }


        return true;
    }

    public BarView getBar(int index) {
        return (BarView) mBarContainer.getChildAt(index);
    }

    public int getBarCount() {
        return mBarContainer.getChildCount();
    }

    private int barActualHeight(BarView v) {

        Rect bar = v.getRect();
        int retVal;
        if(bar != null) {
            retVal = bar.top - 20;
        }
        else{
            retVal = this.getBottom();
        }
        return retVal;
    }

    public int getGraphChildCount() {
        return mBarContainer.getChildCount();
    }

    public void setAdapter(BaseBarChartAdapter adapter) {
        mAdapter = adapter;
        mAdapter.setDataSetChangeListener(this);
        dataSetDidChange();
    }

    public int getBarColor() {
        return mBColor;
    }

    public void setMax(double max) {
        mGraphMax = max;
    }

    public void changeBarValue(int barIndex, double value, boolean animate) {
        if (animate) {
            ((BarView) mBarContainer.getChildAt(barIndex))
                    .setAmountAnimated((float) value);
        } else {
            ((BarView) mBarContainer.getChildAt(barIndex)).setAmount((float) value);
        }
    }

    public void changeBarColor(int barIndex, int color, boolean animate) {
        if (animate) {
            ((BarView) mBarContainer.getChildAt(barIndex)).setColorAnimated(color);
        } else {
            ((BarView) mBarContainer.getChildAt(barIndex)).setBarColor(color);
        }
    }

    public void add(BarView b) {
        mBarContainer.addView(b);
        layoutBars();
    }

    public void add(double value) {
        BarView b = new BarView(getContext(), value, mGraphMax);
        b.setBarColor(mBColor);
        b.setColorAnimated(mBColor);
        mBarContainer.addView(b);
        layoutBars();
    }

    public void add(double value, String labelText) {
        BarView b = new BarView(getContext(), labelText, value, mGraphMax);
        b.setBarColor(mBColor);
        mBarContainer.addView(b);
        layoutBars();
    }

    public void setLabel(boolean label) {
        mLabel = label;
        layoutBars();
    }

    public void setLabelFontSize(float fontSize) {
        mFontSize = fontSize;
        for (int i = 0; i < mBarContainer.getChildCount(); i++) {
            ((BarView) mBarContainer.getChildAt(i)).setTextSize(mFontSize);
        }
        layoutBars();
    }

    public void setIndividualBarColor(int barIndex, int color) {
        ((BarView) mBarContainer.getChildAt(barIndex)).setBarColor(color);
    }

    public void setMargin(int m) {
        mMargin = m;
        layoutBars();
    }

    public void changeBarColor(int c) {
        mBColor = c;
        for (int i = 0; i < mBarContainer.getChildCount(); i++) {
            ((BarView) mBarContainer.getChildAt(i)).setBarColor(mBColor);
        }
        layoutBars();
    }

    public void removeBar(int b) {
        mBarContainer.removeViewAt(b);
        layoutBars();
    }

    public void animateBarsOutZoomIn() {
        if (mBarContainer.getChildCount() == 0) {
            return;
        } else {
            int timing = 500 / mBarContainer.getChildCount();
            for (int counter = 0; counter < mBarContainer.getChildCount(); counter++) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mBarContainer.getChildAt(counter), "ScaleX", 1, 2);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mBarContainer.getChildAt(counter), "ScaleY", 1, 2);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(mBarContainer.getChildAt(counter), "Alpha", 1, 0);
                scaleX.setStartDelay(counter * timing);
                scaleY.setStartDelay(counter * timing);
                alpha.setStartDelay(counter * timing);
                scaleX.setDuration(500);
                scaleY.setDuration(500);
                alpha.setDuration(500);
                scaleX.start();
                scaleY.start();
                alpha.start();
            }
        }
    }

    public void animateBarsOutZoomOut() {
        if (mBarContainer.getChildCount() == 0) {
            return;
        } else {
            int timing = 300 / mBarContainer.getChildCount();
            for (int counter = 0; counter < mBarContainer.getChildCount(); counter++) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mBarContainer.getChildAt(counter), "ScaleX", (float) .5);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mBarContainer.getChildAt(counter), "ScaleY", (float) .5);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(mBarContainer.getChildAt(counter), "Alpha", 0);
                scaleX.setStartDelay(counter * timing);
                scaleY.setStartDelay(counter * timing);
                alpha.setStartDelay(counter * timing);
                scaleX.setDuration(500);
                scaleY.setDuration(500);
                alpha.setDuration(500);
                scaleX.start();
                scaleY.start();
                alpha.start();
            }
        }
    }

    public void animateBarsInZoomOut() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mBarContainer, "ScaleX", (float) 1.5, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mBarContainer, "ScaleY", (float) 1.5, 1);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.setStartDelay(400);
        scaleY.setStartDelay(400);
        scaleX.start();
        scaleY.start();

    }

    public void animateBarsInZoomIn() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mBarContainer, "ScaleX", (float) .5, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mBarContainer, "ScaleY", (float) .5, 1);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.setStartDelay(400);
        scaleY.setStartDelay(400);
        scaleX.start();
        scaleY.start();
    }

    private void layoutBars() {
        float weight = (float) 1 / (float) (mBarContainer.getChildCount() + 1);
        for (int i = 0; i < mBarContainer.getChildCount(); i++) {
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(0,
                    LayoutParams.MATCH_PARENT, weight);
            layout.setMargins(mMargin, 0, mMargin, 0);
            mBarContainer.getChildAt(i).setLayoutParams(layout);

            ((BarView) mBarContainer.getChildAt(i)).showLabel(mLabel);
            ((BarView) mBarContainer.getChildAt(i)).setBarColor(mBColor);
        }
        invalidate();
    }

}