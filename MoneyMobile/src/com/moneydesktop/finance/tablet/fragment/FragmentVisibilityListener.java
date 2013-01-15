package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;

/**
 * this interface allows fragments to be notified when they are about to be hidden or shown. This should be used to perform
 * initialization and cleanup tasks.
 * @author kentandersen
 *
 */
public interface FragmentVisibilityListener {
    
    public void onShow(Activity activity);
}
