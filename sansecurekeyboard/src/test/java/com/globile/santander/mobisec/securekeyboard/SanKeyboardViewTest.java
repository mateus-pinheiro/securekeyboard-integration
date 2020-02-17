package com.globile.santander.mobisec.securekeyboard;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.when;

import android.text.Editable;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.globile.santander.mobisec.securekeyboard.aosp.KeyboardAOSP;
import com.globile.santander.mobisec.securekeyboard.enums.ShiftMode;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboard;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardManager;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardType;
import com.globile.santander.mobisec.securekeyboard.listeners.SanEventCallbacks;
import com.globile.santander.mobisec.securekeyboard.listeners.SanTapJackedCallback;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
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
//@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PowerMockIgnore({"org.robolectric.*", "android.*"})
@PrepareForTest({SanKeyboardView.class, SelectLanguageDialog.class})
//@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Not necessary only for OnShift
public class SanKeyboardViewTest {

    // Robolectric > Mock Activity (mock context from real one for accesing resources)
    private ActivityController<AppCompatActivity> activityController;
    private AppCompatActivity activity;

    private SanKeyboardView sanKeyboardView;

    private SanKeyboard sanKeyboard;

    private SanKeyboardManager sanKeyboardManager;

    @Mock
    private MotionEvent motionEvent;

    @Mock
    private SanTapJackedCallback sanTapJackedCallback;

    @Mock
    private BaseSecureEditText secureEditText;

    @Mock
    private AlertDialog selectLanguageDialog;
    @Mock
    private ListView selectLanguageListOptionsView;

    @Mock
    private SanKeyboardView.SanKeyboardCallback sanKeyboardCallback;

    @Mock
    private InputConnection inputConnection;

    public SanKeyboardViewTest() {
    }

    @Before
    public void setupSanKeyboard() {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        setupMocks();

        SanKeyboardManager.setSanTapJackedCallback(sanTapJackedCallback);

        sanKeyboardView = SanKeyboardUtils.createKeyboardView(
                activity.getWindow().getDecorView().findViewById(android.R.id.content));

        sanKeyboardManager = new SanKeyboardManager(activity);

        sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

    }

