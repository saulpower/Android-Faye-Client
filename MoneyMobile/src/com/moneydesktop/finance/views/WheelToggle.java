package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

public class WheelToggle extends RelativeLayout {

	public static final String TAG = "WheelToggle";

    private Context mContext;
    private View mRoot;
    private WheelView mWheel;
    private TextView mTitle;
    private OnCheckedChangeListener mListener;
    private boolean mToggling = false;
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		this.mListener = listener;
	}

	private boolean mOn = true;

    public boolean isOn() {
		return mOn;
	}

	public void setOn(boolean on) {
		
		if (this.mOn != on) {
			toggle();
		}
	}

	public WheelToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mRoot = inflater.inflate(R.layout.wheel_view, this, true);
        mRoot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggle();
			}
		});

        mTitle = (TextView) findViewById(R.id.title);
        Fonts.applyPrimaryFont(mTitle, 12);
        
        mWheel = (WheelView) findViewById(R.id.wheel);
    }
    
	/**
	 * Rotate the toggle wheel to represent the state of the toggle wheel (yes/no)
	 */
    private void toggle() {
    	
    	if (!mToggling) {
    		
	    	mToggling = true;
	    	
	    	ObjectAnimator rotate = ObjectAnimator.ofFloat(mWheel, "rotation", mOn ? 0 : 180, mOn ? 180 : 360);
	    	rotate.setDuration(700);
	    	rotate.setInterpolator(new AnticipateOvershootInterpolator());
	    	rotate.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {}
				
				@Override
				public void onAnimationRepeat(Animator animation) {}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					mToggling = false;
					if (mListener != null) {
						mListener.onCheckedChanged(null, mOn);
					}
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {}
			});
	    	rotate.start();
	    	
	    	mOn = !mOn;
    	}
    }
}
