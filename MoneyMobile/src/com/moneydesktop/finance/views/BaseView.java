package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class BaseView extends View {

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected boolean isPointInsideView(float x, float y, View view){
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        //point is inside view bounds
        if ((x > viewX && x < (viewX + view.getWidth())) && (y > viewY && y < (viewY + view.getHeight()))){
            return true;
        } else {
            return false;
        }
    }

}
