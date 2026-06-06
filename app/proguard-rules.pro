-keepnames class * extends android.app.Activity
-keepnames class * extends androidx.appcompat.app.AppCompatActivity
-keepnames class * extends androidx.fragment.app.FragmentActivity
-keepnames class * extends androidx.fragment.app.Fragment
-keep class com.twinalyze.** { *; }


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
#-dontwarn com.android.volley.DefaultRetryPolicy
#-dontwarn com.android.volley.Request
#-dontwarn com.android.volley.RequestQueue
#-dontwarn com.android.volley.Response$ErrorListener
#-dontwarn com.android.volley.Response$Listener
#-dontwarn com.android.volley.RetryPolicy
#-dontwarn com.android.volley.toolbox.JsonObjectRequest
#-dontwarn com.android.volley.toolbox.Volley
#-dontwarn io.sentry.ILogger
#-dontwarn io.sentry.IScope
#-dontwarn io.sentry.ISentryClient
#-dontwarn io.sentry.Integration
#-dontwarn io.sentry.Scope
#-dontwarn io.sentry.ScopeCallback
#-dontwarn io.sentry.Scopes
#-dontwarn io.sentry.Sentry
#-dontwarn io.sentry.SentryClient
#-dontwarn io.sentry.SentryOptions$BeforeSendCallback
#-dontwarn io.sentry.SentryOptions$Logs
#-dontwarn io.sentry.SentryOptions
#-dontwarn io.sentry.UncaughtExceptionHandlerIntegration
#-dontwarn io.sentry.android.core.AndroidLogger
#-dontwarn io.sentry.logger.ILoggerApi

