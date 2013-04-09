package main.java.com.moneydesktop.finance.data;

import org.json.JSONObject;

import android.content.Context;

public class AuthObject {

    private String userName;
    private String password;
    private Device device;

    public AuthObject() {}

    public AuthObject(Context context, String userName, String password) {

        this.userName = userName;
        this.password = password;
        this.device = Device.sharedInstance(context);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {

        JSONObject json = new JSONObject();

        try {

            json.put("username", userName);
            json.put("password", password);
            json.put("device", device.getData());

        } catch (Exception e) {}

        return json.toString();
    }

}
