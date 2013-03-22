package com.moneydesktop.finance.views.piechart;

public class PieChartBridgeAdapter {

	private BaseExpandablePieChartAdapter mExpandableAdapter;
	private PieChartChildAdapter mChildAdapter;
	private PieChartGroupAdapter mGroupAdapter;
	
	private int mGroupPosition = 0;
	
	public PieChartBridgeAdapter(BaseExpandablePieChartAdapter expandableAdapter) {
		this(expandableAdapter, 0);
	}
	
	public PieChartBridgeAdapter(BaseExpandablePieChartAdapter expandableAdapter, int defaultGroupPosition) {
		mExpandableAdapter = expandableAdapter;
		
		mGroupAdapter = new PieChartGroupAdapter(mExpandableAdapter);
		setGroupPosition(defaultGroupPosition);
	}
	
	public BaseExpandablePieChartAdapter getExpandableAdapter() {
		return mExpandableAdapter;
	}
	
	public void setExpandableAdapter(BaseExpandablePieChartAdapter mExpandableAdapter) {
		this.mExpandableAdapter = mExpandableAdapter;
		mGroupAdapter = new PieChartGroupAdapter(mExpandableAdapter);
	}
	
	public PieChartChildAdapter getChildAdapter() {
		
		if (mChildAdapter == null) {
			mChildAdapter = new PieChartChildAdapter(mExpandableAdapter, 0);
		}
		
		return mChildAdapter;
	}
	
	public PieChartGroupAdapter getGroupAdapter() {
		return mGroupAdapter;
	}
	
	public void setGroupPosition(int groupPosition) {
		
		mGroupPosition = groupPosition;
		
		if (mChildAdapter == null) {
			mChildAdapter = new PieChartChildAdapter(mExpandableAdapter, groupPosition);
		} else {
			mChildAdapter.setGroupPosition(groupPosition);
		}
	}
	
	public int getGroupPosition() {
		return mGroupPosition;
	}
}
