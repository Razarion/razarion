package com.btxtech.server;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.gwt.user.client.rpc.SerializationException;

import javax.enterprise.context.Dependent;
import java.io.File;

/**
 * Created by Beat
 * 01.01.2017.
 */
@Deprecated
public abstract class AbstractGwtSuperDevModeLoggingServlet extends RemoteLoggingServiceImpl {
    private final static String TEMP_DIR = "C:\\Users\\Beat\\AppData\\Local\\Temp";
    private String path = null;

    protected abstract String getModuleName();

    protected abstract String getSimpleModuleName();

    @Override
    public String processCall(String payload) throws SerializationException {
        reconfigureMapsDir();
        return super.processCall(payload);
    }

    private void reconfigureMapsDir() {
        try {
            File[] tmpFiles = new File(TEMP_DIR).listFiles();
            if (tmpFiles == null) {
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
                File[] moduleFiles = new File(last.getPath(), getModuleName()).listFiles();
                if (moduleFiles == null) {
                    System.out.println("Invalid module file: " + new File(last.getPath(), getModuleName()));
                    return;
                }
                for (File f : moduleFiles) {
                    if (f.getName().startsWith("compile-") && (lastCompile == null || f.lastModified() > lastCompile.lastModified())) {
                        File file = new File(f.getPath() + "\\extras\\" + getSimpleModuleName() + "\\symbolMaps\\");
                        if (file.exists()) {
                            lastCompile = f;
                        }
                    }
                }
            }

            if (lastCompile != null) {
                String dirpath = lastCompile.getPath() + "\\extras\\" + getSimpleModuleName() + "\\symbolMaps\\";
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
