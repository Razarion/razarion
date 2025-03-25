package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.server.repository.Model3DRepository;
import com.btxtech.server.rest.ui.Model3DController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Model3DService extends AbstractBaseEntityCrudService<Model3DEntity> {
    @Inject
    private Model3DRepository model3DRepository;
    @Inject
    private GltfService gltfPersistence;

    public Model3DService() {
        super(Model3DEntity.class);
    }

    @Override
    protected JpaRepository<Model3DEntity, Integer> getJpaRepository() {
        return model3DRepository;
    }

    public List<Model3DEntity> getModel3DsByGltf(int gltfId) {
        return model3DRepository.getModel3DsByGltfEntity((GltfEntity) new GltfEntity().id(gltfId));
    }

    @Transactional
    public List<Model3DEntity> readAllBaseEntitiesJson() {
        return getEntities()
                .stream()
                .map(Model3DController::jpa2JsonStatic)
                .collect(Collectors.toList());
    }

    @Override
    protected Model3DEntity jsonToJpa(Model3DEntity model3DEntity) {
        if (model3DEntity.getGltfEntityId() != null) {
            model3DEntity.setGltfEntity(gltfPersistence.getEntity(model3DEntity.getGltfEntityId()));
        }
        return model3DEntity;
    }

}
