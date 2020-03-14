package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupSeq;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Singleton
public class BootImpl extends Boot {

    @Override
    protected StartupSeq getWarm() {
        return GameStartupSeq.WARM;
    }
}
