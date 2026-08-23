# ProGuard & R8 optimization rules for CleanBar

# Preserve Shizuku APIs and Reflection
-keep class rikka.shizuku.** { *; }
-keepclassmembers class rikka.shizuku.Shizuku {
    public static java.lang.Process newProcess(java.lang.String[], java.lang.String[], java.lang.String);
    public static boolean pingBinder();
    public static int checkSelfPermission();
    public static void requestPermission(int);
    public static void addBinderReceivedListenerSticky(rikka.shizuku.Shizuku$OnBinderReceivedListener);
    public static void addBinderDeadListener(rikka.shizuku.Shizuku$OnBinderDeadListener);
    public static void removeBinderReceivedListener(rikka.shizuku.Shizuku$OnBinderReceivedListener);
    public static void removeBinderDeadListener(rikka.shizuku.Shizuku$OnBinderDeadListener);
    public static void addRequestPermissionResultListener(rikka.shizuku.Shizuku$OnRequestPermissionResultListener);
    public static void removeRequestPermissionResultListener(rikka.shizuku.Shizuku$OnRequestPermissionResultListener);
}

# Preserve Compose Runtime
-keepclassmembers class * extends androidx.activity.ComponentActivity {
    <init>();
}
