package com.moneydesktop.finance.tablet.fragment;

import java.util.Random;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.GrowViewPager;
import com.moneydesktop.finance.views.VerticalTextView;

@TargetApi(11)
public class SummaryTabletFragment extends BaseTabletFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private final float SCALE_SIZE = 0.8f;

	private RelativeLayout background, cover;
	private TextView title;
	private VerticalTextView left, right;
	
	private String titleText;
	private Integer color;
	
	private boolean init = false;
	private Float scale, percent;

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	
	public static SummaryTabletFragment newInstance(int position) {
		
		SummaryTabletFragment frag = new SummaryTabletFragment();
		frag.setPosition(position);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.tablet_dummy_view, null);
		
		setupViews();
		setRandomBackground();
		
		setTitleText("Fragment " + getPosition());
		
		title.setText(getTitleText().toUpperCase());
		left.setText(getTitleText().toUpperCase());
		right.setText(getTitleText().toUpperCase());
		
		left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				activity.showNextPage();
			}
		});
		
		right.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				activity.showPrevPage();
			}
		});
		
		return root;
	}
	
	public void onResume() {
		super.onResume();
		
		if (getPosition() == 1 && !init) {
			init = true;
			transitionFragment(GrowViewPager.BASE_ALPHA, GrowViewPager.BASE_SIZE);
			return;
		}
		
		if (getPosition() == 0 && !init) {
			init = true;
			transitionFragment(0f, 1.0f);
		}
	}
	
	private void setupViews() {

		cover = (RelativeLayout) root.findViewById(R.id.cover);
		left = (VerticalTextView) root.findViewById(R.id.title_left);
		right = (VerticalTextView) root.findViewById(R.id.title_right);
		background = (RelativeLayout) root.findViewById(R.id.root);
		title = (TextView) root.findViewById(R.id.title);

		Fonts.applyPrimaryBoldFont(left, 15);
		Fonts.applyPrimaryBoldFont(right, 15);
		
		float[] size = UiUtils.getScreenMeasurements(activity);
		int width = (int) (size[0] * SCALE_SIZE);
		int height = (int) (size[1] * SCALE_SIZE);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		background.setLayoutParams(params);
		
		if (scale != null)
			transitionFragment(percent, scale);
	}
	
	private void setRandomBackground() {

		if (color == null) {
			Random r = new Random();
			color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
		}
		
		background.setBackgroundColor(color);
	}
	
	public void transitionFragment(float percent, float scale) {
		
		this.scale = scale;
		this.percent = percent;
		
		if (getView() != null && cover != null) {
			
			getView().setScaleX(scale);
			getView().setScaleY(scale);
			
			cover.setAlpha(percent);
			cover.setVisibility((percent <= 0.05f) ? View.GONE : View.VISIBLE);
		}
	}
	
	@Override
	public String getFragmentTitle() {
		return null;
	}
}
