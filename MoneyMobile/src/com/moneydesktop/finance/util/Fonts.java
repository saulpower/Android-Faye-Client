package com.moneydesktop.finance.util;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;

public class Fonts {

	private static Map<String, Typeface> fontCache = new HashMap<String, Typeface>();

	private static final String PRIMARY = "fonts/ProximaNova-Regular.otf";
	private static final String PRIMARY_SEMI_BOLD = "fonts/ProximaNova-Semibold.otf";
	private static final String PRIMARY_BOLD = "fonts/ProximaNova-Bold.otf";
	private static final String PRIMARY_ITALIC = "fonts/ProximaNova-Semibold.otf";
	private static final String SECONDARY = "fonts/UtopiaStd-Regular.otf";
	private static final String SECONDARY_SEMI_BOLD = "fonts/UtopiaStd-Semibold.otf";
	private static final String SECONDARY_SEMI_BOLD_ITALIC = "fonts/UtopiaStd-SemiboldIt.otf";
	private static final String SECONDARY_BOLD = "fonts/UtopiaStd-Bold.otf";
	private static final String SECONDARY_ITALIC = "fonts/UtopiaStd-Italic.otf";
	private static final String GLYPH = "fonts/MoneyDesktopIcons.otf";
	
	public static void applyPrimaryFont(TextView view, float size) {
		applyFont(PRIMARY, view, size);
	}
	
	// Does not work
//	public static void applyPrimarySemiBoldFont(TextView view, float size) {
//		applyFont(PRIMARY_SEMI_BOLD, view, size);
//	}
	
	public static void applyPrimaryBoldFont(TextView view, float size) {
		applyFont(PRIMARY_BOLD, view, size);
	}
	
	public static void applyPrimaryItalicFont(TextView view, float size) {
		applyFont(PRIMARY_ITALIC, view, size);
	}
	
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
	
	private static void applyFont(String key, TextView view, float size) {

		if (!fontCache.containsKey(key)) {
			fontCache.put(key, Typeface.createFromAsset(ApplicationContext.getContext().getAssets(), key));
		}
		
		applyFontAndSize(view, fontCache.get(PRIMARY), size);
	}
	
	private static void applyFontAndSize(TextView view, Typeface font, float size) {
		
		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			tv.setTextSize(size);
			tv.setTypeface(font);
			return;
		}
	}
}
