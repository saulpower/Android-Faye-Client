package main.java.com.moneydesktop.finance.shared.Services;

import android.app.IntentService;
import android.content.Intent;

import main.java.com.moneydesktop.finance.data.SyncEngine;


//This method automatically runs/creates on a separate thread. Unlike extending "Service"
public class SyncService extends IntentService{

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SyncEngine.sharedInstance().beginSync();

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

}
