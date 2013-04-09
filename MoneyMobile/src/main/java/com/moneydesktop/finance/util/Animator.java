package main.java.com.moneydesktop.finance.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Animator {

    public static final String TAG = "Animator";

    private static Map<View, Animation> animations = new HashMap<View, Animation>();

    public static void translateView(final View view, final int[] newPosition, long duration) {

        int topMargin = newPosition[1];

        if (topMargin < 0) {
            topMargin = 0;
        }

        final RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(view.getLayoutParams());
        newParams.setMargins(0, topMargin, 0, 0);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

        for (int i = 0; i < params.getRules().length; i++) {
            newParams.addRule(i, params.getRules()[i]);
        }

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

                view.setLayoutParams(newParams);
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
