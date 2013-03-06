package com.moneydesktop.finance.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.moneydesktop.finance.util.DateRange;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.AnchorView.AnchorMoveListener;

import de.greenrobot.event.EventBus;

// TODO: Fix this to work with setting date range in Transactions View
public class DateRangeView extends View implements AnchorMoveListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private final float ITEM_WIDTH = 80.0f;
    private final float THRESHOLD = 2.0f;
    
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
    private PointF mAnchorDistance;
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
    
    public void setDateRange(DateRange range) {
    	
    	mStart = range.getStartDate();
    	mEnd = range.getEndDate();

        moveScroller(mStart);
        anchorDidMove();
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
    	moveScroller(new Date());
    }
    
    private void moveScroller(Date date) {

        DateRangeItem item = getItem(date);
        
        if (item != null && mScroller != null) {
            mScroller.scrollBy(item.getBounds().left / 2 + item.getBounds().width() / 2, 0);
        }
    }
    
    private void initAnchors() {
        
        mLeft = new AnchorView(getContext(), mCurrentItem.getBounds().left, getHeight(), true);
        mLeft.setCallback(this);
        mLeft.setAnchorMoveListener(this);
        mRight = new AnchorView(getContext(), mCurrentItem.getBounds().right, getHeight(), false);
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
 
    public DateRangeItem getItem(float positionX) {
        
        if (mDates == null) {
            return null;
        }
        
        for (DateRangeItem item : mDates) {
            if (item.getBounds().contains((int) positionX, 0) || item.getBounds().left == positionX || item.getBounds().right == positionX) {
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
            mAnchorDistance = new PointF(mLastTouchX - mLeft.getPosition(), mRight.getPosition() - mLastTouchX);
        }
        
        return mTouchingAnchorLeft || mTouchingAnchorRight;
    }
    
    private DateRangeItem isTouchingItem() {
        
        DateRangeItem item = getItem(mLastTouchX);
        
        return item;
    }
    
    private void setAnchors() {
        
        boolean useLeft = false;
        
        DateRangeItem itemLeft = getItem(mLeft.getPosition());
        if (itemLeft != null) {
            useLeft = closerToLeft(itemLeft.getBounds(), mLeft.getPosition());
            mLeft.animateToPosition(useLeft ? itemLeft.getBounds().left : itemLeft.getBounds().right);
            
            // Make adjustment to the right date to set our start date
            if (!useLeft) {
                itemLeft = mDates.get(itemLeft.getIndex() + 1);
            }
            mStart = itemLeft.getDate();
        }
        
        DateRangeItem itemRight = getItem(mRight.getPosition());
        if (itemRight != null) {
            useLeft = closerToLeft(itemRight.getBounds(), mRight.getPosition());
            mRight.animateToPosition(useLeft ? itemRight.getBounds().left : itemRight.getBounds().right);

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
    
    private boolean closerToLeft(Rect bounds, float positionX) {
        
        float difLeft = bounds.left - positionX;
        float difRight = bounds.right - positionX;
        
        return (Math.abs(difLeft) < Math.abs(difRight));
    }
    
    private void moveAnchors() {
        
        // Facilitate moving both anchors at once
        float positionLeft = mLastTouchX;
        float positionRight = mLastTouchX;
        
        if (mTouchingAnchorLeft && !mTouchingAnchorRight) {
            positionRight = mRight.getPosition();
        }
        
        if (!mTouchingAnchorLeft && mTouchingAnchorRight) {
            positionLeft = mLeft.getPosition();
        }
        
        // If we are moving both anchors set the positions
        if (mTouchingAnchorLeft && mTouchingAnchorRight) {
            positionLeft = (mLastTouchX - mAnchorDistance.x);
            positionRight = (mLastTouchX + mAnchorDistance.y);
        }
        
        // Calculate left adjustments if necessary
        if (mTouchingAnchorLeft) {
            if (positionLeft < mDates.get(0).getBounds().left) {
                positionLeft = mDates.get(0).getBounds().left;
            } else if (positionLeft > (positionRight - mDynamicWidth)) {
                positionLeft = (positionRight - mDynamicWidth);
            }
        }
        
        // Calculate right adjustment if necessary
        if (mTouchingAnchorRight) {
            if (positionRight > mDates.get(mDates.size() - 1).getBounds().right) {
                positionRight = mDates.get(mDates.size() - 1).getBounds().right;
            } else if (positionRight < (positionLeft + mDynamicWidth)) {
                positionRight = (positionLeft + mDynamicWidth);
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
        
        mLeft.animateToPosition(item.getBounds().left);
        mRight.animateToPosition(item.getBounds().right);
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
                
                if ((mTouchingAnchorLeft || mTouchingAnchorRight || mTouchingSelection) && (Math.abs(mDistance.x) > getDynamicThreshold() || mAnchorsMoved)) {

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
            mHighlightBounds = new RectF(mLeft.getPosition(), getHeight() * 2 / 5, mRight.getPosition(), getHeight());
        }
        
        mHighlightBounds.left = mLeft.getPosition();
        mHighlightBounds.right = mRight.getPosition();
        
        eventBus.post(new EventMessage().new AnchorChangeEvent(mLeft, mRight));
    }
    
    public interface FilterChangeListener {
        public void filterChanged(int direction);
    }
}
