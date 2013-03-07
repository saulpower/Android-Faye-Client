package com.moneydesktop.finance.data;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.moneydesktop.communication.HttpRequest;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.activity.DebugActivity;

import de.greenrobot.event.EventBus;

public class DataBridge {
	
	private final String TAG = "DataBridge";
	
	private static final String ENDPOINT_DEVICE = "devices";
	private static final String ENDPOINT_FULL_SYNC = "sync/full";
	private static final String ENDPOINT_INSTITUTIONS = "institutions";
	private static final String ENDPOINT_MEMBERS = "members";
	private static final String ENDPOINT_SYNC = "sync";
	private static final String ENDPOINT_ACCOUNTS = "accounts";
	
	private static DataBridge sharedInstance;
	
	private String protocol = "https";
	
	private Context context;

	public static DataBridge sharedInstance() {
		
		if (sharedInstance == null) {
    		sharedInstance = new DataBridge();
    	}
    	
    	return sharedInstance;
	}
	
	public static DataBridge sharedInstance(Context context) {
		
		if (sharedInstance == null) {
    		sharedInstance = new DataBridge(context);
    	}
    	
    	return sharedInstance;
	}
	
	public DataBridge() {
		this.context = ApplicationContext.getContext();
	}
	
	public DataBridge(Context context) {
		this.context = context;
	}
	
	/**
	 * Login user by authenticating to the server with the supplied user name and password.
	 * 
	 * @param userName The user's name
	 * @param password The user's password
	 * @throws Exception
	 */
	public void authenticateUser(String userName, String password) throws Exception {
		
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
        String response = "";
        
		try {
			
			response = HttpRequest.sendGet(url, getHeaders(), null);
			
			JSONObject json = new JSONObject(response);
			
			return json;
	        
		} catch (Exception e) {

			Log.e(TAG, "Error downloading sync", e);
			Log.e(TAG, "Server Response: " + response);
			
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
	
	public JSONObject getBankStatus(String bankId) {

		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s/%s", protocol, baseUrl, ENDPOINT_MEMBERS, bankId);
		
		try {
			
			String response = HttpRequest.sendGet(url, getHeaders(), null);
			
			JSONObject json = new JSONObject(response);
			
			return json;
			
		} catch (Exception e) {
			Log.e(TAG, "Error getting bank status", e);
		}
		
		return null;
	}
	
	public JSONObject saveManualAccount (JSONObject json) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s", protocol, baseUrl, ENDPOINT_ACCOUNTS);
		
		try {
			
			String response = HttpRequest.sendPost(url, getHeaders(), null, json.toString());
			
			JSONObject jsonResponse = new JSONObject(response);
			
			return jsonResponse;
			
		} catch (Exception e) {
			Log.e(TAG, "Error saving manual bank account", e);
		}
		
		return null;
	}
	
	/**
	 * Get the generic headers used in every request sent to the server
	 * 
	 * @return A map of request headers and their value
	 */
	public HashMap<String, String> getHeaders() {
		
		String version = "1.0";
		
		try {
			
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pInfo.versionName;
			
		} catch (Exception e) {
			Log.w(TAG, "Could not get app version");
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
	
	/**
	 * Returns a JSON array of the credential fields necessary to login with
	 * the passed in institution ID.
	 * 
	 * @param guid
	 * @return
	 */
	public JSONArray getInstituteLoginFields(String institutionId) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s/%s", protocol, baseUrl, ENDPOINT_INSTITUTIONS, institutionId);
		
		try {
			
			String response = HttpRequest.sendGet(url, getHeaders(), null);
			
			JSONObject json = new JSONObject(response);
			JSONArray credentials = json.getJSONObject(Constant.KEY_INSTITUTION).getJSONArray(Constant.KEY_CREDENTIALS);
			
			return credentials;
			
		} catch (Exception e) {
			
			Log.e(TAG, "Error getting institute fields", e);
			
			return null;
		}
	}
	
	/**
	 * Get the array of multi-factor authentication questions necessary to login
	 * to the passed in bank ID
	 * 
	 * @param bankId
	 */
	public JSONArray getMfaQuestions(String bankId) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s/%s", protocol, baseUrl, ENDPOINT_MEMBERS, bankId);
		
		try {
			
			String response = HttpRequest.sendGet(url, getHeaders(), null);
			
			JSONObject json = new JSONObject(response);
			JSONObject status = json.getJSONObject(Constant.KEY_MEMBER).getJSONObject(Constant.KEY_PROCESS_STATUS);
			
			if (!status.isNull(Constant.KEY_MFA)) {
				
				return status.getJSONObject(Constant.KEY_MFA).getJSONArray(Constant.KEY_CREDENTIALS);
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Error getting institute fields", e);
		}
		
		return null;
	}
	
	public void setUseSSL(boolean useSSL) {
		
		if (!useSSL)
			protocol = "http";
		else
			protocol = "https";
	}
	
	public JSONObject saveFinancialInstitute(JSONObject data) {

		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s", protocol, baseUrl, ENDPOINT_MEMBERS);
		
		try {
			
			String response = HttpRequest.sendPost(url, getHeaders(), null, data.toString());
			
			JSONObject json = new JSONObject(response);
			
			return json;
			
		} catch (Exception e) {
			Log.e(TAG, "Error getting institute fields", e);
		}
		
		return null;
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
	
	public JSONObject updateLoginFields(String bankId, JSONObject fields) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s/%s", protocol, baseUrl, ENDPOINT_MEMBERS, bankId);
		
		try {
			
			String response = HttpRequest.sendPut(url, getHeaders(), null, fields.toString());
			
			JSONObject json = new JSONObject(response);
			
			return json;
			
		} catch (Exception e) {
			Log.e(TAG, "Error updating login fields", e);
		}
		
		return null;
	}
	
	public JSONObject updateMfaQuestions(String bankId, JSONArray answers) {
		
		String baseUrl = Preferences.getString(Preferences.KEY_API_HOST, DebugActivity.PROD_API_HOST);
		
		String url = String.format("%s://%s/%s/%s", protocol, baseUrl, ENDPOINT_MEMBERS, bankId);
		
		try {
			
			JSONObject json = new JSONObject();
			json.put(Constant.KEY_CREDENTIALS, answers);
			
			String response = HttpRequest.sendPut(url, getHeaders(), null, json.toString());
			
			json = new JSONObject(response);
			
			return json;
			
		} catch (Exception e) {
			Log.e(TAG, "Error updating mfa questions", e);
		}
		
		return null;
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
}
