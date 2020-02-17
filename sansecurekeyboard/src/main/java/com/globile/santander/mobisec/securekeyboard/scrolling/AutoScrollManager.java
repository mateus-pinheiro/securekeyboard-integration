package com.globile.santander.mobisec.securekeyboard.scrolling;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.globile.santander.mobisec.securekeyboard.BaseSecureEditText;
import com.globile.santander.mobisec.securekeyboard.SanKeyboardView;

/**
 * AutoScrollManager is in charge of create a scroll view and wrap root layout supplied to simulate
 * scrolling during showing the SecureKeyboard.
 *
 * All BaseSecureEditText with same RootLayoutResourceId share same instance of AutoScrollManager (since
 * the scroll view and wrapper it's the same for entire layout)
 *
 * > RootLayout must be always the root layout of your Activity or your Fragments:
 *
 *  - Activity: Is the first ViewGroup (LinearLayout, ConstraintLayout, etc) that is wrapping the content
 *              of the Activity and it's normally a child of the CoordinatorLayout if you're using Toolbar
 *              in your application. If not, is the first ViewGroup that you declare in your XML
 *  - Fragments: Is the first ViewGroup that you declare in your XML
 *
 * > What happens if my root layout is a ScrollView or NestedScrollView?
 *
 *  - In this case, developers must declare as root layout the unique child of the Nested/ScrollView, in order
 *    to let the AutoScrollManager reuse current Nested/ScrollView instead of create a new one
 *
 * > Fragments used as pages in ViewPagers:
 *
 *  - If page Fragment root layout is not a Nested/ScrollView, developers must declare current root layout as
 *    the one to use as root but wrap it into a FrameLayout so that there is one level more in layout tree in order
 *    to get correctly inflated the views during starting AutoScroll system
 *
 */
public class AutoScrollManager {

    // ATTRIBUTES
    // ***********************************************************************

    private static final int SCROLL_DELAY = 200;

    /**
     * Current status for this AutoScrollManager
     */
    private AutoScrollManagerStatus currentManagerStatus;

    /**
     * Current type of Root layout
     */
    private RootLayoutType currentRootLayoutType;

    /**
     * Holds resource ID for Root Layout
     */
    @IdRes
    private final int rootLayoutIdForAutoScrolling;

    /**
     * Holds possible existing Scroll View
     */
    private ScrollView autoScrollView;

    /**
     * Holds possible existing NestedScrollView or if it's not exists, by default creates
     * a new NestedScrollView dinamically
     */
    private NestedScrollView nestedAutoScrollView;

    /**
     * Holds special ScrollView that it's lockable. It's used when there is no ScrollView
     * or NestedScrollView already present on the layout to reuse. In that case, we want
     * to scroll only when the keyboard is shown and in non-strict mode, we need to lock
     * the scroll since the scroll wrapper is not removed until the view destroying process
     */
    private LockableScrollView lockableScrollView;

    /**
     * When using LockableScrollView, holds the original scroll position before start/show
     * the Scrolling system for restoring it when hiding
     */
    private Point originalScrollPosition;

    /**
     * Extra bottom space with same height as keyboard view, added at the bottom of the
     * wrapper to force scroll all that area
     */
    private View extraBottomSpace;

    /**
     * Holds last SecureEditText make a request to this AutoScrollManager. This is for knowing
     * if we have to recalculate extra bottom space height by checking type of keyboard and top
     * row buttons setted on the SecureEditText
     */
    private BaseSecureEditText lastSecureEditTextRequest;

    /**
     * Context used to access DisplayMetrics
     */
    private Context context;

    /**
     * Holds the index as a child in its parent ViewGroup. This is needed when restoring layout
     * knowing the original position in child list
     */
    private int viewIndexInParent;

    /**
     * Flag to determine if a new BaseSecureEditText requested focus before restore scrolling system to the previous
     * one that initiated it
     */
    private boolean isResetedBeforeStopped;


    // CONSTRUCTORS
    // ***********************************************************************

