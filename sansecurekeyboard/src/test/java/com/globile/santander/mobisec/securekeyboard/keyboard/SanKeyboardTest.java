package com.globile.santander.mobisec.securekeyboard.keyboard;

import static junit.framework.TestCase.assertTrue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;

import com.globile.santander.mobisec.securekeyboard.R;
import com.globile.santander.mobisec.securekeyboard.aosp.KeyboardAOSP;
import com.globile.santander.mobisec.securekeyboard.enums.ShiftMode;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@Config(sdk = 28)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@PowerMockIgnore({"org.robolectric.*", "android.*"})

public class SanKeyboardTest {

    // Robolectric > Mock Activity (mock context from real one for accesing resources)
    private ActivityController<AppCompatActivity> activityController;
    private AppCompatActivity activity;

    private SanKeyboard sanKeyboard;

    public SanKeyboardTest() {
    }

    @Before
    public void setupSanKeyboard() {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        activityController = Robolectric.buildActivity(AppCompatActivity.class);
        activity = activityController.get();

    }

    // ALPHANUMERIC KEYBOARD
    // -----------------------

    @Test
    public void test_alphanumericKeyboard() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);

        assertEquals(TopRowButtonsOptions.NONE, sanKeyboard.getTopRowButtonsSelected());

    }

    @Test
    public void test_alphanumericKeyboard_TopRow_OneButton() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric_top_row_one_button);

        assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, sanKeyboard.getTopRowButtonsSelected());

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));

    }

    @Test
    public void test_alphanumericKeyboard_TopRow_TwoButtons() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric_top_row_two_buttons);

        assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, sanKeyboard.getTopRowButtonsSelected());

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertFalse(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    @Test
    public void test_alphanumericKeyboard_setShiftMode_Normal() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);

        sanKeyboard.setInitialShift(ShiftMode.LOWER_CASE);

        assertEquals(ShiftMode.LOWER_CASE, sanKeyboard.getInitialShift());

    }

    @Test
    public void test_alphanumericKeyboard_TopRow_OneButton_EnableCustomKeyData() {

        // NOTE: IsKeyEnabled for Cancel is always true becuase it doesn't exists on top row buttons (only Continue)

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric_top_row_one_button);

        assertTrue(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    @Test
    public void test_alphanumericKeyboard_TopRow_TwoButtons_EnableCustomKeyData() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric_top_row_two_buttons);

        assertTrue(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertFalse(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    // NUMERIC KEYBOARD
    // -----------------------

    @Test
    public void test_numericKeyboard() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric);

        assertEquals(TopRowButtonsOptions.NONE, sanKeyboard.getTopRowButtonsSelected());

    }


    @Test
    public void test_numericKeyboard_TopRow_OneButton() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric_top_row_one_button);

        assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, sanKeyboard.getTopRowButtonsSelected());

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));

    }

    @Test
    public void test_numericKeyboard_TopRow_TwoButtons() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric_top_row_two_buttons);

        assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, sanKeyboard.getTopRowButtonsSelected());

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertFalse(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    @Test
    public void test_numericKeyboard_TopRow_OneButton_EnableCustomKeyData() {

        // NOTE: IsKeyEnabled for Cancel is always true becuase it doesn't exists on top row buttons (only Continue)

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric_top_row_one_button);

        assertTrue(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    @Test
    public void test_numericKeyboard_TopRow_TwoButtons_EnableCustomKeyData() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric_top_row_two_buttons);

        assertTrue(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertFalse(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    // DECIMAL KEYBOARD
    // -----------------------

    @Test
    public void test_decimalKeyboard() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_decimal);

        assertEquals(TopRowButtonsOptions.NONE, sanKeyboard.getTopRowButtonsSelected());

    }


    @Test
    public void test_decimalKeyboard_TopRow_OneButton() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_decimal_top_row_one_button);

        assertEquals(TopRowButtonsOptions.CONTINUE_ONLY, sanKeyboard.getTopRowButtonsSelected());

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));

    }

    @Test
    public void test_decimalKeyboard_TopRow_TwoButtons() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_decimal_top_row_two_buttons);

        assertEquals(TopRowButtonsOptions.CANCEL_CONTINUE, sanKeyboard.getTopRowButtonsSelected());

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertFalse(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    @Test
    public void test_decimalKeyboard_TopRow_OneButton_EnableCustomKeyData() {

        // NOTE: IsKeyEnabled for Cancel is always true becuase it doesn't exists on top row buttons (only Continue)

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_decimal_top_row_one_button);

        assertTrue(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    @Test
    public void test_decimalKeyboard_TopRow_TwoButtons_EnableCustomKeyData() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_decimal_top_row_two_buttons);

        assertTrue(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertTrue(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

        sanKeyboard.enableDisableCustomKey(KeyboardAOSP.KEYCODE_CANCEL, false);

        assertFalse(sanKeyboard.isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));
        assertFalse(sanKeyboard.isKeyEnabled(KeyboardAOSP.KEYCODE_CANCEL));

    }

    // GENERIC
    // -----------------------

    @Test
    public void test_getKeyByCode_InvalidCode() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);

        KeyboardAOSP.Key key = sanKeyboard.getKeyByCode(Integer.MIN_VALUE);

        assertNull(key);

    }

    @Test
    public void test_getKeyByCode_ValidCode() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);

        KeyboardAOSP.Key key = sanKeyboard.getKeyByCode(119); // q

        assertEquals(key.codes[0], 119);

    }

    @Test
    public void test_getNearestKeys() {

        float keyHeight = activity.getResources().getDimension(R.dimen.keyboard_alpha_key_height);

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);

        int[] keys = sanKeyboard.getNearestKeys(0, 0); // q

        assertEquals(0, keys[0]);

        keys = sanKeyboard.getNearestKeys(0, (int) keyHeight); // a

        assertEquals(10, keys[0]);

        keys = sanKeyboard.getNearestKeys(0, (int) keyHeight * 2); // z

        assertEquals(19, keys[0]);

        keys = sanKeyboard.getNearestKeys(-1, -1); // not valid position

        assertEquals(keys.length, 0);

    }

    @Test
    public void test_constructor2() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric, 0);

        assertEquals(TopRowButtonsOptions.NONE, sanKeyboard.getTopRowButtonsSelected());

    }

    @Test
    public void test_constructor3() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric, 0, 100, 100);

        assertEquals(TopRowButtonsOptions.NONE, sanKeyboard.getTopRowButtonsSelected());

    }

    @Test
    public void test_constructor4() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric,
                "qwertyuiopasdfghjklzxcvbnm", 20, 8);

        assertEquals(TopRowButtonsOptions.NONE, sanKeyboard.getTopRowButtonsSelected());

    }

    @Test
    public void test_isKeyEnabled_InvalidKey() {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_numeric);

        assertTrue(sanKeyboard.isKeyEnabled(Integer.MIN_VALUE));

    }

    @Test
    public void test_generateCustomKeyDrawable_InvalidKey() throws Exception {

        sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);

        Drawable drawable = Whitebox.invokeMethod(sanKeyboard, "generateCustomKeyDrawable",
                sanKeyboard.getKeyByCode(119), true);

        assertNull(drawable);

    }
}
