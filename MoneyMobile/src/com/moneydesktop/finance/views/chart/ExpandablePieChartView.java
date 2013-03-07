package com.moneydesktop.finance.views.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.moneydesktop.finance.views.FrictionDynamics;
import com.moneydesktop.finance.views.chart.PieChartView.OnInfoClickListener;
import com.moneydesktop.finance.views.chart.PieChartView.OnPieChartChangeListener;
import com.moneydesktop.finance.views.chart.PieChartView.OnPieChartExpandListener;
import com.moneydesktop.finance.views.chart.PieChartView.OnPieChartReadyListener;
import com.moneydesktop.finance.views.chart.PieChartView.OnRotationStateChangeListener;
import com.moneydesktop.finance.views.chart.PieChartView.PieChartAnchor;

public class ExpandablePieChartView extends AdapterView<Adapter> implements OnInfoClickListener {
    
    public final String TAG = this.getClass().getSimpleName();
    
    /** The center point of the chart */
    private PointF mCenter = new PointF();

	private Bitmap mDrawingCache;
	
	BitmapDrawable mDrawableCache;
    
    private PieChartView mGroupChart, mChildChart;
    
    private boolean mGroupReady = false;
    
    private boolean mChildReady = false;
    
    private OnPieChartReadyListener mOnPieChartReadyListener;
	
    /** Bridge Adapter used to manage the two adapters for the two pie charts */
	private PieChartBridgeAdapter mBridgeAdapter;
	
	private AdapterDataSetObserver mDataSetObserver;
	
	private OnExpandablePieChartChangeListener mExpandableChartChangeListener;
	
	private OnExpandablePieChartInfoClickListener mExpandablePieChartInfoClickListener;
	
	private OnPieChartChangeListener mGroupListener = new OnPieChartChangeListener() {

		@Override
		public void onSelectionChanged(int index) {
			
			mBridgeAdapter.setGroupPosition(index);
			configureInfo();
			
			// Propagate change listener to other listeners
			if (mExpandableChartChangeListener != null) {
				mExpandableChartChangeListener.onGroupChanged(index);
			}
		}
	};
	
	private OnPieChartChangeListener mChildListener = new OnPieChartChangeListener() {

		@Override
		public void onSelectionChanged(int index) {
			
			configureInfo(index);
			
			// Propagate change listener to other listeners
			if (mExpandableChartChangeListener != null) {
				mExpandableChartChangeListener.onChildChanged(mBridgeAdapter.getGroupPosition(), index);
			}		
		}
	};
	
	private PieChartView.OnItemClickListener mOnGroupChartClicked = new PieChartView.OnItemClickListener() {
		
		@Override
		public void onItemClick(boolean secondTap, View parent, Drawable drawable, int position, long id) {
			
			if (secondTap) {
				toggleGroup();
				return;
			}
		}
	};
	
	private OnPieChartExpandListener mOnPieChartExpandListener = new OnPieChartExpandListener() {

		@Override
		public void onPieChartExpanded() {
			
			if (mExpandableChartChangeListener != null) {
				mExpandableChartChangeListener.onGroupExpanded(mBridgeAdapter.getGroupPosition(), mChildChart.getCurrentIndex());
			}
		}

		@Override
		public void onPieChartCollapsed() {
			
			if (mExpandableChartChangeListener != null) {
				mExpandableChartChangeListener.onGroupCollapsed(mBridgeAdapter.getGroupPosition());
			}
		}
	};
	
	public Bitmap getDrawingCache() {
		return mDrawingCache;
	}
	
	public void setExpandableChartChangeListener(
			OnExpandablePieChartChangeListener mExpandableChartChangeListener) {
		this.mExpandableChartChangeListener = mExpandableChartChangeListener;
	}

	public void setExpandablePieChartInfoClickListener(
			OnExpandablePieChartInfoClickListener mExpandablePieChartInfoClickListener) {
		this.mExpandablePieChartInfoClickListener = mExpandablePieChartInfoClickListener;
	}

	public void setOnPieChartReadyListener(
			OnPieChartReadyListener mOnPieChartReadyListener) {
		this.mOnPieChartReadyListener = mOnPieChartReadyListener;
	}
	
	public void toggleGroup() {
		
		int state = mChildChart.toggleChart();
		
		switch (state) {
			case PieChartView.CHART_SHOWING:
	
				configureInfo(mChildChart.getCurrentIndex());
				
				break;
				
			case PieChartView.CHART_HIDDEN:
	
				configureInfo();
				
				break;
		}
	}

	private void configureInfo() {
		
		int groupPosition = mBridgeAdapter.getGroupPosition();
		InfoDrawable info = mChildChart.getInfoDrawable();
		PieSliceDrawable slice = mGroupChart.getSlice(groupPosition);
		
		mBridgeAdapter.getExpandableAdapter().configureGroupInfo(info, slice, groupPosition);
		updateCache();
	}
	
