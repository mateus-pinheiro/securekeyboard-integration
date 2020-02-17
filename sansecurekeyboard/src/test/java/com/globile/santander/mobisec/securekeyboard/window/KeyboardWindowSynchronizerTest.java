package com.globile.santander.mobisec.securekeyboard.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.view.Window;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Map;

@RunWith(PowerMockRunner.class)
public class KeyboardWindowSynchronizerTest {

    @Mock
    private Window currentWindow;

    private KeyboardWindowSynchronizer keyboardWindowSynchronizer;

    @Before
    public void setUp(){

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        keyboardWindowSynchronizer = KeyboardWindowSynchronizer.getInstance();

    }

    @After
    public void tearDown() {

        resetKeyboardWindowSynchronizerSingleton();

    }

    @Test
    public void test_getWindowManagerFor(){

        KeyboardWindowManager windowManager = keyboardWindowSynchronizer.getWindowManagerFor(currentWindow);

        Map<Window, KeyboardWindowManager> windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer,
                "windowsMap");
        Map<KeyboardWindowManager, Integer> windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer,
                "subscribersMap");

        assertNotNull(windowManager);

        assertEquals(windowManagersMap.size(), 1);
        assertEquals(windowManagerSubscribers.size(), 1);

    }

    @Test
    public void test_releaseWindowManagerFor() {

        KeyboardWindowManager windowManager = keyboardWindowSynchronizer.getWindowManagerFor(currentWindow);

        Map<Window, KeyboardWindowManager> windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer,
                "windowsMap");
        Map<KeyboardWindowManager, Integer> windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer,
                "subscribersMap");

        assertNotNull(windowManagersMap);
        assertNotNull(windowManagerSubscribers);

        keyboardWindowSynchronizer.releaseWindowManagerFor(windowManager);

        windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer, "windowsMap");
        windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer, "subscribersMap");

        assertNull(windowManagersMap);
        assertNull(windowManagerSubscribers);

    }

    @Test
    public void test_getReleaseSeveralWindowManager() {

        KeyboardWindowManager windowManager = keyboardWindowSynchronizer.getWindowManagerFor(currentWindow);

        Map<Window, KeyboardWindowManager> windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer,
                "windowsMap");
        Map<KeyboardWindowManager, Integer> windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer,
                "subscribersMap");

        assertNotNull(windowManagersMap);
        assertNotNull(windowManagerSubscribers);

        assertEquals(windowManagersMap.size(), 1);
        assertEquals(windowManagerSubscribers.size(), 1);

        assertEquals(1, (int) windowManagerSubscribers.get(windowManagersMap.get(currentWindow)));

        KeyboardWindowManager windowManager2 = keyboardWindowSynchronizer.getWindowManagerFor(currentWindow);

        windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer, "windowsMap");
        windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer, "subscribersMap");

        assertNotNull(windowManagersMap);
        assertNotNull(windowManagerSubscribers);

        assertEquals(windowManagersMap.size(), 1);
        assertEquals(windowManagerSubscribers.size(), 1);

        assertEquals(2, (int) windowManagerSubscribers.get(windowManagersMap.get(currentWindow)));

        keyboardWindowSynchronizer.releaseWindowManagerFor(windowManager);

        windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer, "windowsMap");
        windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer, "subscribersMap");

        assertEquals(windowManagersMap.size(), 1);
        assertEquals(windowManagerSubscribers.size(), 1);


        keyboardWindowSynchronizer.releaseWindowManagerFor(windowManager);

        windowManagersMap = Whitebox.getInternalState(keyboardWindowSynchronizer, "windowsMap");
        windowManagerSubscribers = Whitebox.getInternalState(keyboardWindowSynchronizer, "subscribersMap");

        assertNull(windowManagersMap);
        assertNull(windowManagerSubscribers);

    }

    private void resetKeyboardWindowSynchronizerSingleton() {

        try {
            // Reset the singleton instance
            Field instance = KeyboardWindowSynchronizer.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

}
