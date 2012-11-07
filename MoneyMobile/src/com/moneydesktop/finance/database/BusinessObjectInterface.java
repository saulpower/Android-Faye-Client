package com.moneydesktop.finance.database;


public interface BusinessObjectInterface {

	public BusinessObjectBase getBusinessObjectBase();
	public void setBusinessObjectBase(BusinessObjectBase businessObjectBase);
	public void setExternalId(String id);
	public String getExternalId();
}
