package main.java.com.moneydesktop.finance.exception;

import android.util.Log;
import com.flurry.android.FlurryAgent;
import main.java.com.moneydesktop.finance.data.DataController;
import main.java.com.moneydesktop.finance.data.Preferences;

import java.lang.Thread.UncaughtExceptionHandler;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CustomExceptionHandler";

    /*
     * if any of the parameters are null, the respective functionality
     * will not be used
     */
    public CustomExceptionHandler() {
        Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {

        Log.e(TAG, "Error", e);

        String errorId = DataController.createRandomGuid();

        Preferences.saveString(Preferences.KEY_CRASH, errorId);

        FlurryAgent.onError(errorId, e.getMessage(), Log.getStackTraceString(e));

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}