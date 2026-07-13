# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
-keep class com.marketplace.notification.data.** { *; }
-keep class com.jcraft.jsch.** { *; }
-dontwarn com.sun.mail.**
-dontwarn javax.mail.**
