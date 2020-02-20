package com.btxtech.shared.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by Beat
 * 23.08.2016.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "type")
public interface ObjectNameIdProvider {
    ObjectNameId createObjectNameId();
}
