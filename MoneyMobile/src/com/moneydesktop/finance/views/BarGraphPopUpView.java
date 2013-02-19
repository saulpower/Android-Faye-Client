package com.moneydesktop.finance.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

public class BarGraphPopUpView extends LinearLayout {
    TextView mTopLine;
    TextView mMidLine;
    TextView mBottomLine;

    public BarGraphPopUpView(Context context) {
        super(context);
        initViews(context);
        // TODO Auto-generated constructor stub
    }
    private void initViews(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.bar_graph_pop_up_view, this);
        mTopLine = (TextView) findViewById(R.id.popup_date);
        mMidLine = (TextView) findViewById(R.id.popup_date);
        mBottomLine = (TextView) findViewById(R.id.popup_amount);
        Fonts.applySecondaryItalicFont(mTopLine, 12);
        Fonts.applyPrimaryBoldFont(mMidLine, 24);
        Fonts.applySecondaryItalicFont(mBottomLine, 12);
        mMidLine.setTextColor(getResources().getColor(R.color.primaryColor));
        mTopLine.setTextColor(getResources().getColor(R.color.gray4));
        mBottomLine.setTextColor(getResources().getColor(R.color.gray4));
    }
    public void setStrings(String top, String mid, String bottom){
        mTopLine.setText(top);
        mMidLine.setText(mid);
        mBottomLine.setText(bottom);
    }
}
