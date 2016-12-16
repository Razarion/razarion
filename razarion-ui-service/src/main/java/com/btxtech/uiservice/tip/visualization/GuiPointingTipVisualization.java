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
public class GuiPointingTipVisualization extends AbstractGuiTipVisualization{
    public enum Direction {
        NORTH, EAST, SOUTH, WEST;
    }

    private static final long DELAY_MILLIS = 500;
    private Direction direction;
    private Supplier<Index> screenPositionProvider;
    private SimpleScheduledFuture simpleScheduledFuture;
    private Consumer<Index> positionConsumer;

    public GuiPointingTipVisualization(Supplier<Index> screenPositionProvider, Direction direction, Integer imageId) {
        super(imageId);
        this.screenPositionProvider = screenPositionProvider;
        this.direction = direction;
    }

    public void setPositionConsumer(Consumer<Index> positionConsumer) {
        this.positionConsumer = positionConsumer;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public void start(SimpleExecutorService simpleExecutorService) {
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(DELAY_MILLIS, true, this::handlePosition, SimpleExecutorService.Type.UNSPECIFIED);
    }

    private void handlePosition() {
        if (positionConsumer != null) {
            positionConsumer.accept(screenPositionProvider.get());
        }
    }

    @Override
    public void stop() {
        simpleScheduledFuture.cancel();
    }
}
