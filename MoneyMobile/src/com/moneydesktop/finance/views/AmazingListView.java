package com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.moneydesktop.finance.adapters.AmazingAdapter;
import com.moneydesktop.finance.adapters.AmazingAdapter.HasMorePagesListener;

/**
 * A ListView that maintains a header pinned at the top of the list. The
 * pinned header can be pushed up and dissolved as needed.
 * 
 * It also supports pagination by setting a custom view as the loading
 * indicator.
 */
public class AmazingListView extends ListView implements HasMorePagesListener {
    
	public static final String TAG = AmazingListView.class.getSimpleName();
	
	View mListFooter;
	boolean mFooterViewAttached = false;
    boolean mEmptyViewAttached = false;

	private View mEmptyFooter;
	
    private View mHeaderView;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;
    private int mHeaderViewHeight;

    private AmazingAdapter mAdapter;
    
    private boolean mIsPressed = false;
    private boolean mHeaderClicked = false;
    
    private OnSectionHeaderClickListener mSectionHeaderClickListener;
    
    public OnSectionHeaderClickListener getOnSectionHeaderClickListener() {
        return mSectionHeaderClickListener;
    }

    public void setOnSectionHeaderClickListener(OnSectionHeaderClickListener sectionHeaderClickListener) {
        this.mSectionHeaderClickListener = sectionHeaderClickListener;
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;
        
        // Disable vertical fading when the pinned header is present
        // TODO change ListView to allow separate measures for top and bottom fading edge;
        // in this particular case we would like to disable the top, but not the bottom edge.
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
                    
                    if (mSectionHeaderClickListener != null) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        mSectionHeaderClickListener.onSectionHeaderClicked(mHeaderView, mAdapter.getSectionForPosition(getFirstVisiblePosition()));
                        mHeaderClicked = true;
                    }
                    
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
        
        if (!mHeaderClicked && getOnItemClickListener() != null) {
            
            getOnItemClickListener().onItemClick(this, view, position, id);
            playSoundEffect(SoundEffectConstants.CLICK);
            
            return true;
        }

        mHeaderClicked = false;
        
        return false;
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
    
    public void resetHeaderView(int position) {
        
        mAdapter.configurePinnedHeader(mHeaderView, position, 255);
        if (mHeaderView.getTop() != 0) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
        }
        mHeaderViewVisible = true;
        
        invalidate();
    }
    
    public void configureHeaderView() {
        configureHeaderView(getFirstVisiblePosition());
    }

    public void configureHeaderView(int position) {
        if (mHeaderView == null) {
            return;
        }

        int state = mAdapter.getPinnedHeaderState(position);
        
        switch (state) {
            case AmazingAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case AmazingAdapter.PINNED_HEADER_VISIBLE: {
            	mAdapter.configurePinnedHeader(mHeaderView, position, 255);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            }

            case AmazingAdapter.PINNED_HEADER_PUSHED_UP: {
                
                View firstView = getChildAt(0);
                
                if (firstView != null) {
                    
	                int bottom = firstView.getBottom();
	                int headerHeight = mHeaderView.getHeight();
	                int y;
	                int alpha;
	                
	                if (bottom < headerHeight) {
	                    y = (bottom - headerHeight);
	                    alpha = 255; // * (headerHeight + y) / headerHeight;
	                } else {
	                    y = 0;
	                    alpha = 255;
	                }
	                
	                mAdapter.configurePinnedHeader(mHeaderView, position, alpha);
	                
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
    
    public AmazingListView(Context context) {
        super(context);
    }

    public AmazingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AmazingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setLoadingView(View listFooter) {
		this.mListFooter = listFooter;
	}
    
    public View getLoadingView() {
		return mListFooter;
	}
    
    public void setEmptyView(View emptyFooter) {
        this.mEmptyFooter = emptyFooter;
    }
    
    public View getEmptyView() {
        return mEmptyFooter;
    }
    
    @Override
    public void setAdapter(ListAdapter adapter) {
    	if (!(adapter instanceof AmazingAdapter)) {
    		throw new IllegalArgumentException(AmazingListView.class.getSimpleName() + " must use adapter of type " + AmazingAdapter.class.getSimpleName());
    	}
    	
    	// previous adapter
    	if (this.mAdapter != null) {
    		this.mAdapter.setHasMorePagesListener(null);
    		this.setOnScrollListener(null);
    	}
    	
    	this.mAdapter = (AmazingAdapter) adapter;
    	((AmazingAdapter) adapter).setHasMorePagesListener(this);
		this.setOnScrollListener((AmazingAdapter) adapter);
    	
		View dummy = new View(getContext());
    	super.addFooterView(dummy);
    	super.setAdapter(adapter);
    	super.removeFooterView(dummy);
    }
    
    @Override
    public AmazingAdapter getAdapter() {
    	return mAdapter;
    }

	@Override
	public void noMorePages() {
	    
		if (mListFooter != null) {
            this.removeFooterView(mListFooter);
            mFooterViewAttached = false;
        }
		    
	    if (!mEmptyViewAttached && mAdapter.getCount() == 0 && mEmptyFooter != null) {
	        this.addFooterView(mEmptyFooter);
	        mEmptyViewAttached = true;
	    } else if (mEmptyViewAttached && mAdapter.getCount() != 0) {
            this.removeFooterView(mEmptyFooter);
            mEmptyViewAttached = false;
	    }
	}

	@Override
	public void mayHaveMorePages() {
	    
		if (!mFooterViewAttached && mListFooter != null) {
		    this.removeFooterView(mEmptyFooter);
			this.addFooterView(mListFooter);
			mFooterViewAttached = true;
			mEmptyViewAttached = false;
		}
	}
	
	public boolean isLoadingViewVisible() {
		return mFooterViewAttached;
	}
	
	public interface OnSectionHeaderClickListener {
	    public void onSectionHeaderClicked(View view, int section);
	}
}
