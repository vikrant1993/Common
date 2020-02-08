package vk.help.imagepicker.features.common;

import java.util.List;

import vk.help.imagepicker.model.Folder;
import vk.help.imagepicker.model.Image;

public interface ImageLoaderListener {
    void onImageLoaded(List<Image> images, List<Folder> folders);
    void onFailed(Throwable throwable);
}
