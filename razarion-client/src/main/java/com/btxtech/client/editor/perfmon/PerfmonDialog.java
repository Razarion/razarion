package com.btxtech.client.editor.perfmon;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.PerfmonStatisticEntry;
import com.btxtech.uiservice.control.GameEngineControl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.css.CSSStyleDeclaration;
import elemental.svg.SVGRectElement;
import elemental.svg.SVGSVGElement;
import elemental.svg.SVGTextElement;
import elemental.svg.SVGTitleElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Beat
 * 28.11.2016.
 */
@Templated("PerfmonDialog.html#perfmon-dialog")
public class PerfmonDialog extends Composite implements ModalDialogContent<Void> {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 300;
    private static final int BAR_WIDTH = (WIDTH / PerfmonService.COUNT) / 2;
    private static final Set<PerfmonEnum> FILTER = new HashSet<>(Arrays.asList(PerfmonEnum.RENDERER, PerfmonEnum.CLIENT_GAME_ENGINE_UPDATE, PerfmonEnum.GAME_ENGINE));
    // private Logger logger = Logger.getLogger(PerfmonDialog.class.getName());
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private GameEngineControl gameEngineControl;
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();
    private SVGSVGElement svg;

    @PostConstruct
    public void postConstruct() {
        svg = (SVGSVGElement) svgElement;
    }


    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        modalDialogPanel.addNonClosableFooterButton("Refresh", this::display);
    }

    @Override
    public void init(Void aVoid) {
        display();
    }

    private void display() {
        if (gameEngineControl.isStarted()) {
            drawBar(null);
            gameEngineControl.perfmonRequest(this::drawBar);
        } else {
            drawBar(null);
        }
    }

    private void drawBar(Collection<PerfmonStatistic> workerPerfmonStatistics) {
        while (svg.hasChildNodes()) {
            svg.removeChild(svg.getFirstChild());
        }
        Map<PerfmonEnum, PerfmonStatistic> allStatistics = new HashMap<>();
        perfmonService.peekClientPerfmonStatistics().forEach(perfmonStatistic -> allStatistics.put(perfmonStatistic.getPerfmonEnum(), perfmonStatistic));
        if (workerPerfmonStatistics != null) {
            workerPerfmonStatistics.forEach(perfmonStatistic -> allStatistics.put(perfmonStatistic.getPerfmonEnum(), perfmonStatistic));
        }

        int y = 0;
        for (PerfmonEnum perfmonEnum : FILTER) {
            PerfmonStatistic perfmonStatistic = allStatistics.get(perfmonEnum);
            double expectedFrequency = 60;
            double expectedDuration = 100;
            switch (perfmonEnum) {
                case RENDERER:
                    expectedFrequency = 65;
                    expectedDuration = 0.06;
                    break;
                case CLIENT_GAME_ENGINE_UPDATE:
                    expectedFrequency = 12;
                    expectedDuration = 0.020;
                    break;
                case GAME_ENGINE:
                    expectedFrequency = 12;
                    expectedDuration = 0.1;
                    break;
            }
            displayCurve(y, perfmonEnum, perfmonStatistic, expectedFrequency, expectedDuration);

            y += HEIGHT / 2;
        }
    }

    private void displayCurve(int y, PerfmonEnum perfmonEnum, PerfmonStatistic perfmonStatistic, double expectedFrequency, double expectedDuration) {
        // Text
        SVGTextElement descr = Browser.getDocument().createSVGTextElement();
        descr.setTextContent(perfmonEnum.toString());
        descr.setAttribute("x", "10px");
        descr.setAttribute("y", y + 20 + "px");
        descr.getStyle().setProperty("fill", "rgb(142, 142, 243)");
        descr.getStyle().setFontSize(15, CSSStyleDeclaration.Unit.PX);
        svg.appendChild(descr);
        if (perfmonStatistic == null) {
            return;
        }
        // Bars
        for (int index = 0; index < perfmonStatistic.getPerfmonStatisticEntries().size(); index++) {
            PerfmonStatisticEntry perfmonStatisticEntry = perfmonStatistic.getPerfmonStatisticEntries().get(index);
            // First bar is frequency
            SVGRectElement frequencyBar = Browser.getDocument().createSVGRectElement();
            frequencyBar.getX().getBaseVal().setValue(setupX(index, perfmonStatistic, 0));
            frequencyBar.getAnimatedWidth().getBaseVal().setValue(BAR_WIDTH);
            double frequency = perfmonStatisticEntry.getFrequency();
            if (Double.isFinite(frequency) && !Double.isNaN(frequency)) {
                float heightValue = (float) (HEIGHT / 2.0 * frequency / expectedFrequency);
                frequencyBar.getY().getBaseVal().setValue(y + HEIGHT / 2 - heightValue);
                frequencyBar.getAnimatedHeight().getBaseVal().setValue(heightValue);
                SVGTitleElement tooltip = Browser.getDocument().createSVGTitleElement();
                tooltip.setTextContent("[blue] Frequency: " + DisplayUtils.handleDouble2(frequency) + "hz");
                frequencyBar.appendChild(tooltip);
            } else {
                frequencyBar.getY().getBaseVal().setValue(y + HEIGHT / 2);
                frequencyBar.getAnimatedHeight().getBaseVal().setValue(0);
            }
            frequencyBar.getStyle().setProperty("fill", "blue");
            svg.appendChild(frequencyBar);
            // Second bar is avg duration
            SVGRectElement durationBar = Browser.getDocument().createSVGRectElement();
            durationBar.getX().getBaseVal().setValue(setupX(index, perfmonStatistic, BAR_WIDTH));
            durationBar.getAnimatedWidth().getBaseVal().setValue(BAR_WIDTH);
            double avgDuration = perfmonStatisticEntry.getAvgDuration();
            if (Double.isFinite(avgDuration) && !Double.isNaN(avgDuration)) {
                float heightValue = (float) (HEIGHT / 2.0 * avgDuration / expectedDuration);
                durationBar.getY().getBaseVal().setValue(y + HEIGHT / 2 - heightValue);
                durationBar.getAnimatedHeight().getBaseVal().setValue(heightValue);
                SVGTitleElement tooltip = Browser.getDocument().createSVGTitleElement();
                tooltip.setTextContent("[red] avg Duration: " + DisplayUtils.handleDouble3(avgDuration) + "s");
                durationBar.appendChild(tooltip);
            } else {
                durationBar.getY().getBaseVal().setValue(y + HEIGHT / 2);
                durationBar.getAnimatedHeight().getBaseVal().setValue(0);
            }
            durationBar.getStyle().setProperty("fill", "red");
            svg.appendChild(durationBar);
        }
    }

    private float setupX(int index, PerfmonStatistic perfmonStatistic, int additional) {
        long time = perfmonStatistic.getPerfmonStatisticEntries().get(index).getDate().getTime();
        long lapsOfTime = PerfmonService.COUNT * PerfmonService.DUMP_DELAY;
        return (float) (WIDTH * ((double) (time - (System.currentTimeMillis() - lapsOfTime)) / (double) (lapsOfTime)) + additional);
    }

    @Override
    public void onClose() {

    }
}
