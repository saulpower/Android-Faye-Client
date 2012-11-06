package com.moneydesktop.finance.data;

import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.moneydesktop.finance.database.DataController;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.Comparators;
import com.moneydesktop.finance.util.Util;

import de.greenrobot.event.EventBus;

public class SyncEngine {
	
	private final String TAG = "SyncEngine";

	private static SyncEngine sharedInstance;
	
	private DataBridge db;
	private EventBus eventBus;
	
	private boolean needsFullSync = false;
	private boolean needsSync = false;
	private boolean isRunning = false;

	public static SyncEngine sharedInstance() {
		
		if (sharedInstance == null) {
    		sharedInstance = new SyncEngine();
    	}
    	
    	return sharedInstance;
	}
	
	public SyncEngine() {
		
		this.db = DataBridge.sharedInstance();
		this.eventBus = EventBus.getDefault();
	}
	
	public void beginSync(final boolean fullSync) {
		
		needsFullSync = fullSync;
		
		if (!User.getCurrentUser().canSync()) {
			return;
		}
		
		if (!isRunning) {
			
			isRunning = true;
			
			eventBus.post(new EventMessage().new SyncEvent(false));
			
			new AsyncTask<Void, Void, Boolean>() {
	    		
				@Override
				protected Boolean doInBackground(Void... params) {

					try {
						
						performSync();
						
					} catch (JSONException e) {
						Log.e(TAG, "Could not perform sync", e);
					}

					return true;
				}
	    		
	    		@Override
	    		protected void onPostExecute(Boolean result) {

	    			eventBus.post(new EventMessage().new SyncEvent(true));
	    		}
				
			}.execute();
		}
	}
	
	private void performSync() throws JSONException {
		
		JSONObject data = null;
		
		if (needsFullSync) {
			
			data = db.downloadSync(true);
		
		} else {
			
			// TODO: Handle sync when a full sync is not necessary
		}
		
		// TODO: Ability to cancel and clean up sync (SyncEngine: 1587)
		
		if (data != null) {
			
			long start = System.currentTimeMillis();
			
			DataController.ensureCategoryTypesLoaded();
			DataController.ensureAccountTypesLoaded();
			DataController.ensureAccountTypeGroupsLoaded();
			
			JSONObject device = data.getJSONObject(Constant.KEY_DEVICE);
			
			boolean isFullSync = device.getBoolean(Constant.KEY_FULL_SYNC);
			needsFullSync = isFullSync;
			
			String syncToken = null;
			
			if (!isFullSync) {
				syncToken = device.getString(Constant.KEY_SYNC_TOKEN);
			}

			Log.i(TAG, "DB initialization: " + (System.currentTimeMillis() - start) + " ms");
			start = System.currentTimeMillis();
			
			preprocessSyncData(device);
			DataController.saveSyncData(device, isFullSync);
			
			Log.i(TAG, "DB sync: " + (System.currentTimeMillis() - start) + " ms");
			
		} else if (needsFullSync) {
		
			needsSync = true;
		}
		
		// Save last sync'd date
		Preferences.saveLong(Preferences.KEY_LAST_SYNC, System.currentTimeMillis());
		
		isRunning = false;
	}
	
	/**
	 * Sort Transactions and Categories so parents come first to facilitate
	 * proper insertion into database
	 * 
	 * @param device
	 * @throws JSONException 
	 */
	private void preprocessSyncData(JSONObject device) throws JSONException {
		
		JSONObject records = device.getJSONObject(Constant.KEY_RECORDS);
		List<JSONObject> jsonObjects = null;
		
		// Ordering Categories
		JSONArray originalCategories = records.getJSONObject(Constant.KEY_CREATED).optJSONArray(Constant.CATEGORIES);
		if (originalCategories != null) {
				
			jsonObjects = Util.toList(originalCategories);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidNameComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_CREATED).put(Constant.CATEGORIES, new JSONArray(jsonObjects));
		}
		
		originalCategories = records.getJSONObject(Constant.KEY_UPDATED).optJSONArray(Constant.CATEGORIES);
		if (originalCategories != null) {
			
			jsonObjects = Util.toList(originalCategories);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidNameComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_UPDATED).put(Constant.CATEGORIES, new JSONArray(jsonObjects));
		}

		// Ordering Transactions
		JSONArray originalTransactions = records.getJSONObject(Constant.KEY_CREATED).optJSONArray(Constant.TRANSACTIONS);
		if (originalTransactions != null) {
				
			jsonObjects = Util.toList(originalTransactions);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidDateComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_CREATED).put(Constant.TRANSACTIONS, new JSONArray(jsonObjects));
		}
		
		originalTransactions = records.getJSONObject(Constant.KEY_UPDATED).optJSONArray(Constant.TRANSACTIONS);
		if (originalTransactions != null) {
				
			jsonObjects = Util.toList(originalTransactions);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidDateComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_UPDATED).put(Constant.TRANSACTIONS, new JSONArray(jsonObjects));
		}
	}
}
