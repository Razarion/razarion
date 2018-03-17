package com.btxtech.server.rest;

import com.btxtech.server.persistence.ServerDebugHelper;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.dto.LogRecordInfo;
import com.btxtech.shared.dto.StackTraceElementLogInfo;
import com.btxtech.shared.dto.ThrownLogInfo;
import com.btxtech.shared.rest.LoggingProvider;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.core.server.StackTraceDeobfuscator;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.02.2017.
 */
public class LoggingProviderImpl implements LoggingProvider {
    //    private final static String TEMP_DIR = "C:\\Users\\Beat\\AppData\\Local\\Temp";/
//    private final static String MODULE_NAME = "com.btxtech.Razarion-Client";
//    private final static String SIMPLE_MODULE_NAME = "razarion_client";
    private Logger logger = Logger.getLogger(LoggingProviderImpl.class.getName());
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerDebugHelper serverDebugDbLogger;
    @Context
    private ServletContext context;
    // @Inject
    // private FilePropertiesService filePropertiesService;
    private Map<String, StackTraceDeobfuscator> stackTraceDeobfuscators = new HashMap<>();

    @Override
    public void simpleLogger(String logString) {
        logger.severe("simpleLogger: " + setupUserWebString() + "\n" + logString);
    }

    @Override
    public void jsonLogger(LogRecordInfo logRecordInfo) {
        try {
            String s = "jsonLogger: " + setupUserWebString()
                    + "\n" + "GWT module name: " + logRecordInfo.getGwtModuleName()
                    + "\n" + "Client time: " + new Date(Long.parseLong(logRecordInfo.getMillis()))
                    + "\n" + "Message: " + logRecordInfo.getMessage();
            if (logRecordInfo.getThrown() != null) {
                s += "\nThrown: " + thrownToString(convertToThrown(logRecordInfo.getThrown(), logRecordInfo.getGwtModuleName(), logRecordInfo.getGwtStrongName()));
            } else {
                s += "\n";
            }
            s += "Logger name: " + logRecordInfo.getLoggerName();
            s += "\n" + "GWT strong name: " + logRecordInfo.getGwtStrongName();
            logger.log(Level.parse(logRecordInfo.getLevel()), s);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Logging from client failed. LogRecordInfo: " + logRecordInfo, throwable);
        }
    }

    @Override
    public void jsonDebugDbLogger(String debugMessage) {
        try {
            serverDebugDbLogger.debugToDb(debugMessage, "Client", sessionHolder.getPlayerSession().getHttpSessionId());
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Debug client failed. debugMessage: " + debugMessage);
        }
    }

    private String setupUserWebString() {
        return " SessionId: " + sessionHolder.getPlayerSession().getHttpSessionId() + " " + sessionHolder.getPlayerSession().getUserContext();
    }

    private String thrownToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private Throwable convertToThrown(ThrownLogInfo thrownLogInfo, String gwtModuleName, String strongName) {
        Throwable throwable = setupThrown(thrownLogInfo);
        StackTraceDeobfuscator stackTraceDeobfuscator = getStackTraceDeobfuscator(gwtModuleName);
        if (stackTraceDeobfuscator != null) {
            stackTraceDeobfuscator.deobfuscateStackTrace(throwable, strongName);
        }
        return throwable;
    }

    private Throwable setupThrown(ThrownLogInfo thrownLogInfo) {
        Throwable throwable;
        if (thrownLogInfo.getCause() != null) {
            throwable = new Throwable(thrownLogInfo.getClassInfo() + ": " + thrownLogInfo.getMessage(), setupThrown(thrownLogInfo.getCause()));
        } else {
            throwable = new Throwable(thrownLogInfo.getClassInfo() + ": " + thrownLogInfo.getMessage());
        }
        throwable.setStackTrace(setupStackTrace(thrownLogInfo.getStackTrace()));
        return throwable;
    }

    private StackTraceElement[] setupStackTrace(List<StackTraceElementLogInfo> elementLogInfos) {
        if (elementLogInfos == null || elementLogInfos.isEmpty()) {
            return null;
        }
        StackTraceElement[] stackTraceElements = new StackTraceElement[elementLogInfos.size()];
        for (int i = 0; i < elementLogInfos.size(); i++) {
            StackTraceElementLogInfo elementLogInfo = elementLogInfos.get(i);
            stackTraceElements[i] = new StackTraceElement(elementLogInfo.getDeclaringClass(), elementLogInfo.getMethodName(), elementLogInfo.getFileName(), elementLogInfo.getLineNumber());
        }
        return stackTraceElements;
    }

    private StackTraceDeobfuscator getStackTraceDeobfuscator(String gwtModuleName) {
        StackTraceDeobfuscator stackTraceDeobfuscator = stackTraceDeobfuscators.get(gwtModuleName);
        if (stackTraceDeobfuscator != null) {
            return stackTraceDeobfuscator;
        }
        try {
            URL url = context.getResource("/debug/" + gwtModuleName + "/symbolMaps/");
            stackTraceDeobfuscator = StackTraceDeobfuscator.fromUrl(url);
            stackTraceDeobfuscators.put(gwtModuleName, stackTraceDeobfuscator);
            return stackTraceDeobfuscator;
        } catch (MalformedURLException e) {
            exceptionHandler.handleException(e);
        }
        return null;
    }


//    private String getFilePath() {
//        try {
//            File[] tmpFiles = new File(TEMP_DIR).listFiles();
//            if (tmpFiles == null) {
//                System.out.println("Invalid temp file: " + TEMP_DIR);
//                return null;
//            }
//
//            File last = null;
//            for (File f : tmpFiles) {
//                if (f.getName().startsWith("gwt-codeserver-") && (last == null || f.lastModified() > last.lastModified())) {
//                    last = f;
//                }
//            }
//
//            File lastCompile = null;
//            if (last != null) {
//                File[] moduleFiles = new File(last.getPath(), MODULE_NAME).listFiles();
//                if (moduleFiles == null) {
//                    System.out.println("Invalid module file: " + new File(last.getPath(), MODULE_NAME));
//                    return null;
//                }
//                for (File f : moduleFiles) {
//                    if (f.getName().startsWith("compile-") && (lastCompile == null || f.lastModified() > lastCompile.lastModified())) {
//                        File file = new File(f.getPath() + "\\extras\\" + SIMPLE_MODULE_NAME + "\\symbolMaps\\");
//                        if (file.exists()) {
//                            lastCompile = f;
//                        }
//                    }
//                }
//            }
//
//            if (lastCompile != null) {
//                return lastCompile.getPath() + "\\extras\\" + SIMPLE_MODULE_NAME + "\\symbolMaps\\";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}
