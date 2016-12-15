package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 13.12.2016.
 */
public class GuiTipVisualization {
    private static final long DELAY_MILLIS = 500;
    private Supplier<Index> screenPositionProvider;
    private Integer imageId;
    private SimpleScheduledFuture simpleScheduledFuture;
    private Consumer<Index> positionConsumer;

    public GuiTipVisualization(Supplier<Index> screenPositionProvider, Integer imageId) {
        this.screenPositionProvider = screenPositionProvider;
        this.imageId = imageId;
    }

    public void setPositionConsumer(Consumer<Index> positionConsumer) {
        this.positionConsumer = positionConsumer;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void start(SimpleExecutorService simpleExecutorService) {
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(DELAY_MILLIS, true, this::handlePosition, SimpleExecutorService.Type.UNSPECIFIED);
    }

    private void handlePosition() {
        if (positionConsumer != null) {
            positionConsumer.accept(screenPositionProvider.get());
        }
    }

    public void stop() {
        simpleScheduledFuture.cancel();
    }
}
