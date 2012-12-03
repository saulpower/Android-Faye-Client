package com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

public class WheelToggle extends RelativeLayout {

	public static final String TAG = "WheelToggle";

    private Context mContext;
    private View root;
    private WheelView wheel;
    private TextView title;
    private OnCheckedChangeListener listener;
    private boolean toggling = false;
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		this.listener = listener;
	}

	private boolean on = true;

    public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		
		if (this.on != on) {
			toggle();
		}
	}

	public WheelToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        root = inflater.inflate(R.layout.wheel_view, this, true);
        root.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggle();
			}
		});

        title = (TextView) findViewById(R.id.title);
        Fonts.applyPrimaryFont(title, 16);
        
        wheel = (WheelView) findViewById(R.id.wheel);
    }
    
	/**
	 * Rotate the toggle wheel to represent the state of the toggle wheel (yes/no)
	 */
    private void toggle() {
    	
    	if (!toggling) {
    		
	    	toggling = true;
	    	
	    	Animation rotate = new RotateAnimation(on ? 0 : 180, on ? 180 : 360, wheel.getWidth()/2, wheel.getHeight()/2);
	    	rotate.setDuration(700);
	    	rotate.setFillAfter(true);
	    	rotate.setInterpolator(new AnticipateOvershootInterpolator());
	    	rotate.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					toggling = false;
			    	listener.onCheckedChanged(null, on);
				}
			});
	    	
	    	wheel.startAnimation(rotate);
	    	
	    	on = !on;
    	}
    }
}
