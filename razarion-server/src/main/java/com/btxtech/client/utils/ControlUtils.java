package com.btxtech.client.utils;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.html.InputElement;

import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class ControlUtils {
    private static final Logger LOGGER = Logger.getLogger(ControlUtils.class.getName());

    public static void openSingleFileDataUrlUpload(final BiConsumer<String, File> dataUrlConsumer) {
        final InputElement fileSelector = Browser.getDocument().createInputElement();
        fileSelector.setAttribute("type", "file");
        fileSelector.addEventListener(Event.CHANGE, evt -> {
            FileList fileList = fileSelector.getFiles();

            final File file = fileList.item(0);
            final FileReader fileReader = Browser.getWindow().newFileReader();
            fileReader.setOnload(evt1 -> {
                try {
                    dataUrlConsumer.accept((String) fileReader.getResult(), file);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Reading file failed", t);
                }
            });
            fileReader.readAsDataURL(file);
        }, false);
        fileSelector.click();
    }

    public static void openSingleFileTextUpload(final BiConsumer<String, File> textConsumer) {
        final InputElement fileSelector = Browser.getDocument().createInputElement();
        fileSelector.setAttribute("type", "file");
        fileSelector.addEventListener(Event.CHANGE, evt -> {
            FileList fileList = fileSelector.getFiles();

            final File file = fileList.item(0);
            final FileReader fileReader = Browser.getWindow().newFileReader();
            fileReader.setOnload(evt1 -> {
                try {
                    textConsumer.accept((String) fileReader.getResult(), file);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Reading file failed", t);
                }
            });
            fileReader.readAsText(file);
        }, false);
        fileSelector.click();
    }
}
