package com.kissspace.webview.jsbridge;


public interface WebViewJavascriptBridge {
	
	void sendToWeb(String data);

	void sendToWeb(String data, OnBridgeCallback responseCallback);

	void sendToWeb(String function, Object... values);

}
