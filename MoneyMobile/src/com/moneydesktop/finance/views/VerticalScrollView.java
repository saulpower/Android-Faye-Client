package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * User: kentandersen
 * Date: 3/29/13
 * This custom ScrollView will prevent cancel motion events from occurring when trying to swipe right/left.
 * The Y direction has to be greater that the X direction in order for a cancel to happen.
 */
public class VerticalScrollView extends ScrollView {

    private float xDistance, yDistance, lastX, lastY;

    public VerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                lastX = ev.getX();
                lastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                xDistance += Math.abs(curX - lastX);
                yDistance += Math.abs(curY - lastY);
                lastX = curX;
                lastY = curY;
                if(xDistance > yDistance)
                    return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

}
