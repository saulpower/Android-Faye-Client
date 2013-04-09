package main.java.com.moneydesktop.finance.shared.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import main.java.com.moneydesktop.finance.data.Preferences;
import main.java.com.moneydesktop.finance.data.SyncEngine;
import main.java.com.moneydesktop.finance.model.User;
import main.java.com.moneydesktop.finance.shared.activity.DebugActivity;
import com.saulpower.fayeclient.FayeClient;
import com.saulpower.fayeclient.FayeClient.FayeListener;
import org.json.JSONObject;

import java.net.URI;

public class WebSocketService extends IntentService implements FayeListener {

    public final String TAG = this.getClass().getSimpleName();

    FayeClient mClient;
    Handler mHandler;

    public WebSocketService() {
        super("WebSocketService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // SSL bug in pre-Gingerbread devices makes websockets currently unusable
        if (android.os.Build.VERSION.SDK_INT <= 8 || User.getCurrentUser() == null || !User.getCurrentUser().getCanSync()) return;

        Log.i(TAG, "Starting Web Socket");

        try {

            String baseUrl = Preferences.getString(Preferences.KEY_FAYE_HOST, DebugActivity.PROD_FAYE_HOST);

            URI uri = URI.create(String.format("wss://%s:443/events", baseUrl));
            String channel = String.format("/%s/**", User.getCurrentUser().getUserId());

            JSONObject ext = new JSONObject();
            ext.put("authToken", User.getCurrentUser().getAuthorizationToken());

            mClient = new FayeClient(mHandler, uri, channel);
            mClient.setFayeListener(this);
            mClient.connectToServer(ext);

        } catch (Exception ex) {
            Log.e(TAG, "WebSocketService Error", ex);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClient != null) {
            mClient.disconnect();
            mClient.closeWebSocketConnection();
        }
    }

    @Override
    public void connectedToServer() {
        Log.i(TAG, "Connected to Server");
    }

    @Override
    public void disconnectedFromServer() {
        Log.i(TAG, "Disonnected from Server");
    }

    @Override
    public void subscribedToChannel(String subscription) {
        Log.i(TAG, String.format("Subscribed to channel %s on Faye", subscription));
    }

    @Override
    public void subscriptionFailedWithError(String error) {
        Log.i(TAG, String.format("Subscription failed with error: %s", error));
    }

    @Override
    public void messageReceived(JSONObject json) {

        Log.i(TAG, "Message Received: " + json.optString("event_type"));

        long current = System.currentTimeMillis();
        long lastSync = Preferences.getLong(Preferences.KEY_LAST_SYNC, current);

        // Give ourselves a 10 second buffer between syncs
        if (current - lastSync < 10000) return;

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                SyncEngine.sharedInstance().beginSync();
            }
        }, 1500);
    }

}
