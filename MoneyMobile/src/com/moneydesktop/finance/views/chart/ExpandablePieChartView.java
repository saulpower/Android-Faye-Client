package com.moneydesktop.finance.views.chart;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.MeasureSpec;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.CaretDrawable;
import com.moneydesktop.finance.views.FrictionDynamics;
import com.moneydesktop.finance.views.chart.PieChartView.PieChartAnchor;

public class ExpandablePieChartView extends AdapterView<Adapter> {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private static final int STROKE_WIDTH = 3;
	
	/** User is not touching the list */
    private static final int TOUCH_STATE_RESTING = 0;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    /** Current touch state */
    private int mTouchState = TOUCH_STATE_RESTING;
    
    /** Distance to drag before we intercept touch events */
    private int mScrollThreshold;

    /** X-coordinate of the down event */
    private int mTouchStartX;

    /** Y-coordinate of the down event */
    private int mTouchStartY;
    
    /** The center point of the chart */
    private PointF mCenter = new PointF();
    
    private PieChartView mBaseChart, mSubChart;
	
	private Paint mPaint;
	private Paint mStrokePaint;
	private CaretDrawable mCaret;
	
	private float mInfoRadius;
	
	private List<Float> mBaseSlices = new ArrayList<Float>();
	private List<Float> mSubSlices = new ArrayList<Float>();
	
	private BaseExpandablePieChartAdapter mAdapter;
	
	private AdapterDataSetObserver mDataSetObserver;
	
	private boolean mDataChanged = false;
	
	private int mGroupCount = 0;

	public ExpandablePieChartView(Context context) {
		this(context, null);
	}

