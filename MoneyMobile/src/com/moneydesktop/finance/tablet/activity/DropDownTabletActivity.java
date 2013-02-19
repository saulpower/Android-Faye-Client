package com.moneydesktop.finance.tablet.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.LockFragment;
import com.moneydesktop.finance.shared.TransactionDetailController;
import com.moneydesktop.finance.shared.TransactionDetailController.ParentTransactionInterface;
import com.moneydesktop.finance.tablet.activity.DialogBaseActivity.OnKeyboardStateChangeListener;
import com.moneydesktop.finance.tablet.fragment.AccountSettingsTabletFragment;
import com.moneydesktop.finance.tablet.fragment.AddBankTabletFragment;
import com.moneydesktop.finance.tablet.fragment.FixBankTabletFragment;
import com.moneydesktop.finance.tablet.fragment.ShowHideDataTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionTotalsFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment;
import com.moneydesktop.finance.tablet.fragment.UpdateUsernamePassowrdTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment.onBackPressedListener;
import com.moneydesktop.finance.tablet.fragment.TransactionsPageTabletFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import de.greenrobot.event.EventBus;

public class DropDownTabletActivity extends DialogBaseActivity implements onBackPressedListener, ParentTransactionInterface, OnKeyboardStateChangeListener {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private FragmentType mIndex = FragmentType.LOCK_SCREEN;
    
    private TextView mLabel, mArrow, mLabelDetails;
    private RelativeLayout mRoot, mDropdown, mDetailContainer;
    private RelativeLayout mContainer;
    private Animation mIn, mOut;
    private TransactionDetailController mBase;
    private int mOffset = 0;
    private View mEditText;
    private int mLabelSpacing;
    
    public View getEditText() {
        return mEditText;
    }

    public void setEditText(View mEditText) {
        this.mEditText = mEditText;
        
        if (isKeyboardShowing()) {
            adjustViewForKeyboard();
        }
    }
    
    public void animateLabelsForward(String subLabel, boolean isFirstForwardAction) {


    		
    	ValueAnimator animationY = ObjectAnimator.ofFloat(mLabel, "translationY", -((mLabel.getY() - mArrow.getY()) + (mArrow.getHeight()/3) ));
    	ValueAnimator animationSide = ObjectAnimator.ofFloat(mLabel, "translationX", mLabelSpacing);
    	ValueAnimator scaleDownX = ObjectAnimator.ofFloat(mLabel, "scaleX", 1f, 0.75f);
    	ValueAnimator scaleDownY = ObjectAnimator.ofFloat(mLabel, "scaleY", 1f, 0.75f);
    	ValueAnimator animationX = ObjectAnimator.ofFloat(mLabelDetails, "translationX", (mDropdown.getWidth()/2), mContainer.getLeft());
    	ValueAnimator fadeIn = ObjectAnimator.ofFloat(mLabelDetails, "alpha", 0.25f, 1, 1);
    	
    	AnimatorSet set = new AnimatorSet();
    	set.playTogether(animationY, 
		    			animationX, 
		    			animationSide, 
		    			fadeIn,
		    			scaleDownX,
		    			scaleDownY);
    	
    	set.setDuration(500);
    	set.start();
    	mLabelDetails.setText(subLabel);
    	

    	
    	mArrow.setVisibility(View.VISIBLE); 
    }
    
    public void animateLabelsReverse() {

    	ValueAnimator animationY = ObjectAnimator.ofFloat(mLabel, "translationY", ((mLabel.getY() - mArrow.getY()) + (mArrow.getHeight()/3) ));
    	ValueAnimator animationSide = ObjectAnimator.ofFloat(mLabel, "translationX", mLabel.getY());
    	ValueAnimator scaleDownX = ObjectAnimator.ofFloat(mLabel, "scaleX", 0.75f, 1f);
    	ValueAnimator scaleDownY = ObjectAnimator.ofFloat(mLabel, "scaleY", 0.75f, 1f);
    	ValueAnimator animationX = ObjectAnimator.ofFloat(mLabelDetails, "translationX", mContainer.getLeft(), (mDropdown.getWidth()/2));
    	ValueAnimator fadeOut = ObjectAnimator.ofFloat(mLabelDetails, "alpha", 1f, 0, 0);
    	
    	AnimatorSet set = new AnimatorSet();
    	set.playTogether(animationY, 
		    			animationX, 
		    			animationSide, 
		    			fadeOut,
		    			scaleDownX,
		    			scaleDownY);
    	
    	set.setDuration(500);
    	set.start();
    	mArrow.setVisibility(View.GONE);
    }

