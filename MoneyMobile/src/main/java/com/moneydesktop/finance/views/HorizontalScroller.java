package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HorizontalScroller extends HorizontalScrollView {

    public final String TAG = this.getClass().getSimpleName();

    private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    public HorizontalScroller(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:

                // if we can scroll pass the event to the superclass
                if (mScrollable) {
                    return super.onTouchEvent(ev);
                }

                // only continue to handle the touch event if scrolling enabled
                return mScrollable;

            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable){
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

}
