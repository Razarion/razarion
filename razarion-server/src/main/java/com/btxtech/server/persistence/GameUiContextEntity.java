package com.btxtech.server.persistence;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.scene.SceneEntity;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "GAME_UI_CONTEXT")
public class GameUiContextEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToOne(fetch = FetchType.LAZY)
    private PlanetEntity planetEntity;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "gameUiContextEntityId", nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SceneEntity> scenes;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;
    @Enumerated(EnumType.STRING)
    private GameEngineMode gameEngineMode;
    private boolean detailedTracking;

    public Integer getId() {
        return id;
    }

    public GameUiContextConfig toConfig() {
        GameUiContextConfig gameUiContextConfig = new GameUiContextConfig()
                .id(id)
                .internalName(internalName)
                .gameEngineMode(gameEngineMode)
                .scenes(setupScenes(Locale.US))
                .detailedTracking(detailedTracking);
        if (minimalLevel != null) {
            gameUiContextConfig.setMinimalLevelId(minimalLevel.getId());
        }
        if (planetEntity != null) {
            gameUiContextConfig.setPlanetId(planetEntity.getId());
        }
        return gameUiContextConfig;
    }

    public void fromConfig(GameUiContextConfig config, LevelEntity minimalLevel, PlanetEntity planetEntity) {
        internalName = config.getInternalName();
        gameEngineMode = config.getGameEngineMode();
        this.minimalLevel = minimalLevel;
        this.planetEntity = planetEntity;
        detailedTracking = config.isDetailedTracking();
    }

    public WarmGameUiContext toGameWarmGameUiControlConfig(Locale locale) {
        WarmGameUiContext warmGameUiContext = new WarmGameUiContext();
        warmGameUiContext.setGameUiControlConfigId(id);
        if (planetEntity != null) {
            warmGameUiContext.setPlanetConfig(planetEntity.toPlanetConfig());
        }
        warmGameUiContext.setSceneConfigs(setupScenes(locale));
        warmGameUiContext.setGameEngineMode(gameEngineMode);
        warmGameUiContext.setDetailedTracking(detailedTracking);
        return warmGameUiContext;
    }

    private List<SceneConfig> setupScenes(Locale locale) {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        if (scenes != null) {
            for (SceneEntity scene : scenes) {
                sceneConfigs.add(scene.toSceneConfig(locale));
            }
        }
        return sceneConfigs;
    }

    public List<SceneEntity> getScenes() {
        return scenes;
    }

    public void setScenes(List<SceneEntity> scenes) {
        this.scenes = scenes;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
