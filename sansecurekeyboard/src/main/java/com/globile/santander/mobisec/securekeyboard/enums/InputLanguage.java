package com.globile.santander.mobisec.securekeyboard.enums;

import android.content.res.Resources;

import com.globile.santander.mobisec.securekeyboard.R;

import java.util.Locale;

/**
 * InputLanguage is an enum which contains keyboard's possible languages.
 */
public enum InputLanguage {

	// Decimal language keyboard codes: https://www.autoitscript.com/autoit3/docs/appendix/OSLangCodes.htm
	ENGLISH_UK("en", "GB"),
	SPANISH_ES("es", "ES"),
	PORTUGUESE_PT("pt", "PT"),
	POLISH_PL("pl", "PL");
	
	public static final InputLanguage[] defaultLanguages = InputLanguage.values();
	public static final InputLanguage defaultLanguage = defaultLanguages[0];
	
	private final String language;
	private final String country;
	
	InputLanguage(String language, String country) {
		this.language = language;
		this.country = country;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getCountry() {
		return country;
	}
	
	public String getText(Resources resources) {
		return resources.getStringArray(R.array.dialog_languages_array)[ordinal()];
	}
	
	public Locale getLocale() {
		return new Locale(language, country);
	}
	
	/**
	 * Returns {@link com.globile.santander.mobisec.securekeyboard.enums.InputLanguage} for {@link java.util.Locale}
	 *
	 * @param locale the desired {@link Locale}
	 *
	 * @return the {@link InputLanguage} for the {@link Locale}. Else, an {@link InputLanguage} for the same language if available,
	 * or defaultLanguage if no match
	 */
	public static InputLanguage forLocale(Locale locale) {
		InputLanguage sameLanguage = null;
		for (InputLanguage value : values()) {
			if (locale.getLanguage().equals(value.language)) {
				if (locale.getCountry().equals(value.country)) {
					return value;
				}
				if (sameLanguage == null) {//We have our language, but from another country. Use it.
					sameLanguage = value;
				}
			}
		}
		return sameLanguage != null ? sameLanguage : defaultLanguage;
	}
	
}