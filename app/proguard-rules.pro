# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html


#okhttp 相关
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase


#gson and model

-keep class im.fdx.v2ex.ui.main.model.**



-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn org.greenrobot.greendao.**
-dontwarn com.google.devtools.**

-keep class org.jsoup.**

-keep class android.support.** { *; }

-keepattributes Signature

-dontwarn kotlin.**

-keep class * implements android.os.Parcelable {
  *;
}

-keep class * implements im.fdx.v2ex.model.VModel

#Glide
-keep class com.bumptech.glide.**

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


# retrofit2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions