package com.moneydesktop.finance.views.chart;

import java.util.List;

import android.content.Context;
import android.view.ViewGroup;

import com.moneydesktop.finance.data.Constant;

public class PieChartAdapter extends BasePieChartAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

	private Context mContext;
	private List<Float> mObjects;
	
	public PieChartAdapter(Context context, List<Float> objects) {
		init(context, objects);
	}
	
	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public Object getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public PieSliceView getSlice(int position, float offset, ViewGroup parent) {
		
		Float percent = (Float) getItem(position);
		
		position = position > 15 ? position % 16 : position;

		PieSliceView sliceView = new PieSliceView(mContext, offset, percent, getColor(Constant.RANDOM_COLORS[position]));
		
		return sliceView;
	}
	
	private void init(Context context, List<Float> objects) {
		
		mContext = context;
		mObjects = objects;
	}
	
	private int getColor(int colorResource) {
		return mContext.getResources().getColor(colorResource);
	}

	@Override
	public float getPercent(int position) {
		Float percent = (Float) getItem(position);
		
		return percent;
	}
}
