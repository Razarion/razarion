package com.btxtech.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;
import com.google.gwt.core.linker.SymbolMapsLinker;

/**
 * Created by Beat
 * 31.12.2016.
 */
@LinkerOrder(LinkerOrder.Order.PRIMARY)
@Shardable
public class WorkerLinker extends SelectionScriptLinker {

    @Override
    public String getDescription() {
        return "Web Worker Linker";
    }

    @Override
    protected String getCompilationExtension(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
        return ".cache.js";
    }

    @Override
    protected String getModulePrefix(TreeLogger logger, LinkerContext context, String strongName) throws UnableToCompleteException {
        return "var $strongName = '" + strongName + "';";
    }

    @Override
    protected String getModuleSuffix2(TreeLogger logger, LinkerContext context, String strongName) throws UnableToCompleteException {
//        int fragmentId = 0;
//        String sourceMapUrl = getSourceMapUrl(context, strongName, fragmentId);
//        if (sourceMapUrl != null) {
//            return "\n//# sourceMappingURL=" + sourceMapUrl + " " + "\n//# sourceURL=" + context.getModuleName() + "-" + fragmentId + ".js\n";
//        } else {
//            return "";
//        }
        return "\n//# sourceMappingURL=symbolMaps/" + strongName + "_sourceMap0.json";
    }

    @Override
    protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
        return "linker/WebWorkerTemplate.js";
    }

    /**
     * Returns the sourcemap URL that will be put in the comment at the end of a JavaScript
     * fragment, or null if the comment should be omitted. The default implementation uses
     * the includeSourceMapUrl config property.
     */
    private String getSourceMapUrl(LinkerContext context, String strongName, int fragmentId) {
        String val = getStringConfigurationProperty(context, "includeSourceMapUrl", "false");

        if ("false".equalsIgnoreCase(val)) {
            return null;
        }

        if ("true".equalsIgnoreCase(val)) {
            return SymbolMapsLinker.SourceMapArtifact.sourceMapFilenameForFragment(fragmentId);
        }

        return val.replaceAll("__HASH__", strongName)
                .replaceAll("__FRAGMENT__", String.valueOf(fragmentId))
                .replaceAll("__MODULE__", context.getModuleName());
    }

    private String getStringConfigurationProperty(LinkerContext context, String name, String def) {
        for (ConfigurationProperty property : context.getConfigurationProperties()) {
            if (property.getName().equals(name) && property.getValues().size() > 0) {
                if (property.getValues().get(0) != null) {
                    return property.getValues().get(0);
                }
            }
        }
        return def;
    }

}
