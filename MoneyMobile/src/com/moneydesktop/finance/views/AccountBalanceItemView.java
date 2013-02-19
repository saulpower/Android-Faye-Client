
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

public class AccountBalanceItemView extends LinearLayout {
    TextView mTextIcon;
    TextView mAccountType;
    TextView mAccountStatus;
    TextView mAccountAmount;
    LinearLayout mLayout;
    Boolean mIsDebt;

    public AccountBalanceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.AccountBalanceItemView);
        setUpViewFields(a);

    }

    public AccountBalanceItemView(Context context) {
        super(context);
        initViews(context);

    }

    private void initViews(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.account_balance_item_view, this);
        mTextIcon = (TextView) findViewById(R.id.text_icon);
        mAccountType = (TextView) findViewById(R.id.account_type);
        mAccountStatus = (TextView) findViewById(R.id.account_status);
        mAccountAmount = (TextView) findViewById(R.id.account_amount);
        mLayout = (LinearLayout) findViewById(R.id.account_balance_item_view);
    }

    public void setVariableItems(String s, String a) {
        setAccountStatus(s);
        setAccountAmount(a);
        invalidate();
    }

    public void setIcon(String icon) {
        mTextIcon.setText(icon);
    }

    public void setAccountType(String t) {
        mAccountType.setText(t);
    }

    public void setAccountStatus(String s) {
        mAccountStatus.setText(s);
    }

    public void setAccountAmount(String a) {
        mAccountAmount.setText(a);
    }

    public void setDebt(Boolean d) {
        mIsDebt = d;
        if (d == true) {
            displayDebt();
        }
    }

    private void setUpViewFields(TypedArray a) {
        setIcon(a.getString(R.styleable.AccountBalanceItemView_textIcon));
        setAccountType(a.getString(R.styleable.AccountBalanceItemView_accountType));
        if (a.getBoolean(R.styleable.AccountBalanceItemView_isDebt, false)) {
            displayDebt();
        }
        else {
            mLayout.setBackgroundColor(Color.WHITE);
            mTextIcon.setTextColor(getContext().getResources().getColor(R.color.gray2));
            mAccountAmount.setTextColor(getContext().getResources().getColor(R.color.gray4));
        }
        mAccountType.setTextColor(getContext().getResources().getColor(R.color.gray4));
        mAccountStatus.setTextColor(getContext().getResources().getColor(R.color.gray3));
        initFonts();
    }

    private void displayDebt() {
        mIsDebt = Boolean.valueOf(true);
        mLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray0));
        mTextIcon.setTextColor(getContext().getResources().getColor(R.color.gray3));
        mAccountAmount.setTextColor(getContext().getResources().getColor(R.color.debts));
        invalidate();

    }

    private void initFonts() {
        Fonts.applyGlyphFont(mTextIcon, 14);
        Fonts.applyPrimarySemiBoldFont(mAccountType, 10);
        Fonts.applyPrimaryBoldFont(mAccountAmount, 10);
        Fonts.applySecondaryItalicFont(mAccountStatus, 8);
    }

}
