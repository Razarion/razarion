package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.SceneConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "SCENE")
public class SceneEntity {
    @Id
    @GeneratedValue
    private Integer id;
    private String internalName;
    private String introText;
    @Deprecated
    private boolean showQuestSideBar;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "cameraConfigFromPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "cameraConfigFromPositionY")),
    })
    @Embedded
    private Index cameraConfigFromPosition;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "cameraConfigToPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "cameraConfigToPositionY")),
    })
    @Embedded
    private Index cameraConfigToPosition;
    private boolean cameraConfigSmooth;
    private boolean cameraConfigCameraLocked;

    public SceneConfig toSceneConfig() {
        SceneConfig sceneConfig = new SceneConfig();
        sceneConfig.setInternalName(internalName);
        sceneConfig.setIntroText(introText);
        // TODO sceneConfig.setShowQuestSideBar(showQuestSideBar);
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig();
        // TODO viewFieldConfig.setFromPosition(cameraConfigFromPosition);
        // TODO viewFieldConfig.setToPosition(cameraConfigToPosition);
        // TODO viewFieldConfig.setSmooth(cameraConfigSmooth);
        viewFieldConfig.setCameraLocked(cameraConfigCameraLocked);
        sceneConfig.setViewFieldConfig(viewFieldConfig);
        return sceneConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SceneEntity that = (SceneEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
