package com.moneydesktop.finance.model;

import com.moneydesktop.finance.util.UiUtils;

import java.util.Date;

public class BarViewModel {

    private String mLabelText;
    private String mPopupText;

    private float mAmount;

    private int mColor;

    private Date mDate;

    public String getLabelText() {
        return mLabelText;
    }

    public void setLabelText(String mLabelText) {
        this.mLabelText = mLabelText;
    }

    public String getPopupText() {
        return mPopupText;
    }

    public void setPopupText(String mPopupText) {
        this.mPopupText = mPopupText;
    }

    public float getAmount() {
        return mAmount;
    }

    public void setAmount(float mAmount) {
        this.mAmount = mAmount;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public BarViewModel(int index, String labelText, String popupText, double amount, Date date) {
        mLabelText = labelText;
        mPopupText = popupText;
        mAmount = (float) amount;
        mDate = date;
        mColor = UiUtils.getRandomColor(index);
    }

}
