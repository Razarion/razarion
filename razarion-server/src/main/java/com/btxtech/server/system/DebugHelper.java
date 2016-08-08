package com.btxtech.server.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class DebugHelper {
    private static final String DEV_TOOL_RESOURCE_DIR = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-server-emulation\\src\\main\\resources";
    @Inject
    private Logger logger;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void writeToJsonFile(String fileName, Object object) {
        try {
            Gson gson = new Gson();
            File file = new File(DEV_TOOL_RESOURCE_DIR, fileName);
            FileWriter fileWriter = new FileWriter(file);
            gson.toJson(object, fileWriter);
            fileWriter.close();
            logger.severe("JSON file written to: " + file);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
        }
    }
}
