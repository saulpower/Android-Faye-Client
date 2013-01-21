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
import android.os.Handler;
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
    private final float THRESHOLD = 20.0f;
    
    private Float mDynamicThreshold;
    private Float mDynamicWidth;
    
    private List<DateRangeItem> mDates;
    
    private Paint mTopBorderPaint;
    protected Paint mHighlightPaint;
    
    private HorizontalScroller mScroller;
    private AnchorView mLeft, mRight;
    private RectF mHighlightBounds;
    
    private float mLastTouchY, mLastTouchX;
    private PointF mDistance;
    private boolean mTouchingAnchorLeft = false;
    private boolean mTouchingAnchorRight = false;
    private boolean mTouchingSelection = false;
    private boolean mAnchorsMoved = false;
    private DateRangeItem mTapped;
    
    private DateRangeItem mCurrentItem;
    
    private Date mStart, mEnd, mCurrentMonth;
    
    private EventBus eventBus = EventBus.getDefault();
    private FilterChangeListener mListener;
    
    public void setDateRangeChangeListener(FilterChangeListener listener) {
        mListener = listener;
    }
    
    public Date getStartDate() {
        return mStart;
    }
    
    public Date getEndDate() {
        return mEnd;
    }
    
    @TargetApi(11)
	public DateRangeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initStartEndDates();
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
        mTopBorderPaint.setColor(resources.getColor(R.color.gray3));
        mTopBorderPaint.setStrokeWidth(UiUtils.getDynamicPixels(getContext(), 1.0f));
        
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
        c.setTime(getToday());
        c.add(Calendar.YEAR, -1);
        c.set(Calendar.MONTH, 0);
        
        mDates = new ArrayList<DateRangeItem>();
        
        for (int i = 0; i < 24; i++) {
            
            Rect bounds = createBounds(i);
            
            DateRangeItem temp = new DateRangeItem(getContext(), i, c.getTime(), bounds);
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
    
    private void initStartEndDates() {
        
        Calendar c = Calendar.getInstance();    
        c.setTime(getToday());
        c.set(Calendar.DAY_OF_MONTH, 1);
        
        mStart = c.getTime();
        
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, 1);
        
        mEnd = c.getTime();
    }
    
    private Date getToday() {
        
        if (mCurrentMonth == null) {

            Calendar c = Calendar.getInstance();    
            c.setTime(new Date());
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            
            mCurrentMonth = c.getTime();
        }
        
        return mCurrentMonth;
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
            mDynamicWidth = UiUtils.getDynamicPixels(getContext(), ITEM_WIDTH);
        }
        
        return mDynamicWidth;
    }
    
    private Float getDynamicThreshold() {
        
        if (mDynamicThreshold == null) {
            mDynamicThreshold = UiUtils.getDynamicPixels(getContext(), THRESHOLD);
        }
        
        return mDynamicThreshold;
    }
    
    private void moveScroller() {

        DateRangeItem item = getItem(new Date());
        
        if (item != null && mScroller != null) {
            mScroller.scrollBy(item.getBounds().left / 2 + item.getBounds().width() / 2, 0);
        }
    }
    
    private void initAnchors() {
        
        PointF left = new PointF(mCurrentItem.getBounds().left, 0);
        PointF right = new PointF(mCurrentItem.getBounds().right, 0);
        
        mLeft = new AnchorView(getContext(), left, getHeight(), true);
        mLeft.setCallback(this);
        mLeft.setAnchorMoveListener(this);
        mRight = new AnchorView(getContext(), right, getHeight(), false);
        mRight.setCallback(this);
        mRight.setAnchorMoveListener(this);
        
        anchorDidMove();
    }
    
    public DateRangeItem getItem(Date date) {
        
        if (mDates == null) {
            return null;
        }
        
        for (DateRangeItem item : mDates) {
            if (item.getDate().getMonth() == date.getMonth() && item.getDate().getYear() == date.getYear()) {
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
        
        if (mHighlightBounds.contains((int) mLastTouchX, (int) mLastTouchY) && !mTouchingAnchorLeft && !mTouchingAnchorRight) {
            mTouchingAnchorLeft = true;
            mTouchingAnchorRight = true;
            mTouchingSelection = true;
        }
        
        return mTouchingAnchorLeft || mTouchingAnchorRight || mTouchingSelection;
    }
    
    private DateRangeItem isTouchingItem() {
        
        PointF touch = new PointF(mLastTouchX, mLastTouchY);
        
        DateRangeItem item = getItem(touch);
        
        return item;
    }
    
    private void setAnchors() {
        
        boolean useLeft = false;
        
        DateRangeItem itemLeft = getItem(mLeft.getPosition());
        if (itemLeft != null) {
            useLeft = closerToLeft(itemLeft.getBounds(), mLeft.getPosition());
            PointF left = new PointF(useLeft ? itemLeft.getBounds().left : itemLeft.getBounds().right, 0);
            mLeft.animateToPosition(left);
            
            // Make adjustment to the right date to set our start date
            if (!useLeft) {
                itemLeft = mDates.get(itemLeft.getIndex() + 1);
            }
            mStart = itemLeft.getDate();
        }
        
        DateRangeItem itemRight = getItem(mRight.getPosition());
        if (itemRight != null) {
            useLeft = closerToLeft(itemRight.getBounds(), mRight.getPosition());
            PointF right = new PointF(useLeft ? itemRight.getBounds().left : itemRight.getBounds().right, 0);
            mRight.animateToPosition(right);

            // Make adjustment to the right date to set our end date
            if (useLeft) {
                itemRight = mDates.get(itemRight.getIndex() - 1);
            }
            mEnd = itemRight.getEndDate();
        }

        if (mListener != null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                
                @Override
                public void run() {

                    mListener.filterChanged(-1);
                }
            }, 300);
        }
    }
    
    private boolean closerToLeft(Rect bounds, PointF point) {
        
        float difLeft = bounds.left - point.x;
        float difRight = bounds.right - point.x;
        
        return (Math.abs(difLeft) < Math.abs(difRight));
    }
    
    private void moveAnchors() {

        // Facilitate moving both anchors at once
        PointF positionLeft = new PointF(mLastTouchX, 0);
        PointF positionRight = positionLeft;
        
        if (mTouchingAnchorLeft && !mTouchingAnchorRight) {
            positionRight = mRight.getPosition();
        } else if (!mTouchingAnchorLeft && mTouchingAnchorRight) {
            positionLeft = mLeft.getPosition();
        } else {
            positionLeft = new PointF(mLeft.getPosition().x + mDistance.x, 0);
            positionRight = new PointF(mRight.getPosition().x + mDistance.x, 0);
        }
        
        // Calculate left adjustments if necessary
        if (mTouchingAnchorLeft) {
            if (positionLeft.x < mDates.get(0).getBounds().left) {
                positionLeft = new PointF(mDates.get(0).getBounds().left, 0);
            } else if (positionLeft.x > (positionRight.x - mDynamicWidth)) {
                positionLeft = new PointF(positionRight.x - mDynamicWidth, 0);
            }
        }
        
        // Calculate right adjustment if necessary
        if (mTouchingAnchorRight) {
            if (positionRight.x > mDates.get(mDates.size() - 1).getBounds().right) {
                positionRight = new PointF(mDates.get(mDates.size() - 1).getBounds().right, 0);
            } else if (positionRight.x < (positionLeft.x + mDynamicWidth)) {
                positionRight = new PointF(positionLeft.x + mDynamicWidth, 0);
            }
        }
        
        // Apply change if touching this anchor
        if (mTouchingAnchorLeft) {
            mLeft.setPosition(positionLeft);
        }

        // Apply change if touching this anchor
        if (mTouchingAnchorRight) {
            mRight.setPosition(positionRight);
        }
    }
    
    private void moveAnchorsTo(DateRangeItem item) {

        PointF left = new PointF(item.getBounds().left, 0);
        PointF right = new PointF(item.getBounds().right, 0);
        
        mLeft.animateToPosition(left);
        mRight.animateToPosition(right);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                setAnchors();
            }
        }, 300);
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
                
                mDistance = new PointF(0.0f, 0.0f);

                boolean touching = isTouchingAnchor();
                mScroller.setScrollingEnabled(!touching);
                    
                DateRangeItem item = isTouchingItem();
                if (item != null) {
                    mTapped = item;
                }
                
                break;
            }
            
            case MotionEvent.ACTION_UP: {

                mLastTouchY = ev.getY();
                mLastTouchX = ev.getX();
                
                if (mAnchorsMoved) {
                    
                    mAnchorsMoved = false;
                    setAnchors();
                    
                } else if (mTapped != null) {
                    
                    moveAnchorsTo(mTapped);
                }
                
                mTapped = null;
                
                mScroller.setScrollingEnabled(true);
                
                break;
            }
                
            case MotionEvent.ACTION_MOVE: {
                
                final float y = ev.getY();
                final float x = ev.getX();
                
                // Remember this touch position for the next move event
                mDistance.y = y - mLastTouchY;
                mDistance.x = x - mLastTouchX;
                
                // Remember this touch position for the next move event
                mLastTouchY = y;
                mLastTouchX = x;
                
                if ((mTouchingAnchorLeft || mTouchingAnchorRight || mTouchingSelection) && Math.abs(mDistance.x) < getDynamicThreshold()) {

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
        c.drawRect(mHighlightBounds, mHighlightPaint);
        
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
        
        if (mHighlightBounds == null) {
            mHighlightBounds = new RectF(mLeft.getPosition().x, getHeight() * 2 / 5, mRight.getPosition().x, getHeight());
        }
        
        mHighlightBounds.left = mLeft.getPosition().x;
        mHighlightBounds.right = mRight.getPosition().x;
        
        eventBus.post(new EventMessage().new AnchorChangeEvent(mLeft, mRight));
    }
    
    public interface FilterChangeListener {
        public void filterChanged(int direction);
    }
}
