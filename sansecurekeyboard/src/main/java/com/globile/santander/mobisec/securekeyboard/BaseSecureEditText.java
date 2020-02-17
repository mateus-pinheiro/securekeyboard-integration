package com.globile.santander.mobisec.securekeyboard;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.viewpager.widget.ViewPager;

import com.globile.santander.mobisec.logger.GlobileLog;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardType;
import com.globile.santander.mobisec.securekeyboard.scrolling.AutoScrollManager;
import com.globile.santander.mobisec.securekeyboard.scrolling.AutoScrollManagerStatus;
import com.globile.santander.mobisec.securekeyboard.scrolling.AutoScrollSynchronicer;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;
import com.globile.santander.mobisec.securekeyboard.window.KeyboardWindowManager;
import com.globile.santander.mobisec.securekeyboard.window.KeyboardWindowSynchronizer;

import java.util.List;

public abstract class BaseSecureEditText extends AppCompatEditText implements ViewTreeObserver.OnGlobalLayoutListener,
        LifecycleObserver {

    // ATTRIBUTES
    // ***********************************************************************

    private static final int SCROLL_DELAY = 100;
    private static final int SHOW_KEYBOARD_DELAY = 300;

    private InputMethodManager inputMethodManager;
    private SanKeyboardView sanKeyboardView;
    private SanKeyboardType keyboardType;
    private TopRowButtonsOptions topRowButtons;

    @Nullable
    private SanKeyboardView.SanKeyboardCallback sanKeyboardCallback;

    /**
     * Window Manager for managing App Themes during showing the keyboard
     */
    private KeyboardWindowManager keyboardWindowManager;

    /**
     * Overrides onEditorActionListener to answer to events
     */
    private OnEditorActionListener editorActionListener;

    /**
     * Holds a reference to the parentLifecycle in order to get destroyed automatically at same time
     * than the parent Fragment or Activity
     */
    private Lifecycle parentLifecycle;

    /**
     * Flag to determine if we already check if this BaseSecureEditText is contained in a page within a ViewPager
     */
    private boolean hasCheckedViewPagerInParents;

    /**
     * Flag to deal correctly with BACK key when Alternate PopUp Chars is shown
     */
    private boolean hasPopupCharsDismissed;

    /**
     * Indicates if the keyboard is already initiated (onGlobalLayout called) or not
     */
    private boolean isKeyboardReady;

    /**
     * If developer calls ShowSanKeyboard public method before the keyboard is initiated, an exception
     * will be thrown. To prevent this, enqueue the petition until the keyboard is ready and do it
     * once it's totally initiated
     */
    private boolean hasRequestForShownBeforeInit;

    // AutoScrolling
    // ------------------------
    /**
     * Holds the ID for the Root Layout ViewGroup that will be wrapped into a dynamic ScrollView
     */
    @IdRes
    private int rootLayoutIdForAutoScrolling;

    /**
     * Holds the name of the parent Fragment or Activity in order to be used when retrieving
     * AutoScrollManager. This is needed for identifying correctly AutoScrollManager instance
     * that belongs to this BaseSecureEditText.
     * This is because several RootLayout in different Fragments or Activities could use same
     * rootLayoutID but need different instances to manage wrapping and scrolling (one per
     * rootLayout in each Fragment or Activity)
     */
    private String ownerName;

    /**
     * AutoScrollManager that controls the scrolling for the all fields in same Root Layout
     */
    private AutoScrollManager autoScrollManager;

    /**
     * If Root Layout is in a Fragment that is used at same time in several pages of a ViewPager,
     * we need to distinguish which page is referring the field since the root layout ID and field's
     * IDs are the same for all these pages
     */
    private int rootLayoutPageIndexInViewPager;


    /**
     * Holds a reference to a parent ViewPager found in the View Tree hierarchy during onGlobalLayout.
     * If it's NULL, this BaseSecureEditText is NOT contained in (any page of) any ViewPager
     */
    private ViewPager parentViewPager;

    /**
     * If true, scroll system is deployed and removed when this BaseSecureEditTexts gets or loses the focus respectively. If not,
     * scroll system is deployed when creating the view and destroyed when the view is also destroyed.
     * Default behaviour will be NOT strict mode (i.e.: false value)
     */
    private boolean autoScrollingStrictMode;


    // CONSTRUCTORS  && INITIALIZERS
    // ***********************************************************************

    public BaseSecureEditText(Context context) {
        super(context);
        initializeView(context, null);
    }

    public BaseSecureEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public BaseSecureEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    private void initializeView(Context context, AttributeSet attrs) {

        context = context.getApplicationContext();

        setFocusable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setShowSoftInputOnFocus(false);
        } else {
            inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseSecureEditText, 0, 0);

        try {

            setKeyboardType(SanKeyboardType.values()[typedArray.getInt(R.styleable.BaseSecureEditText_sanKeyboardType, 0)]);
            topRowButtons = TopRowButtonsOptions.values()[typedArray.getInt(R.styleable.BaseSecureEditText_sanKeyboardButtons,
                    0)];
            rootLayoutIdForAutoScrolling = typedArray.getResourceId(R.styleable.BaseSecureEditText_rootLayoutIdForAutoScrolling,
                    -1);

            autoScrollingStrictMode = typedArray.getBoolean(R.styleable.BaseSecureEditText_autoScrollingStrictMode, false);

        } finally {
            typedArray.recycle();
        }

        getViewTreeObserver().addOnGlobalLayoutListener(this);

        hasPopupCharsDismissed = false;
        rootLayoutPageIndexInViewPager = -1;

        isKeyboardReady = false;
        hasRequestForShownBeforeInit = false;

        parentViewPager = null;
        hasCheckedViewPagerInParents = false;
        ownerName = "";

        // Switch off text suggestions since are provided by system keyboard and it conflicts with our keyboard
        if (getInputType() == InputType.TYPE_CLASS_TEXT){
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

    }

    /**
     * Makes the root layout focusable, which will take the default focus.
     * Without it, the focus cannot be taken away from the EditText, unless there's another focusable view above.
     */
    private void setRootLayoutFocusableInTouchMode() {

        ViewGroup rootLayout = getRootView().findViewById(android.R.id.content);

        if (rootLayout != null){
            rootLayout.setFocusableInTouchMode(true);
        } else {
            throw new IllegalStateException("Error setting android.R.id.content Focusable In Touch Mode. Maybe this SecureEditText doesn't exist anymore in the current window?");
        }

    }

    /**
     * Inits the SanKeyboard instance
     */
    private void initSanKeyboardInstance() {

        if (sanKeyboardView == null) {
            sanKeyboardView = SanKeyboardUtils.createKeyboardView(this);
        }

        sanKeyboardView.setSanKeyboardCallback(sanKeyboardCallback);
        sanKeyboardView.setInputConnection(this);

        sanKeyboardView.setOnEditorActionListener(editorActionListener);

    }

    /**
     * Retrieves AutoScrollManager instance for this RootLayout
     */
    private void initAutoScrollManager() {

        if (!hasCheckedViewPagerInParents){
            locateViewPagerInParents();
        }

        autoScrollManager = AutoScrollSynchronicer.getInstance().getAutoScrollManagerFor(
                getContext(), ownerName, rootLayoutIdForAutoScrolling, rootLayoutPageIndexInViewPager);

    }

    /**
     * Retrieves KeyboardWindowManager associated to the parent Window of this BaseSecureEditText
     *
     * @param parentActivity Parent activity from which the window will be extracted
     */
    private void initWindowKeyboardManager(FragmentActivity parentActivity){

        keyboardWindowManager = KeyboardWindowSynchronizer.getInstance()
                .getWindowManagerFor(parentActivity.getWindow());

    }

    /**
     * Looks for a ViewPager in the ViewTree hierarchy. In case it exists, then the page containing this BaseSecureEditText
     * is searched to get the index which will be used to identify unequivocally associated AutoScrollManager instance
     */
    private void locateViewPagerInParents() {

        ViewPager viewPager = null;

        ViewGroup parentView = (ViewGroup) getParent();

        while (parentView != null &&                            // If it's not null
                viewPager == null &&                             // We don't found a ViewPager in hierarchy
                parentView.getId() != android.R.id.content) {     // We don't reach the top of hierarchy

            // Climp up one level
            parentView = (ViewGroup) parentView.getParent();

            // Check if it's a ViewPager
            if (parentView instanceof ViewPager) {
                viewPager = (ViewPager) parentView;
            }

        }

        // If we have found a ViewPager in hierarchy, locate the page which this BaseSecureEditText belongs
        if (viewPager != null) {

            parentViewPager = viewPager;

            // Locate the page number where this BaseSecureEditText is contained in the ViewPager
            // This is needed because same Fragment can be instanced several times and this means
            // that root layout in those pages have the same ID. So this loop, looks for the page
            // where this BaseSecureEditText is laid out
            for (int i = 0; i < viewPager.getChildCount(); i++) {

                View page = viewPager.getChildAt(i);

                BaseSecureEditText secureEditText = page.findViewById(getId());

                // If we have found this BaseSecureEditText in the iterated page,
                // save the index to build the key for AutoScrollManager and
                // make it distinguishable between pages
                if (secureEditText != null && secureEditText.equals(this)) {
                    rootLayoutPageIndexInViewPager = i;
                    return;
                }

            }

        }

        hasCheckedViewPagerInParents = true;

    }

    // LIFECYCLE EVENTS
    // ***********************************************************************

    /**
     * Gets the FragmentActivity where this BaseSecureEditText is contained
     * Could be contained inside a Fragment, but that Fragment belongs to a
     * FragmentActivity when showing
     * @return FragmentActivity where this BaseSecureEditText is contained
     */
    private FragmentActivity getFragmentActivity() {

        Context context = getContext();

        while (!(context instanceof FragmentActivity)) {

            try {
                context = ((ContextWrapper) context).getBaseContext();
            } catch (Exception e) {

                // If we found an error trying to cast the Context to a ContextWrapper, then the Context used during
                // the instantiation of this BaseSecureEditText does not belong to an Activity or Fragment
                throw new IllegalArgumentException("The Context used to instantiate this BaseSecureEditText it's not valid. "
                        + "Context must belong to an Activity or Fragment. "
                        + "\nAlso, Activities must be allowed to support Fragments (use FragmentActivity or AppCompatActivity)"
                        + "\nMight are you providing applicationContext instead of Activity/Fragment Context where the widget is "
                        + "laid on?");

            }

        }

        return (FragmentActivity) context;

    }

    /**
     * Locates its parent (an Activity or Fragment) to attach to its parent lifecycle
     * @param parentActivity The parent activity where this BaseSecureEditText belongs
     */
    private void attachToParentLifeCycle(FragmentActivity parentActivity) {

        // If this Activity doesn't hold any Fragment that could contain this SecureEditText
        if (!attachForFragmentIfExists(parentActivity)){

            // If we didn't find this BaseSecureEditText in any of the child fragments, then it should
            // be in the current activity. Use its LifeCycle to attach it to this BaseSecureEditText
            View rootView = parentActivity.findViewById(getId());

            if (rootView != null) {
                parentLifecycle = parentActivity.getLifecycle();
                parentLifecycle.addObserver(this);

                ownerName = parentActivity.getClass().getSimpleName();

            }

        }

    }

    /**
     * Checks if this SecureEditText is laid out within any of the Fragments of the Activity provided.
     * If it's the case, then it will attach to its lifecycle
     * @param parentActivity Parent and active Activity to check if it's holding Fragments
     * @return true if a Fragment owning this SecureEditText was found and attached to its lifecycle; false otherwise
     */
    private boolean attachForFragmentIfExists(FragmentActivity parentActivity){

        // Check if this Activity holds any Fragment
        List<Fragment> fragmentList = parentActivity.getSupportFragmentManager().getFragments();

        // If there are fragments, look for this BaseSecureEditText inside them
        for (int i = 0; i < fragmentList.size(); i++) {

            View rootView = fragmentList.get(i).getView();

            if (rootView == null) {
                continue;
            }

            BaseSecureEditText secureEditText = rootView.findViewById(getId());

            // If this BaseSecureEditText is found in the Fragment, then check if it's inside a ViewPager
            // and once we have correctly located, use its LifeCycle as the one to attach to this
            // BaseSecureEditText
            if (secureEditText != null && secureEditText.equals(this)) {

                // If it's inside a ViewPager, locate the right instance of the Fragment
                if (parentViewPager != null) {

                    return locateParentFragmentInViewPager(fragmentList.get(i));

                } else {

                    return attachToFragmentLifecycle(fragmentList.get(i));

                }

            }

        }

        return false;

    }

    /**
     * Looks for the Fragment containing this SecureEditText, within the pages of the ViewPager found in View Tree hierarchy
     * @param viewPagerFragment The main fragment containing the ViewPager.
     * @return true if the fragment was found and was possible to attach to its lifecycle; false otherwise
     */
    private boolean locateParentFragmentInViewPager(@NonNull Fragment viewPagerFragment){

        // Look for in the Fragment page in the child Fragments just in case the Fragment is in a sub-level (it
        // depends on the implementation of the ViewPagerAdapter used)
        List<Fragment> childFragmentsList = viewPagerFragment.getChildFragmentManager().getFragments();

        for (int j = 0; j < childFragmentsList.size(); j++){

            if (parentViewPager.getChildAt(rootLayoutPageIndexInViewPager).equals(childFragmentsList.get(j).getView()) &&
                attachToFragmentLifecycle(childFragmentsList.get(j))) {

                return true;

            }

        }

        return false;

    }


    /**
     * Attachs this BaseSecureEditText to the supplied Fragment Lifecycle
     * @param parentFragment The Fragment which lifecycle will be observed
     * @return true if the fragment contains this BaseSecureEditText and was possible to attach
     *          to its lifecycle; false otherwise
     */
    private boolean attachToFragmentLifecycle(Fragment parentFragment){

        boolean isAttached = false;

        if (parentFragment.getView() != null) {

            BaseSecureEditText secureEditText = parentFragment.getView().findViewById(getId());

            // If this BaseSecureEditText is found in the Fragment, then use its LifeCycle
            // as the one to attach to this BaseSecureEditText
            if (secureEditText != null && secureEditText.equals(this)) {

                // Use getViewLifecycleOwner() to ensure that our onDestroy callback is called before onDestroyView fragment's event
                parentLifecycle = parentFragment.getViewLifecycleOwner().getLifecycle();
                parentLifecycle.addObserver(this);

                ownerName = parentFragment.getClass().getSimpleName();

                isAttached = true;
            }

        }

        return isAttached;

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        onDestroyView();
    }


    // GETTERS  && SETTERS
    // ***********************************************************************

    public int getRootLayoutIdForAutoScrolling() {
        return rootLayoutIdForAutoScrolling;
    }

    @CallSuper
    public SanKeyboardType getKeyboardType() {
        return keyboardType;
    }

    @CallSuper
    public TopRowButtonsOptions getTopRowButtons() {
        return topRowButtons;
    }

    public boolean isAutoScrollingStrictMode() {
        return autoScrollingStrictMode;
    }

    /**
     * Sets the Root Layout ID For AutoScrolling dinamically
     * WARNING: Be careful to do this in a state where the AutoScrollManager is not already started or an exception
     * will be thrown
     *
     * @param rootLayoutIdForAutoScrolling The RootLayoutID to be used for AutoScrolling system
     */
    public void setRootLayoutIdForAutoScrolling(@IdRes int rootLayoutIdForAutoScrolling) {

        if (autoScrollManager != null && autoScrollManager.getState() != AutoScrollManagerStatus.NOT_SETTED) {
            throw new IllegalStateException("Cannot set Root Layout ID when AutoScroll manager is started");
        }

        this.rootLayoutIdForAutoScrolling = rootLayoutIdForAutoScrolling;

    }

    @CallSuper
    public void setKeyboardType(SanKeyboardType keyboardType) {
        this.keyboardType = keyboardType;
    }

    @CallSuper
    public void setSanKeyboardCallback(@Nullable SanKeyboardView.SanKeyboardCallback sanKeyboardCallback) {
        this.sanKeyboardCallback = sanKeyboardCallback;
    }

    public void setTopRowButtons(TopRowButtonsOptions topRowButtonsOptions) {
        this.topRowButtons = topRowButtonsOptions;
    }

    public void setAutoScrollingStrictMode(boolean isStrictMode) {
        autoScrollingStrictMode = isStrictMode;
    }

    @Override
    public void setOnEditorActionListener(OnEditorActionListener l) {

        editorActionListener = l;

        if (sanKeyboardView != null) {
            sanKeyboardView.setOnEditorActionListener(l);
        }

        super.setOnEditorActionListener(l);

    }

    // VIEW TREE & FOCUS HANDLING METHODS
    // ***********************************************************************

    @CallSuper
    @Override
    public void onGlobalLayout() {

        isKeyboardReady = false;
        getViewTreeObserver().removeOnGlobalLayoutListener(this);

        if (getId() == -1){
            throw new IllegalArgumentException(getClass().getCanonicalName() + " doesn't have an ID assigned! ");
        }

        try {

            initSanKeyboardInstance();

            setRootLayoutFocusableInTouchMode();

        } catch(IllegalStateException e){

            // If an exception is captured during laying out this SecureEditText, it's probably that the view where
            // it belongs has been destroyed (or recycled in case of RecyclerView), so destroy the view and cancel operation
            if (e.getMessage() != null){

                if (e.getMessage().equals("Error creating keyboard!")){
                    GlobileLog.e("Error initializing SanKeyboard instance. Maybe this SecureEditText doesn't exist anymore?");
                } else {
                    GlobileLog.e(e.getMessage());
                }

            }

            onDestroyView();
            return;

        }



        setFocusable(true);
        setFocusableInTouchMode(true);

        // Get current parent Activity
        FragmentActivity parentActivity = getFragmentActivity();

        // Init the Window Manager for managing theme when Keyboard is shown
        initWindowKeyboardManager(parentActivity);

        // Locate if this BaseSecureEditText is inside a Page in a ViewPager (undone the ambiguity)
        locateViewPagerInParents();

        // Attach to parent Lifecycle in order to get destroy at same time that parent
        attachToParentLifeCycle(parentActivity);

        if (rootLayoutIdForAutoScrolling != -1) {
            initAutoScrollManager();
        }

        // If developer has requested to show the keyboard before it's initiated
        // then launch this enqueued petition
        if (hasRequestForShownBeforeInit) {
            requestFocus();
            hasRequestForShownBeforeInit = false;
        } else {
            clearFocus();
        }

        isKeyboardReady = true;

    }

    @CallSuper
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        if (!hasWindowFocus) {

            if (autoScrollManager != null){
                autoScrollManager.clearExtraRequests();
            }

            clearFocus();

        }

        super.onWindowFocusChanged(hasWindowFocus);

    }

    @CallSuper
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showSanKeyboard();
        } else {
            hideSanKeyboard();
        }
    }

    /**
     * Maintains focus in this BaseSecureEditText without generating extra requests in the
     * AutoScrollManager. Useful in special cases for preventing unexpected behaviours on UI
     * caused by AutoScrolling system.
     *
     * Mainly, is called when Language Selector is going to be shown in order to not losing
     * the focus on this Secure Edit Text and prevent the keyboard to be closed
     */
    public void retainFocus() {

        // Request the focus again (it's been lost during showing the language selector dialog
        requestFocus();

        // As we've requested the focus again, clear isResetedBeforeStopped flag in AutoScroll
        // Manager to ensure AutoScrolling system is stopping correctly (if not, there is a risk
        // that changes in layout are not undone)
        clearAutoScrollExtraRequests();

    }

    /**
     * Tells the AutoScrollManager to remove any additional request that could prevent the keyboard of
     * not get hidden
     */
    public void clearAutoScrollExtraRequests() {

        if (autoScrollManager != null) {
            autoScrollManager.clearExtraRequests();
        }

    }

    // KEY EVENTS
    // ***********************************************************************

    @CallSuper
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        boolean result = super.onKeyPreIme(keyCode, event);

        // onKeyPreIme() is called twice when pressing back and the pop-up window is shown.
        // The second time that this method is called, hasPopupCharsDismissed, so we ignore
        // this event returning true and set the popup chars dismissing consumed
        if (hasPopupCharsDismissed) {
            hasPopupCharsDismissed = false;
            return true;
        }

        // onKeyPreIme() is called twice when pressing back and the pop-up window is shown.
        // The first time that this method is called, we check if there is a popup chars showing.
        // If it is, true value will be returned and stored in hasPopupCharsDismissed. In that
        // case we mark the event consumed returning true.
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            hasPopupCharsDismissed = sanKeyboardView.handleBack();
            if (hasPopupCharsDismissed) return true;
        }

        // If no popup was dismissed, then check keyboard visibility
        return isKeyEventConsumed(keyCode, event) || result;

    }

    @CallSuper
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = super.onKeyUp(keyCode, event);
        return isKeyEventConsumed(keyCode, event) || result;
    }

    /**
     * Checks if the key event is consumed or not. Concretely, it checks
     * if Back button pressing it's consumed
     * @param keyCode keyCode to check consumption
     * @param event Kind of event
     * @return true if key event was consumed and should not be propagated; false otherwise
     */
    private boolean isKeyEventConsumed(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && isSanKeyboardShown()) {

            // Remove any flag from AutoScrolling to ensure that hiding the scrolling system
            // is done and not blocked by this flag
            clearAutoScrollExtraRequests();

            // Clearing the focus will also result in hiding the keyboard.
            clearFocus();

            return true;

        }

        return false;

    }


    // METHODS
    // ***********************************************************************

    /**
     * Returns if the keyboard is currently showing or not
     * @return true if KeyboardView is currently displayed on screen; false otherwise
     */
    public boolean isSanKeyboardShown() {
        return sanKeyboardView != null && sanKeyboardView.isShown();
    }

    /**
     * Returns if the Language Selector Dialog is currently showing or not
     * @return true if Language Selector dialog is showing; false otherwise
     */
    public boolean isLanguageSelectorShown() {
        return sanKeyboardView != null && sanKeyboardView.isLanguageSelectorShown();
    }

    /**
     * Shows the keyboard for this BaseSecureEditText
     */
    public void showSanSecureKeyboard() {

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isKeyboardReady) {
                    requestFocus();
                } else {
                    hasRequestForShownBeforeInit = true;
                }

            }
        }, SHOW_KEYBOARD_DELAY);

    }

    /**
     * Hide the keyboard for this BaseSecureEditText
     */
    public void hideSanSecureKeyboard() {
        clearFocus();
    }

    /**
     * Private implementation for showing the keyboard
     */
    private void showSanKeyboard() {

        hideDefaultKeyboard();

        initSanKeyboardInstance();

        if (keyboardWindowManager != null) {
            keyboardWindowManager.initFlagsForShowingKeyboard();
        }

        if (sanKeyboardView.isShown() && !sanKeyboardView.isAnimating()) {

            // This means that the previous focused view was also an instance of this view.
            // The previous view losing focus would trigger a hide animation, so clear it to keep the keyboard.
            sanKeyboardView.clearAnimation();

        } else {
            sanKeyboardView.slideIn();
        }

        // Start (if it isn't already started) the scrolling wrapper for field
        if (autoScrollManager != null) {

            if (autoScrollingStrictMode) {

                // If it's Stric Mode active, then we have to start and deploy totally autoscrolling system
                autoScrollManager.startAutoScrollingForField(this, sanKeyboardView);


            } else {

                // Else, autoscrolling system is deployed first time during view creation and we only
                // need to show the extra bottom space for forcing the scrolling
                if (autoScrollManager.getState().equals(AutoScrollManagerStatus.NOT_SETTED)) {
                    autoScrollManager.startAutoScrollingForField(this, sanKeyboardView);
                }

                autoScrollManager.showExtraSpaceForScrolling(this, sanKeyboardView);

            }

        }

    }

    /**
     * Private implementation for hiding keyboard
     */
    private void hideSanKeyboard() {

        if (isSanKeyboardShown() && !isLanguageSelectorShown()) {

            sanKeyboardView.handleBack();
            sanKeyboardView.slideOut();

            if (keyboardWindowManager != null) {
                keyboardWindowManager.restoreOriginalFlags();
            }

            // Give a little delay before stopping scrolling because maybe, next view
            // with focus is another BaseSecureEditText. In that case, stop will be
            // canceled and reuse scrolling already deployed
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (autoScrollManager != null) {

                        // If it's Strict Mode active, then we have to restore and remove AutoScrolling
                        // system completely from the root layout, else, we only need to hide the Extra
                        // Bottom Space that forces the scrolling
                        if (autoScrollingStrictMode) {
                            autoScrollManager.stopAutoScrollingForField();
                        } else {
                            autoScrollManager.hideExtraBottomSpaceForScrolling();
                        }

                    }

                }
            }, SCROLL_DELAY);

        }

    }

    /**
     * Hides the Android System Keyboard
     */
    private void hideDefaultKeyboard() {

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }

    }

    @CallSuper
    public void enableDisableCustomKey(int code, boolean enabled) {

        initSanKeyboardInstance();

        sanKeyboardView.getKeyboard().enableDisableCustomKey(code, enabled);

    }

    @CallSuper
    public void onDestroyView() {

        hideSanSecureKeyboard();

        if (autoScrollManager != null) {

            autoScrollManager.clearExtraRequests();
            autoScrollManager.stopAutoScrollingForField();

            AutoScrollSynchronicer.getInstance().releaseAutoScrollManagerFor(
                    ownerName, rootLayoutIdForAutoScrolling, rootLayoutPageIndexInViewPager);

            autoScrollManager = null;

        }

        if (keyboardWindowManager != null) {
            keyboardWindowManager.restoreOriginalFlags();
            KeyboardWindowSynchronizer.getInstance().releaseWindowManagerFor(keyboardWindowManager);
            keyboardWindowManager = null;
        }

        if (sanKeyboardView != null){
            sanKeyboardView.onDestroyView();
            sanKeyboardView = null;
        }

        inputMethodManager = null;
        sanKeyboardCallback = null;
        parentViewPager = null;

        if (parentLifecycle != null) {
            parentLifecycle.removeObserver(this);
            parentLifecycle = null;
        }

    }

    @Override
    public boolean equals(Object obj) {

        // Because developers can inherit from BaseSecureEditText to customize their owns
        // SecureEditTextField, we have to ensure that the object to compare is an instance
        // of this class, and the IDs matches (are the same widgets on the layout)
        if (obj instanceof BaseSecureEditText) {
            return getId() == ((BaseSecureEditText) obj).getId();
        }

        return false;

    }

}
