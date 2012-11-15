package com.moneydesktop.finance.database;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.database.CategoryDao.Properties;
import com.moneydesktop.finance.util.Enums.DataState;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;
import de.greenrobot.dao.Query;

// TODO: Track when an object is changed and mark it's DataState so it is processed
// 		 during a sync.

public abstract class BusinessObject implements BusinessObjectInterface {
	
	public static final String TAG = "BusinessObject";

	private static Map<Class<?>, Query<?>> queries = new HashMap<Class<?>, Query<?>>();
	
	protected DataState dataStateEnum;
	protected DataState previousDataStateEnum;
	protected boolean isNew = false;
	protected boolean syncImmediately = false;
	protected boolean syncSuspended = false;
	protected boolean ignoreWillSave = false;
	protected boolean suspendChangeTracking = false;
	protected boolean isDeleted = false;
	
	/********************************************************************************
	 * Property Access Methods
	 ********************************************************************************/
	
	public DataState getDataStateEnum() {
		
		if (dataStateEnum == null && getBusinessObjectBase() != null) {
			dataStateEnum = DataState.fromInteger(getBusinessObjectBase().getDataState());
		}
		
		return dataStateEnum;
	}

	public void setDataStateEnum(DataState dataStateEnum) {
		
		this.dataStateEnum = dataStateEnum;
		
		getBusinessObjectBase().setDataState(dataStateEnum.index());
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public void setIgnoreWillSave(boolean ignoreWillSave) {
		this.ignoreWillSave = ignoreWillSave;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	/********************************************************************************
	 * Methods
	 ********************************************************************************/

	/**
	 * Called to update the DataState of the object
	 */
	public void acceptChanges() {
		
		if (this instanceof BusinessObjectBase)
			return;
		
		previousDataStateEnum = getDataStateEnum();
		
		try {
			
			for (TagInstance ti : getBusinessObjectBase().getTagInstances())
				ti.acceptChanges();
			
		} catch (DaoException ex) {}
		
		syncImmediately = false;
		
		if (getDataStateEnum() != DataState.DATA_STATE_UNCHANGED) {
			setDataStateEnum(DataState.DATA_STATE_UNCHANGED);
		}
	}
	
	/**
	 * Called before the object will be saved in the database
	 * 
	 * @param isDeleted
	 */
	public void willSave(boolean isDeleted) {
		
		if (ignoreWillSave)
			return;

		this.isDeleted = isDeleted;
		
		if (isDeleted) {
			// TODO: notification of deleted object
		}
		
		if (!suspendChangeTracking) {
			
			if (getBusinessObjectBase() != null)
				getBusinessObjectBase().setDateModified(new Date());
			
			if (getDataStateEnum() != DataState.DATA_STATE_UNCHANGED) {
				// TODO: notification of data state change
			}
		}
	}
	
	public void updateDataState() {
		
		if (getDataStateEnum() == DataState.DATA_STATE_UNCHANGED)
			setDataStateEnum(DataState.DATA_STATE_MODIFIED);
	}
	
	public void suspendDataState() {
		suspendChangeTracking = true;
	}
	
	public boolean containsTag(String tagId) {
		
		try {
			
			for (TagInstance ti : getBusinessObjectBase().getTagInstances()) {
				
				if (ti.getTag().getTagId().equals(tagId))
					return true;
			}
			
		} catch (DaoException ex) {}
		
		return false;
	}
	
	public BusinessObject insertBatch() {
		
		BusinessObjectBase bob = addBusinessObjectBase();
		
		if (bob != null)
			bob.insertBatch();
		
		DataController.insert(this);
		
		return this;
	}
	
	public void updateBatch() {
		DataController.update(this);
		DataController.update(getBusinessObjectBase());
	}
	
	public void deleteBatch() {

		DataController.delete(getBusinessObjectBase());
		DataController.delete(this);
	}
	
	/**
	 * Adds a BusinessObjectBase object to the current object
	 */
	protected BusinessObjectBase addBusinessObjectBase() {

		if (this instanceof BusinessObjectBase)
			return null;
		
		BusinessObjectBase bob = new BusinessObjectBase(BusinessObjectBase.nextId());
		setBusinessObjectBase(bob);
		bob.setExternalId(getExternalId());
		bob.setDataState(DataState.DATA_STATE_NEW.index());
		bob.setFlags(0);
		bob.setVersion(0);
		
		return bob;
	}
	
	/********************************************************************************
	 * STATIC METHODS 
	 ********************************************************************************/
    
	/**
	 * Save an object into the database, whether that be inserting, updating,
	 * or deleting the object.  The resulting object is returned.
	 * 
	 * @param json
	 * @param key
	 * @param delete
	 * @return
	 */
	protected static Object saveObject(JSONObject json, Class<?> key, boolean delete) {

		boolean inserting = false;
    	
    	String guid = json.optString(Constant.KEY_GUID);
    	BusinessObject object = (BusinessObject) getObject(key, guid);
    	
    	// Object does not exist return null, no action required
    	if (object == null && delete)
    		return null;
    	
    	// Object exists, delete it
    	else if (object != null && delete) {
    		
    		object.getBusinessObjectBase().deleteBatch();
    		object.deleteBatch();
    		
    		return null;
    	}
    	
    	if (object == null) {
    		
    		inserting = true;
    		object = DatabaseObjectFactory.createInstance(key, guid);
    		object.insertBatch();
    		object.setExternalId(guid);
    	}
    	
    	object.setNew(inserting);
    	
    	if (!inserting)
    		object.updateBatch();
    	
    	return object;
	}
	
	/**
	 * Returns an object from the database for the given class and GUID
	 * 
	 * @param key
	 * @param id
	 * @return
	 */
    public static Object getObject(Class<?> key, String id) {

    	return getObject(key, id, Long.valueOf(id.hashCode()));
    }
    
    /**
	 * Returns an object from the database for the given class and GUID
	 * 
	 * @param key
	 * @param id
	 * @return
	 */
    public static Object getObject(Class<?> key, String guid, Long id) {
    	
    	Object object = null;
    	
    	if (id == null || id.equals(""))
    		return object;
    	
    	object = DataController.checkCache((key.getName() + guid));

        if (object == null)
        	object = getQuery(key, id).unique();

        if (object == null)
        	Log.i(TAG, "Could not get " + key.getCanonicalName() + " - " + guid);
        
    	return object;
    }

    /**
     * Builds a query for lookup by GUID for the given class. The query is 
     * cached for future use.
     * 
     * @param key
     * @param id
     * @return
     */
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
}
