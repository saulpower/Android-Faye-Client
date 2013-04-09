package main.java.com.moneydesktop.finance.tablet.activity;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import main.java.com.moneydesktop.finance.shared.activity.DashboardBaseActivity;
import main.java.com.moneydesktop.finance.util.UiUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@TargetApi(11)
public abstract class DialogBaseActivity extends DashboardBaseActivity {

    private final int KEYBOARD_THRESHOLD = 100;

    private OnKeyboardStateChangeListener mKeyboardStateChangeListener;

    private float mKeyboardThreshold;
    private Rect mVisibleBounds = new Rect();
    private View mRoot;
    private int[] mLocation = new int[2];

    private Map<View, int[]> mModifiedViews = new HashMap<View, int[]>();

    private boolean mKeyboardShowing = false;

    protected OnKeyboardStateChangeListener getKeyboardStateChangeListener() {
        return mKeyboardStateChangeListener;
    }

    protected void setKeyboardStateChangeListener(
            OnKeyboardStateChangeListener mKeyboardStateChangeListener) {
        this.mKeyboardStateChangeListener = mKeyboardStateChangeListener;
    }

    public Rect getVisibleBounds() {
        return mVisibleBounds;
    }

    public boolean isKeyboardShowing() {
        return mKeyboardShowing;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKeyboardThreshold = UiUtils.getDynamicPixels(this, KEYBOARD_THRESHOLD);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupVisibilityTracking();
    }

    private void setupVisibilityTracking() {

        if (mRoot != null) {
            return;
        }

        mRoot = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                int heightDiff = mRoot.getRootView().getHeight() - mRoot.getHeight();
                updateBounds();

                if (Math.abs(heightDiff) > mKeyboardThreshold && !mKeyboardShowing) {

                    mKeyboardShowing = true;
                    notifyKeyboardStateChanged();

                } else if (heightDiff == 0 && mKeyboardShowing) {

                    mKeyboardShowing = false;
                    notifyKeyboardStateChanged();
                    restoreViews();
                }
             }
        });

        updateBounds();
    }

    private void updateBounds() {

        mRoot.getLocationOnScreen(mLocation);

        mVisibleBounds.left = mLocation[0];
        mVisibleBounds.top = mLocation[1];
        mVisibleBounds.right = mVisibleBounds.left + mRoot.getRootView().getWidth();
        mVisibleBounds.bottom = mVisibleBounds.top + mRoot.getRootView().getHeight();
    }

    private void notifyKeyboardStateChanged() {

        if (mKeyboardStateChangeListener != null) {
            mKeyboardStateChangeListener.keyboardStateDidChange(mKeyboardShowing);
        }
    }

    public void makeViewVisible(View v, View parent) {

        if (v == null || parent == null) return;

        // Get the view's location on screen
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        location[1] -= mLocation[1];

        // Determine if it is visible
        if (mVisibleBounds.contains(location[0], (location[1] + v.getHeight())) && mVisibleBounds.contains(location[0], (location[1] - v.getHeight()))) return;

        // Calculate distance needed to be moved
        long moveDistance = (location[1] + (v.getHeight() / 2)) - mVisibleBounds.centerY();

        parent.getLocationOnScreen(location);
        location[1] -= mLocation[1];

        // Cache view and original location so we can restore it
        if (!mModifiedViews.containsKey(parent)) {
            mModifiedViews.put(parent, location);
        }

        // Animate movement of parent view
        ObjectAnimator move = ObjectAnimator.ofFloat(parent, "y", parent.getY(), (parent.getY() - moveDistance));
        move.setDuration(300);
        move.start();
    }

    /**
     * Restores all moved views to their previous positions
     */
    private void restoreViews() {

        for (Entry<View, int[]> entry : mModifiedViews.entrySet()) {

            View v = entry.getKey();
            int[] location = entry.getValue();

            ObjectAnimator move = ObjectAnimator.ofFloat(v, "y", v.getY(), location[1]);
            move.setDuration(300);
            move.start();
        }
    }

    public interface OnKeyboardStateChangeListener {
        public void keyboardStateDidChange(boolean isShowing);
    }
}
