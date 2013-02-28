package com.moneydesktop.finance.views.chart;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BasePieChartAdapter extends BaseAdapter {

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		throw new RuntimeException(
				"For PieChartAdapter, use getSlice(int position, float offset, ViewGroup parent) instead of "
						+ "getView(int position, View convertView, ViewGroup parent)");
	}
	
	public abstract PieSliceDrawable getSlice(View view, int position, float offset);
	public abstract float getPercent(int position);
}
