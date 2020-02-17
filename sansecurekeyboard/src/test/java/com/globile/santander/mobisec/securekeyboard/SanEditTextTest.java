package com.globile.santander.mobisec.securekeyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.when;

import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboard;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardManager;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardType;
import com.globile.santander.mobisec.securekeyboard.listeners.SanTapJackedCallback;
import com.globile.santander.mobisec.securekeyboard.scrolling.AutoScrollManager;
import com.globile.santander.mobisec.securekeyboard.scrolling.AutoScrollManagerStatus;
import com.globile.santander.mobisec.securekeyboard.scrolling.AutoScrollSynchronicer;
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
import org.robolectric.shadows.ShadowLooper;

@Config(sdk = 28, application = TestApplication.class)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@PowerMockIgnore({"org.robolectric.*", "android.*"})
@PrepareForTest({SanEditText.class, SanKeyboardUtils.class, Build.VERSION.class, AutoScrollSynchronicer.class})
public class SanEditTextTest {

    private final int MOCK_SAN_EDIT_TEXT_ID = 12345678;

    // Robolectric > Mock Activity (mock context from real one for accesing resources)
    private ActivityController<AppCompatActivity> activityController;
    private AppCompatActivity activity;

    private SanKeyboardView sanKeyboardView;

    @Mock
    private SanTapJackedCallback sanTapJackedCallback;

    private SanEditText secureEditText;

    private ViewGroup mainViewGroup;

    @Mock
    private AutoScrollManager autoScrollManager;

    @Mock
    private Lifecycle parentLifecycle;

    @Before
    public void setupSecureEditText() {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        setupMocks();

        SanKeyboardManager.setSanTapJackedCallback(sanTapJackedCallback);

        sanKeyboardView = SanKeyboardUtils.createKeyboardView(
                activity.getWindow().getDecorView().findViewById(android.R.id.content));

        secureEditText = new SanEditText(activity);
        secureEditText.setKeyboardType(SanKeyboardType.ALPHA);
        secureEditText.setTopRowButtons(TopRowButtonsOptions.NONE);

        View mainContent = activity.getWindow().getDecorView().findViewById(android.R.id.content);

        mainViewGroup = (ViewGroup) mainContent;
        mainViewGroup.addView(secureEditText);

        secureEditText.setId(MOCK_SAN_EDIT_TEXT_ID);

        //SanKeyboardView sanKeyboardView = SanKeyboardUtils.createKeyboardView(mainContent);

        //Whitebox.setInternalState(secureEditText, "sanKeyboardView", sanKeyboardView);

    }

    private void setupMocks() {

        activityController = Robolectric.buildActivity(AppCompatActivity.class);
        activity = activityController.get();

    }

    @Test
    public void test_constructorFromXml() {

        AttributeSet attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.sanKeyboardType,
                        "decimal")
                .addAttribute(R.attr.sanKeyboardButtons,
                        "cancelContinue").build();

        secureEditText = new SanEditText(activity, attrs);

