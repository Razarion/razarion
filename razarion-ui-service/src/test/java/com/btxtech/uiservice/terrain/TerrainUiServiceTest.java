package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import org.junit.Test;

/**
 * Created by Beat
 * 25.12.2015.
 */
public class TerrainUiServiceTest extends WeldUiBaseIntegrationTest {

    @Test
    public void test() {
        setupUiEnvironment(new PlanetVisualConfig());
        TerrainUiService terrainUiService = getWeldBean(TerrainUiService.class);

        terrainUiService.calculateMousePositionGroundMesh(new DecimalPosition(1,1));
    }
}