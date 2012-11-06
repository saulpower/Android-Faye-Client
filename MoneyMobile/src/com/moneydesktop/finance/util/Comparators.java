package com.moneydesktop.finance.util;

import java.util.Comparator;

import org.json.JSONObject;

import com.moneydesktop.finance.data.Constant;

public class Comparators {

	public class ParentGuidDateComparator implements Comparator<JSONObject> {

	    public int compare(JSONObject a, JSONObject b) {
	    	
	        String guidA = a.optString(Constant.KEY_PARENT_GUID);
	        String guidB = b.optString(Constant.KEY_PARENT_GUID);

	        if ((guidA.equals("") || guidA.equalsIgnoreCase(Constant.VALUE_NULL)) && !guidB.equals("") && !guidB.equalsIgnoreCase(Constant.VALUE_NULL))
	            
	        	return -1;
	        
	        else if ((guidB.equals("") || guidB.equalsIgnoreCase(Constant.VALUE_NULL)) && !guidA.equals("") && !guidA.equalsIgnoreCase(Constant.VALUE_NULL))
	            
	        	return 1;
	        
	        else if ((guidA.equals("") || guidA.equalsIgnoreCase(Constant.VALUE_NULL)) && (guidB.equals("") || guidB.equalsIgnoreCase(Constant.VALUE_NULL)))
	        	
	        	return 0;
	        
	        else
	        	
	        	return Long.valueOf(a.optLong(Constant.KEY_DATE)).compareTo(Long.valueOf(b.optLong(Constant.KEY_DATE)));  
	    }
	}
	
	public class ParentGuidNameComparator implements Comparator<JSONObject> {

	    public int compare(JSONObject a, JSONObject b) {

	        String guidA = a.optString(Constant.KEY_PARENT_GUID);
	        String guidB = b.optString(Constant.KEY_PARENT_GUID);

	        if ((guidA.equals("") || guidA.equalsIgnoreCase(Constant.VALUE_NULL)) && !guidB.equals("") && !guidB.equalsIgnoreCase(Constant.VALUE_NULL))
	            
	        	return -1;
	        
	        else if ((guidB.equals("") || guidB.equalsIgnoreCase(Constant.VALUE_NULL)) && !guidA.equals("") && !guidA.equalsIgnoreCase(Constant.VALUE_NULL))
	            
	        	return 1;
	        
	        else if ((guidA.equals("") || guidA.equalsIgnoreCase(Constant.VALUE_NULL)) && (guidB.equals("") || guidB.equalsIgnoreCase(Constant.VALUE_NULL)))
	        
	        	return 0;
	        	
	        else
	        	
	        	return a.optString(Constant.KEY_NAME).compareTo(b.optString(Constant.KEY_NAME));  
	    }
	}
}
