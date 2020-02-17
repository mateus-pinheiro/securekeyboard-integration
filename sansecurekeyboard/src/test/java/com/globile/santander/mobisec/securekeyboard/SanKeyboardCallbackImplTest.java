package com.globile.santander.mobisec.securekeyboard;

import static junit.framework.TestCase.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SanKeyboardCallbackImplTest {

    private SanKeyboardView.SanKeyboardCallback sanKeyboardCallback;


    @Before
    public void setupSanKeyboard() {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        sanKeyboardCallback = new SanKeyboardCallbackImpl();

    }

    @Test
    public void test_OnContinueClick() {

        sanKeyboardCallback.onContinueClick();

        // Nothing to verify or to assert
        assertTrue(true);

    }

    @Test
    public void test_OnCancelClick() {

        sanKeyboardCallback.onCancelClick();

        // Nothing to verify or to assert
        assertTrue(true);

    }
}
