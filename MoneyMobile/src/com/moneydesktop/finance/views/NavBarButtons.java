package com.moneydesktop.finance.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

import java.util.ArrayList;

public class NavBarButtons extends LinearLayout  {

    private Context mContext;
    private String[] mButtonTexts;
    private ArrayList<OnClickListener> mOnClickListeners;
    private LinearLayout mRoot;
    LayoutInflater mInflater;
    
    public NavBarButtons(Context context, String[] buttonTexts, ArrayList<OnClickListener> onClickListeners) {
        super(context);
        
        mContext = context;
        mButtonTexts = buttonTexts;
        mOnClickListeners = onClickListeners;
        
        populateView();
    }

    private void populateView() {
        
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = (LinearLayout) mInflater.inflate(R.layout.tablet_nav_icon, null);
        
        //just in case the navigation buttons haven't been removed already. wouldn't want them overlapping now would we?
        mRoot.removeAllViews();
        
        for (int i = 0; i < mButtonTexts.length; i++) {
            
            final TextView icon = new TextView(mContext);

            icon.setPadding(0, 0, 25, 0);
            
            icon.setText(mButtonTexts[i]);
            icon.setTextColor(Color.WHITE);
            Fonts.applyGlyphFont(icon, 18);
            
            icon.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
                       
            icon.setOnTouchListener(new OnTouchListener() {
                
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == (MotionEvent.ACTION_DOWN)) {
                        icon.setTextColor(Color.GRAY);
                    }
                    
                    
                    if (event.getAction() == (MotionEvent.ACTION_UP)) {
                        icon.setTextColor(Color.WHITE);
                    }
                    
                    return false;
                }
            });
            
            icon.setOnClickListener(mOnClickListeners.get(i));
            
            mRoot.addView(icon);
            
        }
        
        LinearLayout view = (LinearLayout)((Activity)mContext).findViewById(R.id.nav_bar_icon_container);
        view.removeAllViews();
        view.addView(mRoot);
    }

}