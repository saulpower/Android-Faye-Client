package com.moneydesktop.finance.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {

	/**
	 * Convert a jsonArray to an ArrayList
	 * 
	 * @param jsonArray
	 * @return
	 * @throws JSONException
	 */
	public static List<JSONObject> toList(JSONArray jsonArray) throws JSONException {
		
		List<JSONObject> list = new ArrayList<JSONObject>();
		
		for (int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.getJSONObject(i));
		}
		
		return list;
	}
	
    public static String customFormat(String pattern, double s) { 
        DecimalFormat myFormatter = new DecimalFormat(pattern); 
        String stringformatoutput = myFormatter.format(s); 
        return stringformatoutput; 
     } 
}
