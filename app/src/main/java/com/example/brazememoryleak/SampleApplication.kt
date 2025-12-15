package com.example.brazememoryleak

import android.app.Application
import android.util.Log
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig
import dagger.hilt.android.HiltAndroidApp

/**
 * Sample application demonstrating memory leak in BrazeActivityLifecycleCallbackListener.
 *
 * The leak occurs when:
 * 1. BrazeActivityLifecycleCallbackListener is registered
 * 2. Activities are opened and closed multiple times
 * 3. Destroyed activities are retained via Braze's internal lambdas queued in LinkedBlockingQueue
 *
 * To reproduce:
 * 1. Run the app
 * 2. Navigate between activities multiple times (use the buttons)
 * 3. LeakCanary will detect and report the leaks
 * 4. Or take a heap dump in Android Studio Memory Profiler
 */
@HiltAndroidApp
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Registering BrazeActivityLifecycleCallbackListener...")

        // This registration causes the memory leak
        registerActivityLifecycleCallbacks(
            BrazeActivityLifecycleCallbackListener(
                /* sessionHandlingEnabled */ true,
                /* registerInAppMessageManager */ true
            )
        )

        // Configure Braze
        // API key and endpoint are set in res/values/braze.xml
        val brazeConfig = BrazeConfig.Builder()
            .setDefaultNotificationChannelName("Marketing Push")
            .setDefaultNotificationChannelDescription("Marketing related push")
            .setPushDeepLinkBackStackActivityEnabled(false)
            .setHandlePushDeepLinksAutomatically(true)
            .build()

        Braze.configure(this, brazeConfig)

        Log.d(TAG, "Braze SDK configured with version 40.1.0")
    }

    companion object {
        private const val TAG = "BrazeLeakSample"
    }
}
