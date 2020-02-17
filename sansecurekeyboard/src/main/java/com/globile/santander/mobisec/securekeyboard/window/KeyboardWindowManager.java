package com.globile.santander.mobisec.securekeyboard.window;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * KeyboardWindowManager is in charge of managing Window configuration and theming for showing correctly the Secure Keyboard.
 *
 * Everytime that a SecureEditText is going to show its associated keyboard, an instance of KeyboardWindowManager adjust
 * app theme to show the keyboard without any overlapping or visualization problem. Mainly now it's taking care about
 * translucent navigation on status and navigation system bar
 *
 * Once the keyboard is hidden, the Window configuration is restored to the original theme
 */
public class KeyboardWindowManager {

    private Window currentWindow;

    private boolean isWindowTranslucentNavigationFlag;
    private boolean isWindowTranslucentStatusFlag;
    private boolean isWindowDrawSystemBarBackgrounds;

    private int windowDecorViewSystemUiVisibility;

    private boolean areFlagsInitiatedForShowingKeyboard;

    /**
     * Creates a new instances of KeyboardWindowManager for managing the Window provided as parameter. Besides, Window theme
     * configuration is backed up for restoring original situation when keyboard is hidden
     *
     * @param currentWindow The Window to be managed and from which the configuration will be extracted
     */
    KeyboardWindowManager(Window currentWindow) {
        this.currentWindow = currentWindow;
        areFlagsInitiatedForShowingKeyboard = false;
    }

    /**
     * Returns the current window managed by this manager
     * @return Current window managed by this manager
     */
    Window getManagedWindow() {
        return currentWindow;
    }


    /**
     * Sets the flags necessaries for showing correctly the keyboard without any overlapping by system widget
     */
    public void initFlagsForShowingKeyboard() {

        if (!areFlagsInitiatedForShowingKeyboard){

            backupOriginalFlags();

            // If using Translucent in Status & Navigation bars, clear the translucent on the Navigation
            // bar to avoid Navigation bar overlapping the KeyboardView
            if (isWindowTranslucentNavigationFlag) {

                currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                // If Navigation bar is translucent but NOT the Status Bar, we have to force to "DRAW SYSTEM
                // BAR BACKGROUNDS" in order to maintain the original color in the status bar
                if (!isWindowTranslucentStatusFlag){

                    currentWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    currentWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                }

            }

            areFlagsInitiatedForShowingKeyboard = true;

        }



    }

    /**
     * Restore original configuration copied from the Window managed
     */
    public void restoreOriginalFlags() {

        if (areFlagsInitiatedForShowingKeyboard){

            restoreFlag(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, isWindowTranslucentNavigationFlag);
            restoreFlag(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, isWindowTranslucentStatusFlag);
            restoreFlag(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, isWindowDrawSystemBarBackgrounds);

            currentWindow.getDecorView().setSystemUiVisibility(windowDecorViewSystemUiVisibility);

            areFlagsInitiatedForShowingKeyboard = false;

        }

    }

    /**
     * Sets a value to the supplied flag
     * @param newValue  The value to set
     * @param flag  The flag to be modified
     */
    private void restoreFlag(int flag, boolean newValue) {

        if (newValue) {
            currentWindow.addFlags(flag);
        } else {
            currentWindow.clearFlags(flag);
        }

    }

    private void backupOriginalFlags(){

        isWindowTranslucentNavigationFlag =
                (currentWindow.getAttributes().flags & android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) != 0;
        isWindowTranslucentStatusFlag =
                (currentWindow.getAttributes().flags & android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
        isWindowDrawSystemBarBackgrounds =
                (currentWindow.getAttributes().flags & android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) != 0;

        windowDecorViewSystemUiVisibility = currentWindow.getDecorView().getSystemUiVisibility();

    }

    /**
     * Release resources on destroy
     */
    void onDestroy() {
        currentWindow = null;
    }
}
