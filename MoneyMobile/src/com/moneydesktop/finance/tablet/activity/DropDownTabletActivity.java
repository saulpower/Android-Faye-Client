package com.moneydesktop.finance.tablet.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.TransactionDetailController;
import com.moneydesktop.finance.shared.TransactionDetailController.ParentTransactionInterface;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.shared.fragment.FeedbackFragment;
import com.moneydesktop.finance.shared.fragment.LockFragment;
import com.moneydesktop.finance.tablet.activity.DialogBaseActivity.OnKeyboardStateChangeListener;
import com.moneydesktop.finance.tablet.fragment.AccountSettingsTabletFragment;
import com.moneydesktop.finance.tablet.fragment.AddBankTabletFragment;
import com.moneydesktop.finance.tablet.fragment.FixBankTabletFragment;
import com.moneydesktop.finance.tablet.fragment.ShowHideDataTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionTotalsFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment.onBackPressedListener;
import com.moneydesktop.finance.tablet.fragment.TransactionsPageTabletFragment;
import com.moneydesktop.finance.tablet.fragment.UpdateUsernamePassowrdTabletFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.navigation.AnimatedNavView;
import com.moneydesktop.finance.views.navigation.AnimatedNavView.NavigationListener;

import de.greenrobot.event.EventBus;

public class DropDownTabletActivity extends DialogBaseActivity implements onBackPressedListener, ParentTransactionInterface, OnKeyboardStateChangeListener, NavigationListener {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private FragmentType mIndex = FragmentType.LOCK_SCREEN;
    
    private RelativeLayout mRoot, mDropdown, mDetailContainer;
    private LinearLayout mContainer;
    private Animation mIn, mOut;
    private TransactionDetailController mBase;
    private int mOffset = 0;
    private View mEditText;
    
    private boolean mPopped = false;
    
    private AnimatedNavView mNavView;
    
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
        
        if ((mBase != null && !mBase.getDetailFragment().onBackPressed()) || (mFragment != null && !mFragment.onBackPressed())) {
            dismissDropdown();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_dropdown_view);
        
        setKeyboardStateChangeListener(this);
        
        
        // Set dialog window to fill entire screen
        LayoutParams params = getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        
        setupAnimations();
        setupView();

        mIndex = (FragmentType) getIntent().getSerializableExtra(Constant.EXTRA_FRAGMENT);
        showFragment(mIndex, false);
        
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
        
        mNavView = (AnimatedNavView) findViewById(R.id.nav_view);
        setupNavView();
        
        mContainer = (LinearLayout) findViewById(R.id.container);
        
        mContainer.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dismissDropdown();
            }
        });
    }
    
   private void setupNavView() {
    	
    	mNavView.setNavigationListener(this);
    	mNavView.setFontColor(Color.WHITE);
    	mNavView.setFontSize(UiUtils.getScaledPixels(this, 20));
    	mNavView.setTypeface(Fonts.getFont(Fonts.PRIMARY_SEMI_BOLD));
    }
    

    private void setupTransactionDetail() {
        
        ImageView fakeCell = (ImageView) mRoot.findViewById(R.id.cell);
        mDetailContainer = (RelativeLayout) mRoot.findViewById(R.id.detail_container);
        FrameLayout detail = (FrameLayout) mRoot.findViewById(R.id.detail_fragment);
        
        mBase = new TransactionDetailController(fakeCell, detail, mRoot.getPaddingTop());
        mBase.setDetailFragment(TransactionsDetailTabletFragment.newInstance());
        mBase.getDetailFragment().setListener(this);

        FragmentTransaction ft = mFm.beginTransaction();
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
    public void showFragment(FragmentType type, boolean moveUp) {

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
            case FEEDBACK:
            	return FeedbackFragment.newInstance();
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
                mNavView.setVisibility(View.GONE);
                int[] location = new int[2];
                mRoot.getLocationOnScreen(location);
                mOffset = location[1];
                setupTransactionDetail();
                break;
            case ACCOUNT_SETTINGS:
        	    if (screenInches > 8) {
        	    	configureSize(0.6f, 0.7f);
        	    } else {
        	    	configureSize(0.6f, 0.85f);
        	    }
                break;
            case FEEDBACK:
                configureSize(0.6f, 0.7f);
                mNavView.setVisibility(View.GONE);
            	break;
            case TRANSACTION_SUMMARY:
                mNavView.setVisibility(View.GONE);
                configureSize(0.4f, 0.4f);
                break;
            case ADD_BANK:
            	configureSize(0.6f, 0.8f);
                break;
            case FIX_BANK:
            	configureSize(0.6f, 0.8f);
                break;
            case SHOW_HIDE_DATA:
            	configureSize(0.6f, 0.8f);
                break;
            case UPDATE_USERNAME_PASSWORD:
        	    if (screenInches > 8) {
        	    	configureSize(0.5f, 0.7f);
        	    } else {
        	    	configureSize(0.6f, 0.8f);
        	    }
            	
                break;
            default:
                break;
        }

        mDropdown.startAnimation(mIn);
    }
    
    public AnimatedNavView getAnimatedNavView() {
    	return mNavView;
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
    public void keyboardStateDidChange(boolean isShowing) {
        
        if (isShowing && mEditText != null) {
            adjustViewForKeyboard();
        }
    }
    
    private void adjustViewForKeyboard() {
        makeViewVisible(mEditText, (mBase != null && mBase.isShowing()) ? mDetailContainer : mDropdown);
    }
    
    @Override
	public void updateNavBar(final String titleString, boolean fragmentTitle) {

    	if (!mPopped && mNavView.getVisibility() == View.VISIBLE) {
    		mRoot.post(new Runnable() {
				
				@Override
				public void run() {

		    		mNavView.pushNav(titleString);
				}
			});
    	}
    	
    	mPopped = false;
	}
    
    @Override
	public void popBackStack() {
    	super.popBackStack();

        UiUtils.hideKeyboard(this, mContainer);
        
    	mPopped = true;
    	
    	mNavView.popNav();
    }

	@Override
	public void onNavigationPopped() {
		popBackStack();
	}
}
