package com.moneydesktop.finance.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateRangeView extends View {
    
    public final String TAG = this.getClass().getSimpleName();

    private final float ITEM_WIDTH = 100.0f;
    
    private Float mDynamicWidth;
    private List<DateRangeItem> mDates;
    
    private Paint mTopBorderPaint;
    protected Paint mHighlightPaint;
    
    private HorizontalScroller mScroller;
    private AnchorView mLeft, mRight;
    
    private float mLastTouchY, mLastTouchX;
    
    private DateRangeItem mCurrentItem;
    
    @TargetApi(11)
	public DateRangeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPaints();
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initDateRange();
    }
    
    private void initPaints() {

        Resources resources = getContext().getResources();
        
        mTopBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTopBorderPaint.setStyle(Paint.Style.STROKE);
        mTopBorderPaint.setColor(resources.getColor(R.color.gray7));
        mTopBorderPaint.setStrokeWidth(1.0f);
        
        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setColor(resources.getColor(R.color.primaryColor));
        mHighlightPaint.setStyle(Paint.Style.FILL);
    }
    
    private void initDateRange() {
        
        if (mDates != null) {
            return;
        }
        
        Date today = new Date();

        Calendar c = Calendar.getInstance();    
        c.setTime(today);
        c.add(Calendar.YEAR, -1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, 0);
        
        mDates = new ArrayList<DateRangeItem>();
        
        for (int i = 0; i < 24; i++) {
            
            Rect bounds = createBounds(i);
            
            DateRangeItem temp = new DateRangeItem(getContext(), c.getTime(), bounds);
            temp.setCallback(this);
            mDates.add(temp);
            
            c.add(Calendar.MONTH, 1);
        }
        
        moveScroller();
        initAnchors();
    }
    
    private Rect createBounds(int index) {
        
        int left = (int) (getDynamicWidth() * index) + getPaddingLeft();
        int top = 0;
        int right = (int) (left + getDynamicWidth());
        int bottom = top + getHeight();
        
        return new Rect(left, top, right, bottom);
    }
    
    private Float getDynamicWidth() {
        
        if (mDynamicWidth == null) {
            mDynamicWidth = UiUtils.convertPixelsToDp(ITEM_WIDTH, getContext());
        }
        
        return mDynamicWidth;
    }
    
    private void moveScroller() {

        mCurrentItem = getItem(new Date());
        
        if (mCurrentItem != null && mScroller != null) {
            mScroller.scrollBy(mCurrentItem.getBounds().left + mCurrentItem.getBounds().width() / 2, 0);
        }
    }
    
    private void initAnchors() {
        
    }
    
    public DateRangeItem getItem(Date date) {
        
        if (mDates == null) {
            return null;
        }
        
        for (DateRangeItem item : mDates) {
            if (item.getDate().getMonth() == date.getMonth()) {
                return item;
            }
        }
        
        return null;
    }
    
    public void setScroller(HorizontalScroller scroll) {
        mScroller = scroll;
    }
    
    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        int width = (int) (getDynamicWidth() * 24) + getPaddingLeft();
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        setMeasuredDimension(width, height);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        
        final int action = ev.getAction();
        
        switch (action) {
        
            case MotionEvent.ACTION_DOWN: {
                
                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();

//                mScroller.setScrollingEnabled(false);
                
                break;
            }
            
            case MotionEvent.ACTION_UP: {

                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();
                
//                mScroller.setScrollingEnabled(true);
                
                break;
            }
                
            case MotionEvent.ACTION_MOVE: {
                
                final float y = ev.getY();
                final float x = ev.getX();
                
                // Remember this touch position for the next move event
                mLastTouchY = y;
                mLastTouchX = x;
                
                break;
            }
        }
        
        return true;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        c.drawLine(0, 3, getWidth(), 3, mTopBorderPaint);
        c.drawLine(0, getHeight() * 2 / 5, getWidth(), getHeight() * 2 / 5, mTopBorderPaint);
        
        // Draw highlight
        
        // Draw all of the date range items
        for (DateRangeItem item : mDates) {
            item.draw(c);
        }
        
        // Draw handles
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        super.invalidateDrawable(who);
        
        invalidate();
    }

}
