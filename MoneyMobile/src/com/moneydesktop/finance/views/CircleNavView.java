package com.moneydesktop.finance.views;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.BankAccount;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CircleNavView extends RelativeLayout {

	private static int Y_DISTANCE_POSITIVE = 100;
    private static int Y_DISTANCE_NEGATIVE = -100;

    private View.OnTouchListener touchListener;
    private boolean mIsBeingDragged = false;
    private int mPreviousAction;
    private float mStartingYPosition;
    private float mStartingXPosition;
    private ImageView mPointer;
    private RelativeLayout mPointerContainer;
    private float mNewAngle, mOldAngle;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private Context mContext;
    private View mNavView;
    
    public CircleNavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNavView = inflater.inflate(R.layout.circle_nav, this, true);
        populateView();
    }

    private void populateView () {
    	 mPointer = (ImageView) findViewById(R.id.option_pointer);
         mPointerContainer = (RelativeLayout) findViewById(R.id.test);

         WindowManager wm = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);

         mDeviceWidth = wm.getDefaultDisplay().getWidth();
         mDeviceHeight = wm.getDefaultDisplay().getHeight();

         touchListener = new View.OnTouchListener() {

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
                         animatePointerToDegrees(0);
                     } else if ((mStartingXPosition < mDeviceWidth/2) && (mStartingYPosition > mDeviceHeight/2)) {
                         animatePointerToDegrees(270);
                     } else if ((mStartingXPosition > mDeviceWidth/2) && (mStartingYPosition > mDeviceHeight/2)) {
                         animatePointerToDegrees(180);
                     } else if ((mStartingXPosition > mDeviceWidth/2) && (mStartingYPosition < mDeviceHeight/2)) {
                         animatePointerToDegrees(90);
                     }

                     break;

                 case MotionEvent.ACTION_MOVE:
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

         mNavView.findViewById(R.id.nav_container).setOnTouchListener(touchListener);
         
         mNavView.findViewById(R.id.nav_container).setOnClickListener(new View.OnClickListener(){

 			public void onClick(View v) {
 				if (!mIsBeingDragged) {
 					mNavView.setVisibility(View.GONE);
                 }
 			}
         });

    }
    
    private void animatePointerByDegrees (int rotationDegrees) {
        mNewAngle = mOldAngle - rotationDegrees;

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(300);
        mPointer.startAnimation(a);

        mOldAngle = mNewAngle;
    }

    private void animatePointerToDegrees (int rotationDegrees) {
        mNewAngle =  rotationDegrees;

        RotateAnimation a = new RotateAnimation(mOldAngle, mNewAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillAfter(true);
        a.setDuration(300);
        mPointer.startAnimation(a);

        mOldAngle = mNewAngle;
    }
}