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

package com.btxtech.shared.gameengine.datatypes.exception;

/**
 * User: beat
 * Date: Sep 30, 2009
 * Time: 12:32:11 PM
 */
public class NotYourBaseException extends Exception {
    public NotYourBaseException() {
    }

    public NotYourBaseException(String actorBaseName, String targetBaseName) {
        super("Invalid access from base: " + actorBaseName + " to " + targetBaseName);
    }
}
