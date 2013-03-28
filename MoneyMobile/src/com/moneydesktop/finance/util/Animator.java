package com.moneydesktop.finance.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Animator {

	private static Map<View, Animation> animations = new HashMap<View, Animation>();
	
	public static void translateView(final View view, final float[] newPosition, long duration) {

        final int newLeft = (int) (view.getLeft() + newPosition[0]);
        final int newTop = (int) (view.getTop() + newPosition[1]);

        view.layout(newLeft, newTop, newLeft + view.getMeasuredWidth(), newTop + view.getMeasuredHeight());

		Animation animation = new TranslateAnimation(0, newPosition[0], 0, newPosition[1]);
		animation.setDuration(duration);
		animation.setFillEnabled(true);
		
		animation.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				
				if (view instanceof TextView) {
					view.setEnabled(false);
					view.setFocusable(false);
				}
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {
				
				if (view instanceof TextView) {
					view.setEnabled(true);
					view.setFocusable(true);
				}
				
//				view.layout(newLeft, newTop, newLeft + view.getMeasuredWidth(), newTop + view.getMeasuredHeight());
			}
		});
		
		animations.put(view, animation);
	}
	

	public static void fadeView(final View view, final boolean fadeOut, long duration) {
		
		fadeView(view, fadeOut, duration, 0L);
	}
	
	public static void fadeView(final View view, final boolean fadeOut, long duration, long delay) {
		
		Animation animation = new AlphaAnimation(fadeOut ? 1 : 0, fadeOut ? 0 : 1);
		animation.setDuration(duration);
		animation.setFillAfter(true);
		animation.setStartOffset(delay);
		
		animation.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				
				if (!fadeOut)
					view.setVisibility(View.VISIBLE);
			}
			
			public void onAnimationRepeat(Animation animation) {}
			
			public void onAnimationEnd(Animation animation) {
				
				if (fadeOut)
					view.setVisibility(View.INVISIBLE);
			}
		});
		
		animations.put(view, animation);
	}
	
	public static void startAnimations() {
		
		for (View view : animations.keySet()) {
			
			Animation animation = animations.get(view);
			view.startAnimation(animation);
		}
		
		animations.clear();
	}
}
