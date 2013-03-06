package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.chart.ChartListBridge;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView;

import de.greenrobot.event.EventBus;

public class AccountSummaryTabletFragment extends SummaryTabletFragment {

	public final String TAG = this.getClass().getSimpleName();

	public static AccountSummaryTabletFragment newInstance(int position) {

		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();

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

		mRoot = inflater.inflate(R.layout.tablet_account_summarry_fragment, null);

		setupView();


		return mRoot;
	}

	private void setupView() {
		
	}

	@Override
	public void onResume() {
		super.onResume();

	//	EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();

	//	EventBus.getDefault().unregister(this);
	}

	@Override
	public String getTitleText() {
		return "test";
	}

//	@Override
//	public void transitionFragment(float percent, float scale) {
//		super.transitionFragment(percent, scale);
//
//	}

}
