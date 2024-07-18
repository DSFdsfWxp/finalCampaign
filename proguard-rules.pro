-dontobfuscate
-dontshrink

-keep class finalCampaign.** { *; }
-keep class com.** { *; }
-keep class org.** { *; }
-keep class arc.** { *; }
-keep class javax.** { *; }

-dontwarn com.codedisaster.steamworks.**
-dontwarn javax.**
-dontwarn java.**

-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod