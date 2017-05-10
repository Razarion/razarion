package com.btxtech.server.persistence;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "GAME_UI_CONTROL_CONFIG")
public class GameUiControlConfigEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private PlanetEntity planetEntity;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SceneEntity> scenes;
    @OneToOne
    private LevelEntity minimalLevel;

    public GameUiControlConfig toGameUiControlConfig() {
        GameUiControlConfig gameUiControlConfig = new GameUiControlConfig();
        gameUiControlConfig.setPlanetConfig(planetEntity.toPlanetConfig());
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        for (SceneEntity scene : scenes) {
            sceneConfigs.add(scene.toSceneConfig());
        }
        gameUiControlConfig.setSceneConfigs(sceneConfigs);
        return gameUiControlConfig;
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
