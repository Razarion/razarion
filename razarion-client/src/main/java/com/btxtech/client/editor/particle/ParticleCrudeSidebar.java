package com.btxtech.client.editor.particle;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.10.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ParticleCrudeSidebar extends AbstractCrudeParentSidebar<ParticleEmitterSequenceConfig, ParticlePropertyPanel> {
    // private Logger logger = Logger.getLogger(ParticleCrudeSidebar.class.getName());
    @Inject
    private ParticleCrud particleCrud;
    @Inject
    private Instance<ParticlePropertyPanel> particlePropertyPanelInstance;


    @Override
    protected CrudEditor<ParticleEmitterSequenceConfig> getCrudEditor() {
        return particleCrud;
    }

    @Override
    protected ParticlePropertyPanel createPropertyPanel() {
        return particlePropertyPanelInstance.get();
    }
}
