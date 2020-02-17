package com.globile.santander.mobisec.securekeyboard.window;

import static junit.framework.TestCase.assertEquals;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
public class KeyboardWindowManagerTest {

    @Mock
    private Window currentWindow;

    @Mock
    private WindowManager.LayoutParams attributes;

    @Mock
    private View decorView;


    private int flags;

    private KeyboardWindowManager keyboardWindowManager;

    @Before
    public void setUp(){

        when(currentWindow.getAttributes()).thenReturn(attributes);

        when(currentWindow.getDecorView()).thenReturn(decorView);
        when(currentWindow.getDecorView().getSystemUiVisibility()).thenReturn(0);

        Whitebox.setInternalState(attributes, "flags", -1988034304);

        keyboardWindowManager = new KeyboardWindowManager(currentWindow);

    }

    @Test
    public void test_getManagedWindow(){

        assertEquals(currentWindow, keyboardWindowManager.getManagedWindow());

    }

    @Test
    public void test_initFlagsForShowingKeyboard(){

        keyboardWindowManager.initFlagsForShowingKeyboard();

        verify(currentWindow, times(1)).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        verify(currentWindow, times(1)).addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        verify(decorView, times(1)).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }


    @Test
    public void test_restoreOriginalFlags(){

        keyboardWindowManager.initFlagsForShowingKeyboard();

        keyboardWindowManager.restoreOriginalFlags();

        verify(currentWindow, times(1)).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        verify(currentWindow, times(1)).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        verify(currentWindow, times(2)).addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    }

    @Test
    public void test_restoreOriginalFlags_notSetted(){

        keyboardWindowManager.restoreOriginalFlags();

        verify(currentWindow, never()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        verify(currentWindow, never()).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        verify(currentWindow, never()).addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    }

    @Test
    public void test_onDestroy(){

        keyboardWindowManager.onDestroy();

        assertNull(keyboardWindowManager.getManagedWindow());

    }
}
