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

import javax.enterprise.context.Dependent;

/**
 * User: beat
 * Date: 18.02.2010
 * Time: 12:50:50
 */
@Dependent
public class SimpleStartupTestTask extends AbstractStartupTask {

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
    }

}
