package com.moneydesktop.finance.test;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;
import com.moneydesktop.finance.model.User;

public class ServicesTest extends ActivityInstrumentationTestCase2<DashboardHandsetActivity> {
	
	public final String TAG = "ServicesTest";
	
	private String userName = "saul.howard@moneydesktop.com";
	private String password = "password123";
	
	private String bankId = "MBR-c94b908d-40e6-eb4a-47b0-78347e7373c0";
	private String institutionId = "INS-81128421-831b-e241-6ee6-b55f55b8492a";

	Context context = null;
	DataBridge db = null;
	
	public ServicesTest() {
		super("com.moneydesktop.finance.handset.activity", DashboardHandsetActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		getInstrumentation().getTargetContext().getApplicationContext();
		context = ApplicationContext.getContext();
		db = DataBridge.sharedInstance(context);
	}
	
	public void testContext() {
		
		assertNotNull(context);
		assertNotNull(PreferenceManager.getDefaultSharedPreferences(context));
	}
	
	public void testHeaders() {
		
		Map<String, String> headers = db.getHeaders();
		
		assertTrue(headers.containsKey("Content-Type"));
		assertTrue(headers.containsKey("Accept"));
		assertTrue(headers.containsKey("MD-App-Build"));
	}
	
	public void testAuthentication() {
		
		if (User.getCurrentUser() == null) {
			
			try {
				db.authenticateUser(userName, password);
			} catch (Exception ex) {
				Log.e(TAG, "Error authenticating", ex);
			}
		}
		
		assertNotNull(User.getCurrentUser());
	}
	
	public void testBankStatus() {
		
		JSONObject json = db.getBankStatus(bankId);
		JSONObject member = json.optJSONObject(Constant.KEY_MEMBER);
		
		assertNotNull(json);
		assertNotNull(member);
		assertTrue(member.has(Constant.KEY_LAST_JOB));
	}
	
	public void testInstituteLoginFields() {
		
		JSONArray json = db.getInstituteLoginFields(institutionId);
		
		assertNotNull(json);
		assertTrue(json.length() > 0);
	}
	
	public void testMfaQuestions() {

		JSONArray json = db.getMfaQuestions(bankId);
		
		assertNotNull(json);
		assertTrue(json.length() > 0);
	}
}