    /**
     * Default constructor for AutoScrollManager
     *
     * @param context                      Context used to get metrics
     * @param rootLayoutIdForAutoScrolling Root Layout resource ID to apply AutoScrolling
     */
    AutoScrollManager(Context context, int rootLayoutIdForAutoScrolling) {

        this.context = context.getApplicationContext();

        this.rootLayoutIdForAutoScrolling = rootLayoutIdForAutoScrolling;

        currentManagerStatus = AutoScrollManagerStatus.NOT_SETTED;
        isResetedBeforeStopped = false;

    }

    // GETTERS & SETTERS
    // ***********************************************************************

    /**
     * Returns the current state of this AutoScrollManager
     *
     * @return Current state of AutoScrollManager
     */
    public AutoScrollManagerStatus getState() {
        return currentManagerStatus;
    }

    // METHODS
    // ***********************************************************************

    /**
     * Initializes process to wrap root layout into a scroll view to accomplish scrolling to the EditText
     */
    public void startAutoScrollingForField(final BaseSecureEditText editTextField, SanKeyboardView sanKeyboardView) {

        if (currentManagerStatus == AutoScrollManagerStatus.SETTED) {
            isResetedBeforeStopped = true;
        }

        if (rootLayoutIdForAutoScrolling != -1) {

            // If the ScrollView is not setted, do it
            if (currentManagerStatus == AutoScrollManagerStatus.NOT_SETTED) {

                ViewGroup rootLayout = (ViewGroup) editTextField.getParent();

                // Climb the tree view up to the top until find the root layout id. We have to do in this way
                // for avoiding ambiguos resources ID for root layout (for example, in a ViewPager which uses
                // same Fragment for several pages)
                while (rootLayout != null &&
                        rootLayout.getId() != rootLayoutIdForAutoScrolling &&
                        rootLayout.getId() != android.R.id.content) {

                    rootLayout = (ViewGroup) rootLayout.getParent();

                }

                if (rootLayout == null || rootLayout.getId() == android.R.id.content) {
                    return;
                }

                currentManagerStatus = AutoScrollManagerStatus.STARTING;

                lastSecureEditTextRequest = editTextField;

                // Copy the behaviour from the current RootLayout to the wrapper to maintain UI state
                CoordinatorLayout.Behavior behaviour = getCoordinatorLayoutBehaviour(rootLayout);

                // Create LinearLayout container of Original Root Layout + Extra Bottom Space
                LinearLayout llScrollContainer = getLinearLayoutScrollContainer(
                        (ViewGroup.MarginLayoutParams) rootLayout.getLayoutParams());

                // Get Parent of the Original Root Layout
                ViewGroup parentView = (ViewGroup) rootLayout.getParent();

                // If root layout is already a Scroll View, use it
                if (parentView instanceof ScrollView) {

                    viewIndexInParent = 0;

                    currentRootLayoutType = RootLayoutType.SCROLL_VIEW;
                    rootLayout = reuseScrollView(parentView);

                    // If root layout is already a Nested Scroll View, use it
                } else if (parentView instanceof NestedScrollView) {

                    viewIndexInParent = 0;

                    currentRootLayoutType = RootLayoutType.NESTED_SCROLL_VIEW;
                    rootLayout = reuseNestedScrollView(parentView);

                    // If not, create a new Lockable Scroll View
                } else {

                    currentRootLayoutType = RootLayoutType.VIEW_GROUP;

                    viewIndexInParent = getViewIndex(parentView, rootLayout);

                    // Cannot find in Parent
                    if (viewIndexInParent == -1) {
                        currentManagerStatus = AutoScrollManagerStatus.NOT_SETTED;
                        return;
                    }

                    // Remove it from the parent (we cannot add it to the new layout without doing this before)
                    parentView.removeViewAt(viewIndexInParent);

                    // Create AutoScrollView
                    lockableScrollView = createLockableScrollView();

                }

                // Create Extra Bottom Space
                calculateExtraBottomSpace(sanKeyboardView);
                extraBottomSpace.setVisibility(View.VISIBLE);

                // Add original layout + extra bottom space to new root layout
                llScrollContainer.addView(rootLayout);
                llScrollContainer.addView(extraBottomSpace);

                switch (currentRootLayoutType) {

                    case SCROLL_VIEW:
                        wrapWithScrollView(llScrollContainer);
                        break;

                    case NESTED_SCROLL_VIEW:
                        wrapWithNestedScrollView(parentView, llScrollContainer);
                        break;

                    case VIEW_GROUP:
                        wrapWithLockableScrollView(parentView, llScrollContainer, behaviour);
                        break;
                }

                // Recover the focus (it was lost during layout change)
                editTextField.requestFocus();

                scrollToField(editTextField);

                currentManagerStatus = AutoScrollManagerStatus.SETTED;

                // If it's already setted, then only scroll to the field
            } else {

                // Recalculate Extra Bottom Space for the new keyboard
                if (hasKeyboardLayoutChanged(editTextField)) {
                    calculateExtraBottomSpace(sanKeyboardView);
                }

                extraBottomSpace.setVisibility(View.VISIBLE);
                scrollToField(editTextField);

            }

        }

    }

