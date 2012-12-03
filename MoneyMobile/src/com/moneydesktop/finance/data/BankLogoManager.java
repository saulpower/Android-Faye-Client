package com.moneydesktop.finance.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.CacheUtils;

public class BankLogoManager {

//	private static final String IMAGES_SD = "https://s3.amazonaws.com/MD_Assets/Ipad%20Logos/50x50";
	private static final String IMAGES_HD = "https://s3.amazonaws.com/MD_Assets/Ipad%20Logos/100x100";
	
	public static void getBankImage(final ImageView view, String guid) {
		
		if (guid == null || guid.equals("") || checkForPackagedImage(view, guid))
			return;
		
		new AsyncTask<String, Void, String>() {
			
			@Override
			protected String doInBackground(String... params) {
				
				String guid = params[0];
				
				String imageUrl = String.format("%s/%s_100x100.png", IMAGES_HD, guid);
				String filePath = CacheUtils.cacheResource(imageUrl);
				
				return filePath;
			}
			
			@Override
			protected void onPostExecute(String filePath) {
				
				if (isCancelled()) return; 
				
				Bitmap image = BitmapFactory.decodeFile(filePath);
				
				if (image != null & view != null)
					view.setImageBitmap(image);
			};
			
		}.execute(guid);
	}
	
	private static boolean checkForPackagedImage(ImageView view, String guid) {
		
		if (guid.equalsIgnoreCase("bank")) {
			
			view.setImageResource(R.drawable.bank);
			return true;
		}
		
		if (guid.equalsIgnoreCase("bank1")) {
			
			view.setImageResource(R.drawable.bank1);
			return true;
		}
		
		if (guid.equalsIgnoreCase("bank2")) {
			
			view.setImageResource(R.drawable.bank2);
			return true;
		}
		
		if (guid.equalsIgnoreCase("bank3")) {
			
			view.setImageResource(R.drawable.bank3);
			return true;
		}
		
		if (guid.equalsIgnoreCase("bank4")) {
			
			view.setImageResource(R.drawable.bank4);
			return true;
		}
		
		return false;
	}
}
