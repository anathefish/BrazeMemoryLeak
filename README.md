# Braze Memory Leak Sample

This is a sample app to investigate a potential memory leak in `BrazeActivityLifecycleCallbackListener` (Braze SDK v40.1.0).

## Issue Summary

When using `BrazeActivityLifecycleCallbackListener`, Activities may not be garbage collected after being destroyed. The SDK's internal synthetic lambdas capture Activity references and queue them in a `LinkedBlockingQueue` that never drains.

## Environment

- **Braze SDK Version**: 40.1.0
- **Android Compile SDK**: 35
- **Android Target SDK**: 35
- **Android Min SDK**: 29
- **Kotlin**: 2.2.21
- **Gradle**: 8.14.3
- **AGP**: 8.13.1

## Steps to Reproduce

1. Open project in Android Studio
2. Run the app on a device or emulator
3. Use the **Stress Test** to rapidly cycle through activities:
   - Navigate to Stress Test from any activity
   - Run 20-100 cycles
4. Use the **Background/Foreground Test** to cycle app between background and foreground
5. Check LeakCanary notification or use Android Studio Memory Profiler to take a heap dump

## Expected Behavior

Activities should be garbage collected after `onDestroy()` is called.

## Actual Behavior

Destroyed Activities may be retained in memory. Heap dump shows multiple instances of destroyed activities being held by:

```
Activity (mDestroyed = true)
    |
f$0 in Braze$$ExternalSyntheticLambda180
    |
e in q (Braze internal class)
    |
continuation in DispatchedContinuation
    |
item in LinkedBlockingQueue$Node
    |
next in LinkedBlockingQueue$Node (infinite chain)
```

## Workaround

Disabling `BrazeActivityLifecycleCallbackListener` eliminates the leak:

```kotlin
// Disabled due to memory leak
// registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener(true, true))
```

## App Features

- **MainActivity, SecondActivity, ThirdActivity**: Activities with paging image galleries
- **StressTestActivity**: Rapidly cycles through all activities to stress test memory
- **BackgroundStressTestActivity**: Cycles app between background and foreground
- **LeakCanary**: Automatically detects and reports memory leaks
- **Hilt**: Dependency injection matching production app setup
- **Paging 3**: Infinite scrolling with REST and GraphQL APIs
