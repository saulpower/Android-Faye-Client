package com.moneydesktop.finance.shared;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;
import com.moneydesktop.finance.animation.FlipXAnimation;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment;
import com.moneydesktop.finance.util.UiUtils;

@TargetApi(11)
public class TransactionDetailController {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private static final int MOVE_DURATION = 400;

    private RelativeLayout mBackground, mContainer;
    private FrameLayout mDetail;
    private View mShader;
    private ImageView mFakeCell;
    private View mCellView;
    private int mCenterX, mCellX, mCellY, mHeight;
    private TransactionsDetailTabletFragment mDetailFragment;
    private float mContainerOffset;
    private boolean mShowing = false;
    
    public boolean isShowing() {
        return mShowing;
    }

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
            
            mContainer.setVisibility(View.VISIBLE);
            mContainer.startAnimation(detailFlip);
            shaderAnimation(true).start();
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
            
            mFakeCell.setVisibility(View.INVISIBLE);
            mDetailFragment.viewShowing();
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

            mContainer.setVisibility(View.INVISIBLE);
            
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
            
            mBackground.setVisibility(View.INVISIBLE);
            mFakeCell.setVisibility(View.INVISIBLE);
            mCellView.setVisibility(View.VISIBLE);
            
            mAnimating = false;
            mShowing = false;
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
    
    public TransactionDetailController(ImageView fakeCell, FrameLayout detail, float containerOffset) {
        mFakeCell = fakeCell;
        mDetail = detail;
        mContainerOffset = containerOffset;
    }
    
    protected void addDetailFragment(FragmentManager manager) {
        
        setDetailFragment(TransactionsDetailTabletFragment.newInstance());

        FragmentTransaction ft = manager.beginTransaction();
        ft = manager.beginTransaction();
        ft.replace(R.id.detail_fragment, mDetailFragment);
        ft.commit();
    }
    
    public void setDetailFragment(TransactionsDetailTabletFragment fragment) {
        mDetailFragment = fragment;
    }
    
    public TransactionsDetailTabletFragment getDetailFragment() {
        return mDetailFragment;
    }
    
