package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.moneydesktop.finance.adapters.UltimateAdapter;
import com.moneydesktop.finance.util.UiUtils;

/**
 * A ListView that maintains a header pinned at the top of the list. The
 * pinned header can be pushed up and dissolved as needed.
 * 
 * It also supports pagination by setting a custom view as the loading
 * indicator.
 */
public class UltimateListView extends ExpandableListView {
    
	public static final String TAG = UltimateListView.class.getSimpleName();
	
    private View mHeaderView;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;
    private int mHeaderViewHeight = -1;

    private UltimateAdapter mAdapter;
    
    private boolean mIsPressed = false;
    
    private int mSection = 0;
    private float mLastTouchY;
    private float mThreshold = UiUtils.getDynamicPixels(getContext(), 5);

    public View getHeaderView() {
        return mHeaderView;
    }
    
    private void setHeaderView(View view) {
        
        mHeaderView = view;
        
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        
        requestLayout();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = false;
        
        if (mHeaderView == null || !mHeaderViewVisible) {
            return super.onTouchEvent(ev);
        }
        
        int action = ev.getAction();
        
        switch (action) {

            case MotionEvent.ACTION_DOWN: {
                
                mLastTouchY = ev.getY();
                
                if (isPointInsideHeader(ev.getRawX(), ev.getRawY())) {
                    mHeaderView.setSelected(true);
                    invalidateViews();
                    mIsPressed = true;
                    result = true;
                }
                
                break;
            }

            case MotionEvent.ACTION_UP: {
                
                if (mIsPressed && isPointInsideHeader(ev.getRawX(), ev.getRawY())) {
                    
                    int position = mAdapter.getPositionForSection(mSection);
                    
                    performItemClick(mHeaderView, position, position);

                    mHeaderView.setSelected(false);
                    invalidateViews();
                    
                    mIsPressed = false;
                    result = true;
                }
                
                break;
            }
            
            case MotionEvent.ACTION_MOVE: {
                
                if (!mIsPressed) {
                    break;
                }
                
                float y = ev.getY();
                float dy = mLastTouchY - y;
                
                if (Math.abs(dy) > mThreshold || !isPointInsideHeader(ev.getRawX(), ev.getRawY())) {
                    mHeaderView.setSelected(false);
                    invalidateViews();
                    mIsPressed = false;
                } else {
                    result = true;
                }
                
                break;
            }
        }
        
        return (!result ? super.onTouchEvent(ev) : result);
    }

    protected boolean isPointInsideHeader(float x, float y) {
        
        int location[] = new int[2];
        getLocationOnScreen(location);
        
        int viewX = location[0];
        int viewY = location[1];
        
        // point is inside view bounds
        if ((x > viewX && x < (viewX + mHeaderView.getWidth())) && (y > viewY && y < (viewY + mHeaderView.getHeight()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView();
        }
    }
    
    /**
     * Adjustments have been made to account for group views
     * that are larger in height than the child views.  For
     * the sticky headers to work properly adjustments must
     * be made as to the actual first visible position given
     * the header view is stuck to the top of the listview.
     */
    @Override
    public int getFirstVisiblePosition() {
        
        int position = super.getFirstVisiblePosition();
        
        if (mHeaderViewHeight == -1 || getChildCount() == 0) {
            return position;
        }

        int i = 0;
        
        View v = getChildAt(i);
        i++;
        final int top = (v == null) ? 0 : v.getTop();
        
        int sum = ((v == null) ? 0 : v.getHeight()) + top;
        
        while (sum < mHeaderViewHeight && i < getChildCount()) {
            v = getChildAt(i);
            sum += (v == null) ? 0 : v.getHeight();
            i++;
            position++;
        }
        
        position--;
        
        return position;
    }
    
    public void configureHeaderView() {
        configureHeaderView(getFirstVisiblePosition());
    }

    public void configureHeaderView(int position) {
        
        if (mHeaderView == null) {
            return;
        }
        
        mSection = mAdapter.getSectionForPosition(position);
        int state = mAdapter.getPinnedHeaderState(position, mSection);
        
        switch (state) {
            
            case UltimateAdapter.PINNED_HEADER_GONE: {
                
                mHeaderViewVisible = false;
                
                break;
            }

            case UltimateAdapter.PINNED_HEADER_VISIBLE: {
                
            	mAdapter.configureHeader(mHeaderView, mSection);
            	
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                
                mHeaderViewVisible = true;
                
                break;
            }

            case UltimateAdapter.PINNED_HEADER_PUSHED_UP: {
                
                int i = 1;
                int headerHeight = mHeaderView.getHeight();
                
                while (getChildAt(i) != null && getChildAt(i).getHeight() != headerHeight) {
                    i++;
                }
                
                final View firstView = getChildAt(i);
                
                if (firstView != null) {
                    
	                int top = firstView.getTop();
	                int y;
	                
	                if (top < headerHeight) {
	                    y = (top - headerHeight);
	                } else {
	                    y = -headerHeight;
	                }
	                
	                mAdapter.configureHeader(mHeaderView, mSection);
	                
	                if (mHeaderView.getTop() != y) {
	                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
	                }
	                
	                mHeaderViewVisible = true;
                }
                
                break;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }
    
    public UltimateListView(Context context) {
        super(context);
    }

    public UltimateListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UltimateListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        
    	if (!(adapter instanceof UltimateAdapter)) {
    		throw new IllegalArgumentException(UltimateListView.class.getSimpleName() + " must use adapter of type " + UltimateAdapter.class.getSimpleName());
    	}
    	
    	// previous adapter
    	if (this.mAdapter != null) {
    		this.setOnScrollListener(null);
    	}

    	this.mAdapter = (UltimateAdapter) adapter;
		this.setOnScrollListener(mAdapter);
		
		View header = mAdapter.getSectionView(0, false, null, this);
		setHeaderView(header);
		
        super.setAdapter(adapter);
    }
    
    @Override
    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        
        if (shouldExpandGroup) {
            expandGroup(groupPosition);
        }
        
        if (mAdapter != null) {
            mAdapter.setSelectedGroupPosition(groupPosition);
            mAdapter.setSelectedChildPosition(childPosition);
            mAdapter.notifyDataSetChanged();
        }
        
        return true;
    }
    
    public void expandAll() {
        
        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            expandGroup(i);
        }
    }
}
