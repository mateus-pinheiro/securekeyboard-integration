package com.globile.santander.mobisec.securekeyboard.keyboard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.aosp.KeyboardAOSP;
import com.globile.santander.mobisec.securekeyboard.enums.InputLanguage;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.listeners.SanTapJackedCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Locale;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SanKeyboardManager.class})
public class SanKeyboardManagerTest {

	private final String MOCK_STRING = "MOCK_STRING";

	private final Locale MOCK_LOCALE_EN = new Locale("en", "GB");
	private final Locale MOCK_LOCALE_ES = new Locale("es", "ES");

	@Mock
	private Context context;

	@Mock
	private SanKeyboard alphaNumericKeyboard;
	@Mock
	private SanKeyboard alphaNumericTopRowKeyboardOneButton;
	@Mock
	private SanKeyboard alphaNumericTopRowKeyboardTwoButtons;

	@Mock
	private SanKeyboard decimalKeyboard;
	@Mock
	private SanKeyboard decimalTopRowKeyboardOneButton;
	@Mock
	private SanKeyboard decimalTopRowKeyboardTwoButtons;

	@Mock
	private SanKeyboard numericKeyboard;
	@Mock
	private SanKeyboard numericTopRowKeyboardOneButton;
	@Mock
	private SanKeyboard numericTopRowKeyboardTwoButtons;

	@Mock
	private SanKeyboard specialCharacterKeyboard;
	@Mock
	private SanKeyboard specialCharacterTopRowKeyboardOneButton;
	@Mock
	private SanKeyboard specialCharacterTopRowKeyboardTwoButtons;

	@Mock
	private SanKeyboard specialCharacterKeyboardNext;
	@Mock
	private SanKeyboard specialCharacterTopRowKeyboardNextOneButton;
	@Mock
	private SanKeyboard specialCharacterTopRowKeyboardNextTwoButtons;

	@Mock
	private SanTapJackedCallback sanTappedJackCallback;

	private SanKeyboardManager sanKeyboardManager;
	
	public SanKeyboardManagerTest() {}
	
	@Before
	public void setupSanKeyboard() throws Exception {
		
		// Init Mockito
		MockitoAnnotations.initMocks(this);
		
		setupMocks();

		sanKeyboardManager = new SanKeyboardManager(context);
	
	}

	private void setupMocks() throws Exception {

		when(context.getApplicationContext()).thenReturn(context);
		when(context.getResources()).thenReturn(mock(Resources.class));
		when(context.getResources().getConfiguration()).thenReturn(mock(Configuration.class));

		context.getResources().getConfiguration().locale = MOCK_LOCALE_EN;

		when(context.getString(anyInt())).thenReturn(MOCK_STRING);

        whenNew(KeyboardAOSP.class).withArguments(any(), eq(R.xml.keyboard_alphanumeric), anyInt())
                .thenReturn(alphaNumericKeyboard);

		setupKeyboardMocks();

    }


