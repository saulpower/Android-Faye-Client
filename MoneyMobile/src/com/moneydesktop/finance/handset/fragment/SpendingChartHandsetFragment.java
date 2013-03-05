package com.moneydesktop.finance.handset.fragment;

import net.simonvt.menudrawer.MenuDrawer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity.OnMenuChangeListener;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.shared.adapter.CategoryPieChartAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView;
import com.moneydesktop.finance.views.chart.PieChartView.OnPieChartReadyListener;

import de.greenrobot.event.EventBus;

public class SpendingChartHandsetFragment extends BaseFragment implements OnMenuChangeListener {
	
    public final String TAG = this.getClass().getSimpleName();
    
    private ExpandablePieChartView mChart;
    private TextView mTitle;

	public static SpendingChartHandsetFragment newInstance() {
	    
	    SpendingChartHandsetFragment fragment = new SpendingChartHandsetFragment();
	    fragment.setRetainInstance(true);
	    
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
     
        if (mActivity instanceof DashboardHandsetActivity) {
        	((DashboardHandsetActivity) mActivity).setOnMenuChangeListener(this);
        }
        
        EventBus.getDefault().register(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
	}

	@Override
	public FragmentType getType() {
		return FragmentType.SPENDING;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		Log.i(TAG, "OnCreateView");
		
		mRoot = inflater.inflate(R.layout.handset_spending_view, null);
		
		mTitle = (TextView) mRoot.findViewById(R.id.title);
		Fonts.applySecondaryItalicFont(mTitle, 8);
		
		mChart = (ExpandablePieChartView) mRoot.findViewById(R.id.chart);
		mChart.setOnPieChartReadyListener(new OnPieChartReadyListener() {
			
			@Override
			public void onPieChartReady() {
				EventBus.getDefault().post(new EventMessage().new ChartImageEvent(mChart.getDrawingCache()));
			}
		});
		mChart.setAdapter(new CategoryPieChartAdapter(getActivity()));
		
		mRoot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (mChart.isExpanded()) {
					mChart.toggleGroup();
				}
			}
		});
		
		return mRoot;
	}
	
	public void onEvent(NavigationEvent event) {
		
		if (event.getType() != null && event.getType() == FragmentType.SPENDING) {
			mChart.onResume();
		} else {
			mChart.onPause();
		}
	}
	
	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_fragment_spending_summary).toUpperCase();
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onLeftMenuStateChanged(int oldState, int newState) {
		
		configureChart(newState);
	}

	@Override
	public void onRightMenuStateChanged(int oldState, int newState) {

		configureChart(newState);
	}
	
	private void configureChart(int state) {
		
		if (state == MenuDrawer.STATE_CLOSED && ((DashboardHandsetActivity) mActivity).getCurrentFragment() == getType()) {
			mChart.onResume();
		} else {
			mChart.onPause();
		}
	}
}