	private void configureInfo(int childPosition) {
		
		if (!mChildChart.isLoaded()) return;
		
		int groupPosition = mBridgeAdapter.getGroupPosition();
		InfoDrawable info = mChildChart.getInfoDrawable();
		PieSliceDrawable slice = mChildChart.getSlice(childPosition);
		
		mBridgeAdapter.getExpandableAdapter().configureChildInfo(info, slice, groupPosition, childPosition);
		updateCache();
	}
	
	private OnRotationStateChangeListener mGroupRotationListener = new OnRotationStateChangeListener() {
		
		@Override
		public void onRotationStateChange(int state) {
			
			switch (state) {
			
			case PieChartView.TOUCH_STATE_ROTATE:
				mChildChart.hideChart();
				updateCache();
				break;
			}
		}
	};

	public ExpandablePieChartView(Context context) {
		this(context, null);
	}

	public ExpandablePieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 1);
	}

	public ExpandablePieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * Pause the SurfaceView thread from rendering so not
	 * to impact UI thread performance.
	 */
	public void onPause() {
		
		if (mChildChart == null || mGroupChart == null) return;
		
		mGroupChart.getDrawThread().onPause();
		mChildChart.getDrawThread().onPause();
	}
	
	/**
	 * Resume the SurfaceView thread to render
	 * the Pie Charts.
	 */
	public void onResume() {
		
		if (mChildChart == null || mGroupChart == null) return;

		mChildChart.getDrawThread().onResume();
		mGroupChart.getDrawThread().onResume();
	}
	
	/**
	 * Provides a check to see if the child pie chart is expanded
	 * 
	 * @return True if the child pie chart is expanded
	 */
	public boolean isExpanded() {
		
		if (mChildChart == null) return false;
		
		return !mChildChart.isChartHidden();
	}
	
	/**
	 * Update the drawing cache with the latest changes
	 * from the Pie Chart
	 */
	private void updateCache() {
		
		// Wait for the info panel transition
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				createCache();
			}
		}, 400);
	}
	
	/**
	 * Create the background drawable for when
	 * the chart is moved.
	 */
	private void createCache() {
		
		new AsyncTask<Void, Void, Boolean>() {
    		
			@Override
			protected Boolean doInBackground(Void... params) {

				if (mDrawingCache == null) {
					mDrawingCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
				}

				Canvas cache = new Canvas(mDrawingCache);
				mGroupChart.getDrawThread().drawCache(cache);
				mChildChart.getDrawThread().drawCache(cache);
				
				mDrawableCache = new BitmapDrawable(getResources(), mDrawingCache);

				return true;
			}
    		
    		@Override
    		protected void onPostExecute(Boolean result) {

				setCachedBackground(mDrawableCache);
				
				if (mOnPieChartReadyListener != null) {
					mOnPieChartReadyListener.onPieChartReady();
				}
    		}
			
		}.execute();
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setCachedBackground(Drawable drawable) {
		
		int sdk = android.os.Build.VERSION.SDK_INT;
		
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
		    setBackgroundDrawable(drawable);
		} else {
		    setBackground(drawable);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		// if we don't have an adapter, we don't need to do anything
        if (mBridgeAdapter == null) {
        	resetChart();
            return;
        }
        
		if (getChildCount() == 0) {
			
			addPieCharts();
			
			// Get the center coordinates of the view
			mCenter.x = getWidth() / 2f;
			mCenter.y = getHeight() / 2f;
		}
	}
	
	private void addPieCharts() {
		
		mChildChart = new PieChartView(getContext(), new OnPieChartReadyListener() {
			
			@Override
			public void onPieChartReady() {
				
				mChildReady = true;
				readyCheck();
			}
		});
		mChildChart.setOnInfoClickListener(this);
		mChildChart.setOnPieChartExpandListener(mOnPieChartExpandListener);
		mChildChart.setOnPieChartChangeListener(mChildListener);
		mChildChart.setDynamics(new FrictionDynamics(0.95f));
		mChildChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mChildChart.showInfo();

		mGroupChart = new PieChartView(getContext(), new OnPieChartReadyListener() {
			
			@Override
			public void onPieChartReady() {
				
				mGroupReady = true;
				readyCheck();
			}
		});
		mGroupChart.setDynamics(new FrictionDynamics(0.95f));
		mGroupChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mGroupChart.setOnRotationStateChangeListener(mGroupRotationListener);
		mGroupChart.setOnPieChartChangeListener(mGroupListener);
		mGroupChart.setOnItemClickListener(mOnGroupChartClicked);
		
		initializeChartData();
		
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		int height = getHeight() - getPaddingTop() - getPaddingBottom();
		
		addAndMeasureChart(mGroupChart, 0, width, height);
		int subChartSize = (int) (mGroupChart.getChartDiameter() * 7 / 10);
		addAndMeasureChart(mChildChart, 1, subChartSize, subChartSize);
		
		mChildChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
		mGroupChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
	}
	
	/**
	 * Checks to see if the Group and Child pie charts
	 * have been initialized
	 */
	private void readyCheck() {
		
		if (mGroupReady && mChildReady) {
			
			updateCache();
		}
	}

    /**
     * Adds a view as a child view and takes care of measuring it
     * 
     * @param child The view to add
     * @param layoutMode Either LAYOUT_MODE_ABOVE or LAYOUT_MODE_BELOW
     */
    private void addAndMeasureChart(final PieChartView chart, final int index, int width, int height) {

        LayoutParams params = chart.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        addViewInLayout(chart, index, params, false);
        
        chart.measure(MeasureSpec.EXACTLY | width, MeasureSpec.EXACTLY | height);
    }
	
	private void initializeChartData() {
		
		if (mGroupChart == null || mChildChart == null) return;
		
		mGroupChart.setAdapter(mBridgeAdapter.getGroupAdapter());
		mChildChart.setAdapter(mBridgeAdapter.getChildAdapter());
	}
	
	private void resetChart() {
		
		removeAllViewsInLayout();
		invalidate();
	}

	@Override
	public Adapter getAdapter() {
		throw new RuntimeException(
				"For ExpandablePieChart, use getExpandablePieChartAdapter() instead of "
						+ "getAdapter()");
	}

	@Override
	public View getSelectedView() {
		throw new RuntimeException("Not Supported");
	}

	@Override
	public void setAdapter(Adapter adapter) {
		throw new RuntimeException(
				"For ExpandablePieChart, use setAdapter(ExpandablePieChartAdapter) instead of "
						+ "setAdapter(Adapter)");
		
	}

	@Override
	public void setSelection(int position) {
		
		if (mGroupChart != null) {
			mGroupChart.setSelection(position);
		}
	}
	
	public void setGroupSelection(int groupPosition) {
		setSelection(groupPosition);
	}
	
	public int getSelectedGroup() {
		return mBridgeAdapter.getGroupPosition();
	}
	
	public int getSelectedChild() {
		
		if (!isExpanded()) return -1;
		
		return mChildChart.getCurrentIndex();
	}
	
	public void setChildSelection(int childPosition) {
		
		if (mChildChart != null) {
			mChildChart.setSelection(childPosition);
		}
	}
	
	public BaseExpandablePieChartAdapter getPieChartAdapter() {
		return mBridgeAdapter.getExpandableAdapter();
	}

	public void setAdapter(BaseExpandablePieChartAdapter adapter) {
		
		if (mBridgeAdapter != null && mBridgeAdapter.getExpandableAdapter() != null && mDataSetObserver != null) {
			mBridgeAdapter.getExpandableAdapter().unregisterDataSetObserver(mDataSetObserver);
		}
		
		resetChart();
		
		mBridgeAdapter = new PieChartBridgeAdapter(adapter);
		
		if (mBridgeAdapter.getExpandableAdapter() != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mBridgeAdapter.getExpandableAdapter().registerDataSetObserver(mDataSetObserver);
		}
		
        removeAllViewsInLayout();
        requestLayout();
	}

	@Override
	public void onInfoClicked(int index) {
		
		if (mExpandablePieChartInfoClickListener != null) {
			mExpandablePieChartInfoClickListener.onInfoClicked(mBridgeAdapter.getGroupPosition(), index);
		}
	}
	
	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged() {
			
			initializeChartData();
			
			// Detect the case where a cursor that was previously invalidated
			// has been re-populated with new data.
			if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds() && mInstanceState != null) {
				
				ExpandablePieChartView.this.onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			}
		}

		@Override
		public void onInvalidated() {

			if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds()) {
				
				// Remember the current state for the case where our hosting
				// activity is being stopped and later restarted
				mInstanceState = ExpandablePieChartView.this.onSaveInstanceState();
			}

        	resetChart();

			requestLayout();
		}

		public void clearSavedState() {
			mInstanceState = null;
		}
	}
	
	public interface OnExpandablePieChartChangeListener {
		
		/**
		 * Notify that the group chart has changed
		 * 
		 * @param groupPosition The currently selected groupPosition
		 */
		public void onGroupChanged(int groupPosition);
		
		/**
		 * Notify that the child chart has changed
		 * 
		 * @param groupPosition The currently selected groupPosition
		 * @param childPosition The currently selected childPosition
		 */
		public void onChildChanged(int groupPosition, int childPosition);
		public void onGroupExpanded(int groupPosition, int childPosition);
		public void onGroupCollapsed(int groupPosition);
	}
	
	public interface OnExpandablePieChartInfoClickListener {
		
		/**
		 * Notify the info panel has been clicked
		 * 
		 * @param groupPosition The currently selected groupPosition
		 * @param childPosition The currently selected childPosition.  Will return
		 * 			-1 if child chart is not currently showing.
		 */
		public void onInfoClicked(int groupPosition, int childPosition);
	}
}
