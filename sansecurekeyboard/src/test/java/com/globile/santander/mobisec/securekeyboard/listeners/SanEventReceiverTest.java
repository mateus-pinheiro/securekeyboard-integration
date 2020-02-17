package com.globile.santander.mobisec.securekeyboard.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@Config(sdk = 28)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@PowerMockIgnore({"org.robolectric.*", "android.*"})
public class SanEventReceiverTest {

    // Robolectric > Mock Activity (mock context from real one for accesing resources)
    private ActivityController<AppCompatActivity> activityController;
    private AppCompatActivity activity;

    @Mock
    private SanEventCallbacks sanEventCallbacks;

    private SanEventReceiver sanEventReceiver;

    public SanEventReceiverTest() {
    }

    @Before
    public void setupSanKeyboard() throws Exception {

        // Init Mockito
        //  MockitoAnnotations.initMocks(this);
        sanEventCallbacks = mock(SanEventCallbacks.class);

        setupMocks();

        sanEventReceiver = new SanEventReceiver(sanEventCallbacks);

    }

    private void setupMocks() {

        activityController = Robolectric.buildActivity(AppCompatActivity.class);
        activity = activityController.get();

    }

    @Test
    public void test_OnReceive_Success() {

        Intent i = new Intent();
        i.setAction(SanEventReceiver.ACTION_KEYBOARD_READY);

        sanEventReceiver.onReceive(activity, i);

        verify(sanEventCallbacks, times(1)).onSanKeyboardReady(null);

    }

    @Test
    public void test_OnReceive_Fail() {

        sanEventReceiver.onReceive(activity, null);

        verify(sanEventCallbacks, times(0)).onSanKeyboardReady(any(SanKeyboardView.class));

    }
}
