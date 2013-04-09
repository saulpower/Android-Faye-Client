package main.java.com.moneydesktop.finance.model;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.Preferences;
import main.java.com.moneydesktop.finance.data.Serialization;
import main.java.com.moneydesktop.finance.util.Base64;
import main.java.com.moneydesktop.finance.util.MacUtil;

public class User {

    public static final String TAG = "User";

    private static final String DEMO_NAME = "Demo User";
    private static final String DEMO_ID = "demouserid";
    private static final String DEMO_FIRST_NAME = "Demo";
    private static final String DEMO_LAST_NAME = "User";
    private static final String DEMO_ENTITY = "demoentity";

    private static User sSharedInstance;

    private String mUserId;
    private String mUserName;
    private String mFirstName;
    private String mLastName;
    private String mSystemDeviceId;
    private String mCurrentEntityId;
    private String mDefaultEntityId;
    private boolean mCanSync;

    public static User getCurrentUser() {

        if (sSharedInstance == null) {
            sSharedInstance = load();
        }

        return sSharedInstance;
    }

    public User() {}

    public static void registerUser(JSONObject data, Context context) {

        sSharedInstance = new User();
        sSharedInstance.setUserName(data.optString(Constant.KEY_USERNAME));
        sSharedInstance.setUserId(data.optString(Constant.KEY_USER_GUID));
        sSharedInstance.setSystemDeviceId(data.optString(Constant.KEY_GUID));
        sSharedInstance.setCurrentEntityId(sSharedInstance.getUserId());
        sSharedInstance.setDefaultEntityId(sSharedInstance.getCurrentEntityId());
        sSharedInstance.setCanSync(true);

        Preferences.saveUserToken(data.optString(Constant.KEY_LOGIN_TOKEN));

        sSharedInstance.save();
    }

    public static void registerDemoUser() {

        sSharedInstance = new User();
        sSharedInstance.setUserName(DEMO_NAME);
        sSharedInstance.setUserId(DEMO_ID);
        sSharedInstance.setFirstName(DEMO_FIRST_NAME);
        sSharedInstance.setLastName(DEMO_LAST_NAME);
        sSharedInstance.setSystemDeviceId(MacUtil.getMACAddress());
        sSharedInstance.setCurrentEntityId(DEMO_ENTITY);
        sSharedInstance.setDefaultEntityId(DEMO_ENTITY);
        sSharedInstance.setCanSync(false);

        sSharedInstance.save();
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

        sSharedInstance = null;
    }

    public static User load() {

        User user = null;

        String userString = Preferences.getString(Preferences.KEY_USER, "");

        if (!userString.equals("")) {

            try {
                user = (User) Serialization.deserialize(userString, User.class);
            } catch (Exception e) {
                Log.e(TAG, "Could not deserialize user");
            }
        }

        return user;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public String getSystemDeviceId() {
        return mSystemDeviceId;
    }

    public void setSystemDeviceId(String systemDeviceId) {
        this.mSystemDeviceId = systemDeviceId;
    }

    public String getCurrentEntityId() {
        return mCurrentEntityId;
    }

    public void setCurrentEntityId(String currentEntityId) {
        this.mCurrentEntityId = currentEntityId;
    }

    public String getDefaultEntityId() {
        return mDefaultEntityId;
    }

    public void setDefaultEntityId(String defaultEntityId) {
        this.mDefaultEntityId = defaultEntityId;
    }

    public boolean getCanSync() {
        return mCanSync;
    }

    public void setCanSync(boolean canSync) {
        this.mCanSync = canSync;
    }
}
