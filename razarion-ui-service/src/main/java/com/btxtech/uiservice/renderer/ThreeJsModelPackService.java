package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.gameengine.InitializeService;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@JsType
@Singleton
public class ThreeJsModelPackService {
    private final Map<Integer, ThreeJsModelPackConfig> threeJsModelPackConfigs = new HashMap<>();

    @Inject
    public ThreeJsModelPackService(InitializeService initializeService) {
        initializeService.receiveColdGameUiContext(coldGameUiContext -> {
            threeJsModelPackConfigs.clear();
            if (coldGameUiContext.getStaticGameConfig().getThreeJsModelPackConfigs() != null) {
                coldGameUiContext.getStaticGameConfig().getThreeJsModelPackConfigs().forEach(threeJsModelPackConfig ->
                        threeJsModelPackConfigs.put(threeJsModelPackConfig.getId(), threeJsModelPackConfig));
            }
        });
    }

    public ThreeJsModelPackConfig getThreeJsModelPackConfig(int id) {
        ThreeJsModelPackConfig threeJsModelPackConfig = threeJsModelPackConfigs.get(id);
        if (threeJsModelPackConfig == null) {
            throw new IllegalArgumentException("No ThreeJsModelPackConfig for id: " + id);
        }
        return threeJsModelPackConfig;
    }

}
