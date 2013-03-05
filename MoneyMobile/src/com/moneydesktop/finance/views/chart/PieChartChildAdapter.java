package com.moneydesktop.finance.views.chart;

public class PieChartChildAdapter extends BasePieChartAdapter {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private BaseExpandablePieChartAdapter mExpandableAdapter;
	private int mGroupPosition = -1;
	
	public int getGroupPosition() {
		return mGroupPosition;
	}

	public void setGroupPosition(int mGroupPosition) {
		this.mGroupPosition = mGroupPosition;
		notifyDataSetInvalidated();
	}

	public PieChartChildAdapter(BaseExpandablePieChartAdapter expandableAdapter, int groupPosition) {
		mExpandableAdapter = expandableAdapter;
		mGroupPosition = groupPosition;
	}
	
	@Override
	public int getCount() {
		
		if (mExpandableAdapter.getGroupCount() == 0 || mExpandableAdapter.getGroupCount() < mGroupPosition) {
			return 0;
		}
		
		return mExpandableAdapter.getChildrenCount(mGroupPosition);
	}

	@Override
	public Object getItem(int position) {
		return mExpandableAdapter.getChild(mGroupPosition, position);
	}

	@Override
	public PieSliceDrawable getSlice(PieChartView parent, PieSliceDrawable convertDrawable, int position, float offset) {
		return mExpandableAdapter.getChildSlice(parent, convertDrawable, mGroupPosition, position, offset);
	}

	@Override
	public float getPercent(int position) {
		return mExpandableAdapter.getChildAmount(mGroupPosition, position);
	}
}
