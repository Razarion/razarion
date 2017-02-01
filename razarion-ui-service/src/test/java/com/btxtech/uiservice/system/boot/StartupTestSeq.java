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

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_DEFERRED {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_BACKGROUND {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredBackgroundTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_DEFERRED_FINISH {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredFinishTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_DEFERRED_BACKGROUND_FINISH {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return DeferredBackgroundFinishTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_SIMPLE_EXCEPTION {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return SimpleExceptionTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_MULTI {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return MultiTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_WAIT_FOR_BACKGROUND_SIMPLE {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WaitForBackgroundTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    },
    TEST_WAIT_FOR_BACKGROUND_COMPLEX {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WaitForBackgroundComplexTestTaskEnum.values();
        }

        @Override
        public boolean isCold() {
            return true;
        }
    }

}
