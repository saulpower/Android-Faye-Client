package com.moneydesktop.finance.shared.Services;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import com.moneydesktop.finance.data.SyncEngine;


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
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }

}
