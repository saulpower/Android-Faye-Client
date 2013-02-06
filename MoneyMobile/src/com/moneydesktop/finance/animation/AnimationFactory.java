/**
 * Copyright (c) 2012 Ephraim Tekle genzeb@gmail.com & Saul Howard (saulpower1@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN 
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @author Ephraim A. Tekle
 * @author Saul Howard
 */
package com.moneydesktop.finance.animation;  

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ViewAnimator;

import com.moneydesktop.finance.util.UiUtils;

/**
 * This class contains methods for creating {@link Animation} objects for some of the most common animation, including a 3D flip animation, {@link FlipAnimation}.
 * Furthermore, utility methods are provided for initiating fade-in-then-out and flip animations.
 * 
 * @author Ephraim A. Tekle
 * @author Saul Howard
 *
 */
public class AnimationFactory {
    
    public static final String TAG = "AnimationFactory";
	
	/**
	 * The {@code FlipDirection} enumeration defines the most typical flip view transitions: left-to-right and right-to-left. {@code FlipDirection} is used during the creation of {@link FlipAnimation} animations.
	 * 
	 * @author Ephraim A. Tekle
	 * @author Saul Howard
	 *
	 */
	public static enum FlipDirection {
		LEFT_RIGHT, 
		RIGHT_LEFT, 
        TOP_BOTTOM, 
        BOTTOM_TOP,
        IN_TOP_BOTTOM,
        OUT_BOTTOM_TOP;
		
		public float getStartDegreeForFirstView() {
			
		    int degrees = 0;
            
            switch(this) {
                case BOTTOM_TOP:
                    degrees = 90;
                    break;
                case IN_TOP_BOTTOM:
                    degrees = -90;
                    break;
                case OUT_BOTTOM_TOP:
                    degrees = 0;
                    break;
                default:
                    break;
            }
		    return degrees;
		}
		
		public float getStartDegreeForSecondView() {
		    
            int degrees = 90;
            
			switch(this) {
                case TOP_BOTTOM:
    			case LEFT_RIGHT:
                    degrees = -90;
                    break;
    			case RIGHT_LEFT:
                    degrees = 90;
                    break;
                case IN_TOP_BOTTOM:
                case BOTTOM_TOP:
    			default:
                    degrees = 0;
                    break;
			}
			
			return degrees;
		}
		
		public float getEndDegreeForFirstView() {
		    
		    int degrees = 90;
		    
			switch(this) {
    		    case IN_TOP_BOTTOM:
    		        degrees = 0;
    		        break;
                case TOP_BOTTOM:
    			case LEFT_RIGHT:
    				degrees = 90;
    				break;
                case OUT_BOTTOM_TOP:
    			case RIGHT_LEFT:
    			    degrees = -90;
    			    break;
                case BOTTOM_TOP:
    			default:
    			    degrees = 0;
    			    break;
			}
			
			return degrees;
		}
		
		public float getEndDegreeForSecondView() {
			return 0;
		}
	};
	 
	
	/**
	 * Create a pair of {@link FlipAnimation} that can be used to flip 3D transition from {@code fromView} to {@code toView}. A typical use case is with {@link ViewAnimator} as an out and in transition.
	 * 
	 * NOTE: Avoid using this method. Instead, use {@link #flipTransition}.
	 *  
	 * @param fromView the view transition away from
	 * @param toView the view transition to
	 * @param dir the flip direction
	 * @param duration the transition duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 
	 * @return
	 */
	public static Animation[] flipAnimation(final View fromView, final View toView, FlipDirection dir, long duration) {
		
		Animation[] result = new Animation[2];
		float centerY;

		centerY = fromView.getHeight() / 2.0f; 
		
		Animation outFlip = new FlipAnimation(dir.getStartDegreeForFirstView(), dir.getEndDegreeForFirstView(), true, centerY, dir, fromView.getWidth());
		outFlip.setDuration(duration);
		outFlip.setFillAfter(true);
		outFlip.setInterpolator(new AccelerateInterpolator()); 

		AnimationSet outAnimation = new AnimationSet(true);
		outAnimation.addAnimation(outFlip); 
		result[0] = outAnimation; 
		
		Animation inFlip = new FlipAnimation(dir.getStartDegreeForSecondView(), dir.getEndDegreeForSecondView(), false, centerY, dir, fromView.getWidth());
		inFlip.setDuration(duration);
		inFlip.setFillAfter(true);
		inFlip.setInterpolator(new DecelerateInterpolator());
		inFlip.setStartOffset(duration);
		
		AnimationSet inAnimation = new AnimationSet(true); 
		inAnimation.addAnimation(inFlip); 
		result[1] = inAnimation;  
		
		return result;
	}
	
