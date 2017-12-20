package com.btxtech.client.utils;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.html.ButtonElement;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;

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
            FileReader fileReader = Browser.getWindow().newFileReader();
            fileReader.setOnload(evt1 -> {
                try {
                    dataUrlConsumer.accept((String) fileReader.getResult(), file);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Reading file failed", t);
                }
            });
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
        FileReader fileReader = Browser.getWindow().newFileReader();
        fileReader.setOnload(evt1 -> {
            try {
                textConsumer.accept((String) fileReader.getResult());
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Reading file failed", t);
            }
        });
        fileReader.readAsText(file);
    }

    public static ButtonElement createButton(String text, Runnable runnable) {
        ButtonElement button = Browser.getDocument().createButtonElement();
        button.setInnerHTML(text);
        button.addEventListener(Event.CLICK, evt -> runnable.run(), false);
        return button;
    }

}
