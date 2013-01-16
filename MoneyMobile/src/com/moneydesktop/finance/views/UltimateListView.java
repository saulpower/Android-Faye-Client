package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.moneydesktop.finance.adapters.UltimateAdapter;

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
    private int mHeaderViewHeight;

    private UltimateAdapter mAdapter;
    
    private boolean mIsPressed = false;
    private boolean mHeaderClicked = false;

    private void setHeaderView(View view) {
        
        mHeaderView = view;
        
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        
        requestLayout();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        
        if (!mHeaderViewVisible) {
            return result;
        }
        
        int action = ev.getAction();
        
        switch (action) {

            case MotionEvent.ACTION_DOWN: {
                
                if (isPointInsideHeader(ev.getRawX(), ev.getRawY())) {
                    mIsPressed = true;
                }
                
                break;
            }

            case MotionEvent.ACTION_UP: {
                
                if (mIsPressed && isPointInsideHeader(ev.getRawX(), ev.getRawY())) {
                    
                    int section = mAdapter.getSectionForPosition(getFirstVisiblePosition());
                    
                    if (mAdapter.isSectionVisible(section)) {
                        collapseGroup(section);
                    } else {
                        expandGroup(section);
                    }
                    
                    playSoundEffect(SoundEffectConstants.CLICK);
                    
                    mHeaderClicked = true;
                    result = true;
                    mIsPressed = false;
                }
                
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public boolean performItemClick(View view, int position, long id) {
        
        if (!mHeaderClicked) {
            
            return super.performItemClick(view, position, id);
        }

        mHeaderClicked = false;
        
        return true;
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
    
    public void configureHeaderView() {
        configureHeaderView(getFirstVisiblePosition());
    }

    public void configureHeaderView(int position) {
        
        if (mHeaderView == null) {
            return;
        }
        
        int section = mAdapter.getSectionForPosition(position);
        int state = mAdapter.getPinnedHeaderState(position, section);
        
        switch (state) {
            
            case UltimateAdapter.PINNED_HEADER_GONE: {
                
                mHeaderViewVisible = false;
                
                break;
            }

            case UltimateAdapter.PINNED_HEADER_VISIBLE: {
                
            	mAdapter.configureHeader(mHeaderView, section);
            	
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                
                mHeaderViewVisible = true;
                
                break;
            }

            case UltimateAdapter.PINNED_HEADER_PUSHED_UP: {
                
                View firstView = getChildAt(0);
                
                if (firstView != null) {
                    
	                int bottom = firstView.getBottom();
	                int headerHeight = mHeaderView.getHeight();
	                int y;
	                
	                if (bottom < headerHeight) {
	                    y = (bottom - headerHeight);
	                } else {
	                    y = 0;
	                }
	                
	                mAdapter.configureHeader(mHeaderView, section);
	                
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
}