    private void setupContainer(View view) {
        
        if (mBackground == null) {
            
            mBackground = (RelativeLayout) mDetail.findViewById(R.id.root_container);
            mBackground.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    configureDetailView();
                }
            });
            
            mContainer = (RelativeLayout) mDetail.findViewById(R.id.container);
            mShader = mDetail.findViewById(R.id.shader);
            
            mContainer.getLayoutParams().width = view.getWidth();
            
            if (ApplicationContext.isLargeTablet()) {
                mContainer.getLayoutParams().height *= Constant.LARGE_TABLET_SCALE;
                mDetail.findViewById(R.id.menu_container).getLayoutParams().height *= Constant.LARGE_TABLET_SCALE;
            }
            
            mDetail.requestLayout();
        }
    }
    
    protected boolean mAnimating = false;
    
    public void showTransactionDetails(final View view, final int offset, final Transactions transaction) {
        
        if (mDetailFragment != null && !mShowing) {
            
            mShowing = true;
            mDetailFragment.updateTransaction(transaction);
            
            setupContainer(view);
            
        } else return;
        
        mCellView = view;
        
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {

                final int[] location = new int[2];
                mCellView.getLocationOnScreen(location);
                
                mHeight = view.getHeight();
                
                mCellY = (location[1] - offset);
                mCellX = location[0];
                
                mCenterX = (int) (view.getWidth() / 2.0f);
                
                Bitmap b = UiUtils.convertViewToBitmap(mCellView);
                
                return b;
            }

            @Override
            protected void onPostExecute(Bitmap b) {

                mFakeCell.setImageBitmap(b);
                mFakeCell.setX(mCellX);
                mFakeCell.setY(mCellY);
                
                configureDetailView();
            }

        }.execute();
    }
    
    public void configureDetailView() {
        
        if (mAnimating) {
            return;
        }
        
        if (mCellView.getVisibility() == View.VISIBLE) {

            mAnimating = true;
            
            final AnimatorSet set = moveCell(true);
            set.addListener(mListenerShow);
            
            mCellView.setVisibility(View.INVISIBLE);
            mFakeCell.setVisibility(View.VISIBLE);
            mFakeCell.post(new Runnable() {
                
                @Override
                public void run() {
                    mBackground.setVisibility(View.VISIBLE);
                    set.start();
                }
            });
            
        } else {

            mAnimating = true;
            
            long duration = mDetailFragment.viewWillDisappear();
            
            mFakeCell.postDelayed(new Runnable() {
                
                @Override
                public void run() {

                    if (mDetailFragment != null) {
                        mDetailFragment.updateTransaction(null);
                    }
                    
                    Animation cellFlip = flipCell(false);
                    Animation detailFlip = flipDetailView(false);
                    detailFlip.setAnimationListener(mListenerHide);
                    
                    mFakeCell.setVisibility(View.VISIBLE);
                    mFakeCell.startAnimation(cellFlip);
                    mContainer.startAnimation(detailFlip);
                    shaderAnimation(false).start();
                }
            }, duration);
        }
    }
    
    private AnimatorSet moveCell(final boolean out) {
        
        final float startY = out ? mCellY : mContainer.getY() - mHeight + mContainerOffset;
        final float endY = out ? mContainer.getY() - mHeight + mContainerOffset : mCellY;
        
        final float startX = out ? mCellX : mContainer.getX();
        final float endX = out ? mContainer.getX() : mCellX;
        
        final float startAlpha = out ? 0 : 1;
        final float endAlpha = out ? 1 : 0;
        
        final ObjectAnimator moveY = ObjectAnimator.ofFloat(mFakeCell, "y", startY, endY);
        moveY.setDuration(MOVE_DURATION);
        final ObjectAnimator moveX = ObjectAnimator.ofFloat(mFakeCell, "x", startX, endX);
        moveY.setDuration(MOVE_DURATION);
        final ObjectAnimator fade = ObjectAnimator.ofFloat(mBackground, "alpha", startAlpha, endAlpha);
        fade.setDuration(out ? 300 : 400);
        
        final AnimatorSet set = new AnimatorSet();
        
        if (out) {
            set.play(moveX).with(moveY).after(fade);
        } else {
            set.play(moveX).with(moveY).before(fade);
        }
        
        return set;
    }
    
    private Animation flipCell(boolean up) {
        
        int duration = up ? 800 : 375;
        
        FlipDirection dir = up ? FlipDirection.TOP_BOTTOM : FlipDirection.BOTTOM_TOP;
        
        Animation flip = new FlipXAnimation(dir, (int) (mCenterX + mContainer.getX()), (int) mContainer.getY());
        flip.setDuration(duration);
        
        if (up) {
            flip.setInterpolator(mInterpolator);
        }
        
        return flip;
    }
    
    private Animation flipDetailView(boolean up) {

        int duration = up ? 800 : 375;
        
        FlipDirection dir = up ? FlipDirection.IN_TOP_BOTTOM : FlipDirection.OUT_BOTTOM_TOP;
        
        Animation flip = new FlipXAnimation(dir, (int) (mContainer.getWidth() / 2.0f), 0);
        flip.setDuration(duration);
        
        if (up) {
            flip.setInterpolator(mInterpolator);
        }
        
        return flip;
    }
    
    private ObjectAnimator shaderAnimation(boolean up) {

        int duration = up ? 800 : 375;
        ObjectAnimator fade = ObjectAnimator.ofFloat(mShader, "alpha", up ? 1 : 0, up ? 0 : 1);
        fade.setDuration(duration);
        if (up) {
            fade.setInterpolator(mInterpolator);
        }
        
        return fade;
    }
    
    public void parentOnActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (resultCode == Activity.RESULT_OK) {
            
            long categoryId = data.getLongExtra(Constant.EXTRA_CATEGORY_ID, -1);
            
            if (getDetailFragment() != null && categoryId != -1) {
                getDetailFragment().updateTransactionCategory(categoryId);
            }
            
            return;
        }
    }
    
    public interface ParentTransactionInterface {
        public void parentOnActivityResult(int requestCode, int resultCode, Intent data);
        public void showTransactionDetails(View view, int offset, Transactions transaction);
        public void setDetailFragment(TransactionsDetailTabletFragment fragment);
        public TransactionsDetailTabletFragment getDetailFragment();
    }
}
