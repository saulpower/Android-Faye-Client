package com.moneydesktop.finance.data;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.crypto.SimpleCrypto;

public class Preferences {

	private final static String TAG = "Preferences";

	private static final String KEY_TOKEN = "md_token";
	
	public static final String KEY_USER = "md_user";
	public static final String KEY_API_HOST = "md_api_host";
	public static final String KEY_SYNC_HOST = "md_sync_host";
	public static final String KEY_FAYE_HOST = "md_faye_host";
	public static final String KEY_LAST_SYNC = "md_last_sync";

	private static final String SEED = "moneydesktop";

	private static SharedPreferences getPrefs() {
		
		return PreferenceManager.getDefaultSharedPreferences(ApplicationContext.getContext());
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
			
		} catch (ClassCastException ex) {}
		
		return value == null ? defaultString : value;
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
		} catch (ClassCastException ex) {}
		
		return value == null ? defValue : value; 
	}

	public static String getUserToken() {
		
		try {
			
			String token = getString(KEY_TOKEN, "");
			
			return !token.equals("") ? SimpleCrypto.decrypt(SEED, token) : "";
			
		} catch (Exception e) {
			return "";
		}
	}

	public static void saveUserToken(String userToken) {
		
		try {
			
			saveString(KEY_TOKEN, SimpleCrypto.encrypt(SEED, userToken));
		
		} catch (Exception e) {
			Log.e(TAG, "Error Saving Password", e);
		}
	}
}
