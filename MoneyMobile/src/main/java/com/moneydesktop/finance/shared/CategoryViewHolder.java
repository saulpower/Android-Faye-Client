package main.java.com.moneydesktop.finance.shared;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import main.java.com.moneydesktop.finance.database.Category;

public class CategoryViewHolder {

    public Category parent;

    public TextView icon;
    public TextView title;
    public TextView itemTitle;
    public TextView subCategory;
    public TextView amount;

    public RelativeLayout item;
    public View color;

    public TextView cancel;
    public EditText newCategory;

    public LinearLayout info;
    public LinearLayout addCategory;
    public ViewFlipper flipper;
}
