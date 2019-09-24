package vk.help.imagepicker.features.common;

import vk.help.imagepicker.model.Folder;
import vk.help.imagepicker.model.Image;

import java.util.List;

public interface ImageLoaderListener {
    void onImageLoaded(List<Image> images, List<Folder> folders);
    void onFailed(Throwable throwable);
}
