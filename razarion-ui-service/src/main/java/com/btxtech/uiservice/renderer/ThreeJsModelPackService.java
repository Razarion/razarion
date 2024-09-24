package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import jsinterop.annotations.JsType;

import javax.inject.Singleton;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

@JsType
@Singleton
public class ThreeJsModelPackService {
    private final Map<Integer, ThreeJsModelPackConfig> threeJsModelPackConfigs = new HashMap<>();

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        threeJsModelPackConfigs.clear();
        if (gameUiControlInitEvent.getColdGameUiContext().getStaticGameConfig().getThreeJsModelPackConfigs() != null) {
            gameUiControlInitEvent.getColdGameUiContext().getStaticGameConfig().getThreeJsModelPackConfigs().forEach(threeJsModelPackConfig ->
                    threeJsModelPackConfigs.put(threeJsModelPackConfig.getId(), threeJsModelPackConfig));
        }
    }

    public ThreeJsModelPackConfig getThreeJsModelPackConfig(int id) {
        ThreeJsModelPackConfig threeJsModelPackConfig = threeJsModelPackConfigs.get(id);
        if (threeJsModelPackConfig == null) {
            throw new IllegalArgumentException("No ThreeJsModelPackConfig for id: " + id);
        }
        return threeJsModelPackConfig;
    }

}
