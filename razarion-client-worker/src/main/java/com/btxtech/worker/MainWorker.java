package com.btxtech.worker;


import com.btxtech.shared.gameengine.GameEngine;
import elemental.dom.TimeoutHandler;
import elemental.html.DedicatedWorkerGlobalScope;
import elemental.js.html.JsDedicatedWorkerGlobalScope;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.12.2016.
 */
@EntryPoint
public class MainWorker /*implements EntryPoint*/ {
    private Logger logger = Logger.getLogger(MainWorker.class.getName());
    @Inject
    private GameEngine gameEngine;

    protected MainWorker() {
    }

    // @Override
    @PostConstruct
    public void onModuleLoad() {
        DedicatedWorkerGlobalScope globalScope = getDedicatedWorkerGlobalScope();

        // GWT.log("Worker starting 2");
        globalScope.postMessage("Hallo");
        //globalScope.postMessage(new int[]{1, 2, 3});
//        globalScope.postMessage(new Object[]{1, "Hallo", new Date()});

        logger.severe("Worker starting 1");
//        DtoObject dtoObject = new DtoObject();
//        dtoObject.setNumber(11);
//        dtoObject.setText("Test worker");

//        SyncBaseItemDto syncBaseItemDto = new SyncBaseItemDto();
//        syncBaseItemDto.setBaseItemTypeId(1);
//        syncBaseItemDto.setId(11);
//        syncBaseItemDto.setSpeed(12);
//        globalScope.postMessage(syncBaseItemDto);
//
//
//        AudioConfig audioConfig = new AudioConfig();
//        audioConfig.setDialogOpened(1111);
//        globalScope.postMessage(audioConfig);


        globalScope.setInterval(new TimeoutHandler() {
            @Override
            public void onTimeoutHandler() {
                globalScope.postMessage("Hallo2");

//                SyncBaseItemDto syncBaseItemDto = new SyncBaseItemDto();
//                syncBaseItemDto.id = 1;
//                syncBaseItemDto.baseItemTypeId = 2;
//                syncBaseItemDto.speed = 2.5;
//
//                globalScope.postMessage(syncBaseItemDto);
//                logger.severe("Worker starting 2: " + syncBaseItemDto.id + "|" + syncBaseItemDto.baseItemTypeId);


            }
        }, 1000);

        gameEngine.start();
        logger.severe("gameEngine start");

//        post();

    }

    public static native JsDedicatedWorkerGlobalScope getDedicatedWorkerGlobalScope() /*-{
        return self;
    }-*/;


    private native void post() /*-{
        var jsonObject = {
            firstName: "John",
            lastName: "Doe",
            age: 50,
            eyeColor: "blue"
        };
        return self.postMessage(jsonObject);
    }-*/;


}