    /**
     * Restores original host app layout removing the ScrollView added dinamically
     */
    public void stopAutoScrollingForField() {

        // If another EditText request a scroll before the previous one is cancelled, then
        // abort stop scrolling system and reuse it.
        if (isResetedBeforeStopped) {
            isResetedBeforeStopped = false;
            return;

        }

        // If not, stop Scrolling system if it's already setted
        if (rootLayoutIdForAutoScrolling != -1 && currentManagerStatus == AutoScrollManagerStatus.SETTED) {

            currentManagerStatus = AutoScrollManagerStatus.STOPPING;

            switch (currentRootLayoutType) {

                case SCROLL_VIEW:
                    restoreOriginalScrollView();
                    break;
                case NESTED_SCROLL_VIEW:
                    restoreOriginalNestedScrollView();
                    break;
                case VIEW_GROUP:
                    restoreOriginalLayout();
                    break;

            }

        }

        currentManagerStatus = AutoScrollManagerStatus.NOT_SETTED;

    }

    /**
     * Wraps original layout into a NestedScrollView to accomplish scrolling to EditText field
     */
    private void wrapWithNestedScrollView(ViewGroup parentView, LinearLayout llScrollContainer) {

        // Encapsulate new layout into the autoScrollView
        nestedAutoScrollView.addView(llScrollContainer);

        // If root layout is not a Nested Scroll View (not reusing existing one)
        if (currentRootLayoutType != RootLayoutType.NESTED_SCROLL_VIEW) {
            // Add the parent of the original root layout the new layout with scrolling
            parentView.addView(nestedAutoScrollView, viewIndexInParent);
        }

    }

    /**
     * Wraps original layout into a regular ScrollView to accomplish scrolling to EditText field
     */
    private void wrapWithScrollView(LinearLayout llScrollContainer) {

        // Encapsulate new layout into the autoScrollView
        autoScrollView.addView(llScrollContainer);

    }

    private void wrapWithLockableScrollView(ViewGroup parentView, LinearLayout llScrollContainer,
            @Nullable CoordinatorLayout.Behavior behavior) {

        // Encapsulate new layout into the autoScrollView
        lockableScrollView.addView(llScrollContainer);

        // If a behaviour was copied from original root layout, apply to the new
        // root layout (LockableScrollView)
        if (behavior != null){
            ((CoordinatorLayout.LayoutParams)lockableScrollView.getLayoutParams())
                    .setBehavior(behavior);
        }

        // Add the parent of the original root layout the new layout with scrolling
        parentView.addView(lockableScrollView, viewIndexInParent);

    }

