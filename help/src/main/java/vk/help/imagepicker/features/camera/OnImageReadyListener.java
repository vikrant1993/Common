package vk.help.imagepicker.features.camera;

import vk.help.imagepicker.model.Image;

import java.util.List;

public interface OnImageReadyListener {
    void onImageReady(List<Image> image);
}
