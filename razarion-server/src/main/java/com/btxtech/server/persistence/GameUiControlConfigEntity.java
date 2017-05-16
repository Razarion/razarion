package com.btxtech.server.persistence;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.scene.SceneEntity;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "GAME_UI_CONTROL_CONFIG")
public class GameUiControlConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private PlanetEntity planetEntity;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "gameUiControlConfigEntityId", nullable = false )
    @OrderColumn(name = "orderColumn")
    private List<SceneEntity> scenes;
    @OneToOne
    private LevelEntity minimalLevel;
    @Enumerated(EnumType.STRING)
    private GameEngineMode gameEngineMode;

    public Integer getId() {
        return id;
    }

    public WarmGameUiControlConfig toGameWarmGameUiControlConfig(Locale locale) {
        WarmGameUiControlConfig warmGameUiControlConfig = new WarmGameUiControlConfig();
        warmGameUiControlConfig.setPlanetConfig(planetEntity.toPlanetConfig());
        warmGameUiControlConfig.setPlanetVisualConfig(planetEntity.toPlanetVisualConfig());
        warmGameUiControlConfig.setSceneConfigs(setupScenes(locale));
        warmGameUiControlConfig.setGameEngineMode(gameEngineMode);
        return warmGameUiControlConfig;
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

    public PlanetEntity getPlanetEntity() {
        return planetEntity;
    }

    public void setPlanetEntity(PlanetEntity planetEntity) {
        this.planetEntity = planetEntity;
    }

    public LevelEntity getMinimalLevel() {
        return minimalLevel;
    }

    public void setMinimalLevel(LevelEntity minimalLevel) {
        this.minimalLevel = minimalLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GameUiControlConfigEntity that = (GameUiControlConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
