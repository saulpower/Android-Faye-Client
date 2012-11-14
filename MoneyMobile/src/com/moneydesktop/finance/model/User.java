package com.moneydesktop.finance.model;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Preferences;
import com.moneydesktop.finance.data.Serialization;
import com.moneydesktop.finance.util.MacUtil;

public class User {

	private static final String DEMO_NAME = "Demo User";
	private static final String DEMO_ID = "demouserid";
	private static final String DEMO_FIRST_NAME = "Demo";
	private static final String DEMO_LAST_NAME = "User";
	private static final String DEMO_ENTITY = "demoentity";
	
	private static User sharedInstance;
	
	private String userId;
	private String userName;
	private String firstName;
	private String lastName;
	private String systemDeviceId;
	private String currentEntityId;
	private String defaultEntityId;
	private boolean canSync;
	
	public static User getCurrentUser() {
		
		if (sharedInstance == null) {
			sharedInstance = load();
		}
		
		return sharedInstance;
	}
	
	public User() {}
	
	public static void registerUser(JSONObject data, Context context) {
		
		sharedInstance = new User();
		sharedInstance.setUserName(data.optString(Constant.KEY_USERNAME));
		sharedInstance.setUserId(data.optString(Constant.KEY_USER_GUID));
		sharedInstance.setSystemDeviceId(data.optString(Constant.KEY_GUID));
		sharedInstance.setCurrentEntityId(sharedInstance.getUserId());
		sharedInstance.setDefaultEntityId(sharedInstance.getCurrentEntityId());
		sharedInstance.setCanSync(true);
		
		Preferences.saveUserToken(data.optString(Constant.KEY_LOGIN_TOKEN));
		
		sharedInstance.save();
	}
	
	public static void registerDemoUser() {
		
		sharedInstance = new User();
		sharedInstance.setUserName(DEMO_NAME);
		sharedInstance.setUserId(DEMO_ID);
		sharedInstance.setFirstName(DEMO_FIRST_NAME);
		sharedInstance.setLastName(DEMO_LAST_NAME);
		sharedInstance.setSystemDeviceId(MacUtil.getMACAddress());
		sharedInstance.setCurrentEntityId(DEMO_ENTITY);
		sharedInstance.setDefaultEntityId(DEMO_ENTITY);
		sharedInstance.setCanSync(false);
		
		sharedInstance.save();
	}
	
	@JsonIgnore
	public String getAuthorizationToken() {
		
		String secret = Preferences.getUserToken();
		String uid = getUserId();
		String deviceId = getSystemDeviceId();
		
		String token = Base64.encodeToString(String.format("%s|%s|%s", uid, secret, deviceId).getBytes(), Base64.DEFAULT).replace("\n", "");
		
		return token;
	}
	
	public boolean save() {

		String user = "";
		
		try {
			
			user = Serialization.serialize(this);
			
		} catch (Exception e) {}
		
		return Preferences.saveString(Preferences.KEY_USER, user) && !user.equals("");
	}
	
	public static void clear() {
		
		Preferences.saveString(Preferences.KEY_USER, "");
		Preferences.saveUserToken("");
		
		sharedInstance = null;
	}
	
	public static User load() {
		
		User user = null;
		
		String userString = Preferences.getString(Preferences.KEY_USER, "");
		
		if (!userString.equals("")) {
			
			try {
				user = (User) Serialization.deserialize(userString, User.class);
			} catch (Exception e) {}
		}
		
		return user;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getSystemDeviceId() {
		return systemDeviceId;
	}
	
	public void setSystemDeviceId(String systemDeviceId) {
		this.systemDeviceId = systemDeviceId;
	}
	
	public String getCurrentEntityId() {
		return currentEntityId;
	}
	
	public void setCurrentEntityId(String currentEntityId) {
		this.currentEntityId = currentEntityId;
	}
	
	public String getDefaultEntityId() {
		return defaultEntityId;
	}
	
	public void setDefaultEntityId(String defaultEntityId) {
		this.defaultEntityId = defaultEntityId;
	}
	
	public boolean getCanSync() {
		return canSync;
	}
	
	public void setCanSync(boolean canSync) {
		this.canSync = canSync;
	}
}
