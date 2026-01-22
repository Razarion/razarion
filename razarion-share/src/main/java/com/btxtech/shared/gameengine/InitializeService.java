package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

@Singleton
public class InitializeService {
    public final Collection<Consumer<ColdGameUiContext>> coldGameUiContextConsumers = new ArrayList<>();
    public final Collection<Consumer<StaticGameConfig>> staticGameConfigConsumers = new ArrayList<>();
    public final Collection<Consumer<PlanetActivationEvent>> planetActivationEventConsumers = new ArrayList<>();

    @Inject
    public InitializeService() {
    }

    public void receiveColdGameUiContext(Consumer<ColdGameUiContext> coldGameUiContextConsumer) {
        coldGameUiContextConsumers.add(coldGameUiContextConsumer);
    }

    public void setColdGameUiContext(ColdGameUiContext coldGameUiContext) {
        coldGameUiContextConsumers.forEach(consumer -> consumer.accept(coldGameUiContext));
    }

    public void receiveStaticGameConfig(Consumer<StaticGameConfig> staticGameConfigConsumer) {
        staticGameConfigConsumers.add(staticGameConfigConsumer);
    }

    public void setStaticGameConfig(StaticGameConfig staticGameConfig) {
        staticGameConfigConsumers.forEach(consumer -> consumer.accept(staticGameConfig));
    }

    public void receivePlanetActivationEvent(Consumer<PlanetActivationEvent> planetActivationEventConsumer) {
        planetActivationEventConsumers.add(planetActivationEventConsumer);
    }

    public void setPlanetActivationEvent(PlanetActivationEvent planetActivationEvent) {
        planetActivationEventConsumers.forEach(consumer -> consumer.accept(planetActivationEvent));
    }
}
