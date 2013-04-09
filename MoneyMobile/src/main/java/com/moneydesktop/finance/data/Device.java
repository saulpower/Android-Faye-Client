package main.java.com.moneydesktop.finance.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import main.java.com.moneydesktop.finance.util.MacUtil;

public class Device {

    private static Device sharedInstance;

    private Context context;
    private JSONObject data;

    public static Device sharedInstance(Context context) {

        if (sharedInstance == null) {
            sharedInstance = new Device(context);
        }

        return sharedInstance;
    }

    public Device(Context context) {

        this.context = context;
        InitializeDeviceInfo();
    }

    private void InitializeDeviceInfo() {

        data = new JSONObject();

        try {

            String eth = MacUtil.getMACAddress();

            data.put("uid", eth);
            data.put("name", getName());
            data.put("model", getModel());
            data.put("make", getMake());
            data.put("os_name", getOsName());
            data.put("os_version", getOsVersion());

        } catch (JSONException e) {}
    }

    public String getUid() {

        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        return wifiInf.getMacAddress();
    }

    public String getName() {
        return Build.PRODUCT;
    }

    public String getModel() {
        return android.os.Build.MODEL;
    }

    public String getMake() {
        return "Android";
    }

    public String getOsName() {
        return Integer.toString(android.os.Build.VERSION.SDK_INT);
    }

    public String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public JSONObject getData() {
        return data;
    }
}
