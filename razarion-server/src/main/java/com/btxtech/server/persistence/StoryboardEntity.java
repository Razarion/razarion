package com.btxtech.server.persistence;

import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;

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
@Table(name = "STORYBOARD")
public class StoryboardEntity {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    private PlanetEntity planetEntity;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SceneEntity> scenes;

    public StoryboardConfig toStoryboardConfig(GameEngineConfig gameEngineConfig) {
        StoryboardConfig storyboardConfig = new StoryboardConfig();
        storyboardConfig.setGameEngineConfig(gameEngineConfig);
        gameEngineConfig.setPlanetConfig(planetEntity.toPlanetConfig());
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        for (SceneEntity scene : scenes) {
            sceneConfigs.add(scene.toSceneConfig());
        }
        storyboardConfig.setSceneConfigs(sceneConfigs);
        return storyboardConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StoryboardEntity that = (StoryboardEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
