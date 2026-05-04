# Critical

Critical is a special way to call native functions 
without the overhead of the JVM/Android environment.

Supported when using:

- Foreign Functions & Memory API
- JVMCI
- Android

To activate this acceleration, you need to add the `[Critical]`
annotation to the function in the NDL file:
```webidl
namespace global {
    [Critical] void fastFunc();
}
```

!!! danger "The function must meet the following requirements:"

    - Returns a primitive value only (arrays and strings are not permitted)
    - Accepts only string, primitive or primitive array arguments (callbacks and dictionaries are not allowed)
    - **Must** execute and return as quickly as possible

You can read more about how it works in the CriticalNative [documentation](https://developer.android.com/reference/dalvik/annotation/optimization/CriticalNative) for Android.