    private void setupMocks() {

        activityController = Robolectric.buildActivity(AppCompatActivity.class);
        activity = activityController.get();

        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.ALPHA);
        when(secureEditText.getTopRowButtons()).thenReturn(TopRowButtonsOptions.NONE);
        when(secureEditText.getText()).thenReturn(Editable.Factory.getInstance().newEditable("abc "));
        when(secureEditText.onCreateInputConnection(any(EditorInfo.class))).thenReturn(inputConnection);

    }


    @Test(expected = InflateException.class)
    public void test_setTapJackedCallbackNull() {

        // Original exception is IllegalStateException because SanTapJackedCallback is null, but this exception
        // generates InflateException because Keyboard cannot be inflated

        ActivityController<AppCompatActivity> activityController = Robolectric.buildActivity(AppCompatActivity.class);
        AppCompatActivity activity = activityController.get();

        SanKeyboardManager.setSanTapJackedCallback(null);

        SanKeyboardView sanKeyboardView = SanKeyboardUtils.createKeyboardView(
                activity.getWindow().getDecorView().findViewById(android.R.id.content));

        SanKeyboardManager sanKeyboardManager = new SanKeyboardManager(activity);

        SanKeyboard sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.ALPHA, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

    }

    @Test
    public void test_OnKey_InputConnectioNull() {

        sanKeyboardView.onKey(119, new int[0]);

        verify(inputConnection, never()).commitText(eq("q"), eq(1));

    }

    @Test
    public void test001_setKeyboard_Success() {

        sanKeyboardView.setKeyboard(sanKeyboard);

        assertEquals(sanKeyboard, sanKeyboardView.getKeyboard());

    }

    @Test(expected = IllegalArgumentException.class)
    public void test002_setKeyboard_Fail() {

        sanKeyboardView.setKeyboard(mock(KeyboardAOSP.class));

    }

    @Test
    public void test003_onFilterTouchEvent() {

        boolean touchEventDispatched = sanKeyboardView.onFilterTouchEventForSecurity(motionEvent);

        verify(sanTapJackedCallback, times(0)).onObscuredTouchEvent(motionEvent);

        assertTrue(touchEventDispatched);

    }

    @Test
    public void test004_onFilterTouchEvent_WindowIsObscured() {

        when(motionEvent.getFlags()).thenReturn(MotionEvent.FLAG_WINDOW_IS_OBSCURED);

        boolean touchEventDispatched = sanKeyboardView.onFilterTouchEventForSecurity(motionEvent);

        verify(sanTapJackedCallback, times(1)).onObscuredTouchEvent(motionEvent);

        assertFalse(touchEventDispatched);
    }

    @Test
    public void test005_onTouchEvent_DeletePressed() {

        SanKeyboard spy = spy(sanKeyboard);
        sanKeyboardView.setKeyboard(spy);

        sanKeyboardView.onPress(KeyboardAOSP.KEYCODE_DELETE);

        verify(spy, times(1)).getKeyByCode(KeyboardAOSP.KEYCODE_DELETE);

        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);

        sanKeyboardView.onTouchEvent(motionEvent);

        verify(spy, times(2)).getKeyByCode(KeyboardAOSP.KEYCODE_DELETE);

    }

    @Test
    public void test006_onTouchEvent_SpacePressed() {

        SanKeyboard spy = spy(sanKeyboard);
        sanKeyboardView.setKeyboard(spy);

        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);
        Whitebox.setInternalState(sanKeyboardView, "isSpacePressed", true);

        sanKeyboardView.onTouchEvent(motionEvent);

        verify(spy, times(1)).getKeyByCode(SanKeyboard.KEYCODE_SPACE);

    }

    // TODO:
    @Test
    public void test007_onTouchEvent_SpacePressed_Long() throws Exception {

        SanKeyboardView spyView = spy(sanKeyboardView);
        spyView.setKeyboard(sanKeyboard);

        // TODO: Remove this mock. It's to suppress calling to create and show Language Dialog becuase
        // Robolectric crash test on AlerDialog.Builder.create() line
        when(spyView, "getActivity").thenReturn(null);

        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);
        Whitebox.setInternalState(spyView, "isSpacePressed", true);
        sanKeyboard.getKeyByCode(SanKeyboard.KEYCODE_SPACE).pressed = true;


        //   spyView.invalidateKey(0);     // TODO: Fix NPE on getPaddingLeft()
        Whitebox.setInternalState(spyView, "isSpacePressed", true);

        /*
        // TODO: Make Robolectric totally compatible with PowerMockito in order to mock static method
        // that creates the Select Language Dialog. Right now, seems compatible, but @PrepareForTest
        // annotation is not working or is ignored and "mockStatic()" doesn't work

        mockStatic(SelectLanguageDialog.class);     // Not working because @PrepareForTest is ignored ¿?
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(SelectLanguageDialog.class, "showSelectLanguageDialog");

        Keyboard.Key spaceKey = spy.getKeyByCode(SanKeyboard.KEYCODE_SPACE);
        Whitebox.invokeMethod(spyView, "startLanguageDialog", spaceKey);
        */

    }

    @Test
    public void test008_selectLanguageDialog_OnLanguageSelected() throws Exception {

        SanKeyboardView spyView = spy(sanKeyboardView);
        spyView.setKeyboard(sanKeyboard);

        when(selectLanguageDialog.getListView()).thenReturn(selectLanguageListOptionsView);
        when(selectLanguageListOptionsView.getCheckedItemPosition()).thenReturn(1);

        spyView.setInputConnection(secureEditText);

        Whitebox.invokeMethod(spyView, "onLanguageSelected", selectLanguageDialog);

        verifyPrivate(spyView, times(3)).invoke("changeModeTo", any());

    }

    @Test
    public void test009_selectLanguageDialog_OnDismiss() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);

        Whitebox.invokeMethod(sanKeyboardView, "undoInsertSpace");

        verify(secureEditText, times(2)).getText();
        verify(secureEditText, times(1)).setText("abc");

    }

    @Test
    public void test010_OnKey_Shift() {

        SanKeyboard sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);
        SanKeyboardView sanKeyboardView = SanKeyboardUtils.createKeyboardView(
                activity.getWindow().getDecorView().findViewById(android.R.id.content));
        sanKeyboardView.setKeyboard(sanKeyboard);

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(KeyboardAOSP.KEYCODE_SHIFT, new int[0]);

