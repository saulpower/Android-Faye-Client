package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/14/13
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LabelEditCurrency extends LabelEditText {

    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");

    public LabelEditCurrency(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onTextChanged (CharSequence text, int start, int lengthBefore, int lengthAfter) {
//        setText(mFormatter.format(text));
    }
}
