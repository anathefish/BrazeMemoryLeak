# Braze Memory Leak Sample

This is a minimal sample app demonstrating a memory leak in `BrazeActivityLifecycleCallbackListener` (Braze SDK v40.0.2).

## Issue Summary

When using `BrazeActivityLifecycleCallbackListener`, Activities are not garbage collected after being destroyed. The SDK's internal synthetic lambdas capture Activity references and queue them in a `LinkedBlockingQueue` that never drains.

## Environment

- **Braze SDK Version**: 40.0.2 (leak present)
- **Working Version**: 39.0.0 (no leak)
- **Android Compile SDK**: 34
- **Kotlin**: 1.9.20

## Steps to Reproduce

1. Open project in Android Studio
2. Run the app on a device or emulator
3. Navigate between activities:
   - Tap "Open Second Activity"
   - Press back
   - Repeat 10-20 times
4. LeakCanary will automatically detect and report the leaks
5. Alternatively, use Android Studio Memory Profiler to take a heap dump

## Expected Behavior

Activities should be garbage collected after `onDestroy()` is called.

## Actual Behavior

Destroyed Activities are retained in memory. Heap dump shows multiple instances of destroyed activities being held by:

```
Activity (mDestroyed = true)
    ↓
f$0 in Braze$$ExternalSyntheticLambda180
    ↓
e in q (Braze internal class)
    ↓
continuation in DispatchedContinuation
    ↓
item in LinkedBlockingQueue$Node
    ↓
next in LinkedBlockingQueue$Node (infinite chain)
```

## Workaround

Disabling `BrazeActivityLifecycleCallbackListener` eliminates the leak:

```kotlin
// Disabled due to memory leak
// registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener(true, true))
```

## To Test with SDK 39.0.0 (No Leak)

Change the dependency in `app/build.gradle.kts`:

```kotlin
// Change from:
implementation("com.braze:android-sdk-ui:40.0.2")

// To:
implementation("com.braze:android-sdk-ui:39.0.0")
```

Rebuild and repeat the reproduction steps - no leak will be detected.
