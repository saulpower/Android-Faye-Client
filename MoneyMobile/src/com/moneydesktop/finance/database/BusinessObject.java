package com.moneydesktop.finance.database;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.database.CategoryDao.Properties;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.Query;

public abstract class BusinessObject implements BusinessObjectInterface {
	
	public static final String TAG = "BusinessObject";

	private static Map<Class<?>, Query<?>> sQueries = new HashMap<Class<?>, Query<?>>();
	
	protected DataState mDataStateEnum;
	protected DataState mPreviousDataStateEnum;
	protected boolean mIsNew = false;
	protected boolean mSyncImmediately = false;
	protected boolean mSyncSuspended = false;
	protected boolean mIgnoreWillSave = false;
	protected boolean mSuspendChangeTracking = false;
	protected boolean mIsDeleted = false;
	protected boolean mChangesAccepted = false;
	
	/********************************************************************************
	 * Property Access Methods
	 ********************************************************************************/
	
	public DataState getDataStateEnum() {
		
	    try {
	    	if (getBusinessObjectBase() != null) {
    			mDataStateEnum = DataState.fromInteger(getBusinessObjectBase().getDataState());
    		} else if (this instanceof BusinessObjectBase) {
	    		mDataStateEnum = DataState.fromInteger(((BusinessObjectBase) this).getDataState());
	    	}
	    } catch (DaoException ex) {
	    	mDataStateEnum = DataState.DATA_STATE_UNCHANGED;
	    }
		
		return mDataStateEnum;
	}

	public void setDataStateEnum(DataState dataStateEnum) {
		
		this.mDataStateEnum = dataStateEnum;

	    try {
			if (getBusinessObjectBase() != null) {
			    getBusinessObjectBase().setDataState(dataStateEnum.index());
			} else if (this instanceof BusinessObjectBase) {
				((BusinessObjectBase) this).setDataState(dataStateEnum.index());
			}
	    } catch (DaoException ex) {}
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
        setDataStateEnum(DataState.DATA_STATE_DELETED);
	}
	
	/********************************************************************************
	 * Methods
	 ********************************************************************************/

	/**
	 * Called to update the DataState of the object
	 */
	public void acceptChanges() {
		
		if (this instanceof BusinessObjectBase) {
			return;
		}
		
		mChangesAccepted = true;
		mPreviousDataStateEnum = getDataStateEnum();
		
		try {
			
			if (!(this instanceof TagInstance)) {
				
				for (TagInstance ti : getBusinessObjectBase().getTagInstances()) {
					
					if (ti.getDataStateEnum() == DataState.DATA_STATE_DELETED) {
						ti.deleteBatch();
					}
				}
				
				getBusinessObjectBase().resetTagInstances();
			}
			
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

        mChangesAccepted = false;
        
		if (mIgnoreWillSave) {
			return;
		}
		
		this.mIsDeleted = isDeleted;
		
		if (isDeleted) {
			// TODO: notification of deleted object
		}
		
		if (!mSuspendChangeTracking) {
			
		    try {
    			if (getBusinessObjectBase() != null) {
    				getBusinessObjectBase().setDateModified(new Date());
    			}
		    } catch (DaoException ex) {
		        // catching and doing nothing
		    }
			
			if (getDataStateEnum() != DataState.DATA_STATE_UNCHANGED) {
				// TODO: notification of data state change
			}
		}
	}
	
	public void setModified() {
		
		if (getDataStateEnum() == DataState.DATA_STATE_UNCHANGED && !mChangesAccepted) {
			setDataStateEnum(DataState.DATA_STATE_MODIFIED);
		}
	}
	
	public void suspendDataState() {
		mSuspendChangeTracking = true;
	}
	
	public void save() {
	    DataController.save();
	}
	
	public BusinessObject insertBatch() {
		
		return insertBatch(null);
	}
	
	public BusinessObject insertSingle() {
	    
	    BusinessObject bo = insertBatch(null);
	    DataController.save();
	    
	    return bo;
	}
	
	public BusinessObject insertBatch(String guid) {
		
		BusinessObjectBase bob = addBusinessObjectBase();
		
	    try {
    		if (guid == null) {
    			guid = getExternalId();
    		}
	    } catch (DaoException ex) {}
		
		if (bob != null) {
			bob.insertBatch(guid);
		}
		
		DataController.insert(this, guid);
		
		return this;
	}
	
	public void updateBatch() {
	    
	    setModified();
		DataController.update(this);
		DataController.update(getBusinessObjectBase());
	}
	
	public void updateSingle() {

	    setModified();
        DataController.update(this);
        DataController.update(getBusinessObjectBase());
        DataController.save();
	}
	
	public void deleteBatch() {

		DataController.delete(getBusinessObjectBase());
		DataController.delete(this);
	}
	
	public void deleteSingle() {

        DataController.delete(getBusinessObjectBase());
        DataController.delete(this);
        DataController.save();
	}
    
    public void softDeleteBatch() {

        if (deleteCheck()) {
            setDeleted(true);
            DataController.softDelete(getBusinessObjectBase());
        }
    }
	
	public void softDeleteSingle() {

	    if (deleteCheck()) {
    	    setDeleted(true);
            DataController.softDelete(getBusinessObjectBase());
            DataController.save();
	    }
	}
	
	public boolean deleteCheck() {
	    
	    if (getDataStateEnum() == DataState.DATA_STATE_NEW) {
	        deleteSingle();
	        return false;
	    }
	    
	    return true;
	}
	
	/**
	 * Adds a BusinessObjectBase object to the current object
	 */
	protected BusinessObjectBase addBusinessObjectBase() {

		if (this instanceof BusinessObjectBase) {
			return null;
		}
		
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
    	if (object == null && delete) {
    		return null;
    	}
    	
    	// Object exists, delete it
    	else if (object != null && delete) {
    		
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
    	
    	if (!inserting) {
    		object.updateBatch();
    	}
    	
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

        if (object == null) object = getQuery(key, id).unique();
        if (object == null) object = getByExternalId(key, guid);
        
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
    private static Query<?> getQuery(Class<?> key, Long id) {
    		
		Query<?> queryId = sQueries.get(key);
		
    	if (queryId == null) {

    		AbstractDao<?, Long> abDao = DataController.getDao(key);
    		queryId = abDao.queryBuilder().where(Properties.Id.eq(id)).build();
    		sQueries.put(key, queryId);
    		
    	} else {
    		
    		queryId.setParameter(0, id);
    	}
    	
    	return queryId;
    }
	
	public static Object getByExternalId(Class<?> key, String externalId) {
		
		AbstractDao<?, Long> dao = DataController.getDao(key);
		PowerQuery query = new PowerQuery(dao);
		
		Property foreignKey = new Property(3, long.class, "businessObjectId", false, "BUSINESS_OBJECT_ID");;
		
		query.join(new QueryProperty(BusinessObjectBaseDao.TABLENAME, foreignKey, BusinessObjectBaseDao.Properties.Id));
		query.where(new QueryProperty(BusinessObjectBaseDao.TABLENAME, BusinessObjectBaseDao.Properties.ExternalId), externalId);
		
		List<?> results = dao.queryRaw(query.toString(), query.getSelectionArgs());
		
		if (results.size() > 0) return results.get(0);
		
		return null;
	}
}
