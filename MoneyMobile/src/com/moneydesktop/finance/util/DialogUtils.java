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
	
	private static Dialog dialog;
	private static AlertDialog alert;
    
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
	    	
	    	dialog = new Dialog(context, R.style.MyDialog);
	    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    	dialog.setContentView(progressView);
	    	dialog.show();
	    	
		} catch (Exception ex) {}
    }
    
    public static void hideProgress() {
    	
    	if (dialog != null) {
    		try {
    			dialog.dismiss();
    		} catch (Exception ex) {}
    		dialog = null;
    	}
    }
    
    public static void alertDialog(String title, String message, Context context) {
    	alertDialog(title, message, context.getString(R.string.button_ok), context, null);
    }
    
    public static void alertDialog(String title, String message, Context context, DialogInterface.OnClickListener clickListener) {
    	alertDialog(title, message, context.getString(R.string.button_ok), context, clickListener);
    }

    public static void alertDialog(String title, String message, String positiveButton, Context context, DialogInterface.OnClickListener clickListener) {
    	
    	if (alert != null)
    		dismissAlert();
    	
    	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(positiveButton, clickListener);
		alert = alertBuilder.create();
		
		// Title for AlertDialog
		alert.setTitle(title);
		alert.show();
    }
    
    public static void dismissAlert() {
    	
    	if (alert != null) {
    		alert.dismiss();
    		alert = null;
    	}
    }
    
    public static void clickAlert() {
    	
    	if (alert != null) {
    		alert.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
    		alert = null;
    	}
    }
}
