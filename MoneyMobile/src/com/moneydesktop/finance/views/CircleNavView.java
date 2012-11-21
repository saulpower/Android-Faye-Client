package com.moneydesktop.finance.views;


import com.moneydesktop.finance.R;
import com.moneydesktop.finance.tablet.activity.AccountTypesTabletActivity;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CircleNavView extends RelativeLayout {

	private static int Y_DISTANCE_POSITIVE = 100;
    private static int Y_DISTANCE_NEGATIVE = -100;
    
    private static int DASHBOARD = 0;
	private static int ACCOUNTS = 270;
	private static int SETTINGS = 90;
	private static int REPORTS = 180;

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
    private float mSlope;
    private int xOne = 0;
    private int yOne = 0;
    private int xTwo;
    private int yTwo;

    
    public CircleNavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNavView = inflater.inflate(R.layout.circle_nav, this, true);
        
        mAccountTypesNav = (Button) mNavView.findViewById(R.id.nav_account_types);
        mDashboardNav = (Button) mNavView.findViewById(R.id.nav_dashboard);
        mSettingsNav = (Button) mNavView.findViewById(R.id.nav_settings);
        mReportsNav = (Button) mNavView.findViewById(R.id.nav_reports);
        
   	 	findSlopeOfScreen();
        populateView();
    }

	private void populateView () {    	
    	 mPointer = (ImageView) findViewById(R.id.option_pointer);
         
         mAccountTypesNav.setOnClickListener(new OnClickListener() {	 		
			public void onClick(View v) {
				animatePointerToDegreesThenClick(ACCOUNTS, AccountTypesTabletActivity.class);
			}
	  	 });
         
         mDashboardNav.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {		
				animatePointerToDegreesThenClick(DASHBOARD, DashboardTabletActivity.class);
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

                     if ((mStartingXPosition < mDeviceWidth/2) && (mStartingYPosition < mDeviceHeight/2)) {
                         animatePointerToDegrees(DASHBOARD);
                     } else if ((mStartingXPosition < mDeviceWidth/2) && (mStartingYPosition > mDeviceHeight/2)) {
                         animatePointerToDegrees(ACCOUNTS);
                     } else if ((mStartingXPosition > mDeviceWidth/2) && (mStartingYPosition > mDeviceHeight/2)) {
                         animatePointerToDegrees(REPORTS);
                     } else if ((mStartingXPosition > mDeviceWidth/2) && (mStartingYPosition < mDeviceHeight/2)) {
                         animatePointerToDegrees(SETTINGS);
                     }

                     break;

                 case MotionEvent.ACTION_MOVE:
                	 
                     mStartingYPosition = event.getY();
                     mStartingXPosition = event.getX();
                	 
                	 if (!mIsAnimating) {
                		 if ((mStartingXPosition < mDeviceWidth/2) && (mStartingYPosition < mDeviceHeight/2)) {
                             animatePointerToDegrees(DASHBOARD);
                         } else if ((mStartingXPosition < mDeviceWidth/2) && (mStartingYPosition > mDeviceHeight/2)) {
                             animatePointerToDegrees(ACCOUNTS);
                         } else if ((mStartingXPosition > mDeviceWidth/2) && (mStartingYPosition > mDeviceHeight/2)) {
                             animatePointerToDegrees(REPORTS);
                         } else if ((mStartingXPosition > mDeviceWidth/2) && (mStartingYPosition < mDeviceHeight/2)) {
                             animatePointerToDegrees(SETTINGS);
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
                    animatePointerToDegrees(ACCOUNTS);
                 }
                 return false;
 			}
         };
         
         touchListenerForDashboardNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToDegrees(DASHBOARD);
                 }
                 return false;
 			}
         };
         
         touchListenerForReportsNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToDegrees(REPORTS);
                 }
                 return false;
 			}
         };
         
         touchListenerForSettingsNavButton = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                    animatePointerToDegrees(SETTINGS);
                 }
                 return false;
 			}
         };

        
         mNavView.findViewById(R.id.nav_container).setOnTouchListener(touchListenerForContainer);
         mNavView.findViewById(R.id.nav_container).setOnClickListener(new View.OnClickListener(){ 

 			public void onClick(View v) {
 				if (!mIsBeingDragged) {
 					mNavView.setVisibility(View.GONE);
                 }
 			}
         });
         
         mAccountTypesNav.setOnTouchListener(touchListenerForAccountsNavButton);
         mDashboardNav.setOnTouchListener(touchListenerForDashboardNavButton);
         mSettingsNav.setOnTouchListener(touchListenerForSettingsNavButton);
         mReportsNav.setOnTouchListener(touchListenerForReportsNavButton);

    }
    
    private void findSlopeOfScreen() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        mDeviceWidth = wm.getDefaultDisplay().getWidth();
        mDeviceHeight = wm.getDefaultDisplay().getHeight();
        
        xTwo = mDeviceWidth;
        yTwo = mDeviceHeight;
        
		mSlope = (yTwo - yOne) / (xTwo - xOne);
	}

	private void animatePointerByDegrees (int rotationDegrees) {
        mNewAngle = mOldAngle - rotationDegrees;

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(300);
        
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

    private void animatePointerToDegrees (int rotationDegrees) {
        mNewAngle =  rotationDegrees;

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(300);
        
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
    
    private void animatePointerToDegreesThenClick (final int rotationDegrees, final Object object) {
        mNewAngle =  rotationDegrees;

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(300);
        
        a.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {		
			}
			
			public void onAnimationRepeat(Animation animation) {			
			}
			
			public void onAnimationEnd(Animation animation) {=
				
				if (mContext.getClass() == object) {
					((Activity)mContext).onBackPressed();
				} else {
					Intent intent = new Intent(mContext, (Class)object);
					mContext.startActivity(intent);
					((Activity) mContext).finish();
				}
			
			}
		});
        
        mPointer.startAnimation(a);
        mOldAngle = mNewAngle;
    }
}