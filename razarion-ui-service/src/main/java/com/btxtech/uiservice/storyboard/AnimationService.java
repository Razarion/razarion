package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.dto.AnimatedMeshConfig;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.utils.CompletionListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 12.07.2016.
 */
@Singleton
public class AnimationService {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private RenderService renderService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private Map<Integer, AnimatedMesh> animatedMeshes = new HashMap<>();
    private int lastId = 1;

    public void runAnimation(AnimatedMeshConfig animatedMeshConfig, CompletionListener completionListener) {
        final AnimatedMesh animatedMesh = new AnimatedMesh(lastId, animatedMeshConfig);
        animatedMeshes.put(animatedMesh.getId(), animatedMesh);
        lastId++;

        simpleExecutorService.schedule(animatedMeshConfig.getDuration(), new Runnable() {
            @Override
            public void run() {
                animatedMeshes.remove(animatedMesh.getId());
                renderService.disenrollAnimation(animatedMesh.getId());
            }
        });
        renderService.enrollAnimation(animatedMesh.getId());
    }

    public Matrix4 getModelMatrix(int animatedMeshId) {
        AnimatedMesh animatedMesh = animatedMeshes.get(animatedMeshId);
        if(animatedMesh == null) {
            return null;
        }
        return animatedMesh.calculateModelMatrix();
    }

    public VertexContainer getVertexContainer(int animatedMeshId) {
        return animatedMeshes.get(animatedMeshId).getAnimatedMeshConfig().getVertexContainer();
    }
}
