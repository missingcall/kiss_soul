package com.kissspace.webview.jsbridge;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.netease.nimlib.sdk.util.NIMUtil;
import com.tencent.smtt.sdk.WebView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;


public class BridgeUtil {
	private static String currentProcessName = null;
	final static String YY_OVERRIDE_SCHEMA = "yy://";
	final static String YY_RETURN_DATA = YY_OVERRIDE_SCHEMA + "return/";//格式为   yy://return/{function}/returncontent
	final static String YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/";
	final static String EMPTY_STR = "";
	final static String UNDERLINE_STR = "_";
	final static String SPLIT_MARK = "/";
	
	final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
	final static String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative(%s);";
	final static String JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();";
//	public final static String JAVASCRIPT_STR = "javascript:";


	public static final String JAVA_SCRIPT = "WebViewJavascriptBridge.js";
	public final static String JAVASCRIPT_STR = "javascript:%s";

	/**
	 * js 文件将注入为第一个script引用
	 * @param view WebView
	 * @param url url
	 */
	public static void webViewLoadJs(WebView view, String url){
		String js = "var newscript = document.createElement(\"script\");";
		js += "newscript.src=\"" + url + "\";";
		js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);";
		view.loadUrl("javascript:" + js);
	}

	/**
	 * 这里只是加载lib包中assets中的 WebViewJavascriptBridge.js
	 * @param view webview
	 * @param path 路径
	 */
    public static void webViewLoadLocalJs(WebView view, String path){
        String jsContent = assetFile2Str(view.getContext(), path);
		view.loadUrl("javascript:" + jsContent);
    }

	/**
	 * 解析assets文件夹里面的代码,去除注释,取可执行的代码
	 * @param c context
	 * @param urlStr 路径
	 * @return 可执行代码
	 */
	public static String assetFile2Str(Context c, String urlStr){
		InputStream in = null;
		try{
			in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) { // 去除注释
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();
 
            return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static String getFunctionFromReturnUrl(String url) {
		return "";
	}

	public static String getDataFromReturnUrl(String url) {
		return "";
	}

	public static String parseFunctionName(String jsUrl) {
		return "";
	}

	public static boolean isMainProcess(Context context) {
		if (context == null) {
			return false;
		}

		String packageName = context.getApplicationContext().getPackageName();
		String processName = NIMUtil.getProcessName(context);
		return packageName.equals(processName);
	}

	public static String getProcessName(Context context) {
		if(!TextUtils.isEmpty(currentProcessName))
		{
			return currentProcessName;
		}
		currentProcessName = getCurrentProcessNameByApplication();
		if(!TextUtils.isEmpty(currentProcessName))
		{
			return currentProcessName;
		}

		currentProcessName = getCurrentProcessNameByActivityThread();

		if(!TextUtils.isEmpty(currentProcessName))
		{
			return currentProcessName;
		}

		currentProcessName = getProcessFromFile();

		if(!TextUtils.isEmpty(currentProcessName))
		{
			return currentProcessName;
		}

//        // 如果装了xposed一类的框架，上面可能会拿不到，回到遍历迭代的方式
//        currentProcessName = getProcessNameByAM(context);
//        Log.d(TAG, "get processName from ActivityManager = "+currentProcessName);
		return currentProcessName;
	}

	private static String getProcessFromFile() {
		BufferedReader reader = null;
		try {
			int pid = android.os.Process.myPid();
			String file = "/proc/" + pid + "/cmdline";
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));
			int c;
			StringBuilder processName = new StringBuilder();
			while ((c = reader.read()) > 0) {
				processName.append((char) c);
			}
			return processName.toString();
		} catch (Exception e) {
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 通过Application新的API获取进程名，无需反射，无需IPC，效率最高。
	 */
	private static String getCurrentProcessNameByApplication() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			return Application.getProcessName();
		}
		return null;
	}

	/**
	 * 通过反射ActivityThread获取进程名，避免了ipc
	 */
	private static String getCurrentProcessNameByActivityThread() {
		String processName = null;
		try {
			final Method declaredMethod = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader())
					.getDeclaredMethod("currentProcessName", (Class<?>[]) new Class[0]);
			declaredMethod.setAccessible(true);
			final Object invoke = declaredMethod.invoke(null, new Object[0]);
			if (invoke instanceof String) {
				processName = (String) invoke;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return processName;
	}
}
