package com.moneydesktop.finance.shared.fragment;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.adapter.GrowPagerAdapter;
import com.moneydesktop.finance.util.UiUtils;

public abstract class GrowFragment extends BaseFragment {

	public final String TAG = this.getClass().getSimpleName();

	private final float SCALE_SIZE = 0.8f;
	protected final static String POSITION = "position";

	private Float mScale, mPercent;

	private boolean mInit = false;
	private RelativeLayout mBackground, mCover;
	protected GrowPagerAdapter mAdapter;
	private ViewPager mPager;

	private int mCurrentPosition = 0;
	
	public ViewPager getmPager() {
		return mPager;
	}

	public void setmPager(ViewPager mPager) {
		this.mPager = mPager;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		configureView();
	}

	public int getPosition() {
		return getArguments().getInt(POSITION, -1);
	}

	public void setPosition(int position) {
		getArguments().putInt(POSITION, position);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setupViews();
		configureView();
	}

	@Override
	public void onResume() {
		super.onResume();

		mAdapter = mActivity.getPagerAdapter();
		mAdapter.addFragment(this);
		mCurrentPosition = mAdapter.getCurrentPage();

		if ((getPosition() == (mCurrentPosition + 1) || getPosition() == (mCurrentPosition - 1))
				&& !mInit) {
			mInit = true;
			transitionFragment(GrowPagerAdapter.BASE_ALPHA,
					GrowPagerAdapter.BASE_SIZE);
			return;
		}

		if (getPosition() == mCurrentPosition && !mInit) {
			mInit = true;
			transitionFragment(0.00f, 1.0f);
		}
	}

	protected void setupViews() {

		mBackground = (RelativeLayout) mRoot.findViewById(R.id.root);
		mCover = (RelativeLayout) mRoot.findViewById(R.id.cover);

		if (mBackground == null) {
			throw new NullPointerException(
					"You must have a RelativeLayout with id root to use a GrowFragment");
		}
	}

	protected void configureView() {

		float[] size = UiUtils.getScreenMeasurements(mActivity);
		int width = (int) (size[0] * SCALE_SIZE);
		int height = (int) (size[1] * SCALE_SIZE);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, height);
		mBackground.setLayoutParams(params);

		if (mScale != null) {
			transitionFragment(mPercent, mScale);
		}
	}

	@TargetApi(11)
	public void transitionFragment(float percent, float scale) {

		this.mScale = scale;
		this.mPercent = percent;

		if (getView() != null && android.os.Build.VERSION.SDK_INT >= 11) {

			getView().setScaleX(scale);
			getView().setScaleY(scale);

			if (mCover != null) {
				mCover.setAlpha(percent);
				mCover.setVisibility((percent <= 0.05f) ? View.INVISIBLE
						: View.VISIBLE);
			}
		}
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

}
