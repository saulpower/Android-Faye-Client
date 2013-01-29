package com.moneydesktop.finance.data;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.data.Enums.TxType;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeGroup;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountBalance;
import com.moneydesktop.finance.database.BudgetItem;
import com.moneydesktop.finance.database.BusinessObject;
import com.moneydesktop.finance.database.BusinessObjectBase;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.CategoryType;
import com.moneydesktop.finance.database.DaoSession;
import com.moneydesktop.finance.database.DatabaseDefaults;
import com.moneydesktop.finance.database.Institution;
import com.moneydesktop.finance.database.Location;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.database.TagInstance;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;
import de.greenrobot.event.EventBus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataController {
	
	public static final String TAG = "DatabaseDefaults";

	private static Map<Class<?>, List<Object>> sPendingInsertTx = new HashMap<Class<?>, List<Object>>();
	private static Map<Class<?>, List<Object>> sPendingUpdateTx = new HashMap<Class<?>, List<Object>>();
	private static Map<Class<?>, List<Object>> sPendingDeleteTx = new HashMap<Class<?>, List<Object>>();
	
	private static Map<String, Object> sPendingCache = new HashMap<String, Object>();
	
	private static int sCount = 0;
	
	private static Class<?>[] sObjectOrder = new Class<?>[] {
        AccountType.class,
        AccountTypeGroup.class,
        Bank.class,
        BankAccount.class,
        BankAccountBalance.class,
        BudgetItem.class,
        Category.class,
        CategoryType.class,
        Institution.class,
        Location.class,
        Tag.class,
        TagInstance.class,
        Transactions.class,
        BusinessObjectBase.class
    };
	
	/**
	 * Saves all the pending transactions in a batch fashion
	 */
	public static synchronized void save() {

		long start = System.currentTimeMillis();
		sCount = 0;
		
		processTx(sPendingInsertTx, TxType.INSERT);
		processTx(sPendingUpdateTx, TxType.UPDATE);
		processTx(sPendingDeleteTx, TxType.DELETE);
		
		// Reset pending transaction list
		sPendingInsertTx.clear();
		sPendingUpdateTx.clear();
		sPendingDeleteTx.clear();
		sPendingCache.clear();
		
		EventBus.getDefault().post(new EventMessage().new DatabaseSaveEvent());
		Log.i(TAG, "Processed " + sCount + " records in " + (System.currentTimeMillis() - start) + " ms");
	}
	
	/**
	 * Iterates through the passed-in map, which contains a list of entities
	 * for a class type.  For each list the appropriate DAO is retrieved based
	 * on the class type.  The list of entities is then applied the appropriate
	 * transaction in a batch fashion.
	 * 
	 * @param pendingTx
	 * @param type
	 */
	@SuppressWarnings("unchecked")
	private static synchronized void processTx(Map<Class<?>, List<Object>> pendingTx, TxType type) {
		
		if (pendingTx.keySet().size() == 0) {
			return;
		}
		
		for (Class<?> key : sObjectOrder) {
			
		    List<Object> entities = pendingTx.get(key);
		    
		    if (entities != null && entities.size() > 0) {
			    
			    AbstractDao<Object, Long> dao = (AbstractDao<Object, Long>) getDao(key);
			    
			    for (Object object : entities) {
			    	((BusinessObject) object).willSave(type == TxType.DELETE);
			    }
			    
			    try {
			    	
				    switch (type) {
				    case INSERT:
				    	dao.insertInTx(entities);
				    	break;
					case UPDATE:
				    	dao.updateInTx(entities);
						break;
					case DELETE:
				    	dao.deleteInTx(entities);
						break;
				    }
				    
			    } catch (DaoException ex) {
			    	Log.e(TAG, "Error processing transaction", ex);
			    } catch (SQLiteConstraintException ex) {
                    Log.e(TAG, "Error processing transaction", ex);
                }
			    
			    sCount += entities.size();
		    }
		}
	}
	
	/**
	 * Returns the appropriate DAO object for the given class
	 * 
	 * @param key
	 * @return
	 */
	public static AbstractDao<?, Long> getDao(Class<?> key) {
		
		DaoSession session = ApplicationContext.getDaoSession();
		
		if (key.equals(BusinessObjectBase.class))
			return session.getBusinessObjectBaseDao();
		if (key.equals(Tag.class))
			return session.getTagDao();
		if (key.equals(Category.class))
			return session.getCategoryDao();
		if (key.equals(CategoryType.class))
			return session.getCategoryTypeDao();
		if (key.equals(AccountType.class))
			return session.getAccountTypeDao();
		if (key.equals(AccountTypeGroup.class))
			return session.getAccountTypeGroupDao();
		if (key.equals(Bank.class))
			return session.getBankDao();
		if (key.equals(BankAccount.class))
			return session.getBankAccountDao();
		if (key.equals(BankAccountBalance.class))
			return session.getBankAccountBalanceDao();
		if (key.equals(BudgetItem.class))
			return session.getBudgetItemDao();
		if (key.equals(Institution.class))
			return session.getInstitutionDao();
		if (key.equals(Location.class))
			return session.getLocationDao();
		if (key.equals(TagInstance.class))
			return session.getTagInstanceDao();
		if (key.equals(Transactions.class))
			return session.getTransactionsDao();
		
		return null;
	}
	
	public static void insert(Object object, String guid) {
		
		addTransaction(object, guid, TxType.INSERT);
	}
	
	public static void update(Object object) {

		addTransaction(object, TxType.UPDATE);
	}
    
    public static void softDelete(Object object) {

        addTransaction(object, TxType.UPDATE);
    }
	
	public static void delete(Object object) {
		
		((BusinessObject) object).setDeleted(true);
		addTransaction(object, TxType.DELETE);
	}
	
	private static void addTransaction(Object object, TxType type) {
		addTransaction(object, null, type);
	}
	
	/**
	 * Adds a transaction to the appropriate pending transaction map.  We also
	 * add the passed-in object to a cache in case it needs to be referenced
	 * before it has been inserted into the database.
	 * 
	 * @param object
	 * @param type
	 */
	private static void addTransaction(Object object, String guid, TxType type) {
		
		if (object == null) {
			return;
		}
		
		Class<?> key = object.getClass();
		
		if (!object.getClass().equals(BusinessObjectBase.class)) {

			if (guid == null) {
				guid = ((BusinessObject) object).getExternalId();
			}
			
			String keyId = key.getName() + guid;
			sPendingCache.put(keyId, object);
		}
		
		List<Object> list = null;
		Map<Class<?>, List<Object>> pendingTx = null;
		
		switch (type) {
    		case INSERT:
    			pendingTx = sPendingInsertTx;
    			break;
    		case UPDATE:
    			pendingTx = sPendingUpdateTx;
    			break;
    		case DELETE:
    			pendingTx = sPendingDeleteTx;
    			break;
		}
		
		if (pendingTx != null) {
			
			list = getList(key, pendingTx);
			
			if (!list.contains(object)) {
				list.add(object);
			}
		}
	}
	
	/**
	 * Returns a list from the map based on the given key.  If no list exists for
	 * the key a list is created and added to the map.
	 * 
	 * @param key
	 * @param map
	 * @return
	 */
	private static List<Object> getList(Class<?> key, Map<Class<?>, List<Object>> map) {
		
		List<Object> list = map.get(key);
		
		if (list == null) {
			list = new ArrayList<Object>();
			map.put(key, list);
		}
		
		return list;
	}
	
	/**
	 * Returns an object that is sitting in one of our pending transaction maps.
	 * Used to check for objects that have been created but not yet inserted
	 * into the database.
	 * 
	 * @param id
	 * @return
	 */
	public static Object checkCache(String id) {
		
		return sPendingCache.get(id);
	}
	
	/**
	 * Deletes all objects in the database for a given
	 * class.
	 * 
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public static synchronized void deleteData(Class<?> key) {
		
		AbstractDao<Object, Long> dao = (AbstractDao<Object, Long>) getDao(key);
		List<Object> entities = (List<Object>) dao.loadAll();
		
		for (Object object : entities) {
			
			if (object instanceof BusinessObject)
				((BusinessObject) object).suspendDataState();
		}
		
		dao.deleteInTx(entities);
	}
	
	/**
	 * Iterates through the JSON response and performs the appropriate commands
	 * on the included objects in the database.
	 * 
	 * @param device
	 * @param fullSyncRequired
	 * @throws JSONException
	 */
	public static void saveSyncData(JSONObject device, boolean fullSyncRequired) throws JSONException {

		long start = System.currentTimeMillis();
		int count = 0;
		
		JSONObject records = device.getJSONObject(Constant.KEY_RECORDS);
		
		for (String operation : Constant.OPERATION_ORDER) {
			
			boolean delete = operation.equals(Constant.KEY_DELETED);
			JSONObject operationObj = records.getJSONObject(operation);
			
			for (int type = 0; type < Constant.OBJECT_ORDER.length; type++) {
				
				String objectType = Constant.OBJECT_ORDER[type];
				JSONArray objects = operationObj.optJSONArray(objectType);
				
				if (objects != null) {
					
					for (int i = 0; i < objects.length(); i++) {
						
						try {
							
							JSONObject data = objects.getJSONObject(i);
							
							// parse based on object type
							parseAndSave(data, type, delete);
							
							count++;
							
						} catch (JSONException ex) {
							Log.e(TAG, "Could not save sync object", ex);
						}
					}
				}
			}
		}
		
		Log.i(TAG, "DB Parsed " + count + " records in " + (System.currentTimeMillis() - start) + " ms");
		
		save();
	}
	
	/**
	 * Parse an object based on its class and queues it up to be saved
	 * in the database.
	 * 
	 * @param json
	 * @param type
	 * @param delete
	 */
	private static void parseAndSave(JSONObject json, int type, boolean delete) {
		
		switch (type) {
			case 0:
				Tag.saveTag(json, delete);
				break;
			case 1:
				Category.saveCategory(json, delete);
				break;
			case 2:
				Bank.saveIncomingBank(json, delete);
				break;
			case 3:
				BankAccount.saveBankAccount(json, delete);
				break;
			case 4:
				Transactions.saveTransaction(json, delete);
				break;
			case 5:
				BudgetItem.saveBudgetItem(json, delete);
				break;
		}
	}
	
	/**
	 * Query the database and return all objects of the given class 
	 * with the given data state.
	 * 
	 * @param key
	 * @param dataState
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> getChangedData(Class<?> key, DataState dataState) {
		
		try {
		
			AbstractDao<Object, Long> dao = (AbstractDao<Object, Long>) getDao(key);
			
			return dao.queryRaw(Constant.QUERY_BUSINESS_BASE_JOIN, Integer.toString(dataState.index()));
		
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Removes all shared preferences and resets the database
	 */
	public static void deleteAllLocalData() {
		
		Preferences.remove(Preferences.KEY_LAST_INSTITUTION_SYNC);
		Preferences.remove(Preferences.KEY_LAST_SYNC);
		
		DatabaseDefaults.resetDatabase();
	}
}
