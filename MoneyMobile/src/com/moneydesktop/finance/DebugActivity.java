package com.moneydesktop.finance;

import com.moneydesktop.finance.data.Preferences;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DebugActivity extends ListActivity {

	public static final String SAND_API_HOST = "sand-firefly1.moneydesktop.com";
	public static final String SAND_SYNC_HOST = "sand-synchronicity1.moneydesktop.com";
	public static final String SAND_FAYE_HOST = "sand-faye1.moneydesktop.com";

	public static final String QA_API_HOST = "qa-firefly1.moneydesktop.com";
	public static final String QA_SYNC_HOST = "qa-synchronicity1.moneydesktop.com";
	public static final String QA_FAYE_HOST = "qa-faye1.moneydesktop.com";

	public static final String STAGE_API_HOST = "stage-firefly.moneydesktop.com";
	public static final String STAGE_SYNC_HOST = "stage-synchronicity.moneydesktop.com";
	public static final String STAGE_FAYE_HOST = "stage-faye.moneydesktop.com";

	public static final String PROD_API_HOST = "firefly.moneydesktop.com";
	public static final String PROD_SYNC_HOST = "synchronicity.moneydesktop.com";
	public static final String PROD_FAYE_HOST = "faye.moneydesktop.com";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String[] values = new String[] { "Sand", "QA", "Staging", "Production" };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        
        setListAdapter(adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, "Switching to " + item, Toast.LENGTH_SHORT).show();
		
		switch (position) {
		case 0:
			Preferences.saveString(Preferences.KEY_API_HOST, SAND_API_HOST);
			Preferences.saveString(Preferences.KEY_SYNC_HOST, SAND_SYNC_HOST);
			Preferences.saveString(Preferences.KEY_FAYE_HOST, SAND_FAYE_HOST);
			break;
		case 1:
			Preferences.saveString(Preferences.KEY_API_HOST, QA_API_HOST);
			Preferences.saveString(Preferences.KEY_SYNC_HOST, QA_SYNC_HOST);
			Preferences.saveString(Preferences.KEY_FAYE_HOST, QA_FAYE_HOST);
			break;
		case 2:
			Preferences.saveString(Preferences.KEY_API_HOST, STAGE_API_HOST);
			Preferences.saveString(Preferences.KEY_SYNC_HOST, STAGE_SYNC_HOST);
			Preferences.saveString(Preferences.KEY_FAYE_HOST, STAGE_FAYE_HOST);
			break;
		case 3:
			Preferences.saveString(Preferences.KEY_API_HOST, PROD_API_HOST);
			Preferences.saveString(Preferences.KEY_SYNC_HOST, PROD_SYNC_HOST);
			Preferences.saveString(Preferences.KEY_FAYE_HOST, PROD_FAYE_HOST);
			break;
		}
		
		finish();
    }

}