    /**
     * Scrolls to the supplied BaseSecureEditText using the Main Nested Scroll View
     */
    private void scrollToField(final BaseSecureEditText editTextField) {

        backupOriginalScrollPosition();

        // Scroll to the field
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                switch (currentRootLayoutType) {

                    case SCROLL_VIEW:
                        if (autoScrollView != null){
                            autoScrollView.smoothScrollTo(0, getEditTextScrollPosition(editTextField));
                        }
                        break;

                    case NESTED_SCROLL_VIEW:
                        if (nestedAutoScrollView != null){
                            nestedAutoScrollView.smoothScrollTo(0, getEditTextScrollPosition(editTextField));
                        }
                        break;

                    case VIEW_GROUP:
                        if (lockableScrollView != null){
                            lockableScrollView.smoothScrollTo(0, getEditTextScrollPosition(editTextField));
                        }
                        break;
                }


            }
        }, SCROLL_DELAY);

    }

    /**
     * If root layout was already a NestedScrollView, this method restores the original
     * content of it
     */
    private void restoreOriginalNestedScrollView() {

        // Get our LinearLayout capsule
        LinearLayout llScrollContainer = (LinearLayout) nestedAutoScrollView.getChildAt(0);

        // Get the Original Root Layout that was encapsulated into our new layout
        ViewGroup originalRootLayout = (ViewGroup) llScrollContainer.getChildAt(0);

        // Clean scroll and container
        llScrollContainer.removeAllViews();
        nestedAutoScrollView.removeViewAt(0);

        // Add original root layout to original scroll view
        nestedAutoScrollView.addView(originalRootLayout, 0);

    }

    /**
     * If root layout was already a ScrollView, this method restores the original
     * content of it
     */
    private void restoreOriginalScrollView() {

        // Get our LinearLayout capsule
        LinearLayout llScrollContainer = (LinearLayout) autoScrollView.getChildAt(0);

        // Get the Original Root Layout that was encapsulated into our new layout
        ViewGroup originalRootLayout = (ViewGroup) llScrollContainer.getChildAt(0);

        // Clean scroll and container
        llScrollContainer.removeAllViews();
        autoScrollView.removeViewAt(0);

        // Add original root layout to original scroll view
        autoScrollView.addView(originalRootLayout, 0);

    }

    /**
     * Restore original root layout when it's distinct of NestedScrollView or ScrollView
     */
    private void restoreOriginalLayout() {

        // Get the parent of the scroll (the parent of the Original Root Layout)
        ViewGroup parentView = (ViewGroup) lockableScrollView.getParent();

        // Get our LinearLayout capsule
        LinearLayout llScrollContainer = (LinearLayout) lockableScrollView.getChildAt(0);

        // Get the Original Root Layout that was encapsulated into our new layout
        ViewGroup originalRootLayout = (ViewGroup) llScrollContainer.getChildAt(0);

        // Clean scroll and container
        llScrollContainer.removeAllViews();
        lockableScrollView.removeViewAt(0);

        // Remove Scroll View from parent
        parentView.removeViewAt(0);

        // Add original root layout to parent
        parentView.addView(originalRootLayout, viewIndexInParent);


    }

    /**
     * If root layout was already a NestedScrollView, reuse it wrapping our LinearLayout + Extra Space
     */
    private ViewGroup reuseScrollView(ViewGroup parentView) {

        autoScrollView = (ScrollView) parentView;
        nestedAutoScrollView = null;
        lockableScrollView = null;

        ViewGroup rootLayout = (ViewGroup) autoScrollView.getChildAt(0);

        autoScrollView.removeViewAt(0);

        return rootLayout;

    }

    /**
     * If root layout was already a ScrollView, reuse it wrapping our LinearLayout + Extra Space into it
     */
    private ViewGroup reuseNestedScrollView(ViewGroup parentView) {

        nestedAutoScrollView = (NestedScrollView) parentView;
        autoScrollView = null;
        lockableScrollView = null;

        ViewGroup rootLayout = (ViewGroup) nestedAutoScrollView.getChildAt(0);

        nestedAutoScrollView.removeViewAt(0);

        return rootLayout;

    }

    /**
     * Gets the index of a View as a child within its parent
     *
     * @param viewGroup    The ViewGroup to iterate childs
     * @param searchedView The searched view within the ViewGroup
     * @return Index of the child or -1 if not found
     */
    private int getViewIndex(ViewGroup viewGroup, View searchedView) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            if (viewGroup.getChildAt(i).equals(searchedView)) {
                return i;
            }
        }

        return -1;

    }

    /**
     * If original root layout it's distinct of NestedScrollView or ScrollView (e.g: LinearLayout, RelativeLayout, etc)
     * then, create a LockableScrollView dinamically to wrap it and accomplish the scrolling to the Edit Text field
     */
    private LockableScrollView createLockableScrollView() {

        LockableScrollView lsv = new LockableScrollView(context);
        autoScrollView = null;
        nestedAutoScrollView = null;

        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        lsv.setFillViewport(true);
        lsv.setLayoutParams(lp);
        lsv.setSmoothScrollingEnabled(true);

        return lsv;
    }

    /**
     * Creates the LinearLayout container for the Scroll View. This LinearLayout contains original root layout +
     * an extra bottom space (keyboard height) for making visible Edit Text field when scrolling
     */
    private LinearLayout getLinearLayoutScrollContainer(ViewGroup.MarginLayoutParams rootLayoutParams) {

        LinearLayout llScrollContainer = new LinearLayout(context);

        LinearLayout.LayoutParams lpScrollContainer = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        llScrollContainer.setLayoutParams(lpScrollContainer);
        llScrollContainer.setOrientation(LinearLayout.VERTICAL);
        llScrollContainer.setFocusable(false);

        // If API is below 24, then we have to transform rootLayout margins into paddings or margin spaces
        // won't be respected once the rootLayout is wrapped into the scroller
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            llScrollContainer.setPadding(
                    rootLayoutParams.leftMargin, rootLayoutParams.topMargin,
                    rootLayoutParams.rightMargin, rootLayoutParams.bottomMargin);
        }

        return llScrollContainer;

    }

    /**
     * Gets the layout for the extra bottom space added to extends space and make scrolling possible
     */
    private void calculateExtraBottomSpace(SanKeyboardView sanKeyboardView) {

        if (extraBottomSpace == null) {
            extraBottomSpace = new View(context);
        }

        // Measure keyboard height
        sanKeyboardView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);

        // Create Space View with height = Keyboard Height + Navigation Bar Height
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                sanKeyboardView.getMeasuredHeight());

        extraBottomSpace.setLayoutParams(slp);

    }

    /**
     * Calculates the position to scroll when the EditText gets the focus
     */
    private int getEditTextScrollPosition(BaseSecureEditText editTextField){

        ViewGroup rootLayout = (ViewGroup) editTextField.getParent();

        int scrollOffset = editTextField.getTop();

        // Backtrack to the rootLayout to calculate how long is the offset that we have to scroll the
        // the editText since "getTop()" returns relative top position to the parent and we need
        // the full length (from the root)
        while (rootLayout != null && rootLayout.getId() != rootLayoutIdForAutoScrolling) {

            scrollOffset += rootLayout.getTop();

            rootLayout = (ViewGroup) rootLayout.getParent();

        }

        // If offset calculation was succesful
        if (rootLayout != null && scrollOffset > 0) {

            // Return scroll offset minus text field height scaled by density to give some margin on top
            return (int) (scrollOffset -
                    editTextField.getMeasuredHeight() * context.getResources().getDisplayMetrics().density);

        } else {

            // Return default Top or Bottom from editTextField whenever it's not zero (or scrolling is ignored)
            return editTextField.getTop() != 0 ? editTextField.getTop() : editTextField.getBottom();

        }

    }

    /**
     * When using Non-Strict Mode, this makes the extra bottom space visible to force scrolling the view if
     * there is no enough space
     *
     * @param secureEditText  secureEditText that request scrolling
     * @param sanKeyboardView keyboard for measuring extra bottom space if necessary
     */
    public void showExtraSpaceForScrolling(BaseSecureEditText secureEditText, SanKeyboardView sanKeyboardView) {

        // If other BaseSecureEditText calls to show ExtraBottomSpace before it gets hidden, then mark it as
        // reseted before stopped to maintain the state
        if (currentManagerStatus == AutoScrollManagerStatus.SETTED &&
                extraBottomSpace.getVisibility() == View.VISIBLE) {

            isResetedBeforeStopped = true;

        }

        // Show Extra Bottom Space and Scroll to the field
        if (currentManagerStatus.equals(AutoScrollManagerStatus.SETTED) && extraBottomSpace != null) {

            // Recalculate Extra Bottom Space for the new keyboard
            if (hasKeyboardLayoutChanged(secureEditText)) {
                calculateExtraBottomSpace(sanKeyboardView);
            }

            extraBottomSpace.setVisibility(View.VISIBLE);

            if (lockableScrollView != null) {
                lockableScrollView.setEnableScrolling(true);
            }

            scrollToField(secureEditText);

        }

    }

    /**
     * When using Non-Strict Mode, this hides the extra bottom space and locks the scroll if necessary
     */
    public void hideExtraBottomSpaceForScrolling() {

        if (currentManagerStatus.equals(AutoScrollManagerStatus.SETTED)) {

            // If another EditText request a scroll before the previous one is cancelled, then
            // abort stop scrolling system and reuse it.
            if (isResetedBeforeStopped) {

                isResetedBeforeStopped = false;
                extraBottomSpace.setVisibility(View.VISIBLE);

                if (lockableScrollView != null) {
                    lockableScrollView.setEnableScrolling(true);
                }

                return;

            }

            extraBottomSpace.setVisibility(View.GONE);

            restoreScrollPosition();

            // If not reusing an existing ScrollView or NestedScrollView, then lock scroll when
            // scroll system is stopped/hidden
            if (lockableScrollView != null) {

                // Restore original scroll position before keyboard was shown
                lockableScrollView.setEnableScrolling(false);

            }

        }

    }

    /**
     * Checks if the current SecureEditText requesting layout it's the same
     * than the last one that did it.
     *
     * @param secureEditText SecureEditText for comparing to the last one used
     * @return true if current SecureEditText is distinct from last one. False otherwise
     */
    private boolean hasKeyboardLayoutChanged(BaseSecureEditText secureEditText) {

        boolean result = lastSecureEditTextRequest == null ||
                lastSecureEditTextRequest.getKeyboardType() != secureEditText.getKeyboardType() ||
                lastSecureEditTextRequest.getTopRowButtons() != secureEditText.getTopRowButtons();

        if (result) {
            lastSecureEditTextRequest = secureEditText;
        }

        return result;

    }


    /**
     * Resets isResetedBeforeStopped flag removing any extra request
     */
    public void clearExtraRequests() {
        isResetedBeforeStopped = false;
    }

    /**
     * Tries to find Coordinator Behaviour setted on the RootLayout supplied
     * @param rootLayout The rootLayout choosen by user to be wrapped by AutoScrollingBehaviour
     * @return If RootLayout has a {@link CoordinatorLayout.Behavior}, extract it and return it;
     *          NULL otherwise
     */
    @Nullable
    private CoordinatorLayout.Behavior getCoordinatorLayoutBehaviour(View rootLayout){

        CoordinatorLayout.Behavior inheritedBehaviour = null;

        try {

            CoordinatorLayout.LayoutParams cllp = (CoordinatorLayout.LayoutParams) rootLayout.getLayoutParams();
            inheritedBehaviour = cllp.getBehavior();

        } catch(Exception ignored){

            // RootLayout is not a CoordinatorLayout child; NULL will be returned

        }

        return inheritedBehaviour;

    }

    /**
     * Called before scroll to the SecureEditText field for creating a backup of the original scroll
     * position in order to be used before when restoring previous situation
     */
    private void backupOriginalScrollPosition() {

        // Don't backup if there is already a backup saved
        if (originalScrollPosition != null) {
            return;
        }

        switch (currentRootLayoutType) {

            case SCROLL_VIEW:
                originalScrollPosition = new Point(autoScrollView.getScrollX(), autoScrollView.getScrollY());
                break;

            case NESTED_SCROLL_VIEW:
                originalScrollPosition = new Point(nestedAutoScrollView.getScrollX(), nestedAutoScrollView.getScrollY());
                break;

            case VIEW_GROUP:
                originalScrollPosition = new Point(lockableScrollView.getScrollX(), lockableScrollView.getScrollY());
                break;
        }

    }

    /**
     * Called when hiding the keyboard in non-strict mode. This restore the original scroll position
     * since if it's a LockableScrollView, user won't be able to scroll and must see on the screen
     * the same than before opening the keyboard
     */
    private void restoreScrollPosition() {

        // Scroll to the field
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (originalScrollPosition != null) {

                    switch (currentRootLayoutType) {

                        case SCROLL_VIEW:
                            if (autoScrollView != null){
                                autoScrollView.smoothScrollTo(originalScrollPosition.x, originalScrollPosition.y);
                            }
                            break;

                        case NESTED_SCROLL_VIEW:
                            if (nestedAutoScrollView != null){
                                nestedAutoScrollView.smoothScrollTo(originalScrollPosition.x, originalScrollPosition.y);
                            }
                            break;

                        case VIEW_GROUP:
                            if (lockableScrollView != null){
                                lockableScrollView.smoothScrollTo(originalScrollPosition.x, originalScrollPosition.y);
                            }
                            break;

                    }

                    originalScrollPosition = null;

                }

            }
        }, SCROLL_DELAY);

    }

    /**
     * Called when last instance of BaseSecureEditText uses this AutoScrollManager to remove
     * references (specially Context) and avoid memory leaks
     */
    void onDestroy() {

        context = null;

        autoScrollView = null;
        nestedAutoScrollView = null;
        lockableScrollView = null;

        extraBottomSpace = null;

        lastSecureEditTextRequest = null;

    }

    /**
     * Resets this AutoScrollManager to its initial status but when it's already created
     * This forces to rewrap the content layout into the scrolling system if wrapper is lost.
     */
    void resetIfNecessary() {

        // If wrapper reference is lost but the AutoScrollManager is SETTED (or STARTING or STOPPING),
        // or the reference is kept but its rootView it's not a DecorView, then the wrapper has been
        // removed from the current window and scrolling actions won't take effect. In both cases, we
        // have to force reset to original status to rewrap the layout. This happens when AutoScroll
        // is already created but the layout associated was destroyed and recreated
        if (scrollerIsNotCorrectlySetted(autoScrollView) &&
                scrollerIsNotCorrectlySetted(nestedAutoScrollView) &&
                scrollerIsNotCorrectlySetted(lockableScrollView) && currentManagerStatus != AutoScrollManagerStatus.NOT_SETTED) {

            autoScrollView = null;
            nestedAutoScrollView = null;
            lockableScrollView = null;

            currentManagerStatus = AutoScrollManagerStatus.NOT_SETTED;
            isResetedBeforeStopped = false;

            originalScrollPosition = null;
            lastSecureEditTextRequest = null;
            extraBottomSpace = null;

        }

    }

    /**
     * Checks if a Scroller ViewGroup (ScrollView, NestedScrollView or LockableScrollView) is setted and if
     * it is, if it's correctly setted: RootView for the Scroller ViewGroup must return a DecorView (top
     * of an activity). If not, it means that the Scroller has been removed from the current window and AutoScroller
     * needs a reset
     *
     * @param scroller Scroll ViewGroup to check setting status
     * @return true if the scroller is null or is not setted within a valid Window; false otherwise (
     */
    private boolean scrollerIsNotCorrectlySetted(ViewGroup scroller) {
        return scroller == null ||
                !scroller.getRootView().getClass().getName().equals("com.android.internal.policy.DecorView");
    }

}
