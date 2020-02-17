package com.globile.santander.mobisec.securekeyboard.window;

import android.view.Window;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class.
 *
 * KeyboardWindowSynchronizer manages instances for {@link KeyboardWindowManager}. All the BaseSecureEditText
 * using same RootLayoutResourcedId share same KeyboardWindowManager instance (as the Window it's the same for
 * all BaseSecureEditText on layout)
 */
public class KeyboardWindowSynchronizer {

    private static KeyboardWindowSynchronizer INSTANCE;

    private Map<Window, KeyboardWindowManager> windowsMap;

    // Map of <KeyboardWindowManager, SubscribersCount> to count how many EditText are sharing same
    // Window
    private Map<KeyboardWindowManager, Integer> subscribersMap;

    // CONSTRUCTORS
    // ***********************************************************************

    public static KeyboardWindowSynchronizer getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new KeyboardWindowSynchronizer();
        }

        return INSTANCE;

    }

    private KeyboardWindowSynchronizer() {

    }

    // METHODS
    // ***********************************************************************

    /**
     * Gets an instance of KeyboardWindowManager for the supplied Window
     *
     * @param window The window for which the manager must be obtained
     * @return KeyboardWindowManager for the supplied Window
     */
    public KeyboardWindowManager getWindowManagerFor(@NonNull Window window) {

        initWindowsMap();

        KeyboardWindowManager manager = windowsMap.get(window);

        if (manager == null) {

            manager = new KeyboardWindowManager(window);
            windowsMap.put(window, manager);

        }

        addSubscriber(manager);

        return manager;

    }

    /**
     * Adds a new subscriber to a KeyboardWindowManager instance.
     * KeyboardWindowSynchronicer counts how many BaseSecureEditText fields are sharing same instance
     * of KeyboardWindowManager to know when the last one release and destroy it
     *
     * @param key Key to locate KeyboardWindowManager instance for RootLayoutID and IndexInViewParent
     */
    private void addSubscriber(KeyboardWindowManager key) {

        initSubscribersMap();

        Integer subscribersCount = subscribersMap.get(key);

        if (subscribersCount == null) {
            subscribersMap.put(key, 1);
        } else {
            subscribersMap.put(key, subscribersCount + 1);
        }

    }

    /**
     * Release an KeyboardWindowManager for key based on the RootLayoutID and IndexInViewParent.
     * If there are more than one BaseSecureEditText sharing the same KeyboardWindowManager instance
     * this method reduces the number of subscribers until it's zero. When there is only one EditText
     * subscribed and it's released, the KeyboardWindowManager instance is destroyed
     */
    public void releaseWindowManagerFor(@NonNull KeyboardWindowManager windowManager) {

        Integer subscribersCount = subscribersMap.get(windowManager);

        if (subscribersCount != null && subscribersCount > 0) {

            if (subscribersCount == 1) {

                KeyboardWindowManager kwm = windowsMap.remove(windowManager.getManagedWindow());

                if (kwm != null) {
                    kwm.onDestroy();
                }

                subscribersMap.remove(windowManager);

            } else {

                subscribersMap.put(windowManager, subscribersCount - 1);

            }

        }

        releaseWindowsMap();
        releaseSubscribersMap();

    }

    /**
     * Initializes map for KeyboardWindowManager instances
     */
    private void initWindowsMap() {

        if (windowsMap == null) {
            windowsMap = new HashMap<>();
        }
    }

    /**
     * Initializes map for Subscribers
     */
    private void initSubscribersMap() {

        if (subscribersMap == null) {
            subscribersMap = new HashMap<>();
        }
    }

    /**
     * Destroys maps for KeyboardWindowManager instances
     */
    private void releaseWindowsMap() {

        if (windowsMap != null && windowsMap.isEmpty()) {
            windowsMap = null;
        }
    }

    /**
     * Destroys maps for Subscribers
     */
    private void releaseSubscribersMap() {

        if (subscribersMap != null && subscribersMap.isEmpty()) {
            subscribersMap = null;
        }

    }

}

