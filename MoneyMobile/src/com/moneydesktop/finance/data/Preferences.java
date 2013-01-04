package com.moneydesktop.finance.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.crypto.StringEncrypter;

public class Preferences {

	private final static String TAG = "Preferences";

	private static final String KEY_TOKEN = "md_token";
	
	public static final String KEY_API_HOST = "md_api_host";
	public static final String KEY_BOB_ID = "md_bob_id";
	public static final String KEY_FAYE_HOST = "md_faye_host";
	public static final String KEY_IS_DEMO_MODE = "md_is_demo_mode";
	public static final String KEY_LAST_INSTITUTION_SYNC = "md_last_institution_sync";
	public static final String KEY_LAST_SYNC = "md_last_sync";
	public static final String KEY_LOCK_CODE = "md_lock_code";
	public static final String KEY_SYNC_HOST = "md_sync_host";
	public static final String KEY_USER = "md_user";
	public static final String KEY_NEEDS_SYNC = "md_needs_sync";
	public static final String KEY_NEEDS_FULL_SYNC = "md_needs_full_sync";

	private static final String PASSWORD = "m0n3y_d3skt0p";

	private static SharedPreferences getPrefs() {
		
		Context context = ApplicationContext.getContext();
		
		if (context == null)
			return null;
		
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean remove(String key) {

		SharedPreferences prefs = getPrefs();
		Editor edit = prefs.edit();
		edit.remove(key);
		
		return edit.commit();
	}

	public static boolean saveString(String key, String value) {
		
		SharedPreferences prefs = getPrefs();
		Editor edit = prefs.edit();
		edit.putString(key, value);
		
		return edit.commit();
	}
	
	public static String getString(String key, String defaultString) {
		
		String value = null;
		
		try {
		
			value = getPrefs().getString(key, defaultString);
			
		} catch (Exception ex) {}
		
		return value == null ? defaultString : value;
	}

	public static boolean saveBoolean(String key, boolean value) {
		
		SharedPreferences prefs = getPrefs();
		Editor edit = prefs.edit();
		edit.putBoolean(key, value);
		
		return edit.commit();
	}
	
	public static boolean getBoolean(String key, boolean defaultBool) {
		
		boolean value = defaultBool;
		
		try {
		
			value = getPrefs().getBoolean(key, defaultBool);
			
		} catch (Exception ex) {}
		
		return value;
	}
	
	public static boolean saveLong(String key, Long value) {
		
		SharedPreferences prefs = getPrefs();
		Editor edit = prefs.edit();
		edit.putLong(key, value);
		
		return edit.commit();
	}
	
	public static Long getLong(String key, Long defValue) {
		
		Long value = null;
		
		try {
			
			value = getPrefs().getLong(key, defValue);
			
		} catch (Exception ex) {}
		
		return value == null ? defValue : value; 
	}

	public static String getUserToken() {
		
		try {
			
			String token = getString(KEY_TOKEN, "");
			
			StringEncrypter crypto = new StringEncrypter(PASSWORD);
			
			return !token.equals("") ? crypto.decrypt(token) : "";
			
		} catch (Exception e) {
		    Log.e(TAG, "Error Decrypting", e);
			return "";
		}
	}

	public static void saveUserToken(String userToken) {
		
		try {

            StringEncrypter crypto = new StringEncrypter(PASSWORD);
            
			saveString(KEY_TOKEN, crypto.encrypt(userToken));
		
		} catch (Exception e) {
			Log.e(TAG, "Error Saving Password", e);
		}
	}
}
