package vk.help.crop;

public interface CropFragmentCallback {

    void loadingProgress(boolean showLoader);
    void onCropFinish(CropFragment.UCropResult result);

}