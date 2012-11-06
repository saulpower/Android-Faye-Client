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
	
	public static void save() {
		
		processTx(pendingInsertTx, TxType.INSERT);
		processTx(pendingUpdateTx, TxType.UPDATE);
		processTx(pendingDeleteTx, TxType.DELETE);
		
		// Reset pending transaction list
		pendingInsertTx = new HashMap<Class<?>, List<Object>>();
		pendingUpdateTx = new HashMap<Class<?>, List<Object>>();
		pendingDeleteTx = new HashMap<Class<?>, List<Object>>();
		pendingCache = new HashMap<String, Object>();
	}
	
	@SuppressWarnings("unchecked")
	private static void processTx(Map<Class<?>, List<Object>> pendingTx, TxType type) {
		
		Class<?>[] objectOrder = new Class<?>[] {
			BusinessObjectBase.class,
			CategoryType.class,
			Category.class,
			Tag.class
		};
		
		for (Class<?> key : objectOrder) {
			
		    List<Object> entities = pendingTx.get(key);
		    
		    if (entities != null && entities.size() > 0) {
		    	
			    AbstractDao<Object, Long> dao = (AbstractDao<Object, Long>) getDao(key);
			    
			    String operation = "Inserted";
			    long start = System.currentTimeMillis();
			    
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
	
	private static AbstractDao<?, Long> getDao(Class<?> key) {
		
		DaoSession session = ApplicationContext.getDaoSession();
		
		// TODO: Update for all DAOs
		
		if (key.equals(BusinessObjectBase.class))
			return session.getBusinessObjectBaseDao();
		if (key.equals(Tag.class))
			return session.getTagDao();
		if (key.equals(Category.class))
			return session.getCategoryDao();
		if (key.equals(CategoryType.class))
			return session.getCategoryTypeDao();
		
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
	 * Batch adding of BusinessObjectBase entities into the database and assigning them to the
	 * passed in list of entities.
	 * 
	 * @param entities A list of entities all of which inherit the interface BusinessObjectInterface
	 */
	public static void addBusinessObjectBase(List<?> entities) {
		
		List<BusinessObjectBase> businessObjects = new ArrayList<BusinessObjectBase>();
		BusinessObjectBaseDao bobDao = ApplicationContext.getDaoSession().getBusinessObjectBaseDao();
		
		for (int i = 0; i < entities.size(); i++) {
			
			businessObjects.add(new BusinessObjectBase());
		}
		
		bobDao.insertInTx(businessObjects);
		
		for (int i = 0; i < entities.size(); i++) {
			
			try {
				
				((BusinessObjectInterface) entities.get(i)).setBusinessObjectBase(businessObjects.get(i));
				
			} catch (ClassCastException ex) {
				
				Log.e(TAG, "Could not add business object to entity.", ex);
				
				bobDao.delete(businessObjects.get(i));
			}
		}
	}
	
	/**
	 * Add a single BusinessObjectBase entity into the database and assigning it to the
	 * passed in entity.
	 * 
	 * @param entity The entity that needs a BusinessObjectBase
	 */
	public static void addBusinessObjectBase(BusinessObjectInterface entity) {
		
		BusinessObjectBaseDao bobDao = ApplicationContext.getDaoSession().getBusinessObjectBaseDao();
		
		BusinessObjectBase businessObjectBase = new BusinessObjectBase();
		bobDao.insert(businessObjectBase);
		
		entity.setBusinessObjectBase(businessObjectBase);
	}
	
	/**
	 * Create default Category Types
	 */
	public static void ensureCategoryTypesLoaded() {
		
		CategoryTypeDao ctDao = ApplicationContext.getDaoSession().getCategoryTypeDao();
		List<CategoryType> categoryTypes = ctDao.queryBuilder().list();
		
		if (categoryTypes.size() == 0) {
			
			categoryTypes.add(CategoryType.createCategoryType("2", ApplicationContext.getContext().getString(R.string.ct_exp)));
			categoryTypes.add(CategoryType.createCategoryType("1", ApplicationContext.getContext().getString(R.string.ct_inc)));
			categoryTypes.add(CategoryType.createCategoryType("4", ApplicationContext.getContext().getString(R.string.ct_asst)));
			categoryTypes.add(CategoryType.createCategoryType("8", ApplicationContext.getContext().getString(R.string.ct_liab)));
			categoryTypes.add(CategoryType.createCategoryType("16", ApplicationContext.getContext().getString(R.string.ct_equity)));
			categoryTypes.add(CategoryType.createCategoryType("64", ApplicationContext.getContext().getString(R.string.ct_flow)));
			categoryTypes.add(CategoryType.createCategoryType("128", ApplicationContext.getContext().getString(R.string.ct_bal)));
			categoryTypes.add(CategoryType.createCategoryType("32", ApplicationContext.getContext().getString(R.string.ct_stat)));
			
			addBusinessObjectBase(categoryTypes);
			
			ctDao.insertInTx(categoryTypes);
		}
	}

	/**
	 * Create default Account Types
	 */
	public static void ensureAccountTypesLoaded() {
		
		AccountTypeDao atDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		List<AccountType> accountTypes = atDao.queryBuilder().list();
		
		if (accountTypes.size() == 0) {
			
			accountTypes.add(AccountType.createAccountType("0", ApplicationContext.getContext().getString(R.string.at_unknown), 1, 0, null));
			accountTypes.add(AccountType.createAccountType("1", ApplicationContext.getContext().getString(R.string.at_checking), 1, 0, null));
			accountTypes.add(AccountType.createAccountType("2", ApplicationContext.getContext().getString(R.string.at_saving), 1, 0, null));
			accountTypes.add(AccountType.createAccountType("3", ApplicationContext.getContext().getString(R.string.at_loans), 1, 1, null));
			accountTypes.add(AccountType.createAccountType("4", ApplicationContext.getContext().getString(R.string.at_cc), 1, 1, null));
			accountTypes.add(AccountType.createAccountType("5", ApplicationContext.getContext().getString(R.string.at_inv), 1, 0, null));
			accountTypes.add(AccountType.createAccountType("6", ApplicationContext.getContext().getString(R.string.at_loc), 1, 1, null));
			accountTypes.add(AccountType.createAccountType("7", ApplicationContext.getContext().getString(R.string.at_mort), 1, 1, null));
			accountTypes.add(AccountType.createAccountType("8", ApplicationContext.getContext().getString(R.string.at_prop), 1, 0, null));
			accountTypes.add(AccountType.createAccountType("8.2", ApplicationContext.getContext().getString(R.string.at_art), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.0", ApplicationContext.getContext().getString(R.string.at_re), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.1", ApplicationContext.getContext().getString(R.string.at_veh), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.3", ApplicationContext.getContext().getString(R.string.at_jew), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.4", ApplicationContext.getContext().getString(R.string.at_fur), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.5", ApplicationContext.getContext().getString(R.string.at_app), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.6", ApplicationContext.getContext().getString(R.string.at_comp), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.7", ApplicationContext.getContext().getString(R.string.at_elec), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.8", ApplicationContext.getContext().getString(R.string.at_se), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("8.9", ApplicationContext.getContext().getString(R.string.at_misc), 1, 0, "8"));
			accountTypes.add(AccountType.createAccountType("9", ApplicationContext.getContext().getString(R.string.at_cash), 1, 0, null));
			
			addBusinessObjectBase(accountTypes);
			
			atDao.insertInTx(accountTypes);
		}
	}
	
	/**
	 * Create default Account Type Groups
	 */
	public static void ensureAccountTypeGroupsLoaded() {
		
		AccountTypeGroupDao atgDao = ApplicationContext.getDaoSession().getAccountTypeGroupDao();
		List<AccountTypeGroup> accountTypeGroups = atgDao.queryBuilder().list();
		
		if (accountTypeGroups.size() == 0) {
			
			accountTypeGroups.add(AccountTypeGroup.createAccountTypeGroup("INVST", ApplicationContext.getContext().getString(R.string.atg_inv), "brokerage.png", 99));
			accountTypeGroups.add(AccountTypeGroup.createAccountTypeGroup("PROP", ApplicationContext.getContext().getString(R.string.atg_prop), "house.png", 3));
			accountTypeGroups.add(AccountTypeGroup.createAccountTypeGroup("SPEND", ApplicationContext.getContext().getString(R.string.atg_cash), "cash.png", 0));
			accountTypeGroups.add(AccountTypeGroup.createAccountTypeGroup("DEBT", ApplicationContext.getContext().getString(R.string.atg_ccd), "cc.png", 1));
			accountTypeGroups.add(AccountTypeGroup.createAccountTypeGroup("ODEBT", ApplicationContext.getContext().getString(R.string.atg_od), "cash.png", 2));
			
			addBusinessObjectBase(accountTypeGroups);
			
			atgDao.insertInTx(accountTypeGroups);
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
		
		// TODO: Is the sorting necessary here since we did it in the SyncEngine?
		if (fullSyncRequired) {
			
			// Sort Created -> Categories by parent_guid
			
			// Sort Created -> Transactions by parent_guid
			
			// Sort Updated -> Transactions by parent_guid
		}
		
		for (String operation : operationOrder) {
			
			boolean delete = operation.equals(Constant.KEY_DELETED);
			JSONObject operationObj = records.getJSONObject(operation);
			
			for (int type = 0; type < objectOrder.length; type++) {
				
				long start = System.currentTimeMillis();
				
				String objectType = objectOrder[type];
				JSONArray objects = operationObj.optJSONArray(objectType);
				
				if (objects != null) {
					
					for (int i = 0; i < objects.length(); i++) {
						
						JSONObject data = objects.getJSONObject(i);
						
						// parse based on object type
						parseAndSave(data, type, delete);
					}
				}
				
				Log.i(TAG, operation + " - " + objectType + " parse/save took " + (System.currentTimeMillis() - start) + " ms");
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
