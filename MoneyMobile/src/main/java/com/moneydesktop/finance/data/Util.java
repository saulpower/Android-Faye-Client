package main.java.com.moneydesktop.finance.data;

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


    public static ArrayList<String> getExcludedAccountsFromPrefs(String preferenceString) {

        ArrayList<String> arrayList = new ArrayList<String>();
        try {
            JSONArray jsonBankAccountIds = new JSONArray(preferenceString);
            for (int i = 0; i < jsonBankAccountIds.length(); i++) {
                arrayList.add(i, String.valueOf(jsonBankAccountIds.getInt(i)));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

}