//        verifyPrivate(spyView).invoke("shiftTo", ShiftMode.class);

    }

    @Test
    public void test011_OnKey_ModeChange_Default() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);
        sanKeyboardView.slideIn();

        SanKeyboardView spyView = spy(sanKeyboardView);


        spyView.onKey(KeyboardAOSP.KEYCODE_MODE_CHANGE, new int[0]);

        verifyPrivate(spyView, times(1)).invoke("changeModeTo", any());

    }

    @Test
    public void test012_OnKey_ModeChange_SpecialChar() {

        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.SPECIAL_CHARACTER);

        sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

        assertEquals(sanKeyboardManager.getKeyboardType(), SanKeyboardType.SPECIAL_CHARACTER);

        Whitebox.setInternalState(sanKeyboardView, "keyboardsManager", sanKeyboardManager);

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(KeyboardAOSP.KEYCODE_MODE_CHANGE, new int[0]);

//        verifyPrivate(spyView, times(1)).invoke("changeModeTo", any());

        assertEquals(sanKeyboardManager.getKeyboardType(), SanKeyboardType.ALPHA);

    }

    @Test
    public void test013_OnKey_SpecialChange_Default() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_SPECIAL_CHANGE, new int[0]);

        verifyPrivate(spyView, times(1)).invoke("changeModeTo", any());

    }

    @Test
    public void test014_OnKey_SpecialChange_SpecialChars() {

        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.SPECIAL_CHARACTER);

        sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

        assertEquals(sanKeyboardManager.getKeyboardType(), SanKeyboardType.SPECIAL_CHARACTER);

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_SPECIAL_CHANGE, new int[0]);

        //  verifyPrivate(spyView, times(1)).invoke("changeModeTo", any());

        assertEquals(sanKeyboardView.getKeyboardType(), SanKeyboardType.SPECIAL_CHARACTER_NEXT);

    }

    @Test
    public void test015_OnKey_SpecialChange_SpecialCharsNext() {

        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.SPECIAL_CHARACTER_NEXT);

        sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER_NEXT, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

        assertEquals(sanKeyboardManager.getKeyboardType(), SanKeyboardType.SPECIAL_CHARACTER_NEXT);

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_SPECIAL_CHANGE, new int[0]);

        // verifyPrivate(spyView, times(1)).invoke("changeModeTo", any());

        assertEquals(sanKeyboardView.getKeyboardType(), SanKeyboardType.SPECIAL_CHARACTER);

    }

    @Test
    public void test016_OnKey_Cancel() {

        sanKeyboardView.setInputConnection(secureEditText);
        sanKeyboardView.setSanKeyboardCallback(sanKeyboardCallback);
        sanKeyboardView.slideIn();

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_CANCEL, new int[0]);

        //verifyPrivate(spyView, times(1)).invoke("hide");

        verify(sanKeyboardCallback, times(1)).onCancelClick();

    }

    @Test
    public void test017_OnKey_Continue() {

        sanKeyboardView.setInputConnection(secureEditText);
        sanKeyboardView.setSanKeyboardCallback(sanKeyboardCallback);
        sanKeyboardView.slideIn();

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_CONTINUE, new int[0]);

        //    verifyPrivate(spyView, times(1)).invoke("hide");

        verify(sanKeyboardCallback, times(1)).onContinueClick();

    }

    @Test
    public void test018_OnKey_Done() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_DONE, new int[0]);

        verifyPrivate(spyView, times(1)).invoke("hide");

    }

    @Test
    public void test019_OnKey_Delete() {

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_DELETE, new int[0]);

        verify(inputConnection, times(1)).deleteSurroundingText(1, 0);

    }

    @Test
    public void test020_OnKey_Delete_WithSelectedText() {

        when(inputConnection.getSelectedText(0)).thenReturn("abc");

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_DELETE, new int[0]);

        verify(inputConnection, times(1)).commitText("", 1);

    }

    @Test
    public void test021_OnKey_SecureKeyboard() {

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_SECURE_KEYBOARD, new int[0]);

        // This key has no action, so nothing to verify o assert

    }

    @Test
    public void test022_OnKey_DecimalPoint_AlphanumericKeyboard() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_DECIMAL_POINT, new int[0]);

        verifyPrivate(spyView, times(1)).invoke("processRegularChar", SanKeyboard.KEYCODE_DECIMAL_POINT);

    }

    @Test
    public void test023_OnKey_DecimalPoint_DecimalKeyboard() throws Exception {

        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.DECIMAL);
        when(secureEditText.getText()).thenReturn(Editable.Factory.getInstance().newEditable("123"));

        sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.DECIMAL, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_DECIMAL_POINT, new int[0]);

        verifyPrivate(spyView, times(1)).invoke("processRegularChar", SanKeyboard.KEYCODE_DECIMAL_POINT);

    }

    @Test
    public void test024_OnKey_DecimalPoint_DecimalKeyboard_AlreadyHasPoint() {

        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.DECIMAL);
        when(secureEditText.getText()).thenReturn(Editable.Factory.getInstance().newEditable("123.45"));

        sanKeyboard = sanKeyboardManager.getKeyboardForType(SanKeyboardType.DECIMAL, TopRowButtonsOptions.NONE);
        sanKeyboardView.setKeyboard(sanKeyboard);

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(SanKeyboard.KEYCODE_DECIMAL_POINT, new int[0]);

        verify(secureEditText, times(2)).getText();

  /*
        // TODO: Not working:

        // Wanted but not invoked:
        // sanKeyboardView.isShifted();
        // -> at com.globile.santander.mobisec.securekeyboard.SanKeyboardView.processRegularChar(SanKeyboardView.java:581)
        //
        // However, there were exactly 2 interactions with this mock:

        verifyPrivate(spyView).invoke("processRegularChar", SanKeyboard.KEYCODE_DECIMAL_POINT);
 */

    }

    @Test
    public void test025_OnKey_RegularKey() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);

        spyView.onKey(119, new int[0]);

        verifyPrivate(spyView, times(1)).invoke("processRegularChar", 119);

    }

    @Test
    public void test026_processRegularChar_UpperCaseShifted() throws Exception {

        sanKeyboardView.setInputConnection(secureEditText);
        sanKeyboardView.setShifted(true);

        sanKeyboard.setInitialShift(ShiftMode.UPPER_CASE_SINGLE);

        sanKeyboardView.setKeyboard(sanKeyboard);

        Whitebox.invokeMethod(sanKeyboardView, "processRegularChar", 119);

    }

    @Test
    public void test027_OnKeyActionEvents() {

        sanKeyboardView.onRelease(1);
        sanKeyboardView.onText("");
        sanKeyboardView.swipeLeft();
        sanKeyboardView.swipeRight();
        sanKeyboardView.swipeUp();
        sanKeyboardView.swipeDown();

        // These methods have no actions, so nothing to verify or to assert
        assertTrue(true);

    }

    @Test
    public void test028_slideIn() throws Exception {

        SanKeyboardView spyView = spy(sanKeyboardView);
        spyView.slideIn();

        verifyPrivate(spyView).invoke("startAnimation", any());

    }

    @Test
    public void test029_slideOut() throws Exception {

        SanKeyboardView spyView = spy(sanKeyboardView);
        spyView.slideOut();

        verifyPrivate(spyView).invoke("startAnimation", any());

    }

    @Test
    public void test030_onAnimationStart() {

        sanKeyboardView.onAnimationStart();

        assertTrue(sanKeyboardView.isAnimating());

    }

    @Test
    public void test031_onAnimationEnd() {

        sanKeyboardView.onAnimationEnd();

        assertFalse(sanKeyboardView.isAnimating());

    }

    @Test
    public void test031_onLongPress() {

        sanKeyboardView.setInputConnection(secureEditText);
        sanKeyboardView.setKeyboard(sanKeyboard);

        KeyboardAOSP.Key aKey = sanKeyboardView.getKeyboard().getKeyByCode(97);

        sanKeyboardView.onLongPress(aKey);

        assertTrue((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressDownDetected"));

    }

    @Test
    public void test032_setEventListener() {

        SanEventCallbacks sanEventCallbacks = mock(SanEventCallbacks.class);

        sanKeyboardView.setEventListener(sanEventCallbacks);

        assertEquals(sanEventCallbacks, sanKeyboardView.getEventListener());

    }

    @Test
    public void test033_onTouchEvent_LongPressOnA() {

        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);

        sanKeyboardView.setInputConnection(secureEditText);
        sanKeyboardView.setKeyboard(sanKeyboard);

        assertFalse((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressDownDetected"));
        assertFalse((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressUpDetected"));

        KeyboardAOSP.Key aKey = sanKeyboardView.getKeyboard().getKeyByCode(97);

        sanKeyboardView.onLongPress(aKey);

        assertTrue((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressDownDetected"));
        assertFalse((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressUpDetected"));

        sanKeyboardView.onTouchEvent(motionEvent);

        assertTrue((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressDownDetected"));
        assertTrue((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressUpDetected"));

        assertTrue(sanKeyboardView.onTouchEvent(motionEvent));

        assertFalse((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressDownDetected"));
        assertFalse((Boolean) Whitebox.getInternalState(sanKeyboardView, "isLongPressUpDetected"));

    }

}
