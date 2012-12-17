package com.moneydesktop.finance.tablet.fragment;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.animation.FlipXAnimation;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment.onBackPressedListener;
import com.moneydesktop.finance.util.UiUtils;

@TargetApi(11)
public class TransactionsTabletFragment extends BaseFragment implements onBackPressedListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static TransactionsTabletFragment sFragment;
	
	private static final int MOVE_DURATION = 300;
	
	private RelativeLayout mContainer;
	private FrameLayout mDetail;
	private ImageView mFakeCell;
	private View mCellView;
	private int mCenterX, mCellX, mCellY, mHeight;
	private TransactionsDetailTabletFragment mDetailFragment;
	
	private AnimatorListener mListenerShow = new AnimatorListener() {
        
        @Override
        public void onAnimationStart(Animator animation) {}
        
        @Override
        public void onAnimationRepeat(Animator animation) {}
        
        @Override
        public void onAnimationEnd(Animator animation) {

            Animation cellFlip = flipCell(true);
            Animation detailFlip = flipDetailView(true);
            detailFlip.setAnimationListener(mShowFinished);

            mFakeCell.startAnimation(cellFlip);
            
            mDetail.setVisibility(View.VISIBLE);
            mDetail.startAnimation(detailFlip);
        }
        
        @Override
        public void onAnimationCancel(Animator animation) {}
    };
    
    private AnimationListener mShowFinished = new AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {}
        
        @Override
        public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {
            
            mFakeCell.setVisibility(View.GONE);
            
            mAnimating = false;
        }
    };
    
    private AnimationListener mListenerHide = new AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {}
        
        @Override
        public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {

            mDetail.setVisibility(View.GONE);
            
            AnimatorSet set = moveCell(false);
            set.addListener(mHideFinished);
            
            set.start();
        }
    };
    
    private AnimatorListener mHideFinished = new AnimatorListener() {
        
        @Override
        public void onAnimationStart(Animator animation) {}
        
        @Override
        public void onAnimationRepeat(Animator animation) {}
        
        @Override
        public void onAnimationEnd(Animator animation) {
            
            mContainer.setVisibility(View.GONE);
            mFakeCell.setVisibility(View.GONE);
            mCellView.setVisibility(View.VISIBLE);
            
            mAnimating = false;
        }
        
        @Override
        public void onAnimationCancel(Animator animation) {}
    };
	
	private Interpolator mInterpolator = new Interpolator() {
        
        @Override
        public float getInterpolation(float t) {
            if ((t) < (1 / 2.75)) {
                return (7.5625f * t * t);
            } else if (t < (2 / 2.75)) {
                return (7.5625f * (t -= (1.5f / 2.75f)) * t + .75f);
            } else if (t < (2.5 / 2.75)) {
                return (7.5625f * (t -= (2.25f / 2.75f)) * t + .9375f);
            } else {
                return (7.5625f * (t -= (2.625f / 2.75f)) * t + .984375f);
            }
        }
    };
	
	private boolean mAnimating = false;
	
	public static TransactionsTabletFragment newInstance() {
			
		sFragment = new TransactionsTabletFragment();
	
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	public void setDetailFragment(TransactionsDetailTabletFragment fragment) {
	    mDetailFragment = fragment;
	    mDetailFragment.setListener(this);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mActivity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();

        this.mActivity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_transactions_view, null);
		
		setupView();
		
		TransactionsPageTabletFragment frag = TransactionsPageTabletFragment.newInstance();
      
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, frag);
        ft.commit();
        
        setDetailFragment(TransactionsDetailTabletFragment.newInstance());
        
        ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.detail_fragment, mDetailFragment);
        ft.commit();
		
		return mRoot;
	}
	
	private void setupView() {

        mFakeCell = (ImageView) mRoot.findViewById(R.id.cell);
	    mContainer = (RelativeLayout) mRoot.findViewById(R.id.detail_container);
	    mDetail = (FrameLayout) mRoot.findViewById(R.id.detail_fragment);
	    
	    mContainer.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                configureDetailView();
            }
        });
	}
	
	public void showTransactionDetails(View view, int offset, Transactions transaction) {
	    
	    if (mDetailFragment != null) {
	        mDetailFragment.updateTransaction(transaction);
	    }
	    
	    mCellView = view;
	    
        int[] location = new int[2];
        mCellView.getLocationOnScreen(location);
        
        mHeight = view.getHeight();
        
        mCellY = (location[1] - offset);
        mCellX = location[0];
        
        mCenterX = (int) (view.getWidth() / 2.0f);
	    
	    createViewImage();
	    
	    configureDetailView();
	}
	
	private void createViewImage() {

        Bitmap b = UiUtils.loadBitmapFromView(mCellView);
        
        mFakeCell.setImageBitmap(b);
        mFakeCell.setX(mCellX);
        mFakeCell.setY(mCellY);
	}
	
	public void configureDetailView() {
	    
	    if (mAnimating) {
	        return;
	    }
	    
	    if (mCellView.getVisibility() == View.VISIBLE) {

            mAnimating = true;
            
            AnimatorSet set = moveCell(true);
            set.addListener(mListenerShow);
            
            mCellView.setVisibility(View.GONE);
            mFakeCell.setVisibility(View.VISIBLE);
            mContainer.setVisibility(View.VISIBLE);
            set.start();
            
        } else {

            mAnimating = true;
            
            if (mDetailFragment != null) {
                mDetailFragment.updateTransaction(null);
            }
            
            Animation cellFlip = flipCell(false);
            Animation detailFlip = flipDetailView(false);
            detailFlip.setAnimationListener(mListenerHide);
            
            mFakeCell.setVisibility(View.VISIBLE);
            mFakeCell.startAnimation(cellFlip);
            mDetail.startAnimation(detailFlip);
        }
	}
	
	private AnimatorSet moveCell(boolean out) {
	    
	    float startY = out ? mCellY : mDetail.getY() - mHeight;
	    float endY = out ? mDetail.getY() - mHeight : mCellY;
        
        float startX = out ? mCellX : mDetail.getX();
        float endX = out ? mDetail.getX() : mCellX;
        
        float startAlpha = out ? 0 : 1;
        float endAlpha = out ? 1 : 0;
	    
	    ObjectAnimator moveY = ObjectAnimator.ofFloat(mFakeCell, "y", startY, endY);
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator moveX = ObjectAnimator.ofFloat(mFakeCell, "x", startX, endX);
        moveX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator fade = ObjectAnimator.ofFloat(mContainer, "alpha", startAlpha, endAlpha);
        
        AnimatorSet set = new AnimatorSet();
        set.play(moveX).with(moveY).with(fade);
        set.setDuration(MOVE_DURATION);
        
        return set;
	}
	
	private Animation flipCell(boolean up) {
	    
	    int duration = up ? 800 : 375;
	    
	    FlipDirection dir = up ? FlipDirection.TOP_BOTTOM : FlipDirection.BOTTOM_TOP;
        
        Animation flip = new FlipXAnimation(dir, (int) (mCenterX + mDetail.getX()), (int) mDetail.getY());
        flip.setDuration(duration);
        
        if (up) {
            flip.setInterpolator(mInterpolator);
        }
        
        return flip;
	}
	
	private Animation flipDetailView(boolean up) {

        int duration = up ? 800 : 375;
        
	    FlipDirection dir = up ? FlipDirection.IN_TOP_BOTTOM : FlipDirection.OUT_BOTTOM_TOP;
        
        Animation flip = new FlipXAnimation(dir, (int) (mDetail.getWidth() / 2.0f), 0);
        flip.setDuration(duration);
        
        if (up) {
            flip.setInterpolator(mInterpolator);
        }
        
        return flip;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onFragmentBackPressed() {
        configureDetailView();
    }
}
