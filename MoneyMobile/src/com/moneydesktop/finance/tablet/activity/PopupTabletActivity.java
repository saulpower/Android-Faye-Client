package com.moneydesktop.finance.tablet.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.tablet.fragment.CategoryPopupTabletFragment;
import com.moneydesktop.finance.views.CaretView;

@TargetApi(11)
public class PopupTabletActivity extends DashboardBaseActivity {

    private int[] mPosition = new int[2];
    private float mHeight = 0;
    private FragmentType mFragment;
    private RelativeLayout mRoot, mPopup;
    private CaretView mCaret;
    private Animation mFadeIn;
    
    @Override
    public void onBackPressed() {
        dismissPopup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_popup_view);
        
        // Set dialog window to fill entire screen
        LayoutParams params = getWindow().getAttributes();
        params.width = LayoutParams.FILL_PARENT;
        params.height = LayoutParams.FILL_PARENT;
        getWindow().setAttributes(params);

        mFragment = (FragmentType) getIntent().getSerializableExtra(Constant.EXTRA_FRAGMENT);
        mPosition[0] = getIntent().getIntExtra(Constant.EXTRA_POSITION_X, 0);
        mPosition[1] = getIntent().getIntExtra(Constant.EXTRA_POSITION_Y, 0);
        
        if (mFragment == null || mPosition[0] == 0) {
            dismissPopup();
        }
        
        loadAnimations();
        setupView();
        
        showFragment(mFragment);
        
        mRoot.post(new Runnable() {
            
            @Override
            public void run() {
                didLayout();
            }
        });
    }
    
    private void loadAnimations() {
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);
    }
    
    private void didLayout() {
     
        // Hack to get the view from resizing when the keyboard is shown
        mRoot.getLayoutParams().width = mRoot.getWidth();
        mRoot.getLayoutParams().height = mRoot.getHeight();
        
        int[] location = new int[2];
        mRoot.getLocationOnScreen(location);
        
        mPosition[0] -= mCaret.getWidth() * 3 / 4;
        mPosition[1] -= location[1];
        
        configurePopup();
    }
    
    private void setupView() {

        mRoot = (RelativeLayout) findViewById(R.id.root);
        mRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dismissPopup();
            }
        });
        
        mPopup = (RelativeLayout) findViewById(R.id.popup);
        mCaret = (CaretView) findViewById(R.id.caret);
    }
    
    private void configurePopup() {
        
        int height = (int) (mHeight * mRoot.getHeight());
        mPopup.getLayoutParams().height = height;
        mPopup.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        
        // Make sure the popup is not off the edge of the screen
        while ((mPosition[0] + mPopup.getWidth() + mCaret.getWidth()) >= mRoot.getWidth()) {
            mPosition[0]--;
        }
        
        mCaret.setX(mPosition[0]);
        mCaret.setY(mPosition[1] - (mCaret.getHeight() / 2));
        mPopup.setX(mPosition[0] + (mCaret.getWidth() * 3 / 4));
        
        int half = mPopup.getMeasuredHeight() / 2;
        
        // Make sure the popup is within the bounds of the screen
        if (half + mPosition[1] < mRoot.getHeight() && mPosition[1] - half > 0) {
            mPopup.setY(mPosition[1] - (mPopup.getMeasuredHeight() / 2));
        } else {
            mPopup.setY((mRoot.getHeight() / 2) - (mPopup.getMeasuredHeight() / 2));
        }
        
        mRoot.setVisibility(View.VISIBLE);
        mRoot.startAnimation(mFadeIn);
    }

    @Override
    public void showFragment(FragmentType index) {

        BaseFragment fragment = getFragment(index);
        
        if (fragment != null) {
            FragmentTransaction ft = mFm.beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }
    }

    private BaseFragment getFragment(FragmentType fragment) {

        switch (fragment) {
            case POPUP_CATEGORIES:
                mHeight = 0.95f;
                return CategoryPopupTabletFragment.newInstance();
            case POPUP_TAGS:
                break;
            default:
                dismissPopup();
        }
        
        return null;
    }
    
    public void dismissPopup() {
        finish();
    }
    
    public void dismissPopup(int resultCode, Intent resultIntent) {
        setResult(resultCode, resultIntent);
        finish();
    }
    
    @Override
    public String getActivityTitle() {
        return null;
    }
}
