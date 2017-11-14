package com.btxtech.client.utils;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.html.ButtonElement;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.html.InputElement;

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

    public static void openSingleFileDataUrlUpload(final BiConsumer<String, File> dataUrlConsumer) {
        final InputElement fileSelector = Browser.getDocument().createInputElement();
        fileSelector.setAttribute("type", "file");
        fileSelector.addEventListener(Event.CLICK, evt -> {
            fileSelector.setValue(null); // Prevents from suppressing loading
        });

        fileSelector.addEventListener(Event.CHANGE, evt -> {
            try {
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
                fileSelector.setValue(null); // Prevents from suppressing loading
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Start reading file failed", t);
            }
        }, false);
        fileSelector.setValue(null); // Prevents from suppressing loading
        fileSelector.click();
    }

    public static void openSingleFileTextUpload(BiConsumer<String, File> textConsumer) {
        final InputElement fileSelector = Browser.getDocument().createInputElement();
        fileSelector.setAttribute("type", "file");
        fileSelector.addEventListener(Event.CLICK, evt -> {
            fileSelector.setValue(null); // May not needed... use google
        });
        fileSelector.addEventListener(Event.CHANGE, evt -> {
            FileList fileList = fileSelector.getFiles();
            File file = fileList.item(0);
            readFileText(file, text -> textConsumer.accept(text, file));
            fileSelector.setValue(null); // Prevents from suppressing loading
        }, false);
        fileSelector.setValue(null); // Prevents from suppressing loading
        fileSelector.click();
    }

    public static void readFileText(File file, Consumer<String> textConsumer) {
        final FileReader fileReader = Browser.getWindow().newFileReader();
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
