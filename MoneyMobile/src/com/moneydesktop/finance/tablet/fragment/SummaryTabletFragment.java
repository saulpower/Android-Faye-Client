
package com.moneydesktop.finance.tablet.fragment;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.adapter.GrowPagerAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.VerticalTextView;

import java.util.Random;

@TargetApi(11)
public abstract class SummaryTabletFragment extends BaseFragment {

    public final String TAG = this.getClass().getSimpleName();

    private final float SCALE_SIZE = 0.8f;

    private RelativeLayout mBackground, mCover;
    private LinearLayout mLeft, mRight;
    private VerticalTextView mLeftText, mRightText;

    private String mTitleText;
    private Integer mColor;

    private boolean mInit = false;

    private Float mScale, mPercent;
    
    private GrowPagerAdapter mAdapter;
    private int mCurrentPosition = 0;

    public abstract String getTitleText();
    
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        
        if (savedInstanceState != null) {
            mColor = savedInstanceState.getInt("color", Color.BLACK);
        }
        
        configureView();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)  {
        
        if (mColor != null) {
            outState.putInt("color", mColor);
        }
        
        super.onSaveInstanceState(outState);
    }
    
    public int getPosition() {
        return getArguments().getInt("position", -1);
    }
    
    public void setPosition(int position) {
        getArguments().putInt("position", position);
    }

    public void onResume() {
        super.onResume();

        mAdapter = ((DashboardTabletActivity) mActivity).getPagerAdapter();
        mAdapter.addFragment(this);
        mCurrentPosition = mAdapter.getCurrentPage();
        
        if ((getPosition() == (mCurrentPosition + 1) || getPosition() == (mCurrentPosition - 1)) && !mInit) {
            mInit = true;
            transitionFragment(GrowPagerAdapter.BASE_ALPHA, GrowPagerAdapter.BASE_SIZE);
            return;
        }

        if (getPosition() == mCurrentPosition && !mInit) {
            mInit = true;
            transitionFragment(0.00f, 1.0f);
        }
    }

    protected void setupViews() {

        mCover = (RelativeLayout) mRoot.findViewById(R.id.cover);
        mLeft = (LinearLayout) mRoot.findViewById(R.id.left);
        mRight = (LinearLayout) mRoot.findViewById(R.id.right);
        mLeftText = (VerticalTextView) mRoot.findViewById(R.id.title_left);
        mRightText = (VerticalTextView) mRoot.findViewById(R.id.title_right);
        mBackground = (RelativeLayout) mRoot.findViewById(R.id.root);

        applyFonts();
    }
    
   protected void configureView() {

        float[] size = UiUtils.getScreenMeasurements(mActivity);
        int width = (int) (size[0] * SCALE_SIZE);
        int height = (int) (size[1] * SCALE_SIZE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        mBackground.setLayoutParams(params);

        if (mScale != null) {
            transitionFragment(mPercent, mScale);
        }
        
        mLeftText.setText(getTitleText().toUpperCase());
        mRightText.setText(getTitleText().toUpperCase());

        mLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                ((DashboardTabletActivity) mActivity).showNextPage();
            }
        });

        mRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                ((DashboardTabletActivity) mActivity).showPrevPage();
            }
        });
    }
    
    private void applyFonts() {
        Fonts.applyPrimaryBoldFont(mLeftText, 12);
        Fonts.applyPrimaryBoldFont(mRightText, 12);
    }

    protected void setRandomBackground() {

        if (mColor == null) {
            Random r = new Random();
            mColor = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        }

        mBackground.setBackgroundColor(mColor);
    }

    public void transitionFragment(float percent, float scale) {
        
        this.mScale = scale;
        this.mPercent = percent;
        
        if (getView() != null && mCover != null) {

            getView().setScaleX(scale);
            getView().setScaleY(scale);

            mCover.setAlpha(percent);
            mCover.setVisibility((percent <= 0.05f) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
