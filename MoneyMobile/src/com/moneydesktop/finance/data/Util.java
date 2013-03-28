package com.moneydesktop.finance.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Util {

	/**
	 * Convert a jsonArray to an ArrayList
	 * 
	 * @param jsonArray
	 * @return
	 * @throws JSONException
	 */
	public static List<JSONObject> toList(JSONArray jsonArray)
			throws JSONException {

		List<JSONObject> list = new ArrayList<JSONObject>();

		for (int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.getJSONObject(i));
		}

		return list;
	}

	public static byte[] serializeObject(Object o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(o);
			out.close();

			// Get the bytes of the serialized object
			byte[] buf = bos.toByteArray();

			return buf;
		} catch (IOException ioe) {
			Log.e("serializeObject", "error", ioe);

			return null;
		}
	}
	
	public static Object deserializeObject(byte[] b) { 
	    try { 
	      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b)); 
	      Object object = in.readObject(); 
	      in.close(); 
	 
	      return object; 
	    } catch(ClassNotFoundException cnfe) { 
	      Log.e("deserializeObject", "class not found error", cnfe); 
	 
	      return null; 
	    } catch(IOException ioe) { 
	      Log.e("deserializeObject", "io error", ioe); 
	 
	      return null; 
	    } 
	  } 
	
	public static Object deserializeObject(String s) {		
		return deserializeObject(s.getBytes());
	  }
	
}