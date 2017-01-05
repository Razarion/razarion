package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.GameEngineWorker;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class DevToolGameEngineWorker extends GameEngineWorker {
    private Consumer<GameEngineControlPackage> packageConsumer;

    @Override
    protected void dispatchPackage(GameEngineControlPackage.Command command) {
        packageConsumer.accept(new GameEngineControlPackage(command, null));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        dispatch(gameEngineControlPackage);
    }

    void setPackageConsumer(Consumer<GameEngineControlPackage> packageConsumer) {
        this.packageConsumer = packageConsumer;
    }
}
