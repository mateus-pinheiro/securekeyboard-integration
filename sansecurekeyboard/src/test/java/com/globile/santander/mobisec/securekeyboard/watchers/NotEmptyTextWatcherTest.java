package com.globile.santander.mobisec.securekeyboard.watchers;

import static junit.framework.TestCase.assertTrue;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

import android.text.Editable;

import com.globile.santander.mobisec.securekeyboard.BaseSecureEditText;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboard;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.ref.WeakReference;

@RunWith(PowerMockRunner.class)
public class NotEmptyTextWatcherTest {

    @Mock
    private BaseSecureEditText secureEditText;

    private NotEmptyTextWatcher notEmptyTextWatcher;


    @Before
    public void setupSanKeyboard() {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        notEmptyTextWatcher = new NotEmptyTextWatcher(SanKeyboard.KEYCODE_CONTINUE, secureEditText);

    }

    @Test
    public void test_OnTextChanged() {

        WeakReference<BaseSecureEditText> weakSanEditText = Whitebox.getInternalState(notEmptyTextWatcher, "weakSecureEditText");
        WeakReference<BaseSecureEditText> spyWeakReference = spy(weakSanEditText);

        Whitebox.setInternalState(notEmptyTextWatcher, spyWeakReference);

        notEmptyTextWatcher.onTextChanged("abc", 0, 2, 3);

        verify(spyWeakReference, times(2)).get();
        verify(secureEditText, times(1)).enableDisableCustomKey(SanKeyboard.KEYCODE_CONTINUE, true);

    }

    @Test
    public void test_BeforeTextChanged() {

        notEmptyTextWatcher.beforeTextChanged("abc", 0, 2, 3);

        // No actions to verify or assert in BeforeTextChanged
        assertTrue(true);
    }

    @Test
    public void test_AfterTextChanged() {

        notEmptyTextWatcher.afterTextChanged(mock(Editable.class));

        // No actions to verify or assert in AfterTextChanged
        assertTrue(true);
    }
}
