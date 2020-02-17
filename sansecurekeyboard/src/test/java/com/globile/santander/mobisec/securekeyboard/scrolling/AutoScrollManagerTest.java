package com.globile.santander.mobisec.securekeyboard.scrolling;

import static junit.framework.TestCase.assertTrue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

import com.globile.santander.mobisec.securekeyboard.BaseSecureEditText;
import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;


@RunWith(PowerMockRunner.class)
@PrepareForTest({AutoScrollManager.class, LinearLayout.class, NestedScrollView.class})
public class AutoScrollManagerTest {

    private final int MOCK_ROOT_LAYOUT_ID = Integer.MAX_VALUE / 2;

    @Mock
    private Context context;

    @Mock
    private BaseSecureEditText secureEditText;

    @Mock
    private SanKeyboardView sanKeyboardView;

    @Mock
    private ViewGroup androidContent;

    @Mock
    private ViewGroup rootLayout;

    @Mock
    private ViewGroup parentView_ViewGroupRootLayout;

    @Mock
    private ScrollView parentViewScrollViewRootLayout;

    @Mock
    private NestedScrollView parentViewNestedScrollViewRootLayout;

    @Mock
    private LinearLayout llScrollContainer;

    @Mock
    private NestedScrollView nestedScrollView;

    @Mock
    private LockableScrollView lockableScrollView;

    @Mock
    private View extraBottomSpace;

    @Mock
    private Handler handler;

    @Mock
    private DisplayMetrics displayMetrics;

    private AutoScrollManager autoScrollManager;

    @Before
    public void setupSanKeyboard() throws Exception {

        // Init Mockito
        MockitoAnnotations.initMocks(this);

        setupMocks();

        autoScrollManager = new AutoScrollManager(context, MOCK_ROOT_LAYOUT_ID);

    }

    private void setupMocks() throws Exception {

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getResources()).thenReturn(mock(Resources.class));
        when(context.getResources().getConfiguration()).thenReturn(mock(Configuration.class));
        when(context.getResources().getDisplayMetrics()).thenReturn(displayMetrics);

        Whitebox.setInternalState(displayMetrics, "heightPixels", 1280);
        Whitebox.setInternalState(displayMetrics, "densityDpi", 480);

        when(secureEditText.getParent()).thenReturn(rootLayout);
        when(secureEditText.getKeyboardType()).thenReturn(SanKeyboardType.ALPHA);
        when(secureEditText.getTopRowButtons()).thenReturn(TopRowButtonsOptions.CANCEL_CONTINUE);

        when(androidContent.getId()).thenReturn(android.R.id.content);

        when(rootLayout.getId()).thenReturn(MOCK_ROOT_LAYOUT_ID);
        when(rootLayout.getParent()).thenReturn(parentView_ViewGroupRootLayout);
        when(rootLayout.getLayoutParams()).thenReturn(mock(ViewGroup.MarginLayoutParams.class));

        when(parentView_ViewGroupRootLayout.getChildCount()).thenReturn(1);
        when(parentView_ViewGroupRootLayout.getChildAt(0)).thenReturn(rootLayout);

        when(parentViewScrollViewRootLayout.getChildCount()).thenReturn(1);
        when(parentViewScrollViewRootLayout.getChildAt(0)).thenReturn(rootLayout);

        when(parentViewNestedScrollViewRootLayout.getChildCount()).thenReturn(1);
        when(parentViewNestedScrollViewRootLayout.getChildAt(0)).thenReturn(rootLayout);

        whenNew(LinearLayout.class).withArguments(context).thenReturn(llScrollContainer);
        whenNew(NestedScrollView.class).withArguments(context).thenReturn(nestedScrollView);
        whenNew(View.class).withArguments(context).thenReturn(extraBottomSpace);
        whenNew(LockableScrollView.class).withArguments(context).thenReturn(lockableScrollView);

        whenNew(Handler.class).withNoArguments().thenReturn(handler);