    @Override
    public void onFragmentAttached(BaseFragment fragment) {
        super.onFragmentAttached(fragment);
        
        mLabel.setText(fragment.getFragmentTitle());
    }
    
    @Override
    public void onBackPressed() {
        dismissDropdown();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_dropdown_view);
        
        setKeyboardStateChangeListener(this);
        
        mLabelSpacing = (int) UiUtils.convertDpToPixel(10, this);
        
        // Set dialog window to fill entire screen
        LayoutParams params = getWindow().getAttributes();
        params.width = LayoutParams.FILL_PARENT;
        params.height = LayoutParams.FILL_PARENT;
        getWindow().setAttributes(params);
        
        setupAnimations();
        setupView();

        mIndex = (FragmentType) getIntent().getSerializableExtra(Constant.EXTRA_FRAGMENT);
        showFragment(mIndex);
        
        // Adjust the popup's window size based on the view passed in
        mRoot.post(new Runnable() {
            
            @Override
            public void run() {
                
                // Hack to get the view from resizing when the keyboard is shown
                mRoot.getLayoutParams().width = mRoot.getWidth();
                mRoot.getLayoutParams().height = mRoot.getHeight();
                
                configureDropdown(mIndex);
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void setupAnimations() {
        mIn = AnimationUtils.loadAnimation(this, R.anim.in_down_bounce);
        mIn.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {

                EventBus.getDefault().post(new EventMessage().new ParentAnimationEvent(true, true));
            }
        });
        mOut = AnimationUtils.loadAnimation(this, R.anim.out_up);
        mOut.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mDropdown.setVisibility(View.GONE);
                finish();
            }
        });
    }
    
    private void setupView() {

        mRoot = (RelativeLayout) findViewById(R.id.root);
        mRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dismissDropdown();
            }
        });
        
        mDropdown = (RelativeLayout) findViewById(R.id.dropdown);
        mDropdown.setSoundEffectsEnabled(false);
        mDropdown.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // Intercept clicks
            }
        });
        
        mArrow = (TextView) findViewById(R.id.arrow);
        mLabel = (TextView) findViewById(R.id.label);
        mLabelDetails = (TextView) findViewById(R.id.label_details);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        
        Fonts.applyPrimaryBoldFont(mLabelDetails, 14);
        Fonts.applyPrimaryBoldFont(mLabel, 14);
        Fonts.applyGlyphFont(mArrow, 14);
        
        backArrowAction(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismissDropdown();
			}
		});
    }
    
    public void backArrowAction(OnClickListener listener) {
    	mArrow.setOnClickListener(listener);
    }
    
    private void setupTransactionDetail() {
        
        ImageView fakeCell = (ImageView) mRoot.findViewById(R.id.cell);
        mDetailContainer = (RelativeLayout) mRoot.findViewById(R.id.detail_container);
        FrameLayout detail = (FrameLayout) mRoot.findViewById(R.id.detail_fragment);
        
        mBase = new TransactionDetailController(fakeCell, detail, mRoot.getPaddingTop());
        mBase.setDetailFragment(TransactionsDetailTabletFragment.newInstance());
        mBase.getDetailFragment().setListener(this);

        FragmentTransaction ft = mFm.beginTransaction();
        ft = mFm.beginTransaction();
        ft.replace(R.id.detail_fragment, mBase.getDetailFragment());
        ft.commit();
    }
    
    public void dismissDropdown() {
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContainer.getWindowToken(), 0);
        
        mDropdown.startAnimation(mOut);
    }
    
    @Override
    public void showTransactionDetails(View view, int offset, Transactions transaction) {
        mBase.showTransactionDetails(view, mOffset, transaction);
    }
    
    @Override
    public void setDetailFragment(TransactionsDetailTabletFragment fragment) {
        mBase.setDetailFragment(fragment);
    }
    
    @Override
    public TransactionsDetailTabletFragment getDetailFragment() {
        return mBase.getDetailFragment();
    }
    
    @Override
    public String getActivityTitle() {
        return null;
    }

    @Override
    public void showFragment(FragmentType type) {

        BaseFragment fragment = getFragment(type);
        
        FragmentTransaction ft = mFm.beginTransaction();
        ft.replace(R.id.fragment, fragment);
        ft.commit();
    }

    private BaseFragment getFragment(FragmentType type) {

        switch (type) {
            case LOCK_SCREEN:
                return LockFragment.newInstance(true);
            case TRANSACTIONS_PAGE:
                return TransactionsPageTabletFragment.newInstance(this, getIntent());
            case ACCOUNT_SETTINGS:
            	return AccountSettingsTabletFragment.newInstance(getIntent());
            case TRANSACTION_SUMMARY:
                return TransactionTotalsFragment.newInstance(getIntent().getStringArrayExtra(Constant.EXTRA_VALUES));
            case ADD_BANK:
            	return AddBankTabletFragment.newInstance();
            case FIX_BANK:
            	return FixBankTabletFragment.newInstance(getIntent());
            case SHOW_HIDE_DATA:
            	return ShowHideDataTabletFragment.newInstance(getIntent());
            case UPDATE_USERNAME_PASSWORD:
            	return UpdateUsernamePassowrdTabletFragment.newInstance(getIntent());
            default:
                finish();
        }
        
        return null;
    }
    
    /**
     * Configures the view for the passed in fragment
     * 
     * @param type
     */
    private void configureDropdown(FragmentType type) {
        
    	DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    double x = Math.pow(dm.widthPixels/dm.xdpi,2);
	    double y = Math.pow(dm.heightPixels/dm.ydpi,2);
	    double screenInches = Math.sqrt(x+y);
    	
        switch (type) {
            case LOCK_SCREEN:
                configureSize(0.4f, 0.65f);
                break;
            case TRANSACTIONS_PAGE:
                configureSize(0.8f, 0.8f);
                int[] location = new int[2];
                mRoot.getLocationOnScreen(location);
                mOffset = location[1];
                mLabel.setVisibility(View.INVISIBLE);
                mArrow.setVisibility(View.INVISIBLE);
                setupTransactionDetail();
                break;
            case ACCOUNT_SETTINGS:
        	    if (screenInches > 8) {
        	    	configureSize(0.6f, 0.7f);
        	    } else {
        	    	configureSize(0.6f, 0.85f);
        	    }
            	
                mLabel.setVisibility(View.VISIBLE);
                mArrow.setVisibility(View.GONE); 
                mLabelDetails.setVisibility(View.VISIBLE);
                break;
            case TRANSACTION_SUMMARY:
                mArrow.setVisibility(View.GONE);
                configureSize(0.4f, 0.4f);
                break;
            case ADD_BANK:
            	configureSize(0.6f, 0.8f);
            	mLabel.setVisibility(View.VISIBLE);
                mArrow.setVisibility(View.GONE);
                mLabelDetails.setVisibility(View.VISIBLE);
                break;
            case FIX_BANK:
            	configureSize(0.6f, 0.8f);
            	mLabel.setVisibility(View.VISIBLE);
                mArrow.setVisibility(View.GONE);
                mLabelDetails.setVisibility(View.VISIBLE);
                break;
            case SHOW_HIDE_DATA:
            	configureSize(0.6f, 0.8f);
            	mLabel.setVisibility(View.VISIBLE);
                mArrow.setVisibility(View.GONE);
                mLabelDetails.setVisibility(View.VISIBLE);
                break;
            case UPDATE_USERNAME_PASSWORD:
        	    if (screenInches > 8) {
        	    	configureSize(0.5f, 0.7f);
                	mLabel.setVisibility(View.VISIBLE);
                    mArrow.setVisibility(View.GONE);
                    mLabelDetails.setVisibility(View.VISIBLE);
        	    } else {
        	    	configureSize(0.6f, 0.8f);
                	mLabel.setVisibility(View.VISIBLE);
                    mArrow.setVisibility(View.GONE);
                    mLabelDetails.setVisibility(View.VISIBLE);
        	    }
            	
                break;
            default:
                break;
        }

        mDropdown.startAnimation(mIn);
    }
    
    /**
     * Sets the size of the popup container for the given fragment that
     * will be displayed.  This is calculated as a percent of the total
     * app-screen width and height.
     * 
     * @param widthPercent
     * @param heightPercent
     */
    private void configureSize(float widthPercent, float heightPercent) {
        mDropdown.getLayoutParams().width = (int) (mRoot.getWidth() * widthPercent);
        mDropdown.getLayoutParams().height = (int) (mRoot.getHeight() * heightPercent); 
    }

    @Override
    public void onFragmentBackPressed() {
        mBase.configureDetailView();
    }

    @Override
    public void parentOnActivityResult(int requestCode, int resultCode, Intent data) {
        mBase.parentOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void keyboardStateDidChange(boolean isShowing) {
        
        if (isShowing && mEditText != null) {
            adjustViewForKeyboard();
        }
    }
    
    private void adjustViewForKeyboard() {
        makeViewVisible(mEditText, (mBase != null && mBase.isShowing()) ? mDetailContainer : mDropdown);
    }
}
