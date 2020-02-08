package vk.help.imagepicker.features.camera;

import java.util.List;

import vk.help.imagepicker.model.Image;

public interface OnImageReadyListener {
    void onImageReady(List<Image> image);
}
