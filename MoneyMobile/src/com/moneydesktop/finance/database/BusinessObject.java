package com.moneydesktop.finance.database;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

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
	
	protected DataState mDataStateEnum;
	protected DataState mPreviousDataStateEnum;
	protected boolean mIsNew = false;
	protected boolean mSyncImmediately = false;
	protected boolean mSyncSuspended = false;
	protected boolean mIgnoreWillSave = false;
	protected boolean mSuspendChangeTracking = false;
	protected boolean mIsDeleted = false;
	
	/********************************************************************************
	 * Property Access Methods
	 ********************************************************************************/
	
	public DataState getDataStateEnum() {
		
		if (mDataStateEnum == null && getBusinessObjectBase() != null) {
			mDataStateEnum = DataState.fromInteger(getBusinessObjectBase().getDataState());
		}
		
		return mDataStateEnum;
	}

	public void setDataStateEnum(DataState dataStateEnum) {
		
		this.mDataStateEnum = dataStateEnum;
		
		getBusinessObjectBase().setDataState(dataStateEnum.index());
	}

	public boolean isNew() {
		return mIsNew;
	}

	public void setNew(boolean isNew) {
		this.mIsNew = isNew;
	}

	public void setIgnoreWillSave(boolean ignoreWillSave) {
		this.mIgnoreWillSave = ignoreWillSave;
	}
	
	public boolean isDeleted() {
		return mIsDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.mIsDeleted = isDeleted;
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
		
		mPreviousDataStateEnum = getDataStateEnum();
		
		try {
			
			for (TagInstance ti : getBusinessObjectBase().getTagInstances())
				ti.acceptChanges();
			
		} catch (DaoException ex) {}
		
		mSyncImmediately = false;
		
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
		
		if (mIgnoreWillSave)
			return;

		this.mIsDeleted = isDeleted;
		
		if (isDeleted) {
			// TODO: notification of deleted object
		}
		
		if (!mSuspendChangeTracking) {
			
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
		mSuspendChangeTracking = true;
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
		
		return insertBatch(null);
	}
	
	public BusinessObject insertBatch(String guid) {
		
		BusinessObjectBase bob = addBusinessObjectBase();
		
		if (guid == null)
			guid = getExternalId();
		
		if (bob != null)
			bob.insertBatch(guid);
		
		DataController.insert(this, guid);
		
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
    		object.insertBatch(guid);
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
