
package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.R.color;

public class AccountBalanceItemView extends LinearLayout {
    TextView mTextIcon;
    TextView mAccountType;
    TextView mAccountStatus;
    TextView mAccountAmount;
    LinearLayout mLayout;

    public AccountBalanceItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.account_balance_item_view, null);
        mTextIcon = (TextView) findViewById(R.id.text_icon);
        mAccountType = (TextView) findViewById(R.id.account_type);
        mAccountStatus = (TextView) findViewById(R.id.account_status);
        mAccountAmount = (TextView) findViewById(R.id.account_amount);
        mLayout = (LinearLayout) findViewById(R.id.account_balance_item_base_layout);
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.AccountBalanceItemView);
        setIcon(a.getString(R.styleable.AccountBalanceItemView_textIcon));
        setAccountType(a.getString(R.styleable.AccountBalanceItemView_accountType));
        setAccountStatus(a.getString(R.styleable.AccountBalanceItemView_accountStatus));
        setAccountAmount(a.getString(R.styleable.AccountBalanceItemView_accountAmount));
        if (a.getBoolean(R.styleable.AccountBalanceItemView_isDebt, false)) {
            mLayout.setBackgroundColor(color.gray3);
            mTextIcon.setTextColor(color.gray9);
            mAccountAmount.setTextColor(color.debts);
        }
        else {
            mLayout.setBackgroundColor(color.white);
            mTextIcon.setTextColor(color.gray4);
            mAccountAmount.setTextColor(color.gray9);
        }
        mAccountType.setTextColor(color.gray9);
        mAccountStatus.setTextColor(color.gray4);

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
}
