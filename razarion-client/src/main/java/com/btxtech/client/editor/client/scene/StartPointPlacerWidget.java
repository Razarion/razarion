package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.client.editor.widgets.marker.PolygonField;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 15.08.2017.
 */
@Templated("StartPointPlacerWidget.html#startPointPlacer")
public class StartPointPlacerWidget extends Composite {
    @Inject
    @DataField
    private DecimalPositionWidget suggestedPosition;
    @Inject
    @DataField
    private CommaDoubleBox enemyFreeRadius;
    @Inject
    @DataField
    private PolygonField allowedArea;
    private BaseItemPlacerConfig baseItemPlacerConfig;
    private Consumer<BaseItemPlacerConfig> baseItemPlacerConfigListener;

    public void init(BaseItemPlacerConfig baseItemPlacerConfig, Consumer<BaseItemPlacerConfig> baseItemPlacerConfigListener) {
        this.baseItemPlacerConfig = baseItemPlacerConfig;
        this.baseItemPlacerConfigListener = baseItemPlacerConfigListener;
        Polygon2D polygon2D = null;
        DecimalPosition decimalPosition = null;
        if (baseItemPlacerConfig != null) {
            polygon2D = baseItemPlacerConfig.getAllowedArea();
            decimalPosition = baseItemPlacerConfig.getSuggestedPosition();
            enemyFreeRadius.setValue(baseItemPlacerConfig.getEnemyFreeRadius());
        }
        suggestedPosition.init(decimalPosition, newDecimalPosition -> {
            if (this.baseItemPlacerConfig == null) {
                this.baseItemPlacerConfig = new BaseItemPlacerConfig();
            }
            this.baseItemPlacerConfig.setSuggestedPosition(newDecimalPosition);
            updateModel();
        });
        allowedArea.init(polygon2D, newPolygon2D -> {
            if (this.baseItemPlacerConfig == null) {
                this.baseItemPlacerConfig = new BaseItemPlacerConfig();
            }
            this.baseItemPlacerConfig.setAllowedArea(newPolygon2D);
            updateModel();
        });
    }

    @EventHandler("enemyFreeRadius")
    public void enemyFreeRadiusChanged(ChangeEvent e) {
        if (baseItemPlacerConfig == null) {
            baseItemPlacerConfig = new BaseItemPlacerConfig();
        }
        baseItemPlacerConfig.setEnemyFreeRadius(enemyFreeRadius.getValue());
        updateModel();
    }

    private void updateModel() {
        if (allowedArea.getPolygon2D() == null && enemyFreeRadius.getValue() == null && suggestedPosition.getValue() == null) {
            baseItemPlacerConfig = null;
        }
        baseItemPlacerConfigListener.accept(baseItemPlacerConfig);
    }

}
