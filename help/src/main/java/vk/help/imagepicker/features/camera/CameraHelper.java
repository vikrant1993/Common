package vk.help.imagepicker.features.camera;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;

import vk.help.R;

public class CameraHelper {
    public static boolean checkCameraAvailability(Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean isAvailable = intent.resolveActivity(context.getPackageManager()) != null;

        if (!isAvailable) {
            Context appContext = context.getApplicationContext();
            Toast.makeText(appContext,
                    appContext.getString(R.string.no_camera), Toast.LENGTH_LONG).show();
        }
        return isAvailable;
    }
}
