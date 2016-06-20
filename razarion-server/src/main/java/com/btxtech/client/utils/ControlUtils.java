package com.btxtech.client.utils;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.html.InputElement;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class ControlUtils {
    public interface SingleFileDataUrlListener {
        void onLoaded(String dataUrl, File file);
    }

    private static final Logger LOGGER = Logger.getLogger(ControlUtils.class.getName());

    public static void openSingleFileDataUrlUpload(final SingleFileDataUrlListener singleFileDataUrlListener) {
        final InputElement fileSelector = Browser.getDocument().createInputElement();
        fileSelector.setAttribute("type", "file");
        fileSelector.addEventListener(Event.CHANGE, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                FileList fileList = fileSelector.getFiles();

                final File file = fileList.item(0);
                final FileReader fileReader = Browser.getWindow().newFileReader();
                fileReader.setOnload(new EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        try {
                            singleFileDataUrlListener.onLoaded((String) fileReader.getResult(), file);
                        } catch (Throwable t) {
                            LOGGER.log(Level.SEVERE, "Reading file failed", t);
                        }
                    }
                });
                fileReader.readAsDataURL(file);
            }
        }, false);
        fileSelector.click();
    }

}
