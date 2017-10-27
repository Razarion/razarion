package com.btxtech.client.utils;

import com.google.gwt.canvas.client.Canvas;
import elemental.html.CanvasRenderingContext2D;
import elemental.html.ImageData;
import elemental.html.ImageElement;
import org.jboss.errai.common.client.dom.Image;
import org.jboss.errai.common.client.dom.Window;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 26.10.2017.
 */
public interface CanvasUtil {

    static void getImageData(String dataUrl, Consumer<ImageData> imageDataConsumer) {
        Image image = (Image) Window.getDocument().createElement("img");
        image.setOnload(event -> {
            Canvas canvas = Canvas.createIfSupported();
            CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
            canvas.setPixelSize(image.getWidth(), image.getHeight());
            ctx.clearRect(0, 0, image.getWidth(), image.getHeight());
            ctx.drawImage((ImageElement) image, 0, 0);
            imageDataConsumer.accept(ctx.getImageData(0, 0, image.getWidth(), image.getHeight()));
        });
        image.setSrc(dataUrl);
    }

}
