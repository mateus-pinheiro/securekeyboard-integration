package com.globile.santander.mobisec.securekeyboard.keyboard;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.SanEditText;
import com.globile.santander.mobisec.securekeyboard.enums.InputLanguage;
import com.globile.santander.mobisec.securekeyboard.enums.ShiftMode;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.listeners.SanTapJackedCallback;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;

import java.util.Locale;

/**
 * Make sure this is synchronised with the
 * {@link SanEditText SanEditText} styleable attrs.
 */
public class SanKeyboardManager {

	// ATTRIBUTES
	// ***********************************************************************

	private static InputLanguage[] possibleLanguages = InputLanguage.defaultLanguages;
	private static SanTapJackedCallback sanTapJackedCallback;

	private SanKeyboardType keyboardType = SanKeyboardType.ALPHA;
	private SanKeyboard current;

	private Context contextForLanguage;
	private InputLanguage currentLanguage;

	private final Context context;

    private SanKeyboard alphaNumericKeyboard;
    private SanKeyboard alphaNumericTopRowKeyboardOneButton;
    private SanKeyboard alphaNumericTopRowKeyboardTwoButtons;

    private SanKeyboard decimalKeyboard;
    private SanKeyboard decimalTopRowKeyboardOneButton;
    private SanKeyboard decimalTopRowKeyboardTwoButtons;

    private SanKeyboard numericKeyboard;
    private SanKeyboard numericTopRowKeyboardOneButton;
    private SanKeyboard numericTopRowKeyboardTwoButtons;

    private SanKeyboard specialCharacterKeyboard;
    private SanKeyboard specialCharacterTopRowKeyboardOneButton;
    private SanKeyboard specialCharacterTopRowKeyboardTwoButtons;

	private SanKeyboard specialCharacterKeyboardNext;
    private SanKeyboard specialCharacterTopRowKeyboardNextOneButton;
    private SanKeyboard specialCharacterTopRowKeyboardNextTwoButtons;


	// CONSTRUCTORS
	// ***********************************************************************

	public SanKeyboardManager(@NonNull Context context) {
		this.context = context;//Must be at the beginning!
		Locale locale = context.getResources().getConfiguration().locale;
		updateKeyboards(InputLanguage.forLocale(locale));//Also creates both alpha keyboards
	}

	public void updateKeyboards(InputLanguage currentLanguage) {
		this.currentLanguage = currentLanguage;
		contextForLanguage = SanKeyboardUtils.getContextForLocale(context.getApplicationContext(), currentLanguage.getLocale());
		inflateKeyboards();
	}

