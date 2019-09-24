package vk.help.imagepicker.listeners;

import vk.help.imagepicker.model.Image;

import java.util.List;

public interface OnImageSelectedListener {
    void onSelectionUpdate(List<Image> selectedImage);
}
