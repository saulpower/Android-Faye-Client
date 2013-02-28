package com.moneydesktop.finance.tablet.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.adapter.GrowPagerAdapter.OnScrollStateChangedListener;
import com.moneydesktop.finance.views.FrictionDynamics;
import com.moneydesktop.finance.views.chart.PieChartAdapter;
import com.moneydesktop.finance.views.chart.PieChartView;
import com.moneydesktop.finance.views.chart.PieChartView.PieChartAnchor;

public class SpendingChartTabletFragment extends SummaryTabletFragment implements OnScrollStateChangedListener {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private PieChartView mChart;
    private ImageView mChartImage;
    
    private boolean mShowing = false;
	
	public static SpendingChartTabletFragment newInstance(int position) {
		
		SpendingChartTabletFragment frag = new SpendingChartTabletFragment();
		
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        
        return frag;
	}

	@Override
	public FragmentType getType() {
		return FragmentType.ACCOUNT_SUMMARY;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_spending_summary_view, null);
		mChartImage = (ImageView) mRoot.findViewById(R.id.chart_image);
		
//		CategoryPieChartAdapter adapter = new CategoryPieChartAdapter();

		List<Float> slices = new ArrayList<Float>();
		
		slices.add(0.25f);
		slices.add(0.05f);
		slices.add(0.1f);
		slices.add(0.05f);
		slices.add(0.2f);
		slices.add(0.3f);
		slices.add(0.05f);
		
		PieChartAdapter adapter = new PieChartAdapter(getActivity(), slices);
		
		mChart = (PieChartView) mRoot.findViewById(R.id.chart);
		mChart.setCoverView(mChartImage);
		mChart.setDynamics(new FrictionDynamics(0.95f));
		mChart.setSnapToAnchor(PieChartAnchor.BOTTOM);
		mChart.setAdapter(adapter);
		
		return mRoot;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mAdapter.setOnScrollStateChangedListener(this);
	}

    @Override
    public String getTitleText() {
        return "Account Balances";
    }
	
	@Override
	public void transitionFragment(float percent, float scale) {
		super.transitionFragment(percent, scale);
		
		mShowing = percent < 0.01f;
	}

	@Override
	public void onScrollStateChanged(int state) {
		
		if (ViewPager.SCROLL_STATE_IDLE == state && mShowing && mChart.getVisibility() != View.VISIBLE) {

			mChart.setVisibility(View.VISIBLE);
			mChart.post(new Runnable() {
				
				@Override
				public void run() {
					mChartImage.setVisibility(View.INVISIBLE);
				}
			});
			
		} else if (ViewPager.SCROLL_STATE_IDLE != state) {

			mChartImage.setVisibility(View.VISIBLE);
			mChart.setVisibility(View.INVISIBLE);
		}
	}
}