	public static void flipTransition(final ViewAnimator viewAnimator, FlipDirection dir, long duration) {   
		flipTransition(viewAnimator, null, null, dir, duration);
	}
	
	/**
	 * Flip to the next view of the {@code ViewAnimator}'s subviews. A call to this method will initiate a {@link FlipAnimation} to show the next View.  
	 * If the currently visible view is the last view, flip direction will be reversed for this transition.
	 *  
	 * @param viewAnimator the {@code ViewAnimator}
	 * @param dir the direction of flip
	 */
	public static void flipTransition(final ViewAnimator viewAnimator, AnimationListener finish1, AnimationListener finish2, FlipDirection dir, long duration) {   
		
		final View fromView = viewAnimator.getCurrentView();
		
		final int currentIndex = viewAnimator.getDisplayedChild();
		final int nextIndex = (currentIndex + 1) % viewAnimator.getChildCount();
		
		final View toView = viewAnimator.getChildAt(nextIndex);

		Animation[] animc = AnimationFactory.flipAnimation(fromView, toView, dir, duration);
  
		if (finish1 != null)
			animc[0].setAnimationListener(finish1);
		
		if (finish2 != null)
			animc[1].setAnimationListener(finish2);
		
		viewAnimator.setOutAnimation(animc[0]);
		viewAnimator.setInAnimation(animc[1]);
		
		viewAnimator.setDisplayedChild(nextIndex);
	}
	
