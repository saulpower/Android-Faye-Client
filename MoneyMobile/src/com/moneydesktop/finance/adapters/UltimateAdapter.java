package com.moneydesktop.finance.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;

import com.moneydesktop.finance.views.UltimateListView;

public abstract class UltimateAdapter extends BaseExpandableListAdapter implements OnScrollListener {
    
	public static final String TAG = UltimateAdapter.class.getSimpleName();
	
	private boolean mAutomaticSectionLoading = false;
    
	private boolean[] mVisible;
    private boolean[] mLoaded;
    private boolean[] mLoading;
    
    protected int mSelectedGroupPosition = -1, mSelectedChildPosition = -1;

    private boolean mInitialized = false;

    /**
     * Pinned header state: don't show the header.
     */
    public static final int PINNED_HEADER_GONE = 0;

    /**
     * Pinned header state: show the header at the top of the list.
     */
    public static final int PINNED_HEADER_VISIBLE = 1;

    /**
     * Pinned header state: show the header. If the header extends beyond
     * the bottom of the first shown element, push it up and clip.
     */
    public static final int PINNED_HEADER_PUSHED_UP = 2;
    
    public int getSelectedGroupPosition() {
        return mSelectedGroupPosition;
    }

    public void setSelectedGroupPosition(int mSelectedGroupPosition) {
        this.mSelectedGroupPosition = mSelectedGroupPosition;
    }

    public int getSelectedChildPosition() {
        return mSelectedChildPosition;
    }

    public void setSelectedChildPosition(int mSelectedChildPosition) {
        this.mSelectedChildPosition = mSelectedChildPosition;
    }

    public void setAutomaticSectionLoading(boolean automaticSectionLoading) {
        mAutomaticSectionLoading = automaticSectionLoading;
    }
    
    /**
     * Computes the desired state of the pinned header for the given
     * position of the first visible list item. Allowed return values are
     * {@link #PINNED_HEADER_GONE}, {@link #PINNED_HEADER_VISIBLE} or
     * {@link #PINNED_HEADER_PUSHED_UP}.
     */
    public int getPinnedHeaderState(int position, int section) {
        
    	if (section < 0 || getGroupCount() == 0) {
    		return PINNED_HEADER_GONE;
    	}
    	
    	// The header should get pushed up if the top item shown
    	// is the last item in a section for a particular letter.
        int nextSectionPosition = getPositionForSection(section + 1);
        
    	if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
    		return PINNED_HEADER_PUSHED_UP;
    	}
    	
    	return PINNED_HEADER_VISIBLE;
    }
    
    protected boolean isSectionLoaded(int section) {
        
        initializeLoaded();
        
        return mLoaded[section];
    }
    
    protected boolean isInitialized() {
        return mInitialized;
    }
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view instanceof UltimateListView) {
			((UltimateListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// nop
	}
	
	private void initializeLoaded() {
	    
	    if (mLoaded == null) {
            mLoaded = new boolean[getGroupCount()];
            mLoading = new boolean[getGroupCount()];
            
            for (int i = 0; i < getGroupCount(); i++) {
                mLoaded[i] = getChildrenCount(i) != 0;
            }
            
            mInitialized = true;
        }
	}
	
	protected int getLoadedAdjustment(int size, int groupPosition) {
	    
	    if (size == 0) {
            size += (isSectionLoaded(groupPosition) || !isInitialized() ? 0 : 1);
        }
	    
	    return size;
	}
	
	@Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        
        View res = getItemView(groupPosition, childPosition, isLastChild, convertView, parent);
        res.requestLayout();
        
        return res;
	}

	@Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		
        initializeLoaded();
	    
		View res = getSectionView(groupPosition, isExpanded, convertView, parent);
		res.requestLayout();
		
		return res;
	}
	
	@Override
	public void onGroupExpanded(int groupPosition) {
	    
	    if (mVisible == null) {
	        mVisible = new boolean[getGroupCount()];
	    }
	    
	    initializeLoaded();
	    
        if (mAutomaticSectionLoading && !mLoaded[groupPosition] && !mLoading[groupPosition]) {
            mLoading[groupPosition] = true;
            onSectionRequested(groupPosition);
        }
	    
	    mVisible[groupPosition] = true;
	}
    
    @Override
    public void onGroupCollapsed(int groupPosition) {
        
        if (mVisible == null) {
            mVisible = new boolean[getGroupCount()];
        }

        mVisible[groupPosition] = false;
    }
    
    public boolean isSectionVisible(int groupPosition) {
        
        boolean result = false;
        
        if (mVisible != null && mVisible.length > groupPosition) {
            result = mVisible[groupPosition];
        }
        
        return result;
    }
	
	protected void sectionLoaded(int section, boolean isLoaded) {
	    
	    mLoading[section] = false;
	    mLoaded[section] = isLoaded;
	    notifyDataSetChanged();
	}
	
	/**
	 * The section is requested to be seen, so do the request 
	 * and call {@link UltimateListView#tellNoMoreData()} if there is no more pages.
	 * 
	 * @param section the section number to load.
	 */
	protected abstract void onSectionRequested(int section);
	
	/**
	 * read: get view too
	 */
	public abstract View getItemView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent);
    public abstract View getSectionView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent);

    /**
     * Configures the pinned header view to match the first visible list item.
     *
     * @param header pinned header view.
     * @param position position of the first visible list item.
     * @param alpha fading of the header view, between 0 and 255.
     */
    public abstract void configureHeader(View header, int section);

    public int getPositionForSection(int section) {

        if (section < 0 || section >= getGroupCount()) {
            return 0;
        }
        
        int count = 0;

        for (int i = 0; i < getGroupCount(); i++) {

            if (section == i) {
                return count;
            }

            if (isSectionVisible(i)) {
                count += getChildrenCount(i);
            }
            count++;
        }

        return 0;
    }

    public int getSectionForPosition(int position) {

        int count = 0;

        for (int i = 0; i < getGroupCount(); i++) {

            int top = count + 1;
            
            if (isSectionVisible(i)) {
                top += getChildrenCount(i);
            }
            
            if (position >= count && position < top) {
                return i;
            }
            
            if (isSectionVisible(i)) {
                count += getChildrenCount(i);
            }
            count++;
        }

        return -1;
    }
}
