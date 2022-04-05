package com.oxgames.rufflewrapper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.webviewlocalserver.WebViewLocalServer;

public class MainActivity extends AppCompatActivity {
    private WebView wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HideStatusBar();
        HideActionBar();

        WebViewLocalServer assetServer = new WebViewLocalServer(this);
        WebViewLocalServer.AssetHostingDetails details = assetServer.hostAssets("ruffle-nightly-2022_04_05-web-selfhosted");
//        Log.d("Prefix", details.getHttpsPrefix().toString());

        wrapper = findViewById(R.id.web_view);
        WebSettings webSettings = wrapper.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            webSettings.setSafeBrowsingEnabled(false);

        wrapper.setWebViewClient(new WebViewClient() {
//            // For KitKat and earlier.
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                return assetServer.shouldInterceptRequest(url);
//            }
            // For Lollipop and above.
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetServer.shouldInterceptRequest(request);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e("WebView", error.getDescription().toString());
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("Page Loaded", url);
            }
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.e("Http Error", errorResponse.toString());
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.e("SSL Error", error.toString());
            }
        });
        wrapper.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("Console", consoleMessage.message());
                return true;
            }
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d("JS Alert", message);
                return super.onJsAlert(view, url, message, result);
            }
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                Log.d("On Create Window", resultMsg.toString());
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }
        });

        wrapper.loadUrl(details.getHttpsPrefix().toString() + "/index.html");
//        wrapper.loadUrl("file:///android_asset/ruffle.html");
//        String html = "<script src=\"file:///ruffle.js\"></script>";
//        wrapper.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", "");
    }

    private void HideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    private void HideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
    }
}