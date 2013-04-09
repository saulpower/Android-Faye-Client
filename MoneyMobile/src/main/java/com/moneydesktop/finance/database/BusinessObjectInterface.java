package main.java.com.moneydesktop.finance.database;

import org.json.JSONException;
import org.json.JSONObject;

public interface BusinessObjectInterface {

    public JSONObject getJson() throws JSONException;

    public void setExternalId(String id);
    public String getExternalId();

    public long getBusinessObjectId();
    public void setBusinessObjectId(long businessObjectId);

    public BusinessObjectBase getBusinessObjectBase();
    public void setBusinessObjectBase(BusinessObjectBase businessObjectBase);
}
