package com.btxtech.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;

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
        return "";
    }

    @Override
    protected String getModuleSuffix2(TreeLogger logger, LinkerContext context, String strongName) throws UnableToCompleteException {
        return "";
    }

    @Override
    protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
        return "linker/WebWorkerTemplate.js";
    }

}
