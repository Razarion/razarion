package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.renderer.ViewField;
import org.junit.Test;

/**
 * Created by Beat
 * 25.12.2015.
 */
public class TerrainUiServiceTest extends WeldUiBaseIntegrationTest {

    @Test
    public void onViewChanged() {
        setupUiEnvironment(new PlanetVisualConfig());
        TerrainUiService terrainUiService = getWeldBean(TerrainUiService.class);
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(new DecimalPosition(10, 0));
        viewField.setBottomRight(new DecimalPosition(40, 0));
        viewField.setTopRight(new DecimalPosition(50, 50));
        viewField.setTopLeft(new DecimalPosition(0, 50));
        terrainUiService.onViewChanged(viewField);
    }
}