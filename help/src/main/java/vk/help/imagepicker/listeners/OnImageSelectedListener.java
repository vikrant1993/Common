package vk.help.imagepicker.listeners;

import java.util.List;

import vk.help.imagepicker.model.Image;

public interface OnImageSelectedListener {
    void onSelectionUpdate(List<Image> selectedImage);
}