	private void inflateKeyboards() {
		
		this.alphaNumericKeyboard = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_alphanumeric);
		this.alphaNumericTopRowKeyboardOneButton = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_alphanumeric_top_row_one_button);
		this.alphaNumericTopRowKeyboardTwoButtons = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_alphanumeric_top_row_two_buttons);

		this.decimalKeyboard = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_decimal);
		this.decimalTopRowKeyboardOneButton = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_decimal_top_row_one_button);
		this.decimalTopRowKeyboardTwoButtons = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_decimal_top_row_two_buttons);
		
		this.numericKeyboard = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_numeric);
		this.numericTopRowKeyboardOneButton = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_numeric_top_row_one_button);
		this.numericTopRowKeyboardTwoButtons = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_numeric_top_row_two_buttons);
		
		this.specialCharacterKeyboard = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_specialcharacters);
		this.specialCharacterTopRowKeyboardOneButton = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_specialcharacters_top_row_one_button);
		this.specialCharacterTopRowKeyboardTwoButtons = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_specialcharacters_top_row_two_buttons);
		
		this.specialCharacterKeyboardNext = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_specialcharacters_next);
		this.specialCharacterTopRowKeyboardNextOneButton = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_specialcharacters_next_top_row_one_button);
		this.specialCharacterTopRowKeyboardNextTwoButtons = new SanKeyboard(contextForLanguage,
				R.xml.keyboard_specialcharacters_next_top_row_two_buttons);
		
	}

	// GETTERS & SETTERS
	// ***********************************************************************

	public static InputLanguage[] getPossibleLanguages() {
		return possibleLanguages;
	}

	public static void setPossibleLanguages(InputLanguage[] possibleLanguages) {
		SanKeyboardManager.possibleLanguages = possibleLanguages;
	}

	public static SanTapJackedCallback getSanTapJackedCallback() {
		return sanTapJackedCallback;
	}

	public static void setSanTapJackedCallback(SanTapJackedCallback sanTapJackedCallback) {
		SanKeyboardManager.sanTapJackedCallback = sanTapJackedCallback;
	}

	public InputLanguage getCurrentLanguage() {
		return currentLanguage;
	}

	private void setKeyboardType(SanKeyboardType keyboardType) {
		this.keyboardType = keyboardType;
	}

	public SanKeyboardType getKeyboardType() {
		return this.keyboardType;
	}


	// METHODS
	// ***********************************************************************

	/**
	 * Method used to change SanKeyboard type depending on argument keyboard type passed
	 *
	 * @param keyboardType The type of the keyboard to render
	 * @param topRowButtonsCode Configuration for Top Row (None, One or Two buttons)
	 *
	 * @return SanKeyboard
	 */
	public SanKeyboard getKeyboardForType(SanKeyboardType keyboardType, TopRowButtonsOptions topRowButtonsCode) {
		
		this.setKeyboardType(keyboardType);
		
		//Check if active EditText requires top row
		SanKeyboard shown;

		switch (keyboardType) {

			case ALPHA:
				shown = selectAlphanumericKeyboard(topRowButtonsCode);
				shown.setInitialShift(ShiftMode.LOWER_CASE);
				break;

			case ALPHA_UPPER:
				shown = selectAlphanumericKeyboard(topRowButtonsCode);
				shown.setInitialShift(ShiftMode.UPPER_CASE_SINGLE);
				break;

			case ALPHA_UPPER_PERM:
				shown = selectAlphanumericKeyboard(topRowButtonsCode);
				shown.setInitialShift(ShiftMode.UPPER_CASE_CONTINUOUS);
				break;

			case DECIMAL:
				
				switch(topRowButtonsCode){
					
					default:
					case NONE:
						shown = decimalKeyboard;
						break;
					
					case CONTINUE_ONLY:
						shown = decimalTopRowKeyboardOneButton;
						break;
					
					case CANCEL_CONTINUE:
						shown = decimalTopRowKeyboardTwoButtons;
						break;
				}

				break;

			case NUMERIC_PASSWORD:
			case NUMERIC:
				
				switch(topRowButtonsCode){
					
					default:
					case NONE:
						shown = numericKeyboard;
						break;
						
					case CONTINUE_ONLY:
						shown = numericTopRowKeyboardOneButton;
						break;
					
					case CANCEL_CONTINUE:
						shown = numericTopRowKeyboardTwoButtons;
						break;
				}
				
				break;

			case SPECIAL_CHARACTER:
				
				switch(topRowButtonsCode){
					
					default:
					case NONE:
						shown = specialCharacterKeyboard;
						break;
					
					case CONTINUE_ONLY:
						shown = specialCharacterTopRowKeyboardOneButton;
						break;
					
					case CANCEL_CONTINUE:
						shown = specialCharacterTopRowKeyboardTwoButtons;
						break;
				}
				
				break;

			case SPECIAL_CHARACTER_NEXT:
				
				switch(topRowButtonsCode){
					
					default:
					case NONE:
						shown = specialCharacterKeyboardNext;
						break;
					
					case CONTINUE_ONLY:
						shown = specialCharacterTopRowKeyboardNextOneButton;
						break;
					
					case CANCEL_CONTINUE:
						shown = specialCharacterTopRowKeyboardNextTwoButtons;
						break;
				}
				
				break;
			
			default:
				throw new IllegalStateException("Keyboard types are not properly updated.");
		}
		

		current = shown;
		
		return current;
		
	}
	
	private SanKeyboard selectAlphanumericKeyboard(TopRowButtonsOptions topRowButtonsCode){
		
		switch(topRowButtonsCode){
			
			default:
			case NONE:
				return alphaNumericKeyboard;
			
			case CONTINUE_ONLY:
				return alphaNumericTopRowKeyboardOneButton;
				
			case CANCEL_CONTINUE:
				return alphaNumericTopRowKeyboardTwoButtons;
				
		}
		
	}


	/**
	 * Nullable: may had not used any keyboard yet
	 *
	 * @return last keyboard shown
	 */
	@Nullable
	public SanKeyboard getCurrent() {
		return current;
	}

}
