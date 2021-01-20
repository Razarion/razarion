package com.btxtech.client.editor.generic.custom;

import com.btxtech.client.guielements.VertexBox;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.particle.ParticleService;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("ParticleCustomWidget.html#particleCustomWidget")
public class ParticleCustomWidget implements CustomWidget<ParticleEmitterSequenceConfig> {
    // private Logger logger = Logger.getLogger(ParticleCustomWidget.class.getName());
    @Inject
    private ParticleService particleService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private VertexBox testPosition;
    @Inject
    @DataField
    private VertexBox testDirection;
    @Inject
    @DataField
    private HTMLButtonElement testParticleButton;
    @Inject
    @DataField
    private HTMLButtonElement clearParticleButton;
    private ParticleEmitterSequenceConfig rootPropertyValue;

    @Override
    public void setRootPropertyValue(ParticleEmitterSequenceConfig rootPropertyValue) {
        this.rootPropertyValue = rootPropertyValue;
    }

    @EventHandler("testParticleButton")
    private void testParticleButtonClick(ClickEvent event) {
        try {
            Vertex position = testPosition.getValue();
            if (position != null) {
                particleService.start(System.currentTimeMillis(), position, testDirection.getValue(), rootPropertyValue);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("clearParticleButton")
    private void clearParticleButtonClick(ClickEvent event) {
        particleService.clear();
    }

}
