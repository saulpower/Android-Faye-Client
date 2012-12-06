package com.moneydesktop.finance.views;

import java.util.List;

import com.moneydesktop.finance.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopupWindowAtLocation extends FrameLayout {

	final int mX;
	final int mY;
	final Context mContext;
	String[] mButtonTitles;
	List<OnClickListener> mButtonClickListeners;
	ViewGroup mParentView;
	LinearLayout mRoot;
	LayoutInflater mInflater;
	
    /**
     * 
     * @param context -- the context
     * @param parentView -- the layout view that the popup will be displayed in
     * @param positionX -- the X position for the popup to be aligned with
     * @param positionY -- the Y position for the popup to be aligned with
     * @param gravity -- Gravity.Left will use XY coordinates and put the popup on the right of the coordinates. Gravity.Right will do the opposite
     * @param buttonTitles -- the text that will be displayed for each of the buttons. Note** MUST be put in the list in the same order as the onClickListeners
     * @param onClickListeners -- onclick listeners for the buttons supplied. Note** MUST be put in the list in the same order as the Button Titles
     */
	public PopupWindowAtLocation(Context context, ViewGroup parentView, int positionX, int positionY, 
			String[] buttonTitles, List<OnClickListener> onClickListeners) {
		super(context);
		
		mContext = context;
		mX = positionX;
		mY = positionY;
		mButtonClickListeners = onClickListeners;
		mButtonTitles = buttonTitles;
		mParentView = parentView;
		
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mRoot = (LinearLayout) mInflater.inflate(R.layout.popup_with_buttons, null);
	    
	    LinearLayout.LayoutParams overlayParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	    mRoot.setLayoutParams(overlayParams);
	    
	    mParentView.addView(mRoot);
	    populateView();
	
	}

	private void populateView() {
	    LinearLayout overlay = (LinearLayout)mRoot.findViewById(R.id.popup_overlay);
	    LinearLayout subOverlay = (LinearLayout)mRoot.findViewById(R.id.popup_sub_overlay);
	    LinearLayout buttonContainer = (LinearLayout)mRoot.findViewById(R.id.popup_container);
	    
	    LinearLayout.LayoutParams subOverlayParams = (LinearLayout.LayoutParams) subOverlay.getLayoutParams();
	    subOverlayParams.leftMargin = mX;
	    subOverlayParams.topMargin = mY;
	    
	    subOverlay.setLayoutParams(subOverlayParams);
	    
	    for (int i = 0; i < mButtonTitles.length; i++) {
	    	
	    	View popupButton = mInflater.inflate(R.layout.popup_button_layout, null);
	    	
	    	TextView button = (TextView)popupButton.findViewById(R.id.popup_button);	    	
	    	button.setText(mButtonTitles[i]);
	    	button.setOnClickListener(mButtonClickListeners.get(i));
	    	
	    	buttonContainer.addView(popupButton);
	    }
	    
	    Animation loadPopupAnimation = AnimationUtils.loadAnimation(mContext, R.anim.scale_fade_in);
	    mRoot.startAnimation(loadPopupAnimation);

	    overlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation dismissPopupAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out_fast);
				dismissPopupAnimation.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						
					}
				});
				mRoot.startAnimation(dismissPopupAnimation);
				mParentView.removeView(mRoot);
			}
		});
	}
}