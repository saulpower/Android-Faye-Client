package com.moneydesktop.finance.util;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;

public class Fonts {
	
	public static final String TAG = "Fonts";

	private static Map<String, Typeface> fontCache = new HashMap<String, Typeface>();

	public static final String PRIMARY = "fonts/ProximaNova-Reg.otf";
	public static final String PRIMARY_SEMI_BOLD = "fonts/ProximaNova-Sbold.otf";
	public static final String PRIMARY_BOLD = "fonts/ProximaNova-Bold.otf";
//	public static final String PRIMARY_ITALIC = "fonts/ProximaNova-Semibold.otf";
	public static final String SECONDARY = "fonts/UtopiaStd-Regular.ttf";
	public static final String SECONDARY_SEMI_BOLD = "fonts/UtopiaStd-Semibold.otf";
	public static final String SECONDARY_SEMI_BOLD_ITALIC = "fonts/UtopiaStd-SemiboldIt.otf";
	public static final String SECONDARY_BOLD = "fonts/UtopiaStd-Bold.otf";
	public static final String SECONDARY_ITALIC = "fonts/UtopiaStd-Italic.otf";
	public static final String GLYPH = "fonts/MoneyDesktopIcons.otf";
	public static final String NAV_ICONS = "fonts/MDiPhoneNavIcons.otf";
	
	public static void applyPrimaryFont(TextView view, float size) {
		applyFont(PRIMARY, view, size);
	}
	
	// Does not work
	public static void applyPrimarySemiBoldFont(TextView view, float size) {
		applyFont(PRIMARY_SEMI_BOLD, view, size);
	}
	
	public static void applyPrimaryBoldFont(TextView view, float size) {
		applyFont(PRIMARY_BOLD, view, size);
	}
	
	// Does not work
//	public static void applyPrimaryItalicFont(TextView view, float size) {
//		applyFont(PRIMARY_ITALIC, view, size);
//	}
	
	public static void applySecondaryFont(TextView view, float size) {
		applyFont(SECONDARY, view, size);
	}
	
	public static void applySecondarySemiBoldFont(TextView view, float size) {
		applyFont(SECONDARY_SEMI_BOLD, view, size);
	}
	
	public static void applySecondarySemiBoldItalicFont(TextView view, float size) {
		applyFont(SECONDARY_SEMI_BOLD_ITALIC, view, size);
	}
	
	public static void applySecondaryBoldFont(TextView view, float size) {
		applyFont(SECONDARY_BOLD, view, size);
	}
	
	public static void applySecondaryItalicFont(TextView view, float size) {
		applyFont(SECONDARY_ITALIC, view, size);
	}
	
	public static void applyGlyphFont(TextView view, float size) {
		applyFont(GLYPH, view, size);
	}
	
	public static void applyNavIconFont(TextView view, float size) {
		applyFont(NAV_ICONS, view, size);
	}
	
	private static void applyFont(String key, TextView view, float size) {

		Typeface font = getFont(key);
		
		if (font != null)
			applyFontAndSize(view, font, size);
	}
	
	public static Typeface getFont(String key) {

		if (!fontCache.containsKey(key)) {
			
			try {
				
				Typeface type = Typeface.createFromAsset(ApplicationContext.getContext().getAssets(), key);
				fontCache.put(key, type);
				
			} catch (Exception ex) {
				Log.e(TAG, "Could not load font " + key);
				return null;
			}
		}
		
		return fontCache.get(key);
	}
	
	private static void applyFontAndSize(TextView view, Typeface font, float size) {
		
		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			float additional = UiUtils.getScreenAdjustment();
			float adjusted = UiUtils.getScaledPixels(ApplicationContext.getContext(), size) * additional;
			tv.setTextSize(adjusted);
			tv.setTypeface(font);
			return;
		}
	}
}
