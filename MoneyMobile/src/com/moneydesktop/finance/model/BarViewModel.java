package com.moneydesktop.finance.model;

public class BarViewModel {
    public String mLabelText;
    public String mPopupText;
    public double mAmount;
    public double mMaxAmount;
    public long mDate;

    public BarViewModel(String label, String popup, double amount, double max, long date) {
        mLabelText = label;
        mPopupText = popup;
        mAmount = amount;
        mMaxAmount = max + (max * .45);
        mDate = date;
    }

}
