package com.btxtech.client;

import com.btxtech.client.math3d.ViewTransformation;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 12.07.2015.
 */
@Singleton
public class ViewFieldMover {
    @Inject
    private ViewTransformation viewTransformation;
    private Index startMove;
    private double factor = 0.5;

    public void activate(Canvas canvas) {
        canvas.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT) {
                    if (startMove != null) {
                        Index endMove = new Index(event.getX(), event.getY());
                        Index delta = endMove.sub(startMove);
                        if (delta.isNull()) {
                            return;
                        }
                        viewTransformation.setTranslateX(viewTransformation.getTranslateX() + factor * (double) delta.getX());
                        viewTransformation.setTranslateY(viewTransformation.getTranslateY() + factor * (double) -delta.getY());
                        startMove = endMove;
                    }
                } else {
                    startMove = null;
                }
            }
        });
        canvas.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                startMove = new Index(event.getX(), event.getY());
            }
        });
        canvas.addMouseWheelHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_LEFT) == NativeEvent.BUTTON_LEFT) {
                    double newAngleX = viewTransformation.getRotateX() + Math.toRadians(event.getDeltaY());
                    if (newAngleX < 0) {
                        newAngleX = 0;
                    } else if (newAngleX > MathHelper.QUARTER_RADIANT) {
                        newAngleX = MathHelper.QUARTER_RADIANT;
                    }
                    viewTransformation.setRotateX(newAngleX);
                } else if ((eventGetButton(event.getNativeEvent()) & NativeEvent.BUTTON_RIGHT) == NativeEvent.BUTTON_RIGHT) {
                    double newAngleZ = viewTransformation.getRotateZ() + Math.toRadians(event.getDeltaY());
                    if (newAngleZ < 0) {
                        newAngleZ = 0;
                    } else if (newAngleZ > MathHelper.ONE_RADIANT) {
                        newAngleZ = MathHelper.ONE_RADIANT;
                    }
                    viewTransformation.setRotateZ(newAngleZ);
                } else {
                    viewTransformation.setTranslateZ(viewTransformation.getTranslateZ() + event.getDeltaY());
                    event.preventDefault();
                }
            }
        });
    }

    public native int eventGetButton(NativeEvent evt) /*-{
        return evt.buttons;
    }-*/;

}
