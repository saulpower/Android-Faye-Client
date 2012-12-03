package com.moneydesktop.finance.views;


import android.content.Context;
import android.util.AttributeSet;
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
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.tablet.fragment.AccountSummaryTabletFragment;
import com.moneydesktop.finance.tablet.fragment.AccountTypesTabletFragment;

public class CircleNavView extends RelativeLayout {
    
    private static int DASHBOARD = 0;
	private static int ACCOUNTS = 270;
	private static int SETTINGS = 90;
	private static int REPORTS = 180;
	private static int ROTATE_BY = 90;

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
    }

	private void populateView () {    	
    	 mPointer = (ImageView) findViewById(R.id.option_pointer);
    	 
         WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
         mDeviceWidth = wm.getDefaultDisplay().getWidth();
         mDeviceHeight = wm.getDefaultDisplay().getHeight();
         
         mAccountTypesNav.setOnClickListener(new OnClickListener() {	 		
			public void onClick(View v) {
				//animatePointerToDegreesThenClick(ACCOUNTS, AccountSummaryTabletActivity.class);
				animatePointerToDegreesThenClick(ACCOUNTS, new AccountTypesTabletFragment());
			}
	  	 });
         
         mDashboardNav.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {		
				animatePointerToDegreesThenClick(DASHBOARD, new AccountSummaryTabletFragment());
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
            			 animatePointerToDegrees(ACCOUNTS);
            		 } else if ((mStartingXPosition > mDeviceWidth/4) 
            				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/3)) 
            				 && (mStartingYPosition < mDeviceHeight/3)) {
            			 animatePointerToDegrees(DASHBOARD);
            		 } else if ((mStartingXPosition > mDeviceWidth - (mDeviceWidth/3))
            				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))
            				 && (mStartingYPosition > mDeviceHeight/3)) {
            			 animatePointerToDegrees(SETTINGS);
            		 } else if ((mStartingXPosition > mDeviceWidth/4)
            				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/4))
            				 && (mStartingYPosition > mDeviceHeight - (mDeviceHeight/3))) {
            			 animatePointerToDegrees(REPORTS);
            		 }
                     
                     break;

                 case MotionEvent.ACTION_MOVE:
                	 
                     mStartingYPosition = event.getY();
                     mStartingXPosition = event.getX();
                	 
                	 if (!mIsAnimating) {
                		 
                		 if ((mStartingXPosition < mDeviceWidth/3) 
                				 && (mStartingYPosition > mDeviceHeight/3)
                				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))) {
                			 animatePointerToDegrees(ACCOUNTS);
                		 } else if ((mStartingXPosition > mDeviceWidth/4) 
                				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/3)) 
                				 && (mStartingYPosition < mDeviceHeight/3)) {
                			 animatePointerToDegrees(DASHBOARD);
                		 } else if ((mStartingXPosition > mDeviceWidth - (mDeviceWidth/3))
                				 && (mStartingYPosition < mDeviceHeight - (mDeviceHeight/4))
                				 && (mStartingYPosition > mDeviceHeight/3)) {
                			 animatePointerToDegrees(SETTINGS);
                		 } else if ((mStartingXPosition > mDeviceWidth/4)
                				 && (mStartingXPosition < mDeviceWidth - (mDeviceWidth/4))
                				 && (mStartingYPosition > mDeviceHeight - (mDeviceHeight/3))) {
                			 animatePointerToDegrees(REPORTS);
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
    

	private void animatePointerByDegrees (int rotationDegrees) {
        mNewAngle = mOldAngle - rotationDegrees;

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(200);
        
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

        RotateAnimation a;
        if ((mOldAngle == DASHBOARD) && (mNewAngle == ACCOUNTS)) {
        	a = new RotateAnimation(mOldAngle, -90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        } else if ((mOldAngle == ACCOUNTS) && (mNewAngle == DASHBOARD)) {
        	a = new RotateAnimation(mOldAngle, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);        	
        } else {
        	a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        
        a.setFillAfter(true);
        a.setDuration(200);
        
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
    
    private void animatePointerToDegreesThenClick (final int rotationDegrees, final Object fragment) {
        mNewAngle =  rotationDegrees;

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
				((DashboardTabletActivity)mContext).mCircleNav.setVisibility(View.GONE);
				
				
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
}