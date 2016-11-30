package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.CursorType;
import com.btxtech.webglemulator.WebGlEmulatorController;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;

/**
 * Created by Beat
 * 30.11.2016.
 */
@ApplicationScoped
public class DevToolCursorService extends CursorService {
    @Inject
    private WebGlEmulatorController webGlEmulatorController;

    @Override
    protected void setDefaultCursorInternal() {
        System.out.println("+++ setDefaultCursorInternal()");
        webGlEmulatorController.getCanvas().setCursor(Cursor.DEFAULT);
    }

    @Override
    protected void setPointerCursorInternal() {
        System.out.println("+++ setPointerCursorInternal()");
        webGlEmulatorController.getCanvas().setCursor(Cursor.HAND);
    }

    @Override
    protected void setCursorInternal(CursorType cursorType, boolean allowed) {
        System.out.println("+++ setCursorInternal: " + cursorType + " allowed: " + allowed);
        // webGlEmulatorController.getCanvas().setCursor(Cursor.MOVE);

        String urlString = "/cursors/" + cursorType.getName(allowed) + ".png";
        URL url = getClass().getResource(urlString);
        if (url != null) {
            Image image = new Image(url.toString());
            webGlEmulatorController.getCanvas().setCursor(new ImageCursor(image, image.getWidth() / 2, image.getHeight() / 2));
        } else {
            System.out.println("Can not load cursor url: " + urlString);
        }

    }
}
