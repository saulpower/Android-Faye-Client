package com.moneydesktop.finance.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;

import de.greenrobot.dao.AbstractDao;

public class DataController {
	
	public enum TxType {
		INSERT, UPDATE, DELETE
	}
	
	private static final String TAG = "DatabaseDefaults";

	private static Map<Class<?>, List<Object>> pendingInsertTx = new HashMap<Class<?>, List<Object>>();
	private static Map<Class<?>, List<Object>> pendingUpdateTx = new HashMap<Class<?>, List<Object>>();
	private static Map<Class<?>, List<Object>> pendingDeleteTx = new HashMap<Class<?>, List<Object>>();
	
	private static Map<String, Object> pendingCache = new HashMap<String, Object>();
	
	/**
	 * Saves all the pending transactions in a batch fashion
	 */
	public static void save() {
		
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
			BusinessObjectBase.class,
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
			TagInstance.class,
			Transaction.class
		};
		
		for (Class<?> key : objectOrder) {
			
		    List<Object> entities = pendingTx.get(key);
		    
		    if (entities != null && entities.size() > 0) {

			    long start = System.currentTimeMillis();
			    
			    AbstractDao<Object, Long> dao = (AbstractDao<Object, Long>) getDao(key);
			    
			    String operation = "Inserted";
			    
			    switch (type) {
			    case INSERT:
			    	dao.insertInTx(entities);
			    	break;
				case UPDATE:
					operation = "Updated";
			    	dao.updateInTx(entities);
					break;
				case DELETE:
					operation = "Deleted";
			    	dao.deleteInTx(entities);
					break;
			    }
			    
				Log.i(TAG, operation + " " + entities.size() + " (" + key + ") in " + (System.currentTimeMillis() - start) + " ms");
		    }
		}
	}
	
	/**
	 * Returns the appropriate Dao object for the given class
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
		if (key.equals(Transaction.class))
			return session.getTransactionDao();
		
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
	private static void addTransaction(Object object, TxType type) {
		
		Class<?> key = object.getClass();
		
		if (!object.getClass().equals(BusinessObjectBase.class)) {

			pendingCache.put(((BusinessObjectInterface) object).getBusinessObjectBase().getExternalId(), object);
		}
		
		List<Object> list = null;
		
		switch (type) {
		case INSERT:
			list = getList(key, pendingInsertTx);
			list.add(object);
			break;
		case UPDATE:
			list = getList(key, pendingUpdateTx);
			list.add(object);
			break;
		case DELETE:
			list = getList(key, pendingDeleteTx);
			list.add(object);
			break;
		default:
			break;
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
	
	public static Object getFromCache(String id) {
		
		return pendingCache.get(id);
	}
	
	/**
	 * Create default Category Types
	 */
	public static void ensureCategoryTypesLoaded() {
		
		CategoryTypeDao ctDao = ApplicationContext.getDaoSession().getCategoryTypeDao();
		List<CategoryType> categoryTypes = ctDao.queryBuilder().list();
		
		if (categoryTypes.size() == 0) {
			
			CategoryType.createCategoryType("2", ApplicationContext.getContext().getString(R.string.ct_exp)).insert();
			CategoryType.createCategoryType("1", ApplicationContext.getContext().getString(R.string.ct_inc)).insert();
			CategoryType.createCategoryType("4", ApplicationContext.getContext().getString(R.string.ct_asst)).insert();
			CategoryType.createCategoryType("8", ApplicationContext.getContext().getString(R.string.ct_liab)).insert();
			CategoryType.createCategoryType("16", ApplicationContext.getContext().getString(R.string.ct_equity)).insert();
			CategoryType.createCategoryType("64", ApplicationContext.getContext().getString(R.string.ct_flow)).insert();
			CategoryType.createCategoryType("128", ApplicationContext.getContext().getString(R.string.ct_bal)).insert();
			CategoryType.createCategoryType("32", ApplicationContext.getContext().getString(R.string.ct_stat)).insert();
		}
	}

	/**
	 * Create default Account Types
	 */
	public static void ensureAccountTypesLoaded() {
		
		AccountTypeDao atDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		List<AccountType> accountTypes = atDao.queryBuilder().list();
		
		if (accountTypes.size() == 0) {
			
			AccountType.createAccountType("0", ApplicationContext.getContext().getString(R.string.at_unknown), 1, 0, null).insert();
			AccountType.createAccountType("1", ApplicationContext.getContext().getString(R.string.at_checking), 1, 0, null).insert();
			AccountType.createAccountType("2", ApplicationContext.getContext().getString(R.string.at_saving), 1, 0, null).insert();
			AccountType.createAccountType("3", ApplicationContext.getContext().getString(R.string.at_loans), 1, 1, null).insert();
			AccountType.createAccountType("4", ApplicationContext.getContext().getString(R.string.at_cc), 1, 1, null).insert();
			AccountType.createAccountType("5", ApplicationContext.getContext().getString(R.string.at_inv), 1, 0, null).insert();
			AccountType.createAccountType("6", ApplicationContext.getContext().getString(R.string.at_loc), 1, 1, null).insert();
			AccountType.createAccountType("7", ApplicationContext.getContext().getString(R.string.at_mort), 1, 1, null).insert();
			AccountType.createAccountType("8", ApplicationContext.getContext().getString(R.string.at_prop), 1, 0, null).insert();
			AccountType.createAccountType("8.2", ApplicationContext.getContext().getString(R.string.at_art), 1, 0, "8").insert();
			AccountType.createAccountType("8.0", ApplicationContext.getContext().getString(R.string.at_re), 1, 0, "8").insert();
			AccountType.createAccountType("8.1", ApplicationContext.getContext().getString(R.string.at_veh), 1, 0, "8").insert();
			AccountType.createAccountType("8.3", ApplicationContext.getContext().getString(R.string.at_jew), 1, 0, "8").insert();
			AccountType.createAccountType("8.4", ApplicationContext.getContext().getString(R.string.at_fur), 1, 0, "8").insert();
			AccountType.createAccountType("8.5", ApplicationContext.getContext().getString(R.string.at_app), 1, 0, "8").insert();
			AccountType.createAccountType("8.6", ApplicationContext.getContext().getString(R.string.at_comp), 1, 0, "8").insert();
			AccountType.createAccountType("8.7", ApplicationContext.getContext().getString(R.string.at_elec), 1, 0, "8").insert();
			AccountType.createAccountType("8.8", ApplicationContext.getContext().getString(R.string.at_se), 1, 0, "8").insert();
			AccountType.createAccountType("8.9", ApplicationContext.getContext().getString(R.string.at_misc), 1, 0, "8").insert();
			AccountType.createAccountType("9", ApplicationContext.getContext().getString(R.string.at_cash), 1, 0, null).insert();
		}
	}
	
	/**
	 * Create default Account Type Groups
	 */
	public static void ensureAccountTypeGroupsLoaded() {
		
		AccountTypeGroupDao atgDao = ApplicationContext.getDaoSession().getAccountTypeGroupDao();
		List<AccountTypeGroup> accountTypeGroups = atgDao.queryBuilder().list();
		
		if (accountTypeGroups.size() == 0) {
			
			AccountTypeGroup.createAccountTypeGroup("INVST", ApplicationContext.getContext().getString(R.string.atg_inv), "brokerage.png", 99).insert();
			AccountTypeGroup.createAccountTypeGroup("PROP", ApplicationContext.getContext().getString(R.string.atg_prop), "house.png", 3).insert();
			AccountTypeGroup.createAccountTypeGroup("SPEND", ApplicationContext.getContext().getString(R.string.atg_cash), "cash.png", 0).insert();
			AccountTypeGroup.createAccountTypeGroup("DEBT", ApplicationContext.getContext().getString(R.string.atg_ccd), "cc.png", 1).insert();
			AccountTypeGroup.createAccountTypeGroup("ODEBT", ApplicationContext.getContext().getString(R.string.atg_od), "cash.png", 2).insert();
		}
	}
	
	public static void saveSyncData(JSONObject device, boolean fullSyncRequired) throws JSONException {
		
		String[] operationOrder = new String[] {
				Constant.KEY_CREATED, 
				Constant.KEY_UPDATED, 
				Constant.KEY_DELETED
			};
		
		String[] objectOrder = new String[] {
				Constant.TAGS, 
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
	
	private static void parseAndSave(JSONObject json, int type, boolean delete) {
		
		switch (type) {
			case 0:
				Tag.saveTag(json, delete);
				break;
			case 1:
				Category.saveCategory(json, delete);
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			case 5:
				break;
		}
	}
}
