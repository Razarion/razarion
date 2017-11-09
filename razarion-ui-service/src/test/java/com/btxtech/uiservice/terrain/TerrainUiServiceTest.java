package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.WeldUiBaseTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 25.12.2015.
 */
public class TerrainUiServiceTest extends WeldUiBaseTest{

    @Test
    public void test() {
        setupUiEnvironment();
        TerrainUiService terrainUiService = getWeldBean(TerrainUiService.class);

        terrainUiService.calculateMousePositionGroundMesh(new DecimalPosition(1,1));
    }
}