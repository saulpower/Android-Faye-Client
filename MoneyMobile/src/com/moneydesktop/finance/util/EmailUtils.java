package com.moneydesktop.finance.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.moneydesktop.finance.R;

public class EmailUtils {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-\\+]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    public static void sendEmail(Context context, String subject, String text, String attachmentPath) {
        sendEmail(context, subject, text, new String[] {}, attachmentPath);
    }
        
    public static void sendEmail(Context context, String subject, String text, String[] recipients, String attachmentPath) {

        Uri attachmentUri = Uri.parse(attachmentPath);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("image/jpg");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentUri);

        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.email_title)));
    }
    
    public static void sendEmail(Context context, String subject, String text, String[] recipients) {
        
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        
        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.email_title)));
    }

    public static boolean validateEmail(String email) {

        Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
