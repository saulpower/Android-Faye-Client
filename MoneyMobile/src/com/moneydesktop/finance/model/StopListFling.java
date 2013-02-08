package com.moneydesktop.finance.model;

import android.widget.AbsListView;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StopListFling {

    private static Field mFlingEndField = null;
    private static Method mFlingEndMethod = null;

    static {
        try {
            mFlingEndField = AbsListView.class.getDeclaredField("mFlingRunnable");
            mFlingEndField.setAccessible(true);
            mFlingEndMethod = mFlingEndField.getType().getDeclaredMethod("endFling");
            mFlingEndMethod.setAccessible(true);
        } catch (Exception e) {
            mFlingEndMethod = null;
        }
    }

    public static void stop(ListView list) {
        if (mFlingEndMethod != null) {
            try {
                mFlingEndMethod.invoke(mFlingEndField.get(list));
            } catch (Exception e) {
            }
        }
    }
}
