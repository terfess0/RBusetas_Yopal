# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.terfess.busetasyopal.modelos_dato* { *; }
-keep class com.terfess.busetasyopal.admin** { *; }
-keep class com.terfess.busetasyopal.room** { *; }
-keep class com.terfess.busetasyopal.clases_utiles.UtilidadesMenores* { *; }
-keep class com.terfess.busetasyopal.actividades.Splash** { *; }

-keep class android.app.servertransaction.** { *; }
-keep class android.os.** { *; }

-dontwarn io.grpc.internal.DnsNameResolverProvider
-dontwarn io.grpc.internal.PickFirstLoadBalancerProvider
