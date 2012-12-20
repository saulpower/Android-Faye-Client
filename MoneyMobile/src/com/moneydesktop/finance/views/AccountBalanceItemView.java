
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.color;
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
        mLayout = (LinearLayout) findViewById(R.id.account_balance_item_base_layout);
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
            mLayout.setBackgroundColor(color.white);
            mTextIcon.setTextColor(color.gray4);
            mAccountAmount.setTextColor(color.gray9);
        }
        mAccountType.setTextColor(color.gray9);
        mAccountStatus.setTextColor(color.gray4);
        Fonts.applyGlyphFont(mTextIcon, 24);
        Fonts.applyPrimarySemiBoldFont(mAccountType, 18);
        Fonts.applyPrimaryBoldFont(mAccountAmount, 18);
        Fonts.applySecondaryItalicFont(mAccountStatus, 16);
    }

    private void displayDebt() {
        mIsDebt = new Boolean(true);
        mLayout.setBackgroundColor(color.gray3);
        mTextIcon.setTextColor(color.gray9);
        mAccountAmount.setTextColor(color.debts);
        invalidate();
    }
}
