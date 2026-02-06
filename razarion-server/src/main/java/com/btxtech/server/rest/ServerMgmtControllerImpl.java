package com.btxtech.server.rest;

import com.btxtech.shared.Constants;
import com.btxtech.shared.rest.ServerMgmtController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/servermgmt")
public class ServerMgmtControllerImpl implements ServerMgmtController {

    @Override
    @GetMapping(value = "serverstatus", produces = MediaType.TEXT_PLAIN_VALUE)
    // Enum as return value not working. Not proper JSON. Too many quotation marks.
    public String getServerStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @GetMapping(value = "interfaceVersion", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getInterfaceVersion() {
        return String.valueOf(Constants.INTERFACE_VERSION);
    }


}
