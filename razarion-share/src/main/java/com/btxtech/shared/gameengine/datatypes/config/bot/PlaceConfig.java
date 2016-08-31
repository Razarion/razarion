package com.btxtech.shared.gameengine.datatypes.config.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.Region;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 23.07.2016.
 */
@Portable
public class PlaceConfig {
    private Region region;
    private DecimalPosition position;

    public Region getRegion() {
        return region;
    }

    public PlaceConfig setRegion(Region region) {
        this.region = region;
        return this;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public PlaceConfig setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }
}
