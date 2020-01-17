package vk.help.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ExtendedWebView extends WebView {

    private Context context;
    private ValueCallback<Uri[]> filePathCallback = null;

    public ExtendedWebView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ExtendedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    void init() {
        getSettings().setAllowFileAccess(true);
        getSettings().setJavaScriptEnabled(true);
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> _filePathCallback, FileChooserParams fileChooserParams) {
                filePathCallback = _filePathCallback;
                return checkPermissions();
            }
        });
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionList.size() > 0) {
                ActivityCompat.requestPermissions(((AppCompatActivity) context), permissionList.toArray(new String[0]), 100);
                return false;
            } else {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                ((AppCompatActivity) context).startActivityForResult(Intent.createChooser(i, "File Browser"), 101);
                return true;
            }
        } else {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            ((AppCompatActivity) context).startActivityForResult(Intent.createChooser(i, "File Browser"), 101);
            return true;
        }
    }

    public void handleResult(int requestCode, int resultCode, @Nullable Intent intent) {
        Uri result = (requestCode == 101 && resultCode == Activity.RESULT_OK && intent != null) ? intent.getData() : null;
        onReceiveValue(new Uri[]{result});
    }

    private void onReceiveValue(Uri[] result) {
        try {
            filePathCallback.onReceiveValue(result);
        } catch (Exception e) {
            filePathCallback.onReceiveValue(null);
            e.printStackTrace();
        } finally {
            filePathCallback = null;
        }
    }
}