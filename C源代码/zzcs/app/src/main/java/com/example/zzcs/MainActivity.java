package com.example.zzcs;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class MainActivity extends AppCompatActivity {

    private static final int PERMS_REQUEST_CODE = 0;
    private WebView webView;
    private  int REQUEST_CODE=200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZXingLibrary.initDisplayOpinion(this);


        //进入程序时提示要开启什么权限
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //是否开启相机权限
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            builder.setIcon(R.drawable.ic_baseline_build_24)
                    .setTitle("权限开启")
                    .setMessage("您需要开启、相机权限，否则无法使用扫一扫")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            admin();
                            init();
                        }
                    })
                    .create()
                    .show();
        }else{
            init();
        }
    }

    public void init(){

        webView = (WebView) findViewById(R.id.webView);
        WebSettings ws = webView.getSettings();
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setJavaScriptEnabled(true);
        ws.setAppCacheEnabled(true);
        ws.setSupportZoom(true);
        ws.setAllowFileAccess(true); //设置可以访问文件
        ws.setDomStorageEnabled(true);
        ws.setAppCacheMaxSize(1024 * 1024 * 8);
        ws.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        ws.setLoadsImagesAutomatically(true); //支持自动加载图片
        ws.setDefaultTextEncodingName("utf-8");//设置编码格式
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setPluginState(WebSettings.PluginState.ON);
        //设置缓存模式
        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //开启DOM storage API 功能
        ws.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new JSInterface(),"app");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        String files = "file:";
        String path = "///android_asset/dist/";
        String mainPath = "index.";
        String doc = "html";
        webView.loadUrl(files+path+mainPath+doc);

    }

    private final class JSInterface{
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void sys(String userInfo){
            cu();
        }
    }

    public void cu(){
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void admin(){
        //让用户手动打开需要的权限
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri1 = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
        intent.setData(uri1);
        startActivity(intent);
    }
}