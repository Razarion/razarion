package com.btxtech.server;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.gwt.user.client.rpc.SerializationException;

import java.io.File;

/**
 * Created by Beat
 * 18.04.2015.
 */
public class GwtSuperDevModeLoggingServlet extends RemoteLoggingServiceImpl {
    private final static String TEMP_DIR = "C:\\Users\\Beat\\AppData\\Local\\Temp";
    private final static String MODULE_NAME = "com.btxtech.webgl";
    private final static String SIMPLE_MODULE_NAME = "webgl";

    private String path = null;

    @Override
    public String processCall(String payload) throws SerializationException {
        reconfigureMapsDir();
        return super.processCall(payload);
    }

    private void reconfigureMapsDir() {
        try {
            File[] tmpFiles = new File(TEMP_DIR).listFiles();
            if(tmpFiles == null) {
                System.out.println("Invalid temp file: " + TEMP_DIR);
                return;
            }

            File last = null;
            for (File f : tmpFiles) {
                if (f.getName().startsWith("gwt-codeserver-") && (last == null || f.lastModified() > last.lastModified())) {
                    last = f;
                }
            }

            File lastCompile = null;
            if (last != null) {
                File[] moduleFiles = new File(last.getPath(), MODULE_NAME).listFiles();
                if(moduleFiles == null) {
                    System.out.println("Invalid module file: " + new File(last.getPath(), MODULE_NAME));
                    return;
                }
                for (File f : moduleFiles) {
                    if (f.getName().startsWith("compile-") && (lastCompile == null || f.lastModified() > lastCompile.lastModified())) {
                        lastCompile = f;
                    }
                }
            }

            if (lastCompile != null) {
                String dirpath = lastCompile.getPath() + "\\extras\\" + SIMPLE_MODULE_NAME + "\\symbolMaps\\";
                if (!dirpath.equals(path)) {
                    path = dirpath;
                    setSymbolMapsDirectory(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
