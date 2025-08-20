package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.PlanetEntity;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GAME_UI_CONTEXT")
public class GameUiContextEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private PlanetEntity planetEntity;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;
    @Enumerated(EnumType.STRING)
    private GameEngineMode gameEngineMode;

    public GameUiContextConfig toConfig() {
        GameUiContextConfig gameUiContextConfig = new GameUiContextConfig()
                .id(getId())
                .internalName(getInternalName())
                .gameEngineMode(gameEngineMode)
                .scenes(setupScenes());
        if (minimalLevel != null) {
            gameUiContextConfig.setMinimalLevelId(minimalLevel.getId());
        }
        if (planetEntity != null) {
            gameUiContextConfig.setPlanetId(planetEntity.getId());
        }
        return gameUiContextConfig;
    }

    public void fromConfig(GameUiContextConfig config, LevelEntity minimalLevel, PlanetEntity planetEntity) {
        setInternalName(config.getInternalName());
        gameEngineMode = config.getGameEngineMode();
        this.minimalLevel = minimalLevel;
        this.planetEntity = planetEntity;
    }

    public WarmGameUiContext toGameWarmGameUiControlConfig() {
        WarmGameUiContext warmGameUiContext = new WarmGameUiContext();
        warmGameUiContext.setGameUiControlConfigId(getId());
        if (planetEntity != null) {
            warmGameUiContext.setPlanetConfig(planetEntity.toPlanetConfig());
        }
        warmGameUiContext.setSceneConfigs(setupScenes());
        warmGameUiContext.setGameEngineMode(gameEngineMode);
        return warmGameUiContext;
    }

    private List<SceneConfig> setupScenes() {
        return new ArrayList<>();
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GameUiContextEntity that = (GameUiContextEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
