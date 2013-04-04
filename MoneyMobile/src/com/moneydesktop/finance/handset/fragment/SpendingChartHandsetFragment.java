package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity.OnMenuChangeListener;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.piechart.ChartListBridge;
import com.moneydesktop.finance.views.piechart.ExpandablePieChartView;
import de.greenrobot.event.EventBus;
import net.simonvt.menudrawer.MenuDrawer;

public class SpendingChartHandsetFragment extends BaseFragment implements OnMenuChangeListener {
	
    public final String TAG = this.getClass().getSimpleName();

    private ChartListBridge mBridge;
    
    private ExpandablePieChartView mChart;
    private TextView mTitle;
    
    private boolean mPaused = false;

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
		
		if (mRoot != null) {
			
			View oldParent = (View) mRoot.getParent();
			
			if (oldParent != container) {
				((ViewGroup) oldParent).removeView(mRoot);
			}
			
			return mRoot;
		}
		
		mRoot = inflater.inflate(R.layout.handset_spending_view, null);
		
		mTitle = (TextView) mRoot.findViewById(R.id.title);
		Fonts.applySecondaryItalicFont(mTitle, 8);

        configureChart();
		
		return mRoot;
	}

    private void configureChart() {

        mChart = (ExpandablePieChartView) mRoot.findViewById(R.id.chart);

        mBridge = new ChartListBridge(mActivity, mChart);
        mBridge.setFragmentManager(getFragmentManager());

        mRoot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mChart.isExpanded()) {
                    mChart.toggleGroup();
                }
            }
        });
    }
	
	public void onEvent(DatabaseSaveEvent event) {
	    
	    if (event.didDatabaseChange() && event.getChangedClassesList().contains(Transactions.class)) {
	        mBridge.updateChart();
	    }
	}
    
    @Override
    public void isShowing() {
        super.isShowing();

        ((DashboardHandsetActivity) mActivity).resetRightMenu();

        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {

                mPaused = false;
                configureChart(true);
            }
        }, 500);
    }

    @Override
    public void isHiding() {
        super.isHiding();

        mPaused = true;
        configureChart(false);
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
		
		configureChart(newState == MenuDrawer.STATE_CLOSED);
	}

	@Override
	public void onRightMenuStateChanged(int oldState, int newState) {

		configureChart(newState == MenuDrawer.STATE_CLOSED);
	}
	
	private void configureChart(boolean showChart) {

		if (showChart && mActivity.getCurrentFragmentType() == getType() && !mPaused) {
            mChart.onResume();
		} else {
			mChart.onPause();
		}
	}
}
