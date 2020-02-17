package com.globile.santander.mobisec.securekeyboard.scrolling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Map;

public class AutoScrollSynchronizerTest {

    private final int MOCK_VIEW_INDEX_IN_PARENT = 0;
    private final int MOCK_ROOT_LAYOUT_ID = Integer.MAX_VALUE / 2;
    private final String MOCK_OWNER_NAME = "OwnerName";

    @Mock
    private Context context;

    private AutoScrollSynchronicer autoScrollSynchronicer;


    @Before
    public void setupSanKeyboard() throws Exception {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        setupMocks();

        autoScrollSynchronicer = AutoScrollSynchronicer.getInstance();

    }

    private void setupMocks() throws Exception {

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getResources()).thenReturn(mock(Resources.class));
        when(context.getResources().getConfiguration()).thenReturn(mock(Configuration.class));

        // Reset Singleton
        Field instance = AutoScrollSynchronicer.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);

    }

    @Test
    public void test_getAutoScrollManagerForRootLayoutId() {

        AutoScrollManager autoScrollManager = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);

        Map<String, AutoScrollManager> autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "autoScrollManagersMap");
        Map<String, Integer> managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "managerSubscriptionsMap");
        int rootLayoutId = Whitebox.getInternalState(autoScrollManager, "rootLayoutIdForAutoScrolling");

        assertNotNull(autoScrollManager);

        assertEquals(MOCK_ROOT_LAYOUT_ID, rootLayoutId);

        assertEquals(autoScrollManagersMap.size(), 1);
        assertEquals(managerSubscriptionsMap.size(), 1);

    }

    @Test
    public void test_getAutoScrollManagerForRootLayoutId_TwoPages() {

        AutoScrollManager autoScrollManager = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);
        AutoScrollManager autoScrollManager2 = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME + "2", MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT + 1);

        Map<String, AutoScrollManager> autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "autoScrollManagersMap");
        Map<String, Integer> managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "managerSubscriptionsMap");
        int rootLayoutId = Whitebox.getInternalState(autoScrollManager, "rootLayoutIdForAutoScrolling");
        int rootLayoutId2 = Whitebox.getInternalState(autoScrollManager2, "rootLayoutIdForAutoScrolling");

        assertNotNull(autoScrollManager);
        assertNotNull(autoScrollManager2);

        assertEquals(MOCK_ROOT_LAYOUT_ID, rootLayoutId);
        assertEquals(MOCK_ROOT_LAYOUT_ID, rootLayoutId2);

        assertEquals(autoScrollManagersMap.size(), 2);
        assertEquals(managerSubscriptionsMap.size(), 2);

    }

    @Test
    public void test_getAutoScrollManagerForRootLayoutId_TwoEditTextOnSameLayout() throws Exception {

        AutoScrollManager autoScrollManager = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT);
        AutoScrollManager autoScrollManager2 = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT);

        Map<String, AutoScrollManager> autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "autoScrollManagersMap");
        Map<String, Integer> managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "managerSubscriptionsMap");
        int rootLayoutId = Whitebox.getInternalState(autoScrollManager, "rootLayoutIdForAutoScrolling");
        int rootLayoutId2 = Whitebox.getInternalState(autoScrollManager2, "rootLayoutIdForAutoScrolling");

        assertNotNull(autoScrollManager);
        assertNotNull(autoScrollManager2);

        assertEquals(autoScrollManager, autoScrollManager2);

        assertEquals(MOCK_ROOT_LAYOUT_ID, rootLayoutId);
        assertEquals(MOCK_ROOT_LAYOUT_ID, rootLayoutId2);

        assertEquals(autoScrollManagersMap.size(), 1);
        assertEquals(managerSubscriptionsMap.size(), 1);

        String key = Whitebox.invokeMethod(autoScrollSynchronicer, "getKeyForRootLayout", MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT);

        assertEquals(2, (int) managerSubscriptionsMap.get(key));

    }

    @Test
    public void test_releaseAutoScrollManagerForRootLayoutId() {

        AutoScrollManager autoScrollManager = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);
        AutoScrollManager autoScrollManager2 = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME + "2",
                        MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT + 1);

        Map<String, AutoScrollManager> autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "autoScrollManagersMap");
        Map<String, Integer> managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "managerSubscriptionsMap");

        assertNotNull(autoScrollManager);
        assertNotNull(autoScrollManager2);

        assertEquals(autoScrollManagersMap.size(), 2);
        assertEquals(managerSubscriptionsMap.size(), 2);

        autoScrollSynchronicer
                .releaseAutoScrollManagerFor(MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);

        assertEquals(autoScrollManagersMap.size(), 1);
        assertEquals(managerSubscriptionsMap.size(), 1);

        autoScrollSynchronicer
                .releaseAutoScrollManagerFor(MOCK_OWNER_NAME + "2", MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT + 1);

        assertEquals(autoScrollManagersMap.size(), 0);
        assertEquals(managerSubscriptionsMap.size(), 0);

        autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer, "autoScrollManagersMap");
        managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer, "managerSubscriptionsMap");

        assertNull(autoScrollManagersMap);
        assertNull(managerSubscriptionsMap);

    }

    @Test
    public void test_releaseAutoScrollManagerForRootLayoutId_TwoPages() {

        AutoScrollManager autoScrollManager = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);

        Map<String, AutoScrollManager> autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "autoScrollManagersMap");
        Map<String, Integer> managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "managerSubscriptionsMap");

        assertNotNull(autoScrollManager);

        assertEquals(autoScrollManagersMap.size(), 1);
        assertEquals(managerSubscriptionsMap.size(), 1);

        autoScrollSynchronicer
                .releaseAutoScrollManagerFor(MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);

        assertEquals(autoScrollManagersMap.size(), 0);
        assertEquals(managerSubscriptionsMap.size(), 0);

        autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer, "autoScrollManagersMap");
        managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer, "managerSubscriptionsMap");

        assertNull(autoScrollManagersMap);
        assertNull(managerSubscriptionsMap);
    }

    @Test
    public void test_releaseAutoScrollManagerForRootLayoutId_TwoEditTextOnSameLayout() throws Exception {

        AutoScrollManager autoScrollManager = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT);
        AutoScrollManager autoScrollManager2 = autoScrollSynchronicer
                .getAutoScrollManagerFor(context, MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT);

        Map<String, AutoScrollManager> autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "autoScrollManagersMap");
        Map<String, Integer> managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer,
                "managerSubscriptionsMap");

        assertNotNull(autoScrollManager);
        assertNotNull(autoScrollManager2);

        String key = Whitebox.invokeMethod(autoScrollSynchronicer, "getKeyForRootLayout", MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID,
                MOCK_VIEW_INDEX_IN_PARENT);

        assertEquals(2, (int) managerSubscriptionsMap.get(key));

        assertEquals(autoScrollManagersMap.size(), 1);
        assertEquals(managerSubscriptionsMap.size(), 1);

        autoScrollSynchronicer
                .releaseAutoScrollManagerFor(MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);

        assertEquals(1, (int) managerSubscriptionsMap.get(key));

        assertEquals(autoScrollManagersMap.size(), 1);
        assertEquals(managerSubscriptionsMap.size(), 1);

        autoScrollSynchronicer
                .releaseAutoScrollManagerFor(MOCK_OWNER_NAME, MOCK_ROOT_LAYOUT_ID, MOCK_VIEW_INDEX_IN_PARENT);

        assertEquals(autoScrollManagersMap.size(), 0);
        assertEquals(managerSubscriptionsMap.size(), 0);

        autoScrollManagersMap = Whitebox.getInternalState(autoScrollSynchronicer, "autoScrollManagersMap");
        managerSubscriptionsMap = Whitebox.getInternalState(autoScrollSynchronicer, "managerSubscriptionsMap");

        assertNull(autoScrollManagersMap);
        assertNull(managerSubscriptionsMap);
    }
}