	private void setupKeyboardMocks() throws Exception {

		// ALPHANUMERIC

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_alphanumeric))
				.thenReturn(alphaNumericKeyboard);
		when(alphaNumericKeyboard.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.NONE);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_alphanumeric_top_row_one_button))
				.thenReturn(alphaNumericTopRowKeyboardOneButton);
		when(alphaNumericTopRowKeyboardOneButton.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CONTINUE_ONLY);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_alphanumeric_top_row_two_buttons))
				.thenReturn(alphaNumericTopRowKeyboardTwoButtons);
		when(alphaNumericTopRowKeyboardTwoButtons.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CANCEL_CONTINUE);


		// SPECIAL CHAR

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_specialcharacters))
				.thenReturn(specialCharacterKeyboard);
		when(specialCharacterKeyboard.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.NONE);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_specialcharacters_top_row_one_button))
				.thenReturn(specialCharacterTopRowKeyboardOneButton);
		when(specialCharacterTopRowKeyboardOneButton.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CONTINUE_ONLY);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_specialcharacters_top_row_two_buttons))
				.thenReturn(specialCharacterTopRowKeyboardTwoButtons);
		when(specialCharacterTopRowKeyboardTwoButtons.getTopRowButtonsSelected()).thenReturn(
				TopRowButtonsOptions.CANCEL_CONTINUE);


		// SPECIAL CHAR NEXT

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_specialcharacters_next))
				.thenReturn(specialCharacterKeyboardNext);
		when(specialCharacterKeyboardNext.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.NONE);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_specialcharacters_next_top_row_one_button))
				.thenReturn(specialCharacterTopRowKeyboardNextOneButton);
		when(specialCharacterTopRowKeyboardNextOneButton.getTopRowButtonsSelected()).thenReturn(
				TopRowButtonsOptions.CONTINUE_ONLY);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_specialcharacters_next_top_row_two_buttons))
				.thenReturn(specialCharacterTopRowKeyboardNextTwoButtons);
		when(specialCharacterTopRowKeyboardNextTwoButtons.getTopRowButtonsSelected()).thenReturn(
				TopRowButtonsOptions.CANCEL_CONTINUE);


		// DECIMAL

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_decimal))
				.thenReturn(alphaNumericKeyboard);
		when(decimalKeyboard.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.NONE);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_decimal_top_row_one_button))
				.thenReturn(alphaNumericTopRowKeyboardOneButton);
		when(decimalTopRowKeyboardOneButton.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CONTINUE_ONLY);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_decimal_top_row_two_buttons))
				.thenReturn(alphaNumericTopRowKeyboardTwoButtons);
		when(decimalTopRowKeyboardTwoButtons.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CANCEL_CONTINUE);


		// NUMERIC

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_numeric))
				.thenReturn(numericKeyboard);
		when(numericKeyboard.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.NONE);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_numeric_top_row_one_button))
				.thenReturn(numericTopRowKeyboardOneButton);
		when(numericTopRowKeyboardOneButton.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CONTINUE_ONLY);

		whenNew(SanKeyboard.class).withArguments(any(), eq(R.xml.keyboard_numeric_top_row_two_buttons))
				.thenReturn(numericTopRowKeyboardTwoButtons);
		when(numericTopRowKeyboardTwoButtons.getTopRowButtonsSelected()).thenReturn(TopRowButtonsOptions.CANCEL_CONTINUE);

	}


	@Test
	public void test_getCurrentLanguage_English() {

		assertEquals(sanKeyboardManager.getCurrentLanguage().getLocale(), MOCK_LOCALE_EN);
		assertEquals(sanKeyboardManager.getCurrentLanguage().getLanguage(), MOCK_LOCALE_EN.getLanguage());
		assertEquals(sanKeyboardManager.getCurrentLanguage().getCountry(), MOCK_LOCALE_EN.getCountry());
	}

	@Test
	public void test_getCurrentLanguage_Spanish() {

		sanKeyboardManager.updateKeyboards(InputLanguage.SPANISH_ES);

		assertEquals(sanKeyboardManager.getCurrentLanguage().getLocale(), MOCK_LOCALE_ES);
		assertEquals(sanKeyboardManager.getCurrentLanguage().getLanguage(), MOCK_LOCALE_ES.getLanguage());
		assertEquals(sanKeyboardManager.getCurrentLanguage().getCountry(), MOCK_LOCALE_ES.getCountry());
	}

	@Test
	public void test_getKeyboardForType_Alphanumeric() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA, TopRowButtonsOptions.NONE);

		assertEquals(keyboard, alphaNumericKeyboard);
		assertEquals(SanKeyboardType.ALPHA, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Alphanumeric_TopRow_OneButton() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA, TopRowButtonsOptions.CONTINUE_ONLY);

		assertEquals(keyboard, alphaNumericTopRowKeyboardOneButton);
		assertEquals(SanKeyboardType.ALPHA, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Alphanumeric_TopRow_TwoButton() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA,
				TopRowButtonsOptions.CANCEL_CONTINUE);

		assertEquals(keyboard, alphaNumericTopRowKeyboardTwoButtons);
		assertEquals(SanKeyboardType.ALPHA, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_AlphanumericUpper() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA_UPPER, TopRowButtonsOptions.NONE);

		assertEquals(keyboard, alphaNumericKeyboard);
		assertEquals(SanKeyboardType.ALPHA_UPPER, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_AlphanumericUpperPerm() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA_UPPER_PERM,
				TopRowButtonsOptions.NONE);

		assertEquals(keyboard, alphaNumericKeyboard);
		assertEquals(SanKeyboardType.ALPHA_UPPER_PERM, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_SpecialChars() {

		SanKeyboard keyboard = sanKeyboardManager
				.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, TopRowButtonsOptions.NONE);

		assertEquals(keyboard, specialCharacterKeyboard);
		assertEquals(SanKeyboardType.SPECIAL_CHARACTER, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_SpecialChars_TopRow_OneButton() {

		SanKeyboard keyboard = sanKeyboardManager
				.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, TopRowButtonsOptions.CONTINUE_ONLY);

		assertEquals(keyboard, specialCharacterTopRowKeyboardOneButton);
		assertEquals(SanKeyboardType.SPECIAL_CHARACTER, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_SpecialChars_TopRow_TwoButton() {

		SanKeyboard keyboard = sanKeyboardManager
				.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, TopRowButtonsOptions.CANCEL_CONTINUE);

		assertEquals(keyboard, specialCharacterTopRowKeyboardTwoButtons);
		assertEquals(SanKeyboardType.SPECIAL_CHARACTER, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_SpecialCharsNext() {

		SanKeyboard keyboard = sanKeyboardManager
				.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER_NEXT, TopRowButtonsOptions.NONE);

		assertEquals(keyboard, specialCharacterKeyboardNext);
		assertEquals(SanKeyboardType.SPECIAL_CHARACTER_NEXT, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_SpecialCharsNext_TopRow_OneButton() {

		SanKeyboard keyboard = sanKeyboardManager
				.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER_NEXT, TopRowButtonsOptions.CONTINUE_ONLY);

		assertEquals(keyboard, specialCharacterTopRowKeyboardNextOneButton);
		assertEquals(SanKeyboardType.SPECIAL_CHARACTER_NEXT, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_SpecialCharsNext_TopRow_TwoButton() {

		SanKeyboard keyboard = sanKeyboardManager
				.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER_NEXT, TopRowButtonsOptions.CANCEL_CONTINUE);

		assertEquals(keyboard, specialCharacterTopRowKeyboardNextTwoButtons);
		assertEquals(SanKeyboardType.SPECIAL_CHARACTER_NEXT, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Decimal() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.DECIMAL, TopRowButtonsOptions.NONE);

		assertEquals(keyboard, alphaNumericKeyboard);
		assertEquals(SanKeyboardType.DECIMAL, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Decimal_TopRow_OneButton() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.DECIMAL,
				TopRowButtonsOptions.CONTINUE_ONLY);

		assertEquals(keyboard, alphaNumericTopRowKeyboardOneButton);
		assertEquals(SanKeyboardType.DECIMAL, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Decimal_TopRow_TwoButton() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.DECIMAL,
				TopRowButtonsOptions.CANCEL_CONTINUE);

		assertEquals(keyboard, alphaNumericTopRowKeyboardTwoButtons);
		assertEquals(SanKeyboardType.DECIMAL, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Numeric() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.NUMERIC, TopRowButtonsOptions.NONE);

		assertEquals(keyboard, numericKeyboard);
		assertEquals(SanKeyboardType.NUMERIC, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.NONE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Numeric_TopRow_OneButton() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.NUMERIC,
				TopRowButtonsOptions.CONTINUE_ONLY);

		assertEquals(keyboard, numericTopRowKeyboardOneButton);
		assertEquals(SanKeyboardType.NUMERIC, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_getKeyboardForType_Numeric_TopRow_TwoButton() {

		SanKeyboard keyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.NUMERIC,
				TopRowButtonsOptions.CANCEL_CONTINUE);

		assertEquals(keyboard, numericTopRowKeyboardTwoButtons);
		assertEquals(SanKeyboardType.NUMERIC, sanKeyboardManager.getKeyboardType());
		assertEquals(keyboard, sanKeyboardManager.getCurrent());
		assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, keyboard.getTopRowButtonsSelected());

	}

	@Test
	public void test_setSanTappedJackCallback() {

		SanKeyboardManager.setSanTapJackedCallback(sanTappedJackCallback);

		assertEquals(SanKeyboardManager.getSanTapJackedCallback(), sanTappedJackCallback);

	}

	@Test
	public void test_setPossibleLanguages() {

		SanKeyboardManager.setPossibleLanguages(InputLanguage.defaultLanguages);

		assertArrayEquals(InputLanguage.defaultLanguages, SanKeyboardManager.getPossibleLanguages());
	}

}
