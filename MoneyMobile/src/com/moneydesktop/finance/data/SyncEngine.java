package com.moneydesktop.finance.data;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BusinessObject;
import com.moneydesktop.finance.database.BusinessObjectBase;
import com.moneydesktop.finance.database.DatabaseDefaults;
import com.moneydesktop.finance.database.Institution;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.Comparators;

import de.greenrobot.event.EventBus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SyncEngine {
	
	public final String TAG = "SyncEngine";

	private static SyncEngine sharedInstance;

	private final int TIMER_DELAY = 10000;
	
	private DataBridge db;
	private EventBus eventBus;
	
	private ArrayList<Bank> banksUpdating = null;
	private Handler bankStatusTimer = null;
	private Runnable bankStatusTask = new Runnable() {
		
		public void run() {

			bankStatusTimerFired();
			
			if (bankStatusTimer != null)
				bankStatusTimer.postDelayed(this, TIMER_DELAY);
		}
	};
	
	private boolean isRunning = false;
	private boolean shouldSync = false;

	public static SyncEngine sharedInstance() {
		
		if (sharedInstance == null) {
    		sharedInstance = new SyncEngine();
    	}
    	
    	return sharedInstance;
	}
	
	public SyncEngine() {
		
		this.db = DataBridge.sharedInstance();
		this.eventBus = EventBus.getDefault();
		
		this.banksUpdating = new ArrayList<Bank>();
	}
	
	public boolean needsFullSync() {
		return Preferences.getBoolean(Preferences.KEY_NEEDS_FULL_SYNC, false);
	}

	public void setNeedsFullSync(boolean needsFullSync) {
		Preferences.saveBoolean(Preferences.KEY_NEEDS_FULL_SYNC, needsFullSync);
	}

	public boolean needsSync() {
		return Preferences.getBoolean(Preferences.KEY_NEEDS_SYNC, false);
	}

	public void setNeedsSync(boolean needsSync) {
		Preferences.saveBoolean(Preferences.KEY_NEEDS_SYNC, needsSync);
	}

	public boolean isSyncing() {
		return isRunning;
	}

	public boolean hasPendingSync() {
		return shouldSync;
	}
	
	public void syncIfNeeded() {
		
		if (shouldSync && !isRunning && User.getCurrentUser().getCanSync())
			beginSync();
	}

	/**
	 * This method performs a sync synchronously
	 */
	public void debugSync() {
		
		try {
			
			performSync();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void beginBankStatusUpdate(Bank bank) {
		
		if (!banksUpdating.contains(bank)) {
			banksUpdating.add(bank);
		}
		
		if (bankStatusTimer != null)
			return;
		
		startBankStatusTimer();
	}
	
	private void startBankStatusTimer() {
		
		if (User.getCurrentUser().getCanSync() && banksUpdating.size() > 0 && bankStatusTimer == null) {
			
			bankStatusTimer = new Handler();
			bankStatusTimer.postDelayed(bankStatusTask, TIMER_DELAY);
		}
	}
	
	public void endBankStatusTimer() {
		
		if (bankStatusTimer != null) {
			
			bankStatusTimer.removeCallbacks(bankStatusTask);
			bankStatusTimer = null;
		}
		//DataController.save();
	}
	
	private void bankStatusTimerFired() {
		
		new Thread(new Runnable() {
			
			public void run() {
				
				synchronized(banksUpdating) {
					
					List<Bank> updatingCopy = new ArrayList<Bank>();
					
					updatingCopy = Arrays.asList(new Bank[banksUpdating.size()]);  
					
					Collections.copy(updatingCopy, banksUpdating);
					
					for (int i = updatingCopy.size() -1; i >= 0; i--) {
						
						Bank bank = updatingCopy.get(i);

						JSONObject json = DataBridge.sharedInstance().getBankStatus(bank.getBankId());
						bank.updateStatus(json);

						if (bank.getProcessStatus().intValue()  >= 3){
							banksUpdating.remove(i);
						    eventBus.post(new EventMessage().new BankStatusUpdateEvent(bank));
						}
					}
					
					if (banksUpdating.size() == 0) {
						
						// Run code on main thread
						new Handler(ApplicationContext.getContext().getMainLooper()).post(new Runnable() {
							
							public void run() {

								endBankStatusTimer();
								beginSync();
							}
						});
					}
				}
			}
			
		}).start();
	}
	
	public void beginSync() {
		
		if (User.getCurrentUser() == null || !User.getCurrentUser().getCanSync()) {
			return;
		}
		
		if (!isRunning) {
			
		    Log.i(TAG, "Sync Started");
		    
			isRunning = true;
			
			eventBus.post(new EventMessage().new SyncEvent(false));
			
			new AsyncTask<Void, Void, Boolean>() {
	    		
				@Override
				protected Boolean doInBackground(Void... params) {

					try {
						
						performSync();
						
					} catch (JSONException e) {
						Log.e(TAG, "Could not perform sync", e);
					} finally {
						isRunning = false;
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
		
		if (needsFullSync()) {
			
			data = db.downloadSync(true);
			
		} else if (User.getCurrentUser() != null && User.getCurrentUser().getCanSync()) {
			
			processChangeSync();
			
			data = db.downloadSync(false);
			
		} else {
			
			return;
		}
		
		// TODO: Ability to cancel and clean up sync (SyncEngine: 1587)
		
		if (data != null) {
			
			processSyncData(data);
			
		} else if (needsFullSync()) {
		
			setNeedsFullSync(true);
			shouldSync = true;
		}
		
		// Save the sync date to preferences
		Preferences.saveLong(Preferences.KEY_LAST_SYNC, System.currentTimeMillis());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processChangeSync() throws JSONException {
		
		// Collect objects that have DataStates of New, Modified, Deleted
		JSONObject created = new JSONObject();
		JSONObject updated = new JSONObject();
		JSONObject deleted = new JSONObject();
		
		Map<String, Object> temp = null;
		Map<String, Map<String, Object>> keyedRecords = null;
		Map<Class<?>, Map<String, Map<String, Object>>> allChanges = new HashMap<Class<?>, Map<String, Map<String, Object>>>();
		
		for (Map.Entry<String, Class<?>> entry : Constant.OBJECT_TYPES.entrySet()) {
		    String key = entry.getKey();
		    Class<?> value = entry.getValue();
			
			keyedRecords = new HashMap<String, Map<String, Object>>();
			
			for (String operation : Constant.OPERATIONS.keySet()) {
				
				DataState state = Constant.OPERATIONS.get(operation);
				
				temp = mapOfChangedObjects(value, Constant.OPERATIONS.get(operation));
				JSONArray json = (JSONArray) temp.get(Constant.KEY_JSON);
				
				if (json != null && json.length() > 0) {
					
					switch (state) {
					case DATA_STATE_NEW:
						created.put(key, json);
						break;
					case DATA_STATE_MODIFIED:
						updated.put(key, json);
						break;
					case DATA_STATE_DELETED:
						deleted.put(key, json);
						break;
					default:
						break;
					}
					
					keyedRecords.put(operation, temp);
				}
			}
			
			allChanges.put(value, keyedRecords);
		}
		
		// Create JSON structure to sync up data with server
		JSONObject records = new JSONObject();

        if (deleted.length() > 0) {
        	records.put(Constant.KEY_DELETED, deleted);
        }
        if (updated.length() > 0) {
            records.put(Constant.KEY_UPDATED, updated);
        }
		if (created.length() > 0) {
			records.put(Constant.KEY_CREATED, created);
		}
		
		if (records.length() > 0) {
			
			JSONObject uploadData = new JSONObject();
			JSONObject deviceData = new JSONObject();
			
			deviceData.put(Constant.KEY_USER_GUID, User.getCurrentUser().getUserId());
			deviceData.put(Constant.KEY_RECORDS, records);
			uploadData.put(Constant.KEY_DEVICE, deviceData);
			
			// Send data to server
			JSONObject savePackage = db.uploadSync(uploadData);
			
			// User server response and update local objects (DataState)
			if (savePackage != null) {
				
				deviceData = savePackage.optJSONObject(Constant.KEY_DEVICE);
				records = deviceData.optJSONObject(Constant.KEY_RECORDS);
				
				Iterator iterator = records.keys();
				
				while (iterator.hasNext()) {
					
					String key = (String) iterator.next();
					
					JSONObject objects = records.optJSONObject(key);
					Iterator objIterator = objects.keys();
					
					while (objIterator.hasNext()) {
						
						String objectType = (String) objIterator.next();
						
						Class<?> mappedType = Constant.OBJECT_TYPES.get(objectType);
						
						if (objectType.equals(Constant.KEY_TAGS) ||
							objectType.equals(Constant.KEY_MEMBERS) ||
							objectType.equals(Constant.KEY_ACCOUNTS) ||
							objectType.equals(Constant.KEY_TRANSACTIONS) ||
							objectType.equals(Constant.KEY_CATEGORIES) ||
							objectType.equals(Constant.KEY_BUDGETS)) {
								
							updateSaveResults(objects.optJSONArray(objectType), key, (List<Object>) allChanges.get(mappedType).get(key).get(Constant.KEY_OBJECTS));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Create a map with the changed objects and a JSON representation
	 * of those objects.
	 * 
	 * @param key
	 * @param dataState
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Map<String, Object> mapOfChangedObjects(Class<?> key, DataState dataState) throws IllegalArgumentException, JSONException {
		
		if (key.equals(BusinessObjectBase.class)) {
			throw new IllegalArgumentException("Cannot query BusinessObjectBase class");
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		JSONArray json = new JSONArray();
		
		List<Object> objects = DataController.getByDataState(key, dataState);
		
		if (objects != null) {
			
			for (Object object : objects) {
				
				if (object instanceof BusinessObject) {
					json.put(((BusinessObject) object).getJson());
				}
			}
			
			map.put(Constant.KEY_OBJECTS, objects);
			map.put(Constant.KEY_JSON, json);
		}
		
		return map;
	}
	
	private void updateSaveResults(JSONArray results, String operation, List<Object> objects) {
		
		for (int i = 0; i < results.length(); i++) {
			
			JSONObject result = results.optJSONObject(i);
			String externalId = result.optString(Constant.KEY_EXTERNAL_ID);
			String guid = result.optString(Constant.KEY_GUID).toString();
			
			if (externalId.equals("")) {
				externalId = guid;
			}
			BusinessObject bo = null;
			
			for (Object object : objects) {
				if (((BusinessObject) object).getExternalId() != null && ((BusinessObject) object).getExternalId().equals(externalId)) {
					bo = (BusinessObject) object;
					break;
				}
			}
			
			int status = 200;
			
			if (!result.optString(Constant.KEY_STATUS_CODE).equals(Constant.VALUE_NULL)) {
				status = result.optInt(Constant.KEY_STATUS_CODE);
			}
			
			if (operation.equalsIgnoreCase(Constant.KEY_CREATED)) {
				
				switch (status) {
				case 200:
					String systemId = result.optString(Constant.KEY_GUID);
					bo.setExternalId(systemId);
				case 400:
					bo.acceptChanges();
					bo.updateBatch();
					break;
				}
				
			} else if (operation.equalsIgnoreCase(Constant.KEY_UPDATED)) {
				
				switch (status) {
				case 200:
					if (bo.getExternalId() == null || bo.getExternalId().equals("")) {

						String systemId = result.optString(Constant.KEY_GUID);
						bo.setExternalId(systemId);
					}
				case 404:
					bo.acceptChanges();
					bo.updateBatch();
					break;
				}
				
			} else if (operation.equalsIgnoreCase(Constant.KEY_DELETED)) {
				
				switch (status) {
				case 200:
				case 400:
					bo.deleteBatch();
					break;
				}
			}
		}
		
		DataController.save();
	}
	
	private void processSyncData(JSONObject data) throws JSONException {

		DatabaseDefaults.ensureCategoryTypesLoaded();
		DatabaseDefaults.ensureAccountTypeGroupsLoaded();
		DatabaseDefaults.ensureAccountTypesLoaded();
		
		JSONObject device = data.getJSONObject(Constant.KEY_DEVICE);
		
		boolean isFullSync = device.getBoolean(Constant.KEY_FULL_SYNC);
		setNeedsFullSync(isFullSync);
		
		String syncToken = null;
		
		if (!isFullSync) {
			syncToken = device.getString(Constant.KEY_SYNC_TOKEN);
		}
		
		// Process the data received from the sync request
		preprocessSyncData(device);
		DataController.saveSyncData(device, isFullSync);
		
		if (syncToken != null) db.endSync(syncToken);
		
		// Process updating account balances
		BankAccount.buildAccountBalances();
		
		shouldSync = false;
		setNeedsFullSync(false);
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
		JSONArray originalCategories = records.getJSONObject(Constant.KEY_CREATED).optJSONArray(Constant.KEY_CATEGORIES);
		if (originalCategories != null) {
				
			jsonObjects = Util.toList(originalCategories);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidNameComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_CREATED).put(Constant.KEY_CATEGORIES, new JSONArray(jsonObjects));
		}
		
		originalCategories = records.getJSONObject(Constant.KEY_UPDATED).optJSONArray(Constant.KEY_CATEGORIES);
		if (originalCategories != null) {
			
			jsonObjects = Util.toList(originalCategories);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidNameComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_UPDATED).put(Constant.KEY_CATEGORIES, new JSONArray(jsonObjects));
		}

		// Ordering Transactions
		JSONArray originalTransactions = records.getJSONObject(Constant.KEY_CREATED).optJSONArray(Constant.KEY_TRANSACTIONS);
		if (originalTransactions != null) {
				
			jsonObjects = Util.toList(originalTransactions);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidDateComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_CREATED).put(Constant.KEY_TRANSACTIONS, new JSONArray(jsonObjects));
		}
		
		originalTransactions = records.getJSONObject(Constant.KEY_UPDATED).optJSONArray(Constant.KEY_TRANSACTIONS);
		if (originalTransactions != null) {
				
			jsonObjects = Util.toList(originalTransactions);
			Collections.sort(jsonObjects, new Comparators().new ParentGuidDateComparator());
			device.getJSONObject(Constant.KEY_RECORDS).getJSONObject(Constant.KEY_UPDATED).put(Constant.KEY_TRANSACTIONS, new JSONArray(jsonObjects));
		}
	}
	
	public void syncInstitutions() {
		
		DataController.deleteData(Institution.class);
		
		JSONArray json = db.syncInstitutions();
		
		if (json != null) {
			
			for (int i = 0; i < json.length(); i++) {
				
				if ((i % 1000) == 0) {
					
					try {
						
						JSONObject data = json.getJSONObject(i);
						Institution.saveInstitution(data, false);
						
					} catch (JSONException ex) {
						Log.e(TAG, "Could not save institution", ex);
					}
				}
			}
			
			DataController.save();
		}
		
		Preferences.saveLong(Preferences.KEY_LAST_INSTITUTION_SYNC, (new Date()).getTime());
	}
}