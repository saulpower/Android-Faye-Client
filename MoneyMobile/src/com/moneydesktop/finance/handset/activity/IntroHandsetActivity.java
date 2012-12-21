
package com.moneydesktop.finance.handset.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.moneydesktop.finance.BaseActivity;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.handset.fragment.IntroHandsetFragment;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.FixedSpeedScroller;
import com.moneydesktop.finance.views.SmallSpinnerView;
import com.viewpagerindicator.CirclePageIndicator;

import java.lang.reflect.Field;

public class IntroHandsetActivity extends BaseActivity {
    private MyAdapter mAdapter;
    private ViewPager mPager;
    private TextView mLoadingMessage;
    private SmallSpinnerView mSpinner;
    private int mCurrentFragment = 0;
    private final Handler mHandler = new Handler();
    private Runnable mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handset_intro_view);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        CirclePageIndicator mTitleIndicator = (CirclePageIndicator) findViewById(R.id.titles);
        mTitleIndicator.setViewPager(mPager);
        mLoadingMessage = (TextView) findViewById(R.id.loading_text);
        mLoadingMessage.setText(getResources().getText(R.string.loading_app));
        Fonts.applyPrimaryFont(mLoadingMessage, 24);
        // Hack fix to adjust scroller velocity on view pager
        try {

            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mPager.getContext(), null);
            mScroller.set(mPager, scroller);

        } catch (Exception e) {
        }
        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                stopFlipping();
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mTask = new Runnable() {
            @Override
            public void run() {
                if (mCurrentFragment <= mPager.getChildCount()) {
                    mPager.setCurrentItem(mCurrentFragment++);
                } else {
                    mCurrentFragment = 0;
                    mPager.setCurrentItem(mCurrentFragment++);
                }
                mHandler.postDelayed(this, 5000);
            }
        };
        mHandler.postDelayed(mTask, 5000);
    }

    public void stopFlipping() {
        mHandler.removeCallbacks(mTask);
    }

    public void onEvent(SyncEvent event) {

        if (event.isFinished()) {
            TextView mStartButton = (TextView) findViewById(R.id.get_started);
            mSpinner = (SmallSpinnerView) findViewById(R.id.loading_spinner);
            mLoadingMessage.setVisibility(View.GONE);
            mSpinner.setVisibility(View.GONE);
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
            fadeOut.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mLoadingMessage.setVisibility(View.GONE);
                    mSpinner.setVisibility(View.GONE);
                }
            });
            mLoadingMessage.startAnimation(fadeOut);
            mSpinner.startAnimation(fadeOut);
            Fonts.applyPrimarySemiBoldFont(mStartButton, 18);
            mStartButton.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);
            mStartButton.startAnimation(fadeIn);
            mStartButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(IntroHandsetActivity.this, DashboardHandsetActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.none, R.anim.out_down);
                    finish();
                }
            });

        }
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new IntroHandsetFragment(R.drawable.handset_intro_1,
                            R.string.handset_intro_title_1,
                            R.string.handset_intro_desc_1);
                case 1:
                    return new IntroHandsetFragment(R.drawable.handset_intro_2,
                            R.string.handset_intro_title_2,
                            R.string.handset_intro_desc_2);
                case 2:
                    return new IntroHandsetFragment(R.drawable.handset_intro_3,
                            R.string.handset_intro_title_3,
                            R.string.handset_intro_desc_3);
                default:
                    return null;
            }
        }
    }

    @Override
    protected void onPause() {
        this.finish();
        overridePendingTransition(R.anim.none, R.anim.out_down);
        super.onPause();
    }

    @Override
    protected void onStop() {
        overridePendingTransition(R.anim.none, R.anim.out_down);
        super.onPause();
    }

    @Override
    public String getActivityTitle() {
        // TODO Auto-generated method stub
        return null;
    }
}
