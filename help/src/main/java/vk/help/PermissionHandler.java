package vk.help;

import android.app.Activity;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import vk.help.permission.PermissionResultsListener;

public class PermissionHandler {

    private boolean isProcessOnReject = false;
    private Activity activity;
    private PermissionResultsListener listener;

    public PermissionHandler(Activity baseActivity, PermissionResultsListener _listener) {
        activity = baseActivity;
        listener = _listener;
    }

    public void checkPermission(String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.onRequestPermissionsResult(0, null, null);
            if (permissions.length > 0) {
                ActivityCompat.requestPermissions(activity, permissions, 100);
            } else {
                listener.doWhat();
                Common.INSTANCE.showToast(activity, "All Permissions are Allowed");
            }
        }
    }
}