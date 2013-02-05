package com.moneydesktop.finance.shared;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moneydesktop.finance.database.Category;

public class CategoryViewHolder {

    public Category parent;
    
    public TextView icon;
    public TextView title;
    public TextView itemTitle;
    public TextView subCategory;
    
    public TextView cancel;
    public EditText newCategory;
    
    public LinearLayout info;
    public LinearLayout addCategory;
    public ViewFlipper flipper;
}
