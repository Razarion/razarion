/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.uiservice.system.boot;


/**
 * User: beat
 * Date: 18.02.2011
 * Time: 14:35:33
 */
public enum StartupTestSeq implements StartupSeq {
    TEST_SIMPLE {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return SimpleTestTaskEnum.values();
        }

    },
    TEST_DEFERRED {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredTestTaskEnum.values();
        }

    },
    TEST_BACKGROUND {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredBackgroundTestTaskEnum.values();
        }

    },
    TEST_DEFERRED_FINISH {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredFinishTestTaskEnum.values();
        }

    },
    TEST_DEFERRED_BACKGROUND_FINISH {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredBackgroundFinishTestTaskEnum.values();
        }

    },
    TEST_SIMPLE_EXCEPTION {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return SimpleExceptionTestTaskEnum.values();
        }

    },
    TEST_MULTI {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return MultiTestTaskEnum.values();
        }

    },
    TEST_WAIT_FOR_BACKGROUND_SIMPLE {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WaitForBackgroundTestTaskEnum.values();
        }

    },
    TEST_WAIT_FOR_BACKGROUND_COMPLEX {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WaitForBackgroundComplexTestTaskEnum.values();
        }

    },
    TEST_ALARM_RAISING {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return AlarmRaisingTestTaskEnum.values();
        }

    },
    TEST_DEFERRED_ALARM_RAISING {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredAlarmRaisingTestTaskEnum.values();
        }

    }
}
