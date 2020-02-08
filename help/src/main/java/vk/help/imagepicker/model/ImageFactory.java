package vk.help.imagepicker.model;

import java.util.ArrayList;
import java.util.List;

import vk.help.imagepicker.helper.ImagePickerUtils;

public class ImageFactory {

    public static List<Image> singleListFromPath(String path) {
        List<Image> images = new ArrayList<>();
        images.add(new Image(0, ImagePickerUtils.getNameFromFilePath(path), path));
        return images;
    }
}
