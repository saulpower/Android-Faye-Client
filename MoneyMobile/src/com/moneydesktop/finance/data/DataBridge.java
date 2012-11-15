package com.moneydesktop.finance.data;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.moneydesktop.communication.HttpRequest;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.DebugActivity;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;

import de.greenrobot.event.EventBus;

public class DataBridge {
	
	private final String TAG = "DataBridge";
	
	private static final String ENDPOINT_DEVICE = "devices";
	private static final String ENDPOINT_FULL_SYNC = "sync/full";
	private static final String ENDPOINT_SYNC = "sync";
	private static final String ENDPOINT_INSTITUTIONS = "institutions";
	
	private static DataBridge sharedInstance;
	
	private String protocol = "https";
	
	private Context context;

	public static DataBridge sharedInstance() {
		
		if (sharedInstance == null) {
    		sharedInstance = new DataBridge();
    	}
    	
    	return sharedInstance;
	}
	
	public DataBridge() {
		this.context = ApplicationContext.getContext();
	}
	
	/**
	 * Login user by authenticating to the server with the supplied user name and password.
	 * 
	 * @param userName The user's name
	 * @param password The user's password
	 * @throws Exception
	 */
	public void authenticateUser(String userName, String password) throws Exception {
		
		long start = System.currentTimeMillis();
		
		AuthObject auth = new AuthObject(context, userName, password);
        String body = auth.toString();
        
        String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
        		
        String url = String.format("%s://%s/%s", protocol, baseUrl, ENDPOINT_DEVICE);
        
        String response = HttpRequest.sendPost(url, getHeaders(), null, body);
        
        JSONObject json = new JSONObject(response);
        
        if (json != null && json.has(Constant.KEY_DEVICE)) {
        	
        	JSONObject data = json.getJSONObject(Constant.KEY_DEVICE);
        	data.put(Constant.KEY_USERNAME, userName);
        	
        	User.registerUser(data, context);
        	
        	EventBus.getDefault().post(new EventMessage().new AuthEvent());
        }
        
        Log.i(TAG, "Auth in " + (System.currentTimeMillis() - start) + " ms");
	}
	
	/**
	 * Make a sync request with the server to pull down all data that needs to be
	 * sync'd with the mobile client
	 * 
	 * @param fullSync Flag to indicate a full sync is required
	 * @return The response parsed into a JSONObject
	 */
	public JSONObject downloadSync(boolean fullSync) {
		
		String endpoint = fullSync ? ENDPOINT_FULL_SYNC : ENDPOINT_SYNC;
        
        String baseUrl = Preferences.getString(Preferences.KEY_SYNC_HOST, DebugActivity.PROD_SYNC_HOST);
        		
        String url = String.format("%s://%s/%s", protocol, baseUrl, endpoint);
        
		try {

			Long start = System.currentTimeMillis();
			
			String response = HttpRequest.sendGet(url, getHeaders(), null);
			
			Log.i(TAG, "Sync Get: " + (System.currentTimeMillis() - start) + " ms");
			start = System.currentTimeMillis();
			
			JSONObject json = new JSONObject(response);
			
	        Log.i(TAG, "Sync Parsed: " + (System.currentTimeMillis() - start) + " ms");
			
			return json;
	        
		} catch (Exception e) {

			Log.e(TAG, "Error downloading sync", e);
			
			return null;
		}
	}
	
	public JSONObject uploadSync(JSONObject data) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_SYNC_HOST, DebugActivity.PROD_SYNC_HOST);
		
        String url = String.format("%s://%s/%s", protocol, baseUrl, ENDPOINT_SYNC);
        
		try {
			
			String response = HttpRequest.sendPost(url, getHeaders(), null, data.toString());
			
			JSONObject json = new JSONObject(response);
			
			return json;
	        
		} catch (Exception e) {

			Log.e(TAG, "Error downloading sync", e);
			
			return null;
		}
	}
	
	public void endSync(String syncToken) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_SYNC_HOST, DebugActivity.PROD_SYNC_HOST);
		
		String url = String.format("%s://%s/%s", protocol, baseUrl, ENDPOINT_SYNC);
		
		try {
			
			JSONObject body = new JSONObject();
			body.put(Constant.KEY_SYNC_TOKEN, syncToken);
			
			HttpRequest.sendDelete(url, getHeaders(), null, body.toString());
			
		} catch (Exception e) {
			
			Log.e(TAG, "Error ending sync", e);
		}
	}
	
	public JSONArray syncInstitutions() {

		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s", protocol, baseUrl, ENDPOINT_INSTITUTIONS);
		
		try {
			
			String response = HttpRequest.sendGet(url, getHeaders(), null);
			
			JSONArray json = new JSONArray(response);
			
			return json;
			
		} catch (Exception e) {
			
			Log.e(TAG, "Error ending sync", e);
			
			return null;
		}
	}
	
	/**
	 * Get the generic headers used in every request sent to the server
	 * 
	 * @return A map of request headers and their value
	 */
	public HashMap<String, String> getHeaders() {
		
		String version = "0.0";
		
		try {
			
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pInfo.versionName;
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("MD-App-Build", version);
		
		// If the user is logged in then we will need their token to authenticate a request
		if (User.getCurrentUser() != null && !User.getCurrentUser().getUserId().equals("")) {
			headers.put("X-Auth-UserToken", User.getCurrentUser().getAuthorizationToken());
		}
		
		return headers;
	}
	
	public void setUseSSL(boolean useSSL) {
		
		if (!useSSL)
			protocol = "http";
		else
			protocol = "https";
	}
	
}
