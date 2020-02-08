package vk.help.imagepicker.features.cameraonly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import vk.help.imagepicker.features.ImagePickerActivity;
import vk.help.imagepicker.features.ImagePickerConfigFactory;
import vk.help.imagepicker.features.IpCons;

public class ImagePickerCameraOnly {

    private CameraOnlyConfig config = ImagePickerConfigFactory.createCameraDefault();

    public ImagePickerCameraOnly imageDirectory(String directory) {
        config.setImageDirectory(directory);
        return this;
    }

    public ImagePickerCameraOnly imageFullDirectory(String fullPath) {
        config.setImageFullDirectory(fullPath);
        return this;
    }

    public void start(Activity activity) {
        start(activity, IpCons.RC_IMAGE_PICKER);
    }

    public void start(Activity activity, int requestCode) {
        activity.startActivityForResult(getIntent(activity), requestCode);
    }

    public void start(Fragment fragment) {
        start(fragment, IpCons.RC_IMAGE_PICKER);
    }

    public void start(Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getIntent(fragment.getActivity()), requestCode);
    }

    public void start(android.app.Fragment fragment) {
        start(fragment, IpCons.RC_IMAGE_PICKER);
    }

    public void start(android.app.Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getIntent(fragment.getActivity()), requestCode);
    }

    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(CameraOnlyConfig.class.getSimpleName(), config);
        return intent;
    }
}
