package com.moneydesktop.finance.database;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.CategoryDao.Properties;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Query;

public abstract class ObjectBase implements BusinessObjectInterface {
	
	public static final String TAG = "ObjectBase";

	private static Map<Class<?>, Query<?>> queries = new HashMap<Class<?>, Query<?>>();
    
	protected static Object saveObject(JSONObject json, Class<?> key, boolean delete) {

		boolean inserting = false;
    	
    	String guid = json.optString(Constant.KEY_GUID);
    	ObjectBase object = (ObjectBase) getObject(key, guid);
    	
    	// Object does not exist return null, no action required
    	if (object == null && delete)
    		return null;
    	
    	// Object exists, delete it
    	else if (object != null && delete) {
    		
    		object.getBusinessObjectBase().delete();
    		object.delete();
    		
    		return null;
    	}
    	
    	if (object == null) {
    		
    		inserting = true;
    		
    		object = new Category(Long.valueOf(guid.hashCode()));
    		object.setExternalId(guid);
    	}
    	
    	if (inserting)
    		object.insert();
    	else
    		object.update();
    	
    	return object;
	}
	
    protected static Object getObject(Class<?> key, String id) {
    	
    	
    	Object object = null;
    	
    	if (id == null || id.equals(""))
    		return object;
    	
    	object = getQuery(key, Long.valueOf(id.hashCode())).unique();
    	
    	if (object == null)
    		object = DataController.getFromCache(id);
    	
    	return object;
    }

	@SuppressWarnings("unchecked")
    private static Query<?> getQuery(Class<?> key, Long id) {
    		
		Query<?> queryId = queries.get(key);
		
    	if (queryId == null) {

    		AbstractDao<Object, Long> abDao = (AbstractDao<Object, Long>) DataController.getDao(key);
    		queryId = abDao.queryBuilder().where(Properties.Id.eq(id)).build();
    		queries.put(key, queryId);
    		
    	} else {
    		
    		queryId.setParameter(0, id);
    	}
    	
    	return queryId;
    }
	
	protected void insert() {
		addBusinessObjectBase();
		DataController.insert(this);
	}
	
	protected void update() {
		DataController.update(this);
		DataController.update(getBusinessObjectBase());
	}
	
	protected void delete() {
		DataController.delete(getBusinessObjectBase());
		DataController.delete(this);
	}
	
	private void addBusinessObjectBase() {

		BusinessObjectBase bob = new BusinessObjectBase(Long.valueOf(hashCode()));
		bob.setExternalId(getExternalId());
		setBusinessObjectBase(bob);
		
		DataController.insert(bob);
	}
}