	public ExpandablePieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 1);
	}

	public ExpandablePieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mScrollThreshold = ViewConfiguration.get(context).getScaledTouchSlop();
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.WHITE);
		
		mStrokePaint = new Paint(mPaint);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(UiUtils.getDynamicPixels(context, STROKE_WIDTH));
		mStrokePaint.setColor(Color.BLACK);
		mStrokePaint.setAlpha(50);
		
		setDrawingCacheEnabled(true);
	}

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
    	
    	if (!inCircle((int) event.getX(), (int) event.getY())) return false;
    	
        switch (event.getAction()) {
        
            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                return true;

            default:
                endTouch(event.getX(), event.getY(), 0);
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
    	
        if (!inCircle((int) event.getX(), (int) event.getY())) {
        	return false;
        }
        
        switch (event.getAction()) {
        
            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                break;

            case MotionEvent.ACTION_MOVE:
            	
                if (mTouchState == TOUCH_STATE_CLICK) {
                	checkForClick(event);
                }
                
                break;

            case MotionEvent.ACTION_UP:
            	
            	float velocity = 0;
            	
                if (mTouchState == TOUCH_STATE_CLICK) {
                	
                	Log.i(TAG, "Clicked center");
                }

                endTouch(event.getX(), event.getY(), velocity);
                
                break;

            default:
                endTouch(event.getX(), event.getY(), 0);
                break;
        }
        
        return true;
    }

    /**
     * Sets and initializes all things that need to when we start a touch
     * gesture.
     * 
     * @param event The down event
     */
    private void startTouch(final MotionEvent event) {
    	
        // save the start place
        mTouchStartX = (int) event.getX();
        mTouchStartY = (int) event.getY();

        // we don't know if it's a click or a scroll yet, but until we know
        // assume it's a click
        mTouchState = TOUCH_STATE_CLICK;
    }

    /**
     * Resets and recycles all things that need to when we end a touch gesture
     */
    private void endTouch(final float x, final float y, final float velocity) {

        // reset touch state
        mTouchState = TOUCH_STATE_RESTING;
    }

    /**
     * Checks if the user has moved far enough for this to not be a
     * click.
     * 
     * @param event The (move) event
     * @return true if scroll was started, false otherwise
     */
    private boolean checkForClick(final MotionEvent event) {
    	
        final int xPos = (int) event.getX();
        final int yPos = (int) event.getY();
        
        if (isEnabled()
        		&& (xPos < mTouchStartX - mScrollThreshold
                || xPos > mTouchStartX + mScrollThreshold
                || yPos < mTouchStartY - mScrollThreshold
                || yPos > mTouchStartY + mScrollThreshold)) {
            
            mTouchState = TOUCH_STATE_RESTING;
            
            return true;
        }
        
        return false;
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

			mInfoRadius = mBaseChart.getChartRadius() * 2 / 5;
			
			// Get the center coordinates of the view
			mCenter.x = getWidth() / 2f;
			mCenter.y = getHeight() / 2f;
			
			createCaret();
		}
	}
	
	private void resetChart() {
		
		removeAllViewsInLayout();
		mDataChanged = false;
		invalidate();
	}
    
    private boolean inCircle(final int x, final int y) {
    	
        double dx = (x - mCenter.x) * (x - mCenter.x);
        double dy = (y - mCenter.y) * (y - mCenter.y);

        if ((dx + dy) < (mInfoRadius * mInfoRadius)) {
            return true;
        } else {
            return false;
        }
    }
	
	private void addPieCharts() {

		mBaseChart = new PieChartView(getContext());
		mBaseChart.setOnItemClickListener(new PieChartView.OnItemClickListener() {
			
			@Override
			public void onItemClick(View parent, Drawable drawable, int position, long id) {
				
				Log.i(TAG, "Item " + position + " clickd");
				
//				mSubChart.setVisibility(mSubChart.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);	
			}
		});
		
//		mSubChart = new PieChartView(getContext());
		
		initializeBaseChartData();
//		initializeSubChartData();
		
		addAndMeasureChart(mBaseChart, 0, getWidth(), getHeight());
		mBaseChart.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
		
		mBaseChart.setVisibility(View.GONE);
		
//		int subChartSize = (int) (mBaseChart.getChartDiameter() * 7 / 10);
//		int left = getWidth() / 2 - subChartSize / 2;
//		int top = getHeight() / 2 - subChartSize / 2;
//		
//		addAndMeasureChart(mSubChart, 1, subChartSize, subChartSize);
//		mSubChart.layout(left, top, left + subChartSize, top + subChartSize);
//		
//		mSubChart.setVisibility(View.INVISIBLE);
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
		final float total = getGroupTotal();
		
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			mBaseSlices.add(mAdapter.getGroupAmount(i) / total);
		}
		
		PieChartAdapter adapter = new PieChartAdapter(getContext(), mBaseSlices);
		
		mBaseChart.setDynamics(new FrictionDynamics(0.95f));
		mBaseChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mBaseChart.setAdapter(adapter);
	}
	
	private void initializeSubChartData() {
		
		if (mAdapter.getGroupCount() < 1) return;
		
		int groupPosition = 0;
		
		mSubSlices.clear();
		final float total = getChildTotal(groupPosition);
		
		for (int i = 0; i < mAdapter.getChildrenCount(groupPosition); i++) {
			mSubSlices.add(mAdapter.getChildAmount(groupPosition, i) / total);
		}
		
		PieChartAdapter adapter = new PieChartAdapter(getContext(), mSubSlices);
		
		mSubChart.setDynamics(new FrictionDynamics(0.95f));
		mSubChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mSubChart.setAdapter(adapter);
	}
	
	private float getGroupTotal() {
		
		float total = 0;
		
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			total += mAdapter.getGroupAmount(i);
		}
		
		return total;
	}
	
	private float getChildTotal(int groupPosition) {
		
		float total = 0;
		
		for (int i = 0; i < mAdapter.getChildrenCount(groupPosition); i++) {
			total += mAdapter.getChildAmount(groupPosition, i);
		}
		
		return total;
	}
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        canvas.drawCircle(mCenter.x, mCenter.y, mBaseChart.getChartRadius() * 2 / 5, mStrokePaint);
        mCaret.draw(canvas);
        canvas.drawCircle(mCenter.x, mCenter.y, mBaseChart.getChartRadius() * 2 / 5, mPaint);
    }
	
	private void createCaret() {
		
		PointF position = new PointF(mCenter.x - mInfoRadius / 2, mCenter.y + mInfoRadius / 3);
        mCaret = new CaretDrawable(getContext(), position, mInfoRadius, mInfoRadius);
        mCaret.setColor(Color.WHITE);
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
			
			mDataChanged = true;
			mGroupCount = getPieChartAdapter().getGroupCount();
			
			// Detect the case where a cursor that was previously invalidated
			// has been re-populated with new data.
			if (ExpandablePieChartView.this.getPieChartAdapter().hasStableIds() && mInstanceState != null) {
				
				ExpandablePieChartView.this.onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			}
			
			requestLayout();
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
