package com.moneydesktop.finance.tablet.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.shared.TransactionDetailBaseFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;

@SuppressLint("NewApi")
public class TransactionsDetailTabletFragment extends TransactionDetailBaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private onBackPressedListener mListener;
    
    private TextView mMenu;
    private RelativeLayout mContainer;
    private LinearLayout mMenuContainer, mDeleteItem, mEmailItem;
    private TextView mMenuTitle, mEmailLabel, mEmailIcon, mDeleteLabel, mDeleteIcon;
    
    private float mLeftMove, mRightMove;
    
    private Animation mPushIn, mPushOut;
    private boolean mInitialized = false;
    
    private boolean mShowingMenu = false;
    
    public void setListener(onBackPressedListener mListener) {
        this.mListener = mListener;
    }

    public static TransactionsDetailTabletFragment newInstance() {
        
        TransactionsDetailTabletFragment frag = new TransactionsDetailTabletFragment();
        
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        mCategory.setSelected(false);
        mTags.setSelected(false);
        
        if (mActivity != null && mActivity instanceof DashboardBaseActivity) {
            ((DashboardBaseActivity) mActivity).setDetailFragment(this);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.tablet_transaction_detail_view, null);
        initialize();
        
        if (mActivity instanceof DropDownTabletActivity) {
            ((DropDownTabletActivity) mActivity).setEditText(mPayee);
        }
        
        return mRoot;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        
        switch (requestCode) {
            
            case Constant.CODE_CATEGORY_DETAIL:
                
                long categoryId = data.getLongExtra(Constant.EXTRA_CATEGORY_ID, -1);
                
                if (categoryId != -1) {
                    updateTransactionCategory(categoryId);
                }
                break;
        }
    }
    
    public void viewShowing() {
        
        initializeContainer();
        
        mMenu.setVisibility(View.VISIBLE);
        mMenu.startAnimation(mPushIn);
        mMenuContainer.setVisibility(View.VISIBLE);
    }
    
    public long viewWillDisappear() {
        
        long duration = 0;
        
        if (mShowingMenu) {
            
            configureContainer();
            duration += 300;
            
        }

        mMenu.postDelayed(new Runnable() {
            
            @Override
            public void run() {

                mMenu.startAnimation(mPushOut);
                mMenuContainer.setVisibility(View.INVISIBLE);
            }
        }, duration);
        
        duration += mPushOut.getDuration();
        
        return duration;
    }
    
    @Override
    protected void setupAnimations() {
        super.setupAnimations();
        
        mPushIn = AnimationUtils.loadAnimation(getActivity(), R.anim.in_left);
        mPushOut = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
        mPushOut.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mMenu.setVisibility(View.INVISIBLE);
            }
        });
    }
    
    @Override
    protected void setupViews() {
        super.setupViews();
        
        mContainer = (RelativeLayout) mRoot.findViewById(R.id.container);
        mMenuContainer = (LinearLayout) mRoot.findViewById(R.id.menu_container);
        mDeleteItem = (LinearLayout) mRoot.findViewById(R.id.delete_menu_item);
        mEmailItem = (LinearLayout) mRoot.findViewById(R.id.email_menu_item);
        
        mMenu = (TextView) mRoot.findViewById(R.id.menu);
        
        mMenuTitle = (TextView) mRoot.findViewById(R.id.menu_title);
        mEmailLabel = (TextView) mRoot.findViewById(R.id.email_label);
        mEmailLabel.setText(mEmailLabel.getText().toString().toUpperCase());
        mEmailIcon = (TextView) mRoot.findViewById(R.id.email_icon);
        mDeleteLabel = (TextView) mRoot.findViewById(R.id.delete_label);
        mDeleteLabel.setText(mDeleteLabel.getText().toString().toUpperCase());
        mDeleteIcon = (TextView) mRoot.findViewById(R.id.delete_icon);
    }
    
    private void initializeContainer() {

        if (!mInitialized) {
            
            int[] location = new int[2];
            mContainer.getLocationOnScreen(location);
            mMenuContainer.setX(location[0] + mContainer.getWidth() - mMenuContainer.getWidth());
            mMenuContainer.setY(mContainer.getY());
            
            mMenu.setX(location[0] + mContainer.getWidth());
            mMenu.setY(mContainer.getY());

            int padding = (int) UiUtils.getDynamicPixels(getActivity(), 15);
            int total = padding + mMenuContainer.getWidth() + mMenu.getWidth();
            
            mLeftMove = total / 2;
            mRightMove = mMenuContainer.getWidth() + padding - mLeftMove;
            
            mInitialized = true;
        }
    }
    
    private void configureContainer() {
        
        float leftDistance = mLeftMove;
        float rightDistance = mRightMove;
        
        if (!mShowingMenu) {
            leftDistance *= -1;
        } else {
            rightDistance *= -1;
        }
        
        ObjectAnimator left = ObjectAnimator.ofFloat(mContainer, "x", mContainer.getX(), mContainer.getX() + leftDistance);
        left.setDuration(300);
        
        ObjectAnimator right = ObjectAnimator.ofFloat(mMenuContainer, "x", mMenuContainer.getX(), mMenuContainer.getX() + rightDistance);
        right.setDuration(300);
        
        ObjectAnimator right1 = ObjectAnimator.ofFloat(mMenu, "x", mMenu.getX(), mMenu.getX() + rightDistance);
        right1.setDuration(300);
        
        AnimatorSet set = new AnimatorSet();
        set.play(left).with(right).with(right1);
        set.start();
        
        mMenu.setText(mShowingMenu ? R.string.icon_share : R.string.icon_cancel);
        
        mShowingMenu = !mShowingMenu;
    }
    
    @Override
    protected void configureListeners() {
        super.configureListeners();
        
        mDeleteItem.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
            }
        });
        
        mEmailItem.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
            }
        });
        
        mMenu.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                configureContainer();
            }
        });

        mRoot.setSoundEffectsEnabled(false);
        mRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // intercepting clicks
            }
        });
        
        mCategory.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                showCategoryPopup(v);
            }
        });
        
        mTags.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                showTagPopup(v);
            }
        });
    }
    
    private void showCategoryPopup(View view) {
        
        showPopup(FragmentType.POPUP_CATEGORIES, Constant.CODE_CATEGORY_DETAIL, view);
    }
    
    private void showTagPopup(View view) {
        
        showPopup(FragmentType.POPUP_TAGS, Constant.CODE_TAG_DETAIL, view);
    }
    
    private void showPopup(FragmentType type, int requestCode, View view) {
        
        int[] catLocation = new int[2];
        view.getLocationOnScreen(catLocation);
        
        int adjustedX = catLocation[0] + view.getWidth();
        int adjustedY = catLocation[1] + (view.getHeight() / 2);
        
        Intent intent = new Intent(getActivity(), PopupTabletActivity.class);
        intent.putExtra(Constant.EXTRA_POSITION_X, adjustedX);
        intent.putExtra(Constant.EXTRA_POSITION_Y, adjustedY);
        intent.putExtra(Constant.EXTRA_FRAGMENT, type);
        intent.putExtra(Constant.EXTRA_SOURCE_CODE, requestCode);
        intent.putExtra(Constant.EXTRA_BOB_ID, mTransaction.getBusinessObjectId());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        startActivityForResult(intent, requestCode);
    }
    
    public void updateTransaction(Transactions transaction) {
        
        mTransaction = transaction;

        if (mTransaction == null) return;
        
        configureTransactionView();
    }
    
    @Override
    public void configureTransactionView() {
        super.configureTransactionView();

        if (mTransaction == null) return;
        
        // TODO: Need to show delete option when account is manual
//        mDeleteItem.setVisibility(mTransaction.getBankAccount().getIsLinked() ? View.GONE : View.VISIBLE);

        mDeleteItem.setVisibility(View.GONE);
    }
    
    @Override
    protected void applyFonts() {
        
        Fonts.applyGlyphFont(mMenu, 16);
        Fonts.applyGlyphFont(mEmailIcon, 14);
        Fonts.applyGlyphFont(mDeleteIcon, 14);
        Fonts.applySecondaryItalicFont(mMenuTitle, 12);
        Fonts.applyPrimaryBoldFont(mEmailLabel, 12);
        Fonts.applyPrimaryBoldFont(mDeleteLabel, 12);
        
        Fonts.applyPrimaryBoldFont(mAccountName, 16);
        Fonts.applyPrimaryBoldFont(mBankName, 10);
        Fonts.applyPrimaryBoldFont(mPayee, 20);
        Fonts.applyPrimaryBoldFont(mAmount, 34);
        Fonts.applyPrimaryBoldFont(mDate, 20);
        Fonts.applyPrimaryBoldFont(mCategory, 20);
        Fonts.applyPrimaryBoldFont(mTags, 12);
        Fonts.applyPrimaryBoldFont(mMemo, 12);
        Fonts.applyPrimaryBoldFont(mStatement, 10);
        
        // labels
        mPayee.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mPayee.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
        mMemo.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mMemo.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
        mDate.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mDate.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
        mTags.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mTags.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
        mAmount.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mAmount.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
        mCategory.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mCategory.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
        mStatement.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mStatement.setLabelSize(UiUtils.getScaledPixels(mActivity, 15));
    }
    
    public interface onBackPressedListener {
        public void onFragmentBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        
        if (mTransaction != null && mListener != null) {
            mListener.onFragmentBackPressed();
            
            return true;
        }
        
        return false;
    }
    
    public void onEvent(DatabaseSaveEvent event) {

        configureTransactionView();
    }
}
