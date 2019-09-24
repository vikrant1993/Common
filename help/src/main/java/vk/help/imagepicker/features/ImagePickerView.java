package vk.help.imagepicker.features;

import vk.help.imagepicker.features.common.MvpView;
import vk.help.imagepicker.model.Folder;
import vk.help.imagepicker.model.Image;

import java.util.List;

public interface ImagePickerView extends MvpView {
    void showLoading(boolean isLoading);
    void showFetchCompleted(List<Image> images, List<Folder> folders);
    void showError(Throwable throwable);
    void showEmpty();
    void showCapturedImage();
    void finishPickImages(List<Image> images);
}
