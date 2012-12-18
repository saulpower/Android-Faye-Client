package com.moneydesktop.finance.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.AnchorView.AnchorMoveListener;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateRangeView extends View implements AnchorMoveListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private final float ITEM_WIDTH = 100.0f;
    
    private Float mDynamicWidth;
    private List<DateRangeItem> mDates;
    
    private Paint mTopBorderPaint;
    protected Paint mHighlightPaint;
    
    private HorizontalScroller mScroller;
    private AnchorView mLeft, mRight;
    private RectF mHighlight;
    
    private float mLastTouchY, mLastTouchX;
    private boolean mTouchingAnchorLeft = false, mTouchingAnchorRight = false;
    private boolean mAnchorsMoved = false;
    
    private DateRangeItem mCurrentItem;
    
    private Date mStart, mEnd;
    
    private EventBus eventBus = EventBus.getDefault();
    private DateRangeChangeListener mListener;
    
    public void setDateRangeChangeListener(DateRangeChangeListener listener) {
        mListener = listener;
    }
    
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
            
            if (temp.getDate().getMonth() == today.getMonth()) {
                mCurrentItem = temp;
            }
            
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

        DateRangeItem item = getItem(new Date());
        
        if (item != null && mScroller != null) {
            mScroller.scrollBy(item.getBounds().left + item.getBounds().width() / 2, 0);
        }
    }
    
    private void initAnchors() {
        
        PointF left = new PointF(mCurrentItem.getBounds().left, 0);
        PointF right = new PointF(mCurrentItem.getBounds().right, 0);
        
        mLeft = new AnchorView(getContext(), left, getHeight());
        mLeft.setCallback(this);
        mLeft.setAnchorMoveListener(this);
        mRight = new AnchorView(getContext(), right, getHeight());
        mRight.setCallback(this);
        mRight.setAnchorMoveListener(this);
        
        anchorDidMove();
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
 
    public DateRangeItem getItem(PointF point) {
        
        if (mDates == null) {
            return null;
        }
        
        for (DateRangeItem item : mDates) {
            if (item.getBounds().contains((int) point.x, (int)point.y) || item.getBounds().left == point.x || item.getBounds().right == point.x) {
                return item;
            }
        }
        
        return null;
    }
    
    public void setScroller(HorizontalScroller scroll) {
        mScroller = scroll;
    }
    
    private boolean isTouchingAnchor() {
        
        mTouchingAnchorLeft = mLeft.getBounds().contains((int) mLastTouchX, (int) mLastTouchY);
        mTouchingAnchorRight = mRight.getBounds().contains((int) mLastTouchX, (int) mLastTouchY);
        
        return mTouchingAnchorLeft || mTouchingAnchorRight;
    }
    
    private void setAnchors() {
        
        boolean useLeft = false;
        
        DateRangeItem itemLeft = getItem(mLeft.getPosition());
        if (itemLeft != null) {
            useLeft = closerToLeft(itemLeft.getBounds(), mLeft.getPosition());
            PointF left = new PointF(useLeft ? itemLeft.getBounds().left : itemLeft.getBounds().right, 0);
            mLeft.animateToPosition(left);
        }
        
        DateRangeItem itemRight = getItem(mRight.getPosition());
        if (itemRight != null) {
            useLeft = closerToLeft(itemRight.getBounds(), mRight.getPosition());
            PointF right = new PointF(useLeft ? itemRight.getBounds().left : itemRight.getBounds().right, 0);
            mRight.animateToPosition(right);
        }
    }
    
    private boolean closerToLeft(Rect bounds, PointF point) {
        
        float difLeft = bounds.left - point.x;
        float difRight = bounds.right - point.x;
        
        return (Math.abs(difLeft) < Math.abs(difRight));
    }
    
    private void moveAnchors() {

        PointF position = new PointF(mLastTouchX, 0);
        
        if (mTouchingAnchorLeft && (mRight.getPosition().x - mDynamicWidth) > mLastTouchX && mLastTouchX > mDates.get(0).getBounds().left) {
            
            mLeft.setPosition(position);
            
        } else if (mTouchingAnchorRight && (mLeft.getPosition().x + mDynamicWidth) < mLastTouchX && mLastTouchX < mDates.get(mDates.size() - 1).getBounds().right) {
            
            mRight.setPosition(position);
        }
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

                boolean touching = isTouchingAnchor();
                mScroller.setScrollingEnabled(!touching);
                
                break;
            }
            
            case MotionEvent.ACTION_UP: {

                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();
                
                if (mAnchorsMoved) {
                    
                    mAnchorsMoved = false;
                    setAnchors();
                    
                    if (mListener != null) {
                        mListener.dateRangeChanged();
                    }
                }
                
                mScroller.setScrollingEnabled(true);
                
                break;
            }
                
            case MotionEvent.ACTION_MOVE: {
                
                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();
                
                if (mTouchingAnchorLeft || mTouchingAnchorRight) {

                    mAnchorsMoved = true;
                    moveAnchors();
                }
                
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
        c.drawRect(mHighlight, mHighlightPaint);
        
        // Draw all of the date range items
        for (DateRangeItem item : mDates) {
            item.draw(c);
        }
        
        // Draw handles
        mLeft.draw(c);
        mRight.draw(c);
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        super.invalidateDrawable(who);
        
        invalidate();
    }

    @Override
    public void anchorDidMove() {
        
        if (mHighlight == null) {
            mHighlight = new RectF(mLeft.getPosition().x, getHeight() * 2 / 5, mRight.getPosition().x, getHeight());
        }
        
        mHighlight.left = mLeft.getPosition().x;
        mHighlight.right = mRight.getPosition().x;
        
        eventBus.post(new EventMessage().new AnchorChangeEvent(mLeft, mRight));
    }
    
    public interface DateRangeChangeListener {
        public void dateRangeChanged();
    }
}
