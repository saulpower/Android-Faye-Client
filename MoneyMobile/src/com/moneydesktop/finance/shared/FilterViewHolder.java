package com.moneydesktop.finance.shared;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.views.CaretView;

import java.util.List;

public class FilterViewHolder {
    
    public RelativeLayout mRoot;
    public LinearLayout mInfo;
    
    public TextView mTitle;
    public TextView mSubTitle;
    public TextView mHeaderTitle;
    public TextView mSingleTitle;
    public CaretView mCaret;
    
    public String mText;
    public String mSubText;
    public List<FilterViewHolder> mSubSection;
    public boolean mIsSubSection = false;
    public PowerQuery mQuery;
}
