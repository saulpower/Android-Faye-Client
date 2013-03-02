package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.adapter.GrowPagerAdapter.OnScrollStateChangedListener;
import com.moneydesktop.finance.shared.fragment.GrowFragment;
import com.moneydesktop.finance.views.chart.CategoryPieChartAdapter;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView;

public class SpendingChartHandsetFragment extends GrowFragment implements OnScrollStateChangedListener {
	
    public final String TAG = this.getClass().getSimpleName();
    
    private ExpandablePieChartView mChart;
    
    private boolean mShowing = false;
    
	public static SpendingChartHandsetFragment getInstance(int position) {
	    
	    SpendingChartHandsetFragment fragment = new SpendingChartHandsetFragment();
	    fragment.setRetainInstance(true);
		
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        
        return fragment;
	}

	@Override
	public FragmentType getType() {
		return null;
	}
	
	@Override
	public void transitionFragment(float percent, float scale) {
		super.transitionFragment(percent, scale);
		
		mShowing = percent < 0.01f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_spending_summary_view, null);
		
		CategoryPieChartAdapter adapter = new CategoryPieChartAdapter(getActivity());
		
		mChart = (ExpandablePieChartView) mRoot.findViewById(R.id.chart);
		mChart.setAdapter(adapter);
		
		return mRoot;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mAdapter.setOnScrollStateChangedListener(this);
	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}

	@Override
	public void onScrollStateChanged(int state) {
		
		if (ViewPager.SCROLL_STATE_IDLE == state && mShowing && mChart.getVisibility() != View.VISIBLE) {

			mChart.setVisibility(View.VISIBLE);
			
		} else if (ViewPager.SCROLL_STATE_IDLE != state) {

			mChart.setVisibility(View.GONE);
		}
	}
}