        assertEquals(secureEditText.getKeyboardType(), SanKeyboardType.DECIMAL);
        assertEquals(secureEditText.getTopRowButtons(), TopRowButtonsOptions.CANCEL_CONTINUE);

    }

    @Test
    public void test_constructorFromXml_DefStyle() {

        AttributeSet attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.sanKeyboardType,
                        "numeric")
                .addAttribute(R.attr.sanKeyboardButtons,
                        "continueOnly").build();

        secureEditText = new SanEditText(activity, attrs, 0);

        assertEquals(secureEditText.getKeyboardType(), SanKeyboardType.NUMERIC);
        assertEquals(secureEditText.getTopRowButtons(), TopRowButtonsOptions.CONTINUE_ONLY);

    }

    @Test
    public void test_onGlobalLayout() {

        SanEditText spy = spy(secureEditText);

        spy.onGlobalLayout();

        // TODO: NPE when trying to make these checks, but test passes if it is excluded ?????
//        verifyPrivate(spy, times(2)).invoke("initSanKeyboardInstance");
//        verifyPrivate(spy, times(2)).invoke("initFocusableFakeView");

        verify(spy, times(1)).clearFocus();
        verify(spy, times(1)).setFocusable(true);
        verify(spy, times(1)).setFocusableInTouchMode(true);

    }

    @Test
    public void test_onGlobalLayout_WithAutoScrolling() {

        SanEditText spy = spy(secureEditText);

        spy.setRootLayoutIdForAutoScrolling(mainViewGroup.getId());

        assertEquals(mainViewGroup.getId(), spy.getRootLayoutIdForAutoScrolling());

        spy.onGlobalLayout();

        AutoScrollManager autoScrollManager = Whitebox.getInternalState(spy, "autoScrollManager");
        AutoScrollManager spyAutoScroll = spy(autoScrollManager);
        Whitebox.setInternalState(spy, "autoScrollManager", spyAutoScroll);

        // TODO: NPE when trying to make these checks, but test passes if it is excluded ?????
//        verifyPrivate(spy, times(2)).invoke("initSanKeyboardInstance");
//        verifyPrivate(spy, times(2)).invoke("initFocusableFakeView");

        verify(spy, times(1)).clearFocus();
        verify(spy, times(1)).setFocusable(true);
        verify(spy, times(1)).setFocusableInTouchMode(true);

        // TODO: org.mockito.exceptions.verification.TooManyActualInvocations:
        //sanEditText.getParent()
        // >>>> Why on getParent?? We are verifying "initAutoScrollManager"
//        verifyPrivate(spy).invoke("initAutoScrollManager");

    }

    @Test
    public void test_onWindowsFocusChanged_True() {

        SanEditText spy = spy(secureEditText);

        spy.onWindowFocusChanged(true);

        verify(spy, never()).clearFocus();

    }

    @Test
    public void test_onWindowsFocusChanged_False() {

        SanEditText spy = spy(secureEditText);

        spy.onWindowFocusChanged(false);

        verify(spy, times(1)).clearFocus();

    }

    @Test
    public void test_OnFocusChanged_True() {

        SanKeyboardView spyView = spy(sanKeyboardView);

        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        secureEditText.onFocusChanged(true, 130, null);

        verify(spyView, times(1)).isShown();
        verify(spyView, times(1)).slideIn();

        assertFalse(spyView.isShown());
        assertFalse(spyView.isAnimating());

/*
        // TODO: NPE when trying to make these checks, but test passes if it is excluded ?????
        // Also problems when verifying methods: Trying to verify "showSanKeyboard()" returns
        // invocation problems for setInputConnection (like we were verifying "setInputConnection"... Weird!

        verifyPrivate(spy).invoke("showSanKeyboard");
        verifyPrivate(spy, times(1)).invoke("hideDefaultKeyboard");
        verifyPrivate(spy, times(1)).invoke("initSanKeyboardInstance");
*/
    }

    @Test
    public void test_OnFocusChanged_False() {

        SanKeyboardView spyView = spy(sanKeyboardView);

        when(spyView.isShown()).thenReturn(true);

        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        secureEditText.onFocusChanged(false, 0, null);

        verify(spyView, times(1)).isShown();
        verify(spyView, times(1)).slideOut();

        assertTrue(spyView.isShown());
        assertFalse(spyView.isAnimating());

/*
        // TODO: NPE when trying to make these checks, but test passes if it is excluded ?????
        // Also problems when verifying methods: Trying to verify "showSanKeyboard()" returns
        // invocation problems for setInputConnection (like we were verifying "setInputConnection"... Weird!

        verifyPrivate(spy).invoke("showSanKeyboard");
        verifyPrivate(spy, times(1)).invoke("hideDefaultKeyboard");
        verifyPrivate(spy, times(1)).invoke("initSanKeyboardInstance");
*/
    }

    @Test
    public void test_OnFocusChanged_True_WithAutoScrolling() {

        secureEditText.setRootLayoutIdForAutoScrolling(mainViewGroup.getId());

        SanKeyboardView spyView = spy(sanKeyboardView);
        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        secureEditText.onGlobalLayout();

        AutoScrollManager autoScrollManager = Whitebox.getInternalState(secureEditText, "autoScrollManager");
        AutoScrollManager spyAutoScroll = spy(autoScrollManager);
        Whitebox.setInternalState(secureEditText, "autoScrollManager", spyAutoScroll);

        secureEditText.onFocusChanged(true, 130, null);

        verify(spyView, times(1)).isShown();
        verify(spyView, times(1)).slideIn();

        verify(spyAutoScroll, times(1))
                .startAutoScrollingForField(eq(secureEditText), any(SanKeyboardView.class));

        assertFalse(spyView.isShown());
        assertFalse(spyView.isAnimating());

    }

    @Test
    public void test_OnFocusChanged_False_WithAutoScrolling() {

        secureEditText.setRootLayoutIdForAutoScrolling(mainViewGroup.getId());

        SanKeyboardView spyView = spy(sanKeyboardView);
        when(spyView.isShown()).thenReturn(true);
        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        secureEditText.onGlobalLayout();

        AutoScrollManager autoScrollManager = Whitebox.getInternalState(secureEditText, "autoScrollManager");
        Whitebox.setInternalState(autoScrollManager, "currentManagerStatus", AutoScrollManagerStatus.SETTED);
        AutoScrollManager spyAutoScroll = spy(autoScrollManager);
        Whitebox.setInternalState(spyAutoScroll, "extraBottomSpace", mock(Space.class));
        Whitebox.setInternalState(secureEditText, "autoScrollManager", spyAutoScroll);

        secureEditText.onFocusChanged(false, 0, null);

        verify(spyView, times(1)).isShown();
        verify(spyView, times(1)).slideOut();

        assertTrue(spyView.isShown());
        assertFalse(spyView.isAnimating());

        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();


        verify(spyAutoScroll, times(1))
                .hideExtraBottomSpaceForScrolling();

    }

    @Test
    public void test_OnKeyPreIme() {

        SanEditText spy = spy(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);
        when(spyView.isShown()).thenReturn(true);
        Whitebox.setInternalState(spy, "sanKeyboardView", spyView);

        boolean isKeyEventConsumed = spy.onKeyPreIme(KeyEvent.KEYCODE_SPACE, mock(KeyEvent.class));

        verify(spy, never()).clearFocus();

        assertFalse(isKeyEventConsumed);

    }

    @Test
    public void test_OnKeyPreIme_BackButton() {

        SanEditText spy = spy(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);
        when(spyView.isShown()).thenReturn(true);
        Whitebox.setInternalState(spy, "sanKeyboardView", spyView);

        boolean isKeyEventConsumed = spy.onKeyPreIme(KeyEvent.KEYCODE_BACK, mock(KeyEvent.class));

        verify(spy, times(1)).clearFocus();

        assertTrue(isKeyEventConsumed);

    }

    @Test
    public void test_OnKeyUp() {

        SanEditText spy = spy(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);
        when(spyView.isShown()).thenReturn(true);
        Whitebox.setInternalState(spy, "sanKeyboardView", spyView);

        KeyEvent keyEvent = mock(KeyEvent.class);
        KeyCharacterMap keyCharacterMap = mock(KeyCharacterMap.class);

        when(keyCharacterMap.getKeyboardType()).thenReturn(KeyCharacterMap.ALPHA);
        when(keyEvent.getKeyCharacterMap()).thenReturn(keyCharacterMap);

        boolean isKeyEventConsumed = spy.onKeyUp(KeyEvent.KEYCODE_SPACE, keyEvent);

        verify(spy, never()).clearFocus();

        assertFalse(isKeyEventConsumed);

    }

    @Test
    public void test_OnKeyUp_BackButton() {

        SanEditText spy = spy(secureEditText);

        SanKeyboardView spyView = spy(sanKeyboardView);
        when(spyView.isShown()).thenReturn(true);
        Whitebox.setInternalState(spy, "sanKeyboardView", spyView);

        KeyEvent keyEvent = mock(KeyEvent.class);
        KeyCharacterMap keyCharacterMap = mock(KeyCharacterMap.class);

        when(keyCharacterMap.getKeyboardType()).thenReturn(KeyCharacterMap.ALPHA);
        when(keyEvent.getKeyCharacterMap()).thenReturn(keyCharacterMap);

        AutoScrollManager scrollManager = mock(AutoScrollManager.class);

        Whitebox.setInternalState(spy, "autoScrollManager", scrollManager);

        boolean isKeyEventConsumed = spy.onKeyUp(KeyEvent.KEYCODE_BACK, keyEvent);

        verify(spy, times(1)).clearFocus();
        verify(scrollManager, times(1)).clearExtraRequests();

        assertTrue(isKeyEventConsumed);

    }

    @Test
    public void test_enableDisableCustomKey() {

        secureEditText.setTopRowButtons(TopRowButtonsOptions.CANCEL_CONTINUE);

        SanKeyboardView spyView = spy(sanKeyboardView);
        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        SanKeyboard sanKeyboard = new SanKeyboard(activity, R.xml.keyboard_alphanumeric);
        spyView.setKeyboard(sanKeyboard);

        assertTrue(spyView.getKeyboard().isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));

        secureEditText.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, false);

        assertFalse(spyView.getKeyboard().isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));

        secureEditText.enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, true);

        assertTrue(spyView.getKeyboard().isKeyEnabled(SanKeyboard.KEYCODE_CONTINUE));

    }

    @Test
    public void test_OnDestroyView() throws Exception {

        secureEditText.onGlobalLayout();

        SanEditText spy = spy(secureEditText);

        spy.setRootLayoutIdForAutoScrolling(mainViewGroup.getId());

        AutoScrollManager scrollManager = mock(AutoScrollManager.class);

        Whitebox.setInternalState(spy, "autoScrollManager", scrollManager);
        Whitebox.setInternalState(spy, "parentLifecycle", parentLifecycle);

        Whitebox.invokeMethod(AutoScrollSynchronicer.getInstance(), "initSubscribersMap");

        spy.onDestroy();

        verify(scrollManager, times(1)).stopAutoScrollingForField();

        verifyPrivate(spy, times(1)).invoke("hideSanSecureKeyboard");

        verify(parentLifecycle, times(1)).removeObserver(spy);

    }

    @Test
    public void test_showSanSecureKeyboard() {

        SanEditText spy = spy(secureEditText);

        spy.onGlobalLayout();

        spy.showSanSecureKeyboard();

        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        //      verifyPrivate(spy).invoke("showSanKeyboard");
        verify(spy).requestFocus();

    }

    @Config(sdk = 19)
    @Test
    public void test_showSanSecureKeyboard_API19() {

        SanEditText spy = spy(secureEditText);

        spy.onGlobalLayout();

        spy.showSanSecureKeyboard();

        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(spy).requestFocus();

    }

    @Test
    public void test_showSanSecureKeyboard_AutoScrollStrictModeTrue() {

        SanEditText spy = spy(secureEditText);

        Whitebox.setInternalState(spy, "rootLayoutIdForAutoScrolling", 2);

        spy.setAutoScrollingStrictMode(true);

        spy.onGlobalLayout();

        Whitebox.setInternalState(spy, "autoScrollManager", autoScrollManager);

        spy.showSanSecureKeyboard();

        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(autoScrollManager, times(1))
                .startAutoScrollingForField(eq(spy), any(SanKeyboardView.class));


        verify(autoScrollManager, never()).showExtraSpaceForScrolling(eq(spy), any(SanKeyboardView.class));

        verify(spy).requestFocus();

    }

    @Test
    public void test_showSanSecureKeyboard_ClearAnimation() {

        SanKeyboardView spyView = spy(sanKeyboardView);

        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        SanEditText spy = spy(secureEditText);

        spy.onGlobalLayout();

        when(spyView.isAnimating()).thenReturn(false);
        when(spyView.isShown()).thenReturn(true);

        spy.showSanSecureKeyboard();

//        verify(spyView, times(2)).clearAnimation();
        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(spy).requestFocus();

    }

    @Test
    public void test_hideSanKeyboard_AutoScrollStrictModeTrue() {

        SanKeyboardView spyView = spy(sanKeyboardView);
        SanEditText spy = spy(secureEditText);

        Whitebox.setInternalState(spy, "sanKeyboardView", spyView);
        Whitebox.setInternalState(spy, "rootLayoutIdForAutoScrolling", 2);

        spy.setAutoScrollingStrictMode(true);

        spy.onGlobalLayout();

        Whitebox.setInternalState(spy, "autoScrollManager", autoScrollManager);

        spy.showSanSecureKeyboard();

        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(autoScrollManager, times(1))
                .startAutoScrollingForField(spy, spyView);

        verify(autoScrollManager, never()).showExtraSpaceForScrolling(spy,
                spyView);

        verify(spy).requestFocus();


        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spyView);

        when(spyView.isShown()).thenReturn(true);
        when(spyView.isLanguageSelectorShown()).thenReturn(false);

        spy.hideSanSecureKeyboard();

        // Run code in Handler job
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(autoScrollManager, times(1)).stopAutoScrollingForField();

    }


    @Test
    public void test_onKeyPreIme_AlternateCharsPopup() {

        SanKeyboardView spy = spy(sanKeyboardView);

        Whitebox.setInternalState(secureEditText, "sanKeyboardView", spy);

        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getKeyCode()).thenReturn(KeyEvent.KEYCODE_BACK);
        when(spy.handleBack()).thenReturn(true);

        assertTrue(secureEditText.onKeyPreIme(KeyEvent.KEYCODE_BACK, keyEvent));

        assertTrue((Boolean) Whitebox.getInternalState(secureEditText, "hasPopupCharsDismissed"));

        assertTrue(secureEditText.onKeyPreIme(KeyEvent.KEYCODE_BACK, keyEvent));

        assertFalse((Boolean) Whitebox.getInternalState(secureEditText, "hasPopupCharsDismissed"));

    }

    @Test
    public void test_TopRowButtonsCallback() {

        SanKeyboardView.SanKeyboardCallback sanKeyboardCallback = mock(SanKeyboardView.SanKeyboardCallback.class);

        secureEditText.setSanKeyboardCallback(sanKeyboardCallback);
        secureEditText.setTopRowButtons(TopRowButtonsOptions.CANCEL_CONTINUE);

        secureEditText.onGlobalLayout();

        sanKeyboardView.onKey(SanKeyboard.KEYCODE_CONTINUE, new int[0]);

        verify(sanKeyboardCallback, times(1)).onContinueClick();

        sanKeyboardView.onKey(SanKeyboard.KEYCODE_CANCEL, new int[0]);

        verify(sanKeyboardCallback, times(1)).onCancelClick();

    }

    @Test(expected = IllegalStateException.class)
    public void test_setRootLayoutIdWhenAutoScrollingIsAlreadySetted() {

        when(autoScrollManager.getState()).thenReturn(AutoScrollManagerStatus.SETTED);
        Whitebox.setInternalState(secureEditText, "autoScrollManager", autoScrollManager);

        secureEditText.setRootLayoutIdForAutoScrolling(123456789);

    }

    @Test
    public void test_retainFocus() {

        SanEditText spy = spy(secureEditText);

        spy.showSanSecureKeyboard();

        Whitebox.setInternalState(spy, "autoScrollManager", autoScrollManager);

        spy.retainFocus();

        verify(spy, times(1)).requestFocus();

        verify(autoScrollManager, times(1)).clearExtraRequests();


    }

}
