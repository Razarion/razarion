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
    protected void sendToClient(GameEngineControlPackage.Command command, Object... object) {
        packageConsumer.accept(new GameEngineControlPackage(command, object));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        dispatch(gameEngineControlPackage);
    }

    void setPackageConsumer(Consumer<GameEngineControlPackage> packageConsumer) {
        this.packageConsumer = packageConsumer;
    }
}
