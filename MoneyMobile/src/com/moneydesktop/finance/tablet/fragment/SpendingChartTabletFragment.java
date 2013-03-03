package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.shared.adapter.GrowPagerAdapter.OnScrollStateChangedListener;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.chart.ChartListBridge;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView;

import de.greenrobot.event.EventBus;

public class SpendingChartTabletFragment extends SummaryTabletFragment implements OnScrollStateChangedListener {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private ChartListBridge mBridge;
    private ExpandablePieChartView mChart;
    private ListView mList;
    
    private TextView mTitle, mCategoryTitle, mTotal;
    
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
		
		setupView();
		
		mBridge = new ChartListBridge(getActivity(), mChart, mList, mTotal);
		
		return mRoot;
	}
	
	private void setupView() {

		mChart = (ExpandablePieChartView) mRoot.findViewById(R.id.chart);
		mList = (ListView) mRoot.findViewById(R.id.list);
		
		mTitle = (TextView) mRoot.findViewById(R.id.title);
		mCategoryTitle = (TextView) mRoot.findViewById(R.id.category_title);
		mTotal = (TextView) mRoot.findViewById(R.id.total);
		
		Fonts.applySecondaryItalicFont(mTitle, 14);
		Fonts.applyPrimaryBoldFont(mCategoryTitle, 12);
		Fonts.applyPrimaryBoldFont(mTotal, 12);
		
		mCategoryTitle.setText(mCategoryTitle.getText().toString().toUpperCase());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		EventBus.getDefault().register(this);
		mAdapter.setOnScrollStateChangedListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		EventBus.getDefault().unregister(this);
	}

    @Override
    public String getTitleText() {
        return getString(R.string.title_fragment_spending_summary);
    }
	
	@Override
	public void transitionFragment(float percent, float scale) {
		super.transitionFragment(percent, scale);
		
		mShowing = percent < 0.01f;
	}

	@Override
	public void onScrollStateChanged(int state) {
		
		if (ViewPager.SCROLL_STATE_IDLE == state && mShowing) {

			configureChart(true);
			
		} else if (ViewPager.SCROLL_STATE_IDLE != state) {

			configureChart(false);
		}
	}
    
	public void onEvent(NavigationEvent event) {
		
		if (event.isShowing() != null && event.getDirection() == null) {
			
			configureChart(!event.isShowing());
			return;
		}
		
		if (event.getMovingHome() != null) {
			configureChart(event.getMovingHome());
		}
	}
	
	private void configureChart(boolean showChart) {
		
		if (showChart && mShowing && mActivity.isOnHome()) {
			mChart.onResume();
		} else {
			mChart.onPause();
		}
	}
}
