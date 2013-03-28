package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/14/13
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LabelEditCurrency extends LabelEditText {

    private DecimalFormat mFormatter = new DecimalFormat("#,##0.00;-#,##0.00");
    private Currency mCurrency;
    private String mPrevious;

    public LabelEditCurrency(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCurrency = Currency.getInstance(Locale.getDefault());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        if (text != null) {

            String value = text.toString();

            if (value == null || mCurrency == null) return;

            if (!value.contains(mCurrency.getSymbol())) {
                setText(String.format("%s%s", mCurrency.getSymbol(), value));
                return;
            }

            if (value.indexOf(mCurrency.getSymbol()) != 0) {
                setText(value.substring(value.indexOf(mCurrency.getSymbol())));
                return;
            }

            if (value.indexOf(".") != -1 && (value.length() - value.indexOf(".") - 1) > 2) {
                setText(mPrevious);
                return;
            }

            mPrevious = value;

            setSelection((value.length() - 1));
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {

        if (selStart != getText().toString().length()) {
            setSelection(getText().toString().length());
        }
    }

    @Override
    public void onEditorAction(int actionCode) {

        String value = getText().toString().substring(1);

        if (value.equals("")) {
            value = Integer.toString(0);
        }

        if (actionCode == EditorInfo.IME_ACTION_DONE && !value.equals("")) {
            setText(mFormatter.format(Double.parseDouble(value.replace(",", ""))));
        }

        super.onEditorAction(actionCode);
    }
}
