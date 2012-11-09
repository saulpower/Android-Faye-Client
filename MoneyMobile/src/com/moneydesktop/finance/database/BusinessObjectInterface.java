package com.moneydesktop.finance.database;


public interface BusinessObjectInterface {
    
	public void setExternalId(String id);
	public String getExternalId();
	
	public long getBusinessObjectId();
    public void setBusinessObjectId(long businessObjectId);

	public BusinessObjectBase getBusinessObjectBase();
	public void setBusinessObjectBase(BusinessObjectBase businessObjectBase);
}
