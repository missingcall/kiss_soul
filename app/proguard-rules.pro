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
#-keep class io.agora.**{*;}
#-dontwarn com.netease.nim.**
#-keep class com.netease.nim.** {*;}
#
#-dontwarn com.netease.nimlib.**
#-keep class com.netease.nimlib.** {*;}
#
#-dontwarn com.netease.share.**
#-keep class com.netease.share.** {*;}
#
#-dontwarn com.netease.mobsec.**
#-keep class com.netease.mobsec.** {*;}
#-dontwarn org.apache.lucene.**
#-keep class org.apache.lucene.** {*;}
#-keep class net.sqlcipher.** {*;}
#-keep class com.umeng.** { *; }
#
#-keep class com.uc.** { *; }
#
#-keep class com.efs.** { *; }
#
#-keepclassmembers class *{
#     public<init>(org.json.JSONObject);
#}
#-keepclassmembers enum *{
#      publicstatic**[] values();
#      publicstatic** valueOf(java.lang.String);
#}
#-dontwarn dalvik.**
#-dontwarn com.tencent.smtt.**
#
#-keep class com.tencent.smtt.** {
#    *;
#}
#
#-keep class com.tencent.tbs.** {
#    *;
#}
#友盟
-keep class com.umeng.**{*;}

-keepclassmembers class*{
public <init> (org.json.JSONObject);
}

-keepclassmembers enum*{
public static **[] values();
public static ** valueOf(java.lang.String);
}

#网易易盾混淆
-keeppackagenames com.netease.nis.alivedetected
-keep class com.netease.nis.**{*;}
-dontwarn com.netease.nis.alivedetected.**
-keep class com.netease.cloud.nos.yidun.**{*;}
-dontwarn com.netease.cloud.nos.yidun.**
-keep class com.ishumei.** {*;}

-keep class **.zego.**{*;}
-keep class com.ishumei.** {*;}


#友盟分享混淆
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn com.squareup.okhttp.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes EnclosingMethod
-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}

-keep class com.umeng.commonsdk.statistics.common.MLog {*;}
-keep class com.umeng.commonsdk.UMConfigure {*;}
-keep class com.umeng.** {*;}
-keep class com.umeng.**
-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class com.tencent.mm.sdk.** {
   *;
}
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep class com.kakao.** {*;}
-dontwarn com.kakao.**
-keep public class com.umeng.com.umeng.soexample.R$*{
    public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}
-keep class com.umeng.socialize.impl.ImageImpl {*;}
-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keepattributes Signature

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

 -keep class com.netease.htprotect.**{*;}