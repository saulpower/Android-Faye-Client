package com.moneydesktop.finance;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;

import com.crittercism.app.Crittercism;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneydesktop.finance.data.Enums.LockType;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.DaoMaster;
import com.moneydesktop.finance.database.DaoMaster.DevOpenHelper;
import com.moneydesktop.finance.database.DaoSession;
import com.moneydesktop.finance.database.DatabaseDefaults;
import com.moneydesktop.finance.database.Institution;
import com.moneydesktop.finance.exception.CustomExceptionHandler;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.AuthEvent;
import com.moneydesktop.finance.model.EventMessage.LoginEvent;
import com.moneydesktop.finance.model.EventMessage.LogoutEvent;
import com.moneydesktop.finance.shared.Services.WebSocketService;
import com.moneydesktop.finance.shared.activity.BaseActivity;

import de.greenrobot.event.EventBus;

public class ApplicationContext extends Application {
	
	public final String TAG = "ApplicationContext";

	private static ApplicationContext sInstance;
	
	private static ObjectMapper sMapper;
	
    private static SQLiteDatabase sDb;
    private static DaoMaster sDaoMaster;
    private static DaoSession sDaoSession;
	
	private static boolean sScreenOn = true;
    private static boolean sLockShowing = false;

    private BroadcastReceiver sScreenLock = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				sScreenOn = true;
			}
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				sScreenOn = false;
			}
			
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT) && sScreenOn && BaseActivity.sInForeground) {
				
				String code = Preferences.getString(Preferences.KEY_LOCK_CODE, "");
				if (!code.equals("")) {
					showLockScreen();
				}
			}
		}
	};
	
	public void onCreate() {
		super.onCreate();

        Crittercism.init(this, "50258d166c36f91a1b000004");
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        Intent intent = new Intent(this, WebSocketService.class);
		startService(intent);
        
        initializeDatabase();
		DatabaseDefaults.ensureInstitutionsLoaded();
		
		EventBus.getDefault().register(this);
		
        getObjectMapper();

    	registerScreenLock();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		EventBus.getDefault().unregister(this);
		
        Intent intent = new Intent(this, WebSocketService.class);
		stopService(intent);
		
		unregisterReceiver(sScreenLock);
	}
	
	private void initializeDatabase() {

        DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "finance-db", null);
        sDb = helper.getWritableDatabase();
        sDaoMaster = new DaoMaster(sDb);
        sDaoSession = sDaoMaster.newSession();
	}
	
	public static void startNewDatabaseSession() {
		sDaoMaster.newSession();
	}
	
	private void registerScreenLock() {

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
    	registerReceiver(sScreenLock, filter);
	}
	
	private void showLockScreen() {
		
		EventBus.getDefault().post(new EventMessage().new LockEvent(LockType.LOCK));
	}
	
	/*********************************************************************************
	 * Event Listening
	 *********************************************************************************/
	
	public void onEvent(LoginEvent event) {
		
		SyncEngine.sharedInstance().setNeedsFullSync(true);
		SyncEngine.sharedInstance().beginSync();
		
        Intent intent = new Intent(this, WebSocketService.class);
		startService(intent);
	}
	
	public void onEvent(LogoutEvent event) {

        Intent intent = new Intent(this, WebSocketService.class);
		stopService(intent);
	}
	
	public void onEvent(AuthEvent event) {
		
		Institution.processLocalBanksFile(R.raw.institutions);
	}
	
	/*********************************************************************************
	 * Getters
	 *********************************************************************************/
	
    public ApplicationContext() {
        sInstance = this;
    }
    
    public static Context getContext() {
        return sInstance;
    }
    
    public static ObjectMapper getObjectMapper() {
    	
    	if (sMapper == null) {
    		sMapper = new ObjectMapper();
    		sMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		sMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    	}
    	
    	return sMapper;
    }

	public static SQLiteDatabase getDb() {
		return sDb;
	}

	public static DaoMaster getDaoMaster() {
		return sDaoMaster;
	}

	public static DaoSession getDaoSession() {
		return sDaoSession;
	}
	
    public static boolean isTablet() {
        return Preferences.getBoolean(Preferences.KEY_IS_TABLET, false);
    }

    public static void setIsTablet(boolean sIsTablet) {
        Preferences.saveBoolean(Preferences.KEY_IS_TABLET, sIsTablet);
    }
    
    public static boolean isLargeTablet() {
        return Preferences.getBoolean(Preferences.KEY_IS_LARGE_TABLET, false);
    }

    public static void setIsLargeTablet(boolean sIsLargeTablet) {
        Preferences.saveBoolean(Preferences.KEY_IS_LARGE_TABLET, sIsLargeTablet);
    }

    public static boolean isLockShowing() {
        return sLockShowing;
    }

    public static void setLockShowing(boolean sLockShowing) {
        ApplicationContext.sLockShowing = sLockShowing;
    }
}
