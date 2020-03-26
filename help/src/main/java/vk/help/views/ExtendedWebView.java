package vk.help.views;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import vk.help.Common;
import vk.help.R;

public class ExtendedWebView extends WebView {

    private Context context;
    private ValueCallback<Uri[]> filePathCallback = null;
    private String pickFileType = "*/*";

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
        addJavascriptInterface(new WebViewJavaScriptInterface(), "android");
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> _filePathCallback, FileChooserParams fileChooserParams) {
                filePathCallback = _filePathCallback;
                return checkPermissions();
            }
        });
    }

    private void setFileType(String _type) {
        pickFileType = _type;
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
            i.setType(pickFileType);
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

    public class WebViewJavaScriptInterface {

        @JavascriptInterface
        public void downloadFile(String url) {
            ArrayList<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionList.size() > 0) {
                ActivityCompat.requestPermissions(((AppCompatActivity) context), permissionList.toArray(new String[0]), 1122);
            } else {
                new FileDownload().execute(url);
            }
        }
    }

    class FileDownload extends AsyncTask<String, Integer, Boolean> {

        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        private String savePath;
        private String channelID = "channelID";

        protected void onPreExecute() {
            super.onPreExecute();
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context, channelID);
            mBuilder.setContentTitle("File Download").setContentText("Download in progress").setSmallIcon(R.drawable.ic_cloud_download_black_24dp).setDefaults(Notification.DEFAULT_ALL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(channelID, "download notification", NotificationManager.IMPORTANCE_LOW);
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
            mNotifyManager.notify(0, mBuilder.build());
            Common.INSTANCE.showToast(context, "Download File, See Notification");
        }

        protected Boolean doInBackground(String... params) {
            try {
                int count;
                URL url = new URL(params[0]);
                File f = new File(Environment.getExternalStorageDirectory().getPath());
                if (f.exists()) {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    InputStream is = con.getInputStream();
                    String path = url.getPath();
                    String filename = path.substring(path.lastIndexOf('/') + 1);
                    savePath = f.getPath() + "/Download/" + filename;
                    FileOutputStream fos = new FileOutputStream(savePath);
                    int lengthOfFile = con.getContentLength();
                    byte[] data = new byte[1024];
                    long total = 0;
                    while ((count = is.read(data)) != -1) {
                        total += count;
                        fos.write(data, 0, count);
                        publishProgress((int) ((total * 100) / lengthOfFile));
                        if (isCancelled()) {
                            return false;
                        }
                    }
                    is.close();
                    fos.flush();
                    fos.close();
                    return true;
                } else {
                    Log.e("Error", "Not found: " + f.getPath());
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            try {
                if (new File(savePath).delete()) {
                    Common.INSTANCE.showToast(context, "Canceled");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            mBuilder.setProgress(100, progress[0], false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(channelID, "download notification", NotificationManager.IMPORTANCE_LOW);
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
            mNotifyManager.notify(0, mBuilder.build());
        }

        protected void onPostExecute(Boolean result) {
            mBuilder.setContentText(result ? "Download complete" : "Failed");
            mBuilder.setProgress(0, 0, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(channelID, "download notification", NotificationManager.IMPORTANCE_LOW);
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
            mNotifyManager.notify(0, mBuilder.build());
        }
    }
}