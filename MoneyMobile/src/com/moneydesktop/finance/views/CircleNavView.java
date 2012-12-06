package com.moneydesktop.finance.views;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.fragment.AccountSummaryTabletFragment;
import com.moneydesktop.finance.tablet.fragment.AccountTypesTabletFragment;
import com.moneydesktop.finance.util.Enums.NavDirection;

import de.greenrobot.event.EventBus;

public class CircleNavView extends RelativeLayout {
	
	public final String TAG = this.getClass().getSimpleName();
    
    private static int DASHBOARD = 0;
	private static int ACCOUNTS = 1;
	private static int SETTINGS = 3;
	private static int REPORTS = 2;
	private static int ROTATE_BY = 90;
	
	private static int[] NAVIGATION_ORDER = new int[] {
		0, 
		270, 
		180,
		90
	};

    private View.OnTouchListener touchListenerForContainer;
    private View.OnTouchListener touchListenerForAccountsNavButton;
    private View.OnTouchListener touchListenerForDashboardNavButton;
    private View.OnTouchListener touchListenerForReportsNavButton;
    private View.OnTouchListener touchListenerForSettingsNavButton;
    private boolean mIsBeingDragged = false;
    private int mPreviousAction;
    private float mStartingYPosition;
    private float mStartingXPosition;
    private ImageView mPointer;
    private float mNewAngle, mOldAngle;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private Context mContext;
    private View mNavView;
    private Button mAccountTypesNav;
    private Button mDashboardNav;
    private Button mSettingsNav;
    private Button mReportsNav;
    private boolean mIsAnimating = false;
    
    private int mCurrentPosition = 0;

    public CircleNavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNavView = inflater.inflate(R.layout.circle_nav, this, true);
        
        mAccountTypesNav = (Button) mNavView.findViewById(R.id.nav_account_types);
        mDashboardNav = (Button) mNavView.findViewById(R.id.nav_dashboard);
        mSettingsNav = (Button) mNavView.findViewById(R.id.nav_settings);
        mReportsNav = (Button) mNavView.findViewById(R.id.nav_reports);
        
        populateView();
        
        EventBus.getDefault().register(this);
    }

	private void populateView () {    	
    	 mPointer = (ImageView) findViewById(R.id.option_pointer);
    	 
         WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
         mDeviceWidth = wm.getDefaultDisplay().getWidth();
         mDeviceHeight = wm.getDefaultDisplay().getHeight();
         
         mAccountTypesNav.setOnClickListener(new OnClickListener() {	 		
			public void onClick(View v) {
				//animatePointerToDegreesThenClick(ACCOUNTS, AccountSummaryTabletActivity.class);
				animatePointerToPositionThenClick(ACCOUNTS, new AccountTypesTabletFragment());
			}
	  	 });
         
         mDashboardNav.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {		
				animatePointerToPositionThenClick(DASHBOARD, new AccountSummaryTabletFragment());
			}
		});
                  
         touchListenerForContainer = new View.OnTouchListener() {

 			public boolean onTouch(View v, MotionEvent event) {
 				  boolean result = false;

                 if ((mPreviousAction == MotionEvent.ACTION_DOWN) && (event.getAction() == MotionEvent.ACTION_UP)) {
                     mIsBeingDragged = false;
                     return false;
                 }

                 switch(event.getAction())
                 {
                 case MotionEvent.ACTION_DOWN:
                     mStartingYPosition = event.getY();
                     mStartingXPosition = event.getX();
                     
            		 if ((mStartingXPosition < mDeviceWidth/3) 
            				 && (mStartingYPosition > mDeviceHeight/3)
            				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))) {
            			 animatePointerToPosition(ACCOUNTS);
            		 } else if ((mStartingXPosition > mDeviceWidth/4) 
            				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/3)) 
            				 && (mStartingYPosition < mDeviceHeight/3)) {
            			 animatePointerToPosition(DASHBOARD);
            		 } else if ((mStartingXPosition > mDeviceWidth - (mDeviceWidth/3))
            				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))
            				 && (mStartingYPosition > mDeviceHeight/3)) {
            			 animatePointerToPosition(SETTINGS);
            		 } else if ((mStartingXPosition > mDeviceWidth/4)
            				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/4))
            				 && (mStartingYPosition > mDeviceHeight - (mDeviceHeight/3))) {
            			 animatePointerToPosition(REPORTS);
            		 }
                     
                     break;

                 case MotionEvent.ACTION_MOVE:
                	 
                     mStartingYPosition = event.getY();
                     mStartingXPosition = event.getX();
                	 
                	 if (!mIsAnimating) {
                		 
                		 if ((mStartingXPosition < mDeviceWidth/3) 
                				 && (mStartingYPosition > mDeviceHeight/3)
                				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))) {
                			 animatePointerToPosition(ACCOUNTS);
                		 } else if ((mStartingXPosition > mDeviceWidth/4) 
                				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/3)) 
                				 && (mStartingYPosition < mDeviceHeight/3)) {
                			 animatePointerToPosition(DASHBOARD);
                		 } else if ((mStartingXPosition > mDeviceWidth - (mDeviceWidth/3))
                				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))
                				 && (mStartingYPosition > mDeviceHeight/3)) {
                			 animatePointerToPosition(SETTINGS);
                		 } else if ((mStartingXPosition > mDeviceWidth/4)
                				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/4))
                				 && (mStartingYPosition > mDeviceHeight - (mDeviceHeight/3))) {
                			 animatePointerToPosition(REPORTS);
                		 }
                	 }
                	 
                     result = true;
                     mIsBeingDragged = true;
                     break;

                 case MotionEvent.ACTION_UP:
                     result = true;
                     break;

                 default:
                     result = false;
                     break;
                 }

                 mPreviousAction = event.getAction();

                 return result;
 			}

         };        
         
         touchListenerForAccountsNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToPosition(ACCOUNTS);
                 }
                 return false;
 			}
         };
         
         touchListenerForDashboardNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToPosition(DASHBOARD);
                 }
                 return false;
 			}
         };
         
         touchListenerForReportsNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToPosition(REPORTS);
                 }
                 return false;
 			}
         };
         
         touchListenerForSettingsNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToPosition(SETTINGS);
                 }
                 return false;
 			}
         };

        
         mNavView.findViewById(R.id.nav_container).setOnTouchListener(touchListenerForContainer);
         mNavView.findViewById(R.id.nav_container).setOnClickListener(new View.OnClickListener(){ 

 			public void onClick(View v) {
 				if (!mIsBeingDragged) {
 					hideNavigation();
                 }
 			}
         });
         
         mAccountTypesNav.setOnTouchListener(touchListenerForAccountsNavButton);
         mDashboardNav.setOnTouchListener(touchListenerForDashboardNavButton);
         mSettingsNav.setOnTouchListener(touchListenerForSettingsNavButton);
         mReportsNav.setOnTouchListener(touchListenerForReportsNavButton);

    }
    
//	private void animatePointerByDegrees (int rotationDegrees) {
//        mNewAngle = mOldAngle - rotationDegrees;
//
//        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        a.setFillAfter(true);
//        a.setDuration(200);
//        
//        a.setAnimationListener(new AnimationListener() {
//			
//			public void onAnimationStart(Animation animation) {				
//				mIsAnimating = true;
//			}
//			
//			public void onAnimationRepeat(Animation animation) {			
//			}
//			
//			public void onAnimationEnd(Animation animation) {
//				mIsAnimating = false;
//				
//			}
//		});
//        
//        mPointer.startAnimation(a);
//
//        mOldAngle = mNewAngle;
//    }
	
	public void onEvent(NavigationEvent event) {
		
		if (event.getDirection() != null) {

	    	if (mIsAnimating)
	    		return;
	    	
			if (event.getDirection() == NavDirection.NEXT)
				animateNext();
			else
				animatePrevious();
		}
	}
	
	private void animatePrevious() {
		
		mCurrentPosition++;
		
		if (mCurrentPosition == NAVIGATION_ORDER.length)
			mCurrentPosition = 0;
		
		animatePointerToPosition(mCurrentPosition);
	}
	
	private void animateNext() {
		
		mCurrentPosition--;
		
		if (mCurrentPosition < 0)
			mCurrentPosition = NAVIGATION_ORDER.length - 1;
		
		animatePointerToPosition(mCurrentPosition);
	}

    private void animatePointerToPosition(int position) {
    	
    	mCurrentPosition = position;
        mNewAngle =  NAVIGATION_ORDER[position];

        RotateAnimation a;
        if ((mOldAngle == NAVIGATION_ORDER[DASHBOARD]) && (mNewAngle == NAVIGATION_ORDER[ACCOUNTS])) {
        	a = new RotateAnimation(mOldAngle, -90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        } else if ((mOldAngle == NAVIGATION_ORDER[ACCOUNTS]) && (mNewAngle == NAVIGATION_ORDER[DASHBOARD])) {
        	a = new RotateAnimation(mOldAngle, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);        	
        } else {
        	a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        
        a.setFillAfter(true);
        a.setDuration(150);
        
        a.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {				
				mIsAnimating = true;
			}
			
			public void onAnimationRepeat(Animation animation) {			
			}
			
			public void onAnimationEnd(Animation animation) {
				mIsAnimating = false;
				
			}
		});
        
        mPointer.startAnimation(a);
        mOldAngle = mNewAngle;
    }
    
    private void animatePointerToPositionThenClick(final int position, final Object fragment) {

    	mCurrentPosition = position;
        mNewAngle =  NAVIGATION_ORDER[position];

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(200);
        
        a.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {		
			}
			
			public void onAnimationRepeat(Animation animation) {			
			}
			
			public void onAnimationEnd(Animation animation) {
				
				((DashboardTabletActivity)mContext).showFragment(1); //This needs to be looked at....don't think I'm doing this right.
				hideNavigation();
				
				
//				if (mContext.getClass() == object) {
//					((Activity)mContext).onBackPressed();
//				} else {
//					Intent intent = new Intent(mContext, (Class)object);
//					mContext.startActivity(intent);
//					((Activity) mContext).finish();
//				}
			
			}
		});
        
        mPointer.startAnimation(a);
        mOldAngle = mNewAngle;
    }
    
    private void hideNavigation() {
    	((DashboardTabletActivity)mContext).toggleNavigation();
    }
}