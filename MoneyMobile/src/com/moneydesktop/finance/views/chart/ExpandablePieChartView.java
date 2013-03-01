package com.moneydesktop.finance.views.chart;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.views.FrictionDynamics;
import com.moneydesktop.finance.views.chart.PieChartView.OnPieChartChangeListener;
import com.moneydesktop.finance.views.chart.PieChartView.OnRotationStateChangeListener;
import com.moneydesktop.finance.views.chart.PieChartView.PieChartAnchor;

public class ExpandablePieChartView extends AdapterView<Adapter> {
    
    public final String TAG = this.getClass().getSimpleName();
    
    /** The center point of the chart */
    private PointF mCenter = new PointF();
    
    private PieChartView mBaseChart, mSubChart;
	
	private List<Float> mBaseSlices = new ArrayList<Float>();
	private List<Float> mSubSlices = new ArrayList<Float>();
	
	private BaseExpandablePieChartAdapter mAdapter;
	
	private AdapterDataSetObserver mDataSetObserver;
	
	private boolean mDataChanged = false;
	
	private int mGroupCount = 0;
	
	private OnPieChartChangeListener mBaseListener = new OnPieChartChangeListener() {

		@Override
		public void onSelectionChanged(int index) {
			Log.i(TAG, "BaseChartChange: " + index);
			initializeSubChartData(index);
		}
	};
	
	private OnPieChartChangeListener mSubListener = new OnPieChartChangeListener() {

		@Override
		public void onSelectionChanged(int index) {
			Log.i(TAG, "SubChartChange: " + index);
		}
	};
	
	private OnRotationStateChangeListener mBaseRotationListener = new OnRotationStateChangeListener() {
		
		@Override
		public void onRotationStateChange(int state) {
			mSubChart.hideChart();
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
		
		setDrawingCacheEnabled(true);
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		
		mBaseChart.setVisibility(visibility);
		mSubChart.setVisibility(visibility);
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
        
        boolean useHeight = height < width;
        int size = useHeight ? height : width;
        
		setMeasuredDimension(size, size);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		// if we don't have an adapter, we don't need to do anything
        if (mAdapter == null) {
        	resetChart();
            return;
        }
        
        if (mGroupCount == 0) {
        	resetChart();
        }
        
		if (getChildCount() == 0) {
			
			addPieCharts();
			
			// Get the center coordinates of the view
			mCenter.x = getWidth() / 2f;
			mCenter.y = getHeight() / 2f;
		}
	}
	
	private void resetChart() {
		
		removeAllViewsInLayout();
		mDataChanged = false;
		invalidate();
	}
	
	private void addPieCharts() {

		mBaseChart = new PieChartView(getContext());
		mBaseChart.setDynamics(new FrictionDynamics(0.95f));
		mBaseChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mBaseChart.setOnRotationStateChangeListener(mBaseRotationListener);
		mBaseChart.setOnPieChartChangeListener(mBaseListener);
		mBaseChart.setOnItemClickListener(new PieChartView.OnItemClickListener() {
			
			@Override
			public void onItemClick(boolean secondTap, View parent, Drawable drawable, int position, long id) {
				
				Category cat = (Category) mAdapter.getGroup(position);
				Log.i(TAG, "Item " + cat.getCategoryName() + " " + cat.getParentPercent() + " clicked");
				
				if (secondTap) {
					mSubChart.toggleChart();
				}
			}
		});
		
		mSubChart = new PieChartView(getContext());
		mSubChart.setOnPieChartChangeListener(mSubListener);
		mSubChart.setDynamics(new FrictionDynamics(0.95f));
		mSubChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mSubChart.showInfo();
		
		initializeBaseChartData();
		initializeSubChartData(0);
		
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		int height = getHeight() - getPaddingTop() - getPaddingBottom();
		
		addAndMeasureChart(mBaseChart, 0, width, height);
		mBaseChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
		
		int subChartSize = (int) (mBaseChart.getChartDiameter() * 7 / 10);
		
		addAndMeasureChart(mSubChart, 1, subChartSize, subChartSize);
		mSubChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
		
		buildDrawingCache();
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
	
	private void initializeBaseChartData() {
		
		mBaseSlices.clear();
		
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			mBaseSlices.add(mAdapter.getGroupAmount(i));
		}
		
		PieChartAdapter adapter = new PieChartAdapter(getContext(), mBaseSlices);
		mBaseChart.setAdapter(adapter);
	}
	
	private void initializeSubChartData(int groupPosition) {
		
		if (mAdapter.getGroupCount() <= groupPosition) return;
		
		mSubSlices.clear();
		
		for (int i = 0; i < mAdapter.getChildrenCount(groupPosition); i++) {
			mSubSlices.add(mAdapter.getChildAmount(groupPosition, i));
		}
		
		PieChartAdapter adapter = new PieChartAdapter(getContext(), mSubSlices);
		mSubChart.setAdapter(adapter);
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
		throw new RuntimeException("Not Supported");
	}
	
	public BaseExpandablePieChartAdapter getPieChartAdapter() {
		return mAdapter;
	}

	public void setAdapter(BaseExpandablePieChartAdapter adapter) {
		
		if (mAdapter != null && mDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		
		resetChart();
		
		mAdapter = adapter;
		
		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);
		}
		
        removeAllViewsInLayout();
        requestLayout();
	}
	
	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged() {
			
			Log.i(TAG, "onChanged");
			
			mDataChanged = true;
			mGroupCount = getPieChartAdapter().getGroupCount();
			
			initializeBaseChartData();
			
			// Detect the case where a cursor that was previously invalidated
			// has been re-populated with new data.
			if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds() && mInstanceState != null) {
				
				ExpandablePieChartView.this.onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			}
		}

		@Override
		public void onInvalidated() {
			
			mDataChanged = true;

			if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds()) {
				
				// Remember the current state for the case where our hosting
				// activity is being stopped and later restarted
				mInstanceState = ExpandablePieChartView.this.onSaveInstanceState();
			}
			
			mGroupCount = 0;

			requestLayout();
		}

		public void clearSavedState() {
			mInstanceState = null;
		}
	}
}
