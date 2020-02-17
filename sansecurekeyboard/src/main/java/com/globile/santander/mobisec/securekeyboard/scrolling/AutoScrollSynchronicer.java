package com.globile.santander.mobisec.securekeyboard.scrolling;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class.
 *
 * AutoScroll Syncronicer manages instances for AutoScrollManager. All the BaseSecureEditText
 * using same RootLayoutResourcedId share same AutoScrollManager instance (as the scroll view
 * and its wrapper it's the same for all BaseSecureEditText on layout)
 */
public class AutoScrollSynchronicer {

    // ATTRIBUTES
    // ***********************************************************************

    private static AutoScrollSynchronicer INSTANCE;

    // Map of  <RootLayoutResourceId, AutoScrollManager>
    private Map<String, AutoScrollManager> autoScrollManagersMap;

    // Map of <RootLayoutResourceId, SubscribersCount> to count how many EditText are sharing same
    // AutoScrollManager
    private Map<String, Integer> managerSubscriptionsMap;

    // CONSTRUCTORS
    // ***********************************************************************

    public static AutoScrollSynchronicer getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new AutoScrollSynchronicer();
        }

        return INSTANCE;

    }

    private AutoScrollSynchronicer() {

    }

    // METHODS
    // ***********************************************************************

    /**
     * Gets an instance of AutoScrollManager for a RootLayoutID in concrete index of the parent
     *
     * @param context              Context instance to get DisplayMetrics
     * @param rootLayoutResourceId Root Layout resource ID to apply Scrolling
     * @param indexInViewParent    Index as a child within its parent (needed to restore layout when stopping scrolling)
     */
    public synchronized AutoScrollManager getAutoScrollManagerFor(@NonNull Context context, String owner,
            @IdRes int rootLayoutResourceId, int indexInViewParent) {

        String key = getKeyForRootLayout(owner, rootLayoutResourceId, indexInViewParent);

        initAutoScrollManagerMap();

        AutoScrollManager manager = autoScrollManagersMap.get(key);

        if (manager == null) {

            manager = new AutoScrollManager(context, rootLayoutResourceId);
            autoScrollManagersMap.put(key, manager);

        } else {

            // Reset the manager to force re-wraping the layout into the scrolling system
            // This is because, a BaseSecureEditText tries to get his AutoScrollManager
            // on the first global layout operation, so if it already exists on the map,
            // then we have to force a reset if layout was destroyed and recreated
            manager.resetIfNecessary();

        }

        addSubscriber(key);

        return manager;

    }

    /**
     * Adds a new suscriber to a AutoScrollManager instance.
     * AutoScrollSynchronicer counts how many BaseSecureEditText fields are sharing same instance
     * of AutoScrollManager to know when the last one release and destroy it
     *
     * @param key Key to locate AutoScrollManager instance for RootLayoutID and IndexInViewParent
     */
    private void addSubscriber(String key) {

        initSubscribersMap();

        Integer subscribersCount = managerSubscriptionsMap.get(key);

        if (subscribersCount == null) {
            managerSubscriptionsMap.put(key, 1);
        } else {
            managerSubscriptionsMap.put(key, subscribersCount + 1);
        }

    }

    /**
     * Release an AutoScrollManager for key based on the RootLayoutID and IndexInViewParent.
     * If there are more than one BaseSecureEditText sharing the same AutoScrollManager instance
     * this method reduces the number of suscribers until it's zero. When there is only one EditText
     * subscribed and it's released, the AutoScrollManager instance is destroyed
     */
    public void releaseAutoScrollManagerFor(String owner, @IdRes int rootLayoutResourceId, int indexInViewParent) {

        String key = getKeyForRootLayout(owner, rootLayoutResourceId, indexInViewParent);

        Integer subscribersCount = managerSubscriptionsMap.get(key);

        if (subscribersCount != null && subscribersCount > 0) {

            if (subscribersCount == 1) {

                AutoScrollManager asm = autoScrollManagersMap.remove(key);

                if (asm != null) {
                    asm.onDestroy();
                }

                managerSubscriptionsMap.remove(key);

            } else {

                managerSubscriptionsMap.put(key, subscribersCount - 1);

            }

        }

        releaseAutoScrollManagerMap();
        releaseSubscribersMap();

    }

    /**
     * Composes a key to identify AutoScrollManager instance for the RootLayoutID and IndexInViewParent supplied
     *
     * @return Key to locate AutoScrollManager in map
     */
    private String getKeyForRootLayout(String owner, @IdRes int rootLayoutResourceId, int rootLayoutIndexInViewPager) {

        return owner + "@" + rootLayoutResourceId + "@" + rootLayoutIndexInViewPager;

    }

    /**
     * Initializes map for AutoScrollManager instances
     */
    private void initAutoScrollManagerMap() {

        if (autoScrollManagersMap == null) {
            autoScrollManagersMap = new HashMap<>();
        }
    }

    /**
     * Initializes map for Subscribers
     */
    private void initSubscribersMap() {

        if (managerSubscriptionsMap == null) {
            managerSubscriptionsMap = new HashMap<>();
        }
    }

    /**
     * Destroys maps for AutoScrollManager instances
     */
    private void releaseAutoScrollManagerMap() {

        if (autoScrollManagersMap != null && autoScrollManagersMap.isEmpty()) {
            autoScrollManagersMap = null;
        }
    }

    /**
     * Destroys maps for Subscribers
     */
    private void releaseSubscribersMap() {

        if (managerSubscriptionsMap != null && managerSubscriptionsMap.isEmpty()) {
            managerSubscriptionsMap = null;
        }

    }

}
