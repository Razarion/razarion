package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.CameraConfig;
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
    private Long id;
    private String introText;
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
        sceneConfig.setIntroText(introText);
        CameraConfig cameraConfig = new CameraConfig();
        cameraConfig.setFromPosition(cameraConfigFromPosition);
        cameraConfig.setToPosition(cameraConfigToPosition);
        cameraConfig.setSmooth(cameraConfigSmooth);
        cameraConfig.setCameraLocked(cameraConfigCameraLocked);
        sceneConfig.setCameraConfig(cameraConfig);
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
