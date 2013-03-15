package com.moneydesktop.finance.data;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.util.Comparators;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class SyncEngine {
	
	public final String TAG = "SyncEngine";

	private static SyncEngine sSharedInstance;

	/** We want to wait 5 minutes in between regular sync checks */
	private static final long MINIMUM_SYNC_WAIT = 300000;
	
	private final int TIMER_DELAY = 10000;
	
	private DataBridge mDataBridge;
	private EventBus mEventBus;
	
	private ArrayList<Bank> mBanksUpdating = null;
	private Handler mBankStatusTimer = null;
	private Runnable mBankStatusTask = new Runnable() {
		
		public void run() {

			bankStatusTimerFired();
			
			if (mBankStatusTimer != null)
				mBankStatusTimer.postDelayed(this, TIMER_DELAY);
		}
	};
	
	private boolean mRunning = false;
    private boolean mAccountRunning = false;
	private boolean mShouldSync = false;

	public static SyncEngine sharedInstance() {
		
		if (sSharedInstance == null) {
    		sSharedInstance = new SyncEngine();
    	}
    	
    	return sSharedInstance;
	}
	
	public SyncEngine() {
		
		this.mDataBridge = DataBridge.sharedInstance();
		this.mEventBus = EventBus.getDefault();
		
		this.mBanksUpdating = new ArrayList<Bank>();
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
		return mRunning;
	}

	public boolean hasPendingSync() {
		return mShouldSync;
	}
	
	public void syncIfNeeded() {
		
		if (mShouldSync && !mRunning && User.getCurrentUser().getCanSync()) {
			beginSync();
		}
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
		
		if (!mBanksUpdating.contains(bank)) {
			mBanksUpdating.add(bank);
		}
		
		if (mBankStatusTimer != null)
			return;
		
		startBankStatusTimer();
	}
	
	private void startBankStatusTimer() {
		
		if (User.getCurrentUser() != null && User.getCurrentUser().getCanSync() && mBanksUpdating.size() > 0 && mBankStatusTimer == null) {
			
			mBankStatusTimer = new Handler();
			mBankStatusTimer.postDelayed(mBankStatusTask, TIMER_DELAY);
			
		} else if (mBankStatusTimer != null) {
			
			mBankStatusTimer.removeCallbacks(mBankStatusTask);
		}
	}
	
	public void endBankStatusTimer() {
		
		if (mBankStatusTimer != null) {
			
			mBankStatusTimer.removeCallbacks(mBankStatusTask);
			mBankStatusTimer = null;
		}
		//DataController.save();
	}
	
	private void bankStatusTimerFired() {
		
		new Thread(new Runnable() {
			
			public void run() {
				
				synchronized(mBanksUpdating) {
					
					List<Bank> updatingCopy = new ArrayList<Bank>();
					
					updatingCopy = Arrays.asList(new Bank[mBanksUpdating.size()]);
					
					Collections.copy(updatingCopy, mBanksUpdating);
					
					for (int i = updatingCopy.size() -1; i >= 0; i--) {
						
						Bank bank = updatingCopy.get(i);

						JSONObject json = DataBridge.sharedInstance().getBankStatus(bank.getBankId());
						bank.updateStatus(json);

						if (bank.getProcessStatus().intValue()  >= 3){
							mBanksUpdating.remove(i);
						    mEventBus.post(new EventMessage().new BankStatusUpdateEvent(bank));
						}
					}
					
					if (mBanksUpdating.size() == 0) {
						
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
	
	public void syncCheck() {
		
		long now = System.currentTimeMillis();
		long lastSync = Preferences.getLong(Preferences.KEY_LAST_SYNC, now);
		
		long elapsed = (now - lastSync);
		
		if (MINIMUM_SYNC_WAIT < elapsed) {
			beginSync();
		}
	}
	
	public void beginSync() {
		
		if (User.getCurrentUser() == null || !User.getCurrentUser().getCanSync()) {
			return;
		}
		
		if (!mRunning && !mAccountRunning) {
			
		    Log.i(TAG, "Sync Started");
		    
			mRunning = true;
			
			mEventBus.post(new EventMessage().new SyncEvent(false));
			
			new AsyncTask<Void, Void, Boolean>() {
	    		
				@Override
				protected Boolean doInBackground(Void... params) {

					try {
						
						performSync();
						
					} catch (Exception e) {
						Log.e(TAG, "Could not perform sync", e);
					} finally {
						mRunning = false;
					}

					return true;
				}
	    		
	    		@Override
	    		protected void onPostExecute(Boolean result) {
	    			mEventBus.post(new EventMessage().new SyncEvent(true));
	    		}
				
			}.execute();
		}
	}
	
	private void performSync() throws JSONException {
	    
		JSONObject data = null;
		
		if (needsFullSync()) {
			
			data = mDataBridge.downloadSync(true);
			
		} else if (User.getCurrentUser() != null && User.getCurrentUser().getCanSync()) {
			
			processChangeSync();
			
			data = mDataBridge.downloadSync(false);
			
		} else {
			
			return;
		}
		
		// TODO: Ability to cancel and clean up sync (SyncEngine: 1587)
		
		if (data != null) {
			
			processSyncData(data);
			
		} else if (needsFullSync()) {
		
			setNeedsFullSync(true);
			mShouldSync = true;
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
			JSONObject savePackage = mDataBridge.uploadSync(uploadData);
			
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

            if (bo == null) {
                continue;
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
		
		if (syncToken != null) mDataBridge.endSync(syncToken);
		
		// Process updating account balances
		BankAccount.buildAccountBalances();
		
		mShouldSync = false;
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
		
		JSONArray json = mDataBridge.syncInstitutions();
		
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

    public void syncBankAccount(BankAccount bankAccount) {
        syncBankAccount(bankAccount, false);
    }

    public void syncBankAccount(BankAccount bankAccount, final boolean addNewTransaction) {

        Log.i(TAG, "Sync Running: " + mRunning);

        mAccountRunning = true;

        new AsyncTask<BankAccount, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(BankAccount... params) {

                BankAccount bankAccount = params[0];

                JSONObject jsonResponse = mDataBridge.saveManualBankAccount(bankAccount);

                if (jsonResponse != null) {

                    bankAccount.updateManualAccount(jsonResponse);

                    if (addNewTransaction) {
                        Transactions transactions = Transactions.createNewTransaction(bankAccount);
                        transactions.insertSingle();
                    }
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {

                mAccountRunning = false;

                if (addNewTransaction) {
                    beginSync();
                }
            }

        }.execute(bankAccount);
    }
}