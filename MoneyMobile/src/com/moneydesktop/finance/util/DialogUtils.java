package com.moneydesktop.finance.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.TextView;

import com.moneydesktop.finance.R;

public class DialogUtils {
	
	private static Dialog sDialog;
	private static AlertDialog sAlert;
    
    public static void showProgress(Context context, String message) {

    	try {
	    	
    		hideProgress();
	    	View progressView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
	    	progressView.setBackgroundResource(R.color.transparent);
	    	progressView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
	    	TextView text = (TextView) progressView.findViewById(R.id.spinner_text);
	    	text.setText(message);
	    	
	    	sDialog = new Dialog(context, R.style.MyDialog);
	    	sDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	sDialog.setContentView(progressView);
	    	sDialog.show();
	    	
		} catch (Exception ex) {}
    }
    
    public static void hideProgress() {
    	
    	if (sDialog != null) {
    		try {
    			sDialog.dismiss();
    		} catch (Exception ex) {}
    		sDialog = null;
    	}
    }
    
    public static void alertDialog(String title, String message, Context context) {
    	alertDialog(title, message, context.getString(R.string.button_ok), null, context, null);
    }
    
    public static void alertDialog(String title, String message, Context context, DialogInterface.OnClickListener clickListener) {
    	alertDialog(title, message, context.getString(R.string.button_ok), null, context, clickListener);
    }

    public static void alertDialog(String title, String message, String positiveButton, Context context, DialogInterface.OnClickListener clickListener) {
        alertDialog(title, message, context.getString(R.string.button_ok), null, context, clickListener);
    }
    
    public static void alertDialog(String title, String message, String positiveButton, String negativeButton, Context context, DialogInterface.OnClickListener clickListener) {
    	
    	if (sAlert != null)
    		dismissAlert();
    	
    	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setMessage(message)
				.setCancelable(false)
				.setNegativeButton(positiveButton, clickListener);
		
		if (negativeButton != null) {
		    alertBuilder.setPositiveButton(negativeButton, clickListener);
		}
		
		sAlert = alertBuilder.create();
		
		// Title for AlertDialog
		sAlert.setTitle(title);
		sAlert.show();
    }
    
    public static void dismissAlert() {
    	
    	if (sAlert != null) {
    		sAlert.dismiss();
    		sAlert = null;
    	}
    }
    
    public static void clickAlert() {
    	
    	if (sAlert != null) {
    		sAlert.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
    		sAlert = null;
    	}
    }
}
