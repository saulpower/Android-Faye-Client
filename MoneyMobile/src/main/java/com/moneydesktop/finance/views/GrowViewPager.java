package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import main.java.com.moneydesktop.finance.shared.adapter.GrowPagerAdapter;
import main.java.com.moneydesktop.finance.util.UiUtils;

public class GrowViewPager extends ViewPager {

    public final String TAG = this.getClass().getSimpleName();

    private boolean isPagingEnabled = true;

    private final float MARGIN_SIZE = -0.25f;

    private OnScrollChangedListener mListener;

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mListener = listener;
    }

    public GrowViewPager(Context context) {
        super(context);

        init();
    }

    public GrowViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (this.isPagingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (this.isPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean isPagingEnabled) {
        this.isPagingEnabled = isPagingEnabled;
    }

    public boolean isPagingEnabled() {
        return this.isPagingEnabled;
    }

    private void init() {

        // Get screen size to set margin width accordingly
        final DisplayMetrics metrics = UiUtils.getDisplayMetrics(getContext());

        float margin = MARGIN_SIZE * metrics.widthPixels;

        if (android.os.Build.VERSION.SDK_INT < 11) {
            margin = UiUtils.getDynamicPixels(getContext(), -45);
        }

        setPageMargin((int) margin);

        // Keep 3 pages loaded up at all times
        setOffscreenPageLimit(3);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);

        if (adapter instanceof GrowPagerAdapter) {
            ((GrowPagerAdapter) adapter).setPager(this);
        }
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mListener != null) {
            mListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public interface OnScrollChangedListener {
        public void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
