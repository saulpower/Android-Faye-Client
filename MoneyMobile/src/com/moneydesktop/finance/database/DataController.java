package com.moneydesktop.finance.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.util.Enums.TxType;

import de.greenrobot.dao.AbstractDao;

public class DataController {
	
	public static final String TAG = "DatabaseDefaults";

	private static Map<Class<?>, List<Object>> pendingInsertTx = new HashMap<Class<?>, List<Object>>();
	private static Map<Class<?>, List<Object>> pendingUpdateTx = new HashMap<Class<?>, List<Object>>();
	private static Map<Class<?>, List<Object>> pendingDeleteTx = new HashMap<Class<?>, List<Object>>();
	
	private static Map<String, Object> pendingCache = new HashMap<String, Object>();
	
	/**
	 * Saves all the pending transactions in a batch fashion
	 */
	public static synchronized void save() {
			
		processTx(pendingInsertTx, TxType.INSERT);
		processTx(pendingUpdateTx, TxType.UPDATE);
		processTx(pendingDeleteTx, TxType.DELETE);
		
		// Reset pending transaction list
		pendingInsertTx.clear();
		pendingUpdateTx.clear();
		pendingDeleteTx.clear();
		pendingCache.clear();
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
	private static void processTx(Map<Class<?>, List<Object>> pendingTx, TxType type) {
		
		if (pendingTx.keySet().size() == 0)
			return;
		
		Class<?>[] objectOrder = new Class<?>[] {
			AccountType.class,
			AccountTypeGroup.class,
			Bank.class,
			BankAccount.class,
			BankAccountBalance.class,
			Category.class,
			CategoryType.class,
			Institution.class,
			Location.class,
			Tag.class,
			Transactions.class,
			TagInstance.class,
			BusinessObjectBase.class
		};
		
		for (Class<?> key : objectOrder) {
			
		    List<Object> entities = pendingTx.get(key);
		    
		    if (entities != null && entities.size() > 0) {
			    
			    AbstractDao<Object, Long> dao = (AbstractDao<Object, Long>) getDao(key);
			    
			    for (Object object : entities)
			    	((BusinessObject) object).willSave(type == TxType.DELETE);
			    
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
	
	public static void insert(Object object) {
		
		addTransaction(object, TxType.INSERT);
	}
	
	public static void update(Object object) {
		
		addTransaction(object, TxType.UPDATE);
	}
	
	public static void delete(Object object) {
		
		addTransaction(object, TxType.DELETE);
	}
	
	/**
	 * Adds a transaction to the appropriate pending transaction map.  We also
	 * add the passed-in object to a cache in case it needs to be referenced
	 * before it has been inserted into the database.
	 * 
	 * @param object
	 * @param type
	 */
	private static synchronized void addTransaction(Object object, TxType type) {
		
		if (object == null)
			return;
		
		Class<?> key = object.getClass();
		
		if (!object.getClass().equals(BusinessObjectBase.class)) {

			pendingCache.put((key.getName() + ((BusinessObject) object).getBusinessObjectBase().getExternalId()), object);
		}
		
		List<Object> list = null;
		Map<Class<?>, List<Object>> pendingTx = null;
		
		switch (type) {
		case INSERT:
			pendingTx = pendingInsertTx;
			break;
		case UPDATE:
			pendingTx = pendingUpdateTx;
			break;
		case DELETE:
			pendingTx = pendingDeleteTx;
			break;
		}
		
		if (pendingTx != null) {
			
			list = getList(key, pendingTx);
			
			if (!list.contains(object))
				list.add(object);
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
		
		return pendingCache.get(id);
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
		List<Object> entities = (List<Object>) dao.queryBuilder().list();
		
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
		
		String[] operationOrder = new String[] {
				Constant.KEY_CREATED, 
				Constant.KEY_UPDATED, 
				Constant.KEY_DELETED
			};
		
		String[] objectOrder = new String[] {
				Constant.KEY_TAGS, 
				Constant.CATEGORIES, 
				Constant.MEMBERS, 
				Constant.ACCOUNTS, 
				Constant.TRANSACTIONS, 
				Constant.BUDGETS 
			};
		
		JSONObject records = device.getJSONObject(Constant.KEY_RECORDS);
		
		for (String operation : operationOrder) {
			
			boolean delete = operation.equals(Constant.KEY_DELETED);
			JSONObject operationObj = records.getJSONObject(operation);
			
			for (int type = 0; type < objectOrder.length; type++) {
				
				String objectType = objectOrder[type];
				JSONArray objects = operationObj.optJSONArray(objectType);
				
				if (objects != null) {
					
					for (int i = 0; i < objects.length(); i++) {
						
						JSONObject data = objects.getJSONObject(i);
						
						// parse based on object type
						parseAndSave(data, type, delete);
					}
				}
			}
		}
		
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
				Bank.saveBank(json, delete);
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
}