        when(handler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                if (invocation.getArgument(0) instanceof Runnable) {
                    ((Runnable) invocation.getArgument(0)).run();
                }
                return null;
            }

        });

        // To avoid NPE when NesteScrollView try to scroll
        suppress(method(NestedScrollView.class, "smoothScrollBy"));

    }

    @Test
    public void test_startAutoScrollingForField_ViewGroupRootLayout() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }


    @Test
    public void test_startAutoScrollingForField_ScrollViewRootLayout() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        when(rootLayout.getParent()).thenReturn(parentViewScrollViewRootLayout);

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_NestedScrollViewRootLayout() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        when(rootLayout.getParent()).thenReturn(parentViewNestedScrollViewRootLayout);

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_RootLayoutNotFound() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        when(rootLayout.getId()).thenReturn(MOCK_ROOT_LAYOUT_ID + 1);
        when(rootLayout.getParent()).thenReturn(androidContent);

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ResetedBeforeStopped() {

        Whitebox.setInternalState(autoScrollManager, "currentManagerStatus", AutoScrollManagerStatus.SETTED);
        Whitebox.setInternalState(autoScrollManager, "nestedAutoScrollView", parentViewNestedScrollViewRootLayout);
        Whitebox.setInternalState(autoScrollManager, "currentRootLayoutType", RootLayoutType.NESTED_SCROLL_VIEW);

        boolean isResetedBeforeStop = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertFalse(isResetedBeforeStop);

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        isResetedBeforeStop = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertTrue(isResetedBeforeStop);

    }

    @Test
    public void test_startAutoScrollingForField_RootLayoutNotFoundInParent() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        when(parentView_ViewGroupRootLayout.getChildAt(0)).thenReturn(parentViewNestedScrollViewRootLayout);

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityLow() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 150);
        Whitebox.setInternalState(displayMetrics, "heightPixels", 480);

        when(secureEditText.getTop()).thenReturn(200);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityLow_NearTop() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 150);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityLow_TopRowButtons() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 150);
        Whitebox.setInternalState(displayMetrics, "heightPixels", 480);

        when(secureEditText.getTop()).thenReturn(200);
        when(secureEditText.getTopRowButtons()).thenReturn(TopRowButtonsOptions.NONE);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityMedium() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 180);
        Whitebox.setInternalState(displayMetrics, "heightPixels", 856);

        when(secureEditText.getTop()).thenReturn(200);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityMedium_TopRowButtons() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 180);
        Whitebox.setInternalState(displayMetrics, "heightPixels", 856);

        when(secureEditText.getTopRowButtons()).thenReturn(TopRowButtonsOptions.NONE);
        when(secureEditText.getTop()).thenReturn(200);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityMedium_NearTop() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 180);
        Whitebox.setInternalState(displayMetrics, "heightPixels", 856);

        when(secureEditText.getTopRowButtons()).thenReturn(TopRowButtonsOptions.NONE);
        when(secureEditText.getTop()).thenReturn(100);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_startAutoScrollingForField_ScreenDensityHigh() {

        Whitebox.setInternalState(displayMetrics, "densityDpi", 320);
        Whitebox.setInternalState(displayMetrics, "heightPixels", 1280);

        when(secureEditText.getTopRowButtons()).thenReturn(TopRowButtonsOptions.NONE);
        when(secureEditText.getTop()).thenReturn(100);

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_stopAutoScrollingForField_ViewGroupRootLayout() {

        Whitebox.setInternalState(autoScrollManager, "currentManagerStatus", AutoScrollManagerStatus.SETTED);
        Whitebox.setInternalState(autoScrollManager, "currentRootLayoutType", RootLayoutType.VIEW_GROUP);
        Whitebox.setInternalState(autoScrollManager, "lockableScrollView", lockableScrollView);

        when(lockableScrollView.getParent()).thenReturn(parentView_ViewGroupRootLayout);
        when(lockableScrollView.getChildAt(0)).thenReturn(llScrollContainer);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        autoScrollManager.stopAutoScrollingForField();

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_stopAutoScrollingForField_ScrollViewRootLayout() {

        Whitebox.setInternalState(autoScrollManager, "currentManagerStatus", AutoScrollManagerStatus.SETTED);
        Whitebox.setInternalState(autoScrollManager, "currentRootLayoutType", RootLayoutType.SCROLL_VIEW);
        Whitebox.setInternalState(autoScrollManager, "autoScrollView", parentViewScrollViewRootLayout);

        when(parentViewScrollViewRootLayout.getParent()).thenReturn(parentView_ViewGroupRootLayout);
        when(parentViewScrollViewRootLayout.getChildAt(0)).thenReturn(llScrollContainer);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        autoScrollManager.stopAutoScrollingForField();

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_stopAutoScrollingForField_NestedScrollViewRootLayout() {

        Whitebox.setInternalState(autoScrollManager, "currentManagerStatus", AutoScrollManagerStatus.SETTED);
        Whitebox.setInternalState(autoScrollManager, "currentRootLayoutType", RootLayoutType.NESTED_SCROLL_VIEW);
        Whitebox.setInternalState(autoScrollManager, "nestedAutoScrollView", parentViewNestedScrollViewRootLayout);

        when(parentViewNestedScrollViewRootLayout.getParent()).thenReturn(parentView_ViewGroupRootLayout);
        when(parentViewNestedScrollViewRootLayout.getChildAt(0)).thenReturn(llScrollContainer);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        autoScrollManager.stopAutoScrollingForField();

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_stopAutoScrollingForField_AutoScrollingReseted() {

        Whitebox.setInternalState(autoScrollManager, "currentManagerStatus", AutoScrollManagerStatus.SETTED);
        Whitebox.setInternalState(autoScrollManager, "isResetedBeforeStopped", true);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        autoScrollManager.stopAutoScrollingForField();

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

    }

    @Test
    public void test_showHideExtraSpaceBottom() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        verify(extraBottomSpace, times(1)).setVisibility(View.VISIBLE);


        autoScrollManager.hideExtraBottomSpaceForScrolling();

        verify(extraBottomSpace, times(1)).setVisibility(View.GONE);

        when(extraBottomSpace.getVisibility()).thenReturn(View.GONE);


        autoScrollManager.showExtraSpaceForScrolling(secureEditText, sanKeyboardView);

        verify(extraBottomSpace, times(2)).setVisibility(View.VISIBLE);

    }

    @Test
    public void test_showHideExtraBottomSpace_ResetedBeforeHiding() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        verify(extraBottomSpace, times(1)).setVisibility(View.VISIBLE);

        boolean isResetedBeforeStopped = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertFalse(isResetedBeforeStopped);


        autoScrollManager.showExtraSpaceForScrolling(secureEditText, sanKeyboardView);

        isResetedBeforeStopped = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertTrue(isResetedBeforeStopped);

        verify(extraBottomSpace, times(2)).setVisibility(View.VISIBLE);


        autoScrollManager.hideExtraBottomSpaceForScrolling();

        isResetedBeforeStopped = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertFalse(isResetedBeforeStopped);

        verify(extraBottomSpace, times(3)).setVisibility(View.VISIBLE);


        autoScrollManager.hideExtraBottomSpaceForScrolling();

        verify(extraBottomSpace, times(1)).setVisibility(View.GONE);

    }

    @Test
    public void test_clearExtraRequests() {

        assertEquals(AutoScrollManagerStatus.NOT_SETTED, autoScrollManager.getState());

        autoScrollManager.startAutoScrollingForField(secureEditText, sanKeyboardView);

        assertEquals(AutoScrollManagerStatus.SETTED, autoScrollManager.getState());

        verify(extraBottomSpace, times(1)).setVisibility(View.VISIBLE);

        boolean isResetedBeforeStopped = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertFalse(isResetedBeforeStopped);


        autoScrollManager.showExtraSpaceForScrolling(secureEditText, sanKeyboardView);

        isResetedBeforeStopped = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertTrue(isResetedBeforeStopped);

        verify(extraBottomSpace, times(2)).setVisibility(View.VISIBLE);


        autoScrollManager.clearExtraRequests();

        isResetedBeforeStopped = Whitebox.getInternalState(autoScrollManager, "isResetedBeforeStopped");

        assertFalse(isResetedBeforeStopped);


        autoScrollManager.hideExtraBottomSpaceForScrolling();

        verify(extraBottomSpace, times(1)).setVisibility(View.GONE);

    }

    @Test
    public void test_OnDestroy() {

        autoScrollManager.onDestroy();

        assertNull(Whitebox.getInternalState(autoScrollManager, "context"));
        assertNull(Whitebox.getInternalState(autoScrollManager, "autoScrollView"));
        assertNull(Whitebox.getInternalState(autoScrollManager, "nestedAutoScrollView"));

    }

}
