package com.btxtech.client;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.system.boot.ClientRunner;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.events.ErrorEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MessageEvent;
import elemental.html.Worker;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2015.
 */
@Page(role = DefaultPage.class)
@Templated("MainPage.html#app-template")
public class MainPage extends Composite {
    private Logger logger = Logger.getLogger(MainPage.class.getName());
    @Inject
    private ClientRunner clientRunner;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private KeyboardEventHandler keyboardEventHandler;
    @DataField
    private Canvas canvas = Canvas.createIfSupported();
    @Inject
    private GameCanvas gameCanvas;

    @PostConstruct
    public void init() {
        try {
            if (canvas == null) {
                throw new IllegalStateException("Canvas is not supported");
            }
            canvas.getElement().getStyle().setZIndex(ZIndexConstants.WEBGL_CANVAS);
            gameCanvas.init(canvas);
            keyboardEventHandler.init();
            // clientRunner.start(GameStartupSeq.COLD_SIMULATED);
            Worker worker = Browser.getWindow().newWorker("razarion_client_worker/razarion_client_worker.nocache.js");
//            Worker worker = Browser.getWindow().newWorker("demo_workers.js");
//            handleMessage(worker);
            worker.setOnmessage(new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    MessageEvent messageEvent = (MessageEvent) evt;
                    logger.severe("Web worker Message: " + messageEvent.getData().toString());
                    logger.severe("Web worker Message: " + messageEvent.getData().getClass());

//                    SyncBaseItemDto syncBaseItemDto = toSyncBaseItemDto( messageEvent.getData());
//                    logger.severe("Web worker Message syncBaseItemDto: " + syncBaseItemDto.id + "|" + syncBaseItemDto.baseItemTypeId+ "|" + syncBaseItemDto.speed);

//                    SyncBaseItemDto syncBaseItemDto1 = new SyncBaseItemDto();
//                    syncBaseItemDto1.setBaseItemTypeId(2);
//                    syncBaseItemDto1.setId(22);
//                    syncBaseItemDto1.setSpeed(33);
//
//                    AudioConfig audioConfig = new AudioConfig();
//                    audioConfig.setDialogOpened(1111);
//                    audioConfig.setOnCommandSent(2222);


//                    try {
//                        SyncBaseItemDto  syncBaseItemDto = toSyncBaseItemDto(messageEvent.getData());
//                        logger.severe("syncBaseItemDto.getBaseItemTypeId(): " + syncBaseItemDto.getBaseItemTypeId());
//                        logger.severe("syncBaseItemDto.getId(): " + syncBaseItemDto.getId());
//                        logger.severe("syncBaseItemDto.getSpeed(): " + syncBaseItemDto.getSpeed());
//                    } catch (Throwable t) {
//                        exceptionHandler.handleException(t);
//                    }
                }
            });
            worker.setOnerror(new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    ErrorEvent errorEvent = (ErrorEvent) evt;
                    logger.severe("Web worker Onerror: " + errorEvent.getMessage() + "|" + errorEvent.getType() + "|" + errorEvent.getFilename());
                }
            });
            logger.severe("Start web worker: " + worker);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "MainPage init failed", throwable);
        }
        logger.severe("MainPage init() called twice?");
    }
//
//    private native SyncBaseItemDto toSyncBaseItemDto(Object object) /*-{
//        return object;
//    }-*/;
}
