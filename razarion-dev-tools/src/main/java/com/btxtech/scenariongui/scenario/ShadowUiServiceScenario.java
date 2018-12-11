package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 06.12.2018.
 */
public class ShadowUiServiceScenario extends Scenario{
    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {

        List<DecimalPosition> viewFieldGroundZero = Arrays.asList(new DecimalPosition(94.47715250169206, 95.85786437626905), new DecimalPosition(105.52284749830794, 95.85786437626905), new DecimalPosition(105.52284749830794, 104.14213562373095), new DecimalPosition(94.47715250169206, 104.14213562373095));
        List<DecimalPosition> viewFieldHighest = Arrays.asList(new DecimalPosition(105.52284749830794, 104.14213562373095), new DecimalPosition(94.47715250169206, 104.14213562373095), new DecimalPosition(94.47715250169206, 95.85786437626905), new DecimalPosition(105.52284749830794, 95.85786437626905));
        List<DecimalPosition> viewFieldLowest = Arrays.asList(new DecimalPosition(93.37258300203048, 95.02943725152286), new DecimalPosition(106.62741699796952, 95.02943725152286), new DecimalPosition(106.62741699796952, 104.97056274847714), new DecimalPosition(93.37258300203048, 104.97056274847714));

        extendedGraphicsContext.strokeCurveDecimalPosition(viewFieldGroundZero, 0.1, Color.BLACK, true);
        extendedGraphicsContext.strokeCurveDecimalPosition(viewFieldHighest, 0.02, Color.RED, true);
        extendedGraphicsContext.strokeCurveDecimalPosition(viewFieldLowest, 0.1, Color.BLUE, true);
    }
}
