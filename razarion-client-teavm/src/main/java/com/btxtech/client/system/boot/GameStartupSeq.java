package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.StartupSeq;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

public enum GameStartupSeq implements StartupSeq {
    COLD {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return ColdGameStartupTaskEnum.values();
        }
    },
    WARM {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WarmGameStartupTaskEnum.values();
        }
    }
}