	//////////////

 
	/**
	 * Slide animations to enter a view from left.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation inFromLeftAnimation(long duration, Interpolator interpolator) {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		inFromLeft.setDuration(duration);
		inFromLeft.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator); //AccelerateInterpolator
		return inFromLeft;
	}
 
	/**
	 * Slide animations to hide a view by sliding it to the right
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation outToRightAnimation(long duration, Interpolator interpolator) {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		outtoRight.setDuration(duration);
		outtoRight.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return outtoRight;
	}
 
	/**
	 * Slide animations to enter a view from right.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation inFromRightAnimation(long duration, Interpolator interpolator) {

		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		inFromRight.setDuration(duration);
		inFromRight.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return inFromRight;
	}
 
	/**
	 * Slide animations to hide a view by sliding it to the left.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation outToLeftAnimation(long duration, Interpolator interpolator) {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		outtoLeft.setDuration(duration);
		outtoLeft.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return outtoLeft;
	} 
 
	/**
	 * Slide animations to enter a view from top.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation inFromTopAnimation(long duration, Interpolator interpolator) {
		Animation infromtop = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		infromtop.setDuration(duration);
		infromtop.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator);
		return infromtop;
	} 
 
	/**
	 * Slide animations to hide a view by sliding it to the top
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator) 	
	 * @return a slide transition animation
	 */
	public static Animation outToTopAnimation(long duration, Interpolator interpolator) {
		Animation outtotop = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT, -1.0f
		);
		outtotop.setDuration(duration); 
		outtotop.setInterpolator(interpolator==null?new AccelerateInterpolator():interpolator); 
		return outtotop;
	} 

	/**
	 * A fade animation that will fade the subject in by changing alpha from 0 to 1.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param delay how long to wait before starting the animation, in milliseconds
	 * @return a fade animation
	 * @see #fadeInAnimation(View, long)
	 */
	public static Animation fadeInAnimation(long duration, long delay) {  
		
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());  
		fadeIn.setDuration(duration);
		fadeIn.setStartOffset(delay);
		
		return fadeIn;
	}

	/**
	 * A fade animation that will fade the subject out by changing alpha from 1 to 0.
	 * 
	 * @param duration the animation duration in milliseconds
	 * @param delay how long to wait before starting the animation, in milliseconds
	 * @return a fade animation
	 * @see #fadeOutAnimation(View, long)
	 */
	public static Animation fadeOutAnimation(long duration, long delay) {   

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setStartOffset(delay);
		fadeOut.setDuration(duration);

		return fadeOut;
	} 

	/**
	 * A fade animation that will ensure the View starts and ends with the correct visibility
	 * @param view the View to be faded in
	 * @param duration the animation duration in milliseconds
	 * @return a fade animation that will set the visibility of the view at the start and end of animation
	 */
	public static Animation fadeInAnimation(long duration, final View view) { 
		Animation animation = fadeInAnimation(500, 0); 

	    animation.setAnimationListener(new AnimationListener() { 

			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.VISIBLE);
			} 
			
			public void onAnimationRepeat(Animation animation) {}  
			
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.GONE); 
			} 
	    });
	    
	    return animation;
	}

	/**
	 * A fade animation that will ensure the View starts and ends with the correct visibility
	 * @param view the View to be faded out
	 * @param duration the animation duration in milliseconds
	 * @return a fade animation that will set the visibility of the view at the start and end of animation
	 */
	public static Animation fadeOutAnimation(long duration, final View view) {
		
		Animation animation = fadeOutAnimation(500, 0); 

	    animation.setAnimationListener(new AnimationListener() { 

			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			} 
			
			public void onAnimationRepeat(Animation animation) {}  
			
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE); 
			} 
	    });
	    
	    return animation;
		
	}

	/**
	 * Creates a pair of animation that will fade in, delay, then fade out
	 * @param duration the animation duration in milliseconds
	 * @param delay how long to wait after fading in the subject and before starting the fade out
	 * @return a fade in then out animations
	 */
	public static Animation[] fadeInThenOutAnimation(long duration, long delay) {  
		return new Animation[] {fadeInAnimation(duration,0), fadeOutAnimation(duration, duration+delay)};
	}  
	
	/**
	 * Fades the view in. Animation starts right away.
	 * @param v the view to be faded in
	 */
	public static void fadeOut(View v) { 
		if (v==null) return;  
	    v.startAnimation(fadeOutAnimation(500, v)); 
	} 
	
	/**
	 * Fades the view out. Animation starts right away.
	 * @param v the view to be faded out
	 */
	public static void fadeIn(View v) { 
		if (v==null) return;
		
	    v.startAnimation(fadeInAnimation(500, v)); 
	}
	
	/**
	 * Fades the view in, delays the specified amount of time, then fades the view out
	 * @param v the view to be faded in then out
	 * @param delay how long the view will be visible for
	 */
	public static void fadeInThenOut(final View v, long delay) {
		if (v==null) return;
		 
		v.setVisibility(View.VISIBLE);
		AnimationSet animation = new AnimationSet(true);
		Animation[] fadeInOut = fadeInThenOutAnimation(500,delay); 
	    animation.addAnimation(fadeInOut[0]);
	    animation.addAnimation(fadeInOut[1]);
	    animation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.GONE);
			} 

			public void onAnimationRepeat(Animation animation) { 
			}  
			
			public void onAnimationStart(Animation animation) {
				v.setVisibility(View.VISIBLE); 
			} 
	    });
	    
	    v.startAnimation(animation); 
	}
    
    public static Animation createShakeAnimation(Context context) {
        return createShakeAnimation(context, 15, 2);
    }
	
    public static Animation createShakeAnimation(Context context, int shakeDistance) {
        return createShakeAnimation(context, shakeDistance, 2);
    }
	
	public static Animation createShakeAnimation(Context context, int shakeDistance, int bounces) {
	    
	    float distance = UiUtils.getDynamicPixels(context, shakeDistance);
	    
	    AnimationSet set = new AnimationSet(true);
	    
	    TranslateAnimation translate = new TranslateAnimation(0, (-1 * distance / 2), 0, 0);
        translate.setDuration(100);
        translate.setStartOffset(0);
        set.addAnimation(translate);
	    
	    for (int i = 1; i < (bounces + 1); i++) {
	        
	        int direction = (i % 2 == 0) ? -1 : 1;
	        
	        translate = new TranslateAnimation(0, direction * distance, 0, 0);
	        translate.setDuration(100);
	        translate.setStartOffset(100 * i);
	        set.addAnimation(translate);
	    }
	    
	    translate = new TranslateAnimation(0, (distance / 2), 0, 0);
        translate.setDuration(100);
        translate.setStartOffset((bounces + 1) * 100);
        set.addAnimation(translate);
	    
	    return set;
	}

	public static Animation createPopAnimation(Context context, View view) {

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        
        location[0] += view.getMeasuredWidth() / 2;
        location[1] += view.getHeight() / 2;
        
	    AnimationSet set = new AnimationSet(false);
	    
	    AlphaAnimation alpha = new AlphaAnimation(0, 1);
	    alpha.setDuration(380);
	    set.addAnimation(alpha);
	    
	    ScaleAnimation scale = new ScaleAnimation(0, 1.05f, 0, 1.05f, location[0], location[1]);
	    scale.setDuration(200);
	    set.addAnimation(scale);
	    
	    ScaleAnimation scale1 = new ScaleAnimation(1.05f, 0.95f, 1.05f, 0.95f, location[0], location[1]);
	    scale1.setStartOffset(200);
        scale1.setDuration(200);
        set.addAnimation(scale1);
        
        ScaleAnimation scale2 = new ScaleAnimation(0.95f, 1.0f, 0.95f, 1.0f, location[0], location[1]);
        scale2.setStartOffset(400);
        scale2.setDuration(100);
        set.addAnimation(scale2);
	    
	    return set;
	}
}
