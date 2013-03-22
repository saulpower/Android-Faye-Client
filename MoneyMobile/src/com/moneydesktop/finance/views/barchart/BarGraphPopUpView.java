package com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.CaretDrawable;

public class BarGraphPopUpView extends RelativeLayout {
    TextView mTopLine;
    TextView mMidLine;
    TextView mBottomLine;
    LinearLayout mLayout;
    CaretDrawable mArrow;
    View mTest;
    int mX;
    int mY;

    public BarGraphPopUpView(Context context, int x, int y) {
        super(context);
        mX = x;
        mY = y;
        initViews(context, x, y);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight + (int) UiUtils.convertDpToPixel(20, getContext()));
        mArrow = new CaretDrawable(new PointF(mX, mY), UiUtils.convertDpToPixel(20, getContext()), UiUtils.convertDpToPixel(20, getContext()));
        mArrow.setColor(getResources().getColor(R.color.gray1));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mArrow.setBounds(mX, this.getBottom() - 20, mX + 20, this.getBottom());
        mArrow.draw(canvas);
    }

    private void initViews(Context context, int x, int y) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mTest = inflater.inflate(R.layout.bar_graph_pop_up_view, this);
        mTopLine = (TextView) findViewById(R.id.popup_date);
        mMidLine = (TextView) findViewById(R.id.popup_amount);
        mBottomLine = (TextView) findViewById(R.id.popup_tap_to_view);
        mLayout = (LinearLayout) findViewById(R.id.bar_graph_pop_up_linear);
        Fonts.applySecondaryItalicFont(mTopLine, 12);
        Fonts.applyPrimaryBoldFont(mMidLine, 24);
        Fonts.applySecondaryItalicFont(mBottomLine, 12);

    }

    public void setStrings(String top, String mid, String bottom) {
        mTopLine.setText(top);
        mMidLine.setText(mid);
        mBottomLine.setText(bottom);
    }
}