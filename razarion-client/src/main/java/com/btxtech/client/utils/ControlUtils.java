package com.btxtech.client.utils;

import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.FileReader;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class ControlUtils {
    private static final Logger LOGGER = Logger.getLogger(ControlUtils.class.getName());

    public static void readFirstAsDataURL(FileList fileList, BiConsumer<String, File> dataUrlConsumer) {
        try {
            File file = fileList.item(0);
            FileReader fileReader = new FileReader();
            fileReader.onload = progressEvent -> {
                try {
                    dataUrlConsumer.accept(fileReader.result.asString(), file);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Reading file failed", t);
                }
                return null;
            };
            fileReader.readAsDataURL(file);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Start reading file failed", t);
        }
    }

    public static void readFirstAsText(FileList fileList, BiConsumer<String, File> textConsumer) {
        File file = fileList.item(0);
        readFileText(file, text -> textConsumer.accept(text, file));
    }

    public static void readFileText(File file, Consumer<String> textConsumer) {
        FileReader fileReader = new FileReader();
        fileReader.onload = progressEvent -> {
            try {
                textConsumer.accept(fileReader.result.asString());
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Reading file failed", t);
            }
            return null;
        };
        fileReader.readAsText(file);
    }
}
