package com.moneydesktop.finance.tablet.activity;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.fragment.CategoriesFragment;
import com.moneydesktop.finance.shared.fragment.PopupFragment;
import com.moneydesktop.finance.shared.fragment.TagsFragment;
import com.moneydesktop.finance.tablet.activity.DialogBaseActivity.OnKeyboardStateChangeListener;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.CaretView;

@TargetApi(11)
public class PopupTabletActivity extends DialogBaseActivity implements OnKeyboardStateChangeListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private int[] mPosition = new int[2];
    private float mWidth = 0;
    private FragmentType mFragmentType;
    private RelativeLayout mRoot, mPopup, mContainer;
    private CaretView mCaret;
    private View mEditText;
    
    private AnimationListener mShowing = new Animation.AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {}
        
        @Override
        public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {
            if (mFragment instanceof PopupFragment) {
                ((PopupFragment) mFragment).popupVisible();
            }
        }
    };
    
    public View getEditText() {
        return mEditText;
    }

    public void setEditText(View mEditText) {
        this.mEditText = mEditText;
        
        if (isKeyboardShowing()) {
            adjustViewForKeyboard();
        }
    }
    
    @Override
    public void onBackPressed() {
        dismissPopup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_popup_view);
        
        setKeyboardStateChangeListener(this);
        
        // Set dialog window to fill entire screen
        LayoutParams params = getWindow().getAttributes();
        params.width = LayoutParams.FILL_PARENT;
        params.height = LayoutParams.FILL_PARENT;
        getWindow().setAttributes(params);

        // Grab data from intent for this popup
        mFragmentType = (FragmentType) getIntent().getSerializableExtra(Constant.EXTRA_FRAGMENT);
        mPosition[0] = getIntent().getIntExtra(Constant.EXTRA_POSITION_X, 0);
        mPosition[1] = getIntent().getIntExtra(Constant.EXTRA_POSITION_Y, 0);
        
        if (mFragmentType == null || mPosition[0] == 0) dismissPopup();

        // Initialize view and fragment
        setupView();
        showFragment(mFragmentType, false);
        
        // Post so we know when the view has been layed out
        mCaret.post(new Runnable() {
            
            @Override
            public void run() {
                didLayout();
            }
        });
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
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mCaret = (CaretView) findViewById(R.id.caret);
    }
    
    private void configurePopup() {
        
        // Adjust the popup size based on the type of fragment it holds
        mPopup.getLayoutParams().width = (int) mWidth;
        mPopup.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        
        // Make sure the popup is not off the edge of the screen
        while ((mPosition[0] + mPopup.getMeasuredWidth() + mCaret.getWidth()) >= mRoot.getWidth()) {
            mPosition[0]--;
        }
        
        // Move the caret and popup view to the correct position
        mCaret.setX(mPosition[0]);
        mCaret.setY(mPosition[1] - (mCaret.getHeight() / 2));
        mPopup.setX(mPosition[0] + (mCaret.getWidth() * 3 / 4));
        
        setMargin(false);
        
        // Show view with pop animation
        (new Handler()).post(new Runnable() {
            
            @Override
            public void run() {
                
                mRoot.setVisibility(View.VISIBLE);
                Animation in = AnimationFactory.createPopAnimation(PopupTabletActivity.this, mPopup);
                in.setAnimationListener(mShowing);
                mRoot.startAnimation(in);
            }
        });
    }
    
    public void setMargin(boolean animate) {
        
        float half = mPopup.getHeight() / 2;
        float positionY = (mPosition[1] - half);
        
        if (mPosition[1] + half > mRoot.getHeight() || mPosition[1] - half < 0) {
            positionY = mRoot.getHeight() / 2 - half;
        }
        
        if (animate) {
            
            ObjectAnimator move = ObjectAnimator.ofFloat(mPopup, "y", mPopup.getY(), positionY);
            move.setDuration(200);
            move.addListener(new AnimatorListener() {
                
                @Override
                public void onAnimationStart(Animator animation) {}
                
                @Override
                public void onAnimationRepeat(Animator animation) {}
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    adjustViewForKeyboard();
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {}
            });
            move.start();
            
        } else {

            mPopup.setY(positionY);
            adjustViewForKeyboard();
        }
    }

    @Override
    public void showFragment(FragmentType index, boolean moveUp) {

        mFragment = getFragment(index);
        
        if (mFragment != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.fragment, mFragment);
            ft.commit();
        }
    }

    private PopupFragment getFragment(FragmentType fragment) {

        mWidth = UiUtils.getDynamicPixels(this, 350);
        
        switch (fragment) {
            case POPUP_CATEGORIES:
                return CategoriesFragment.newInstance(getIntent().getLongExtra(Constant.EXTRA_ID, -1));
            case POPUP_TAGS:
                return TagsFragment.newInstance(getIntent().getLongExtra(Constant.EXTRA_ID, -1));
            default:
                dismissPopup();
        }
        
        return null;
    }
    
    public void dismissPopup() {
        UiUtils.hideKeyboard(this, mRoot);
        finish();
    }
    
    @Override
    public String getActivityTitle() {
        return null;
    }

    @Override
    public void keyboardStateDidChange(boolean isShowing) {
        
        adjustViewForKeyboard();
    }
    
    private void adjustViewForKeyboard() {
        if (isKeyboardShowing() && mEditText != null) {
            makeViewVisible(mEditText, mContainer);
        }
    }
}
