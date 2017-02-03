package com.btxtech.client.editor.perfmon;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 28.11.2016.
 */
@Templated("PerfmonDialog.html#perfmon-dialog")
public class PerfmonDialog extends Composite implements ModalDialogContent<Void> {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 300;
    private static final double MAX_AVG_FREQUENCY = 60;
    private static final double MAX_AVG_DURATION = 0.1;
    // private Logger logger = Logger.getLogger(PerfmonDialog.class.getName());
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private GameEngineControl gameEngineControl;
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        modalDialogPanel.addNonClosableFooterButton("Refresh", this::display);
    }

    @Override
    public void init(Void aVoid) {
        display();
    }

    private void display() {
        SVGSVGElement svg = (SVGSVGElement) svgElement;
        while (svg.hasChildNodes()) {
            svg.removeChild(svg.getFirstChild());
        }
        if (gameEngineControl.isStarted()) {
            gameEngineControl.perfmonRequest(this::drawBar);
        } else {
            drawBar(null);
        }
    }

    private void drawBar(Collection<PerfmonStatistic> workerPerfmonStatistics) {
        Collection<PerfmonStatistic> allStatistics = new ArrayList<>();
        allStatistics.addAll(perfmonService.getPerfmonStatistics());
        if (workerPerfmonStatistics != null) {
            allStatistics.addAll(workerPerfmonStatistics);
        }

        SVGSVGElement svg = (SVGSVGElement) svgElement;

        int barPairWidth = WIDTH / PerfmonService.COUNT;
        int barWidth = barPairWidth / 2;
        int y = 0;
        for (PerfmonStatistic perfmonStatistic : allStatistics) {
            // Text
            SVGTextElement descr = Browser.getDocument().createSVGTextElement();
            descr.setTextContent(perfmonStatistic.getPerfmonEnum().toString());
            descr.setAttribute("x", "10px");
            descr.setAttribute("y", y + 20 + "px");
            descr.getStyle().setProperty("fill", "rgb(142, 142, 243)");
            descr.getStyle().setFontSize(15, CSSStyleDeclaration.Unit.PX);
            svg.appendChild(descr);
            // Bars
            for (int index = 0; index < perfmonStatistic.size(); index++) {
                double frequency = perfmonStatistic.getFrequency(index);
                // First bar is frequency
                SVGRectElement frequencyBar = Browser.getDocument().createSVGRectElement();
                frequencyBar.getX().getBaseVal().setValue(index * barPairWidth);
                frequencyBar.getAnimatedWidth().getBaseVal().setValue(barWidth);
                if (Double.isFinite(frequency) && !Double.isNaN(frequency)) {
                    float heightValue = (float) (HEIGHT / 2.0 * frequency / MAX_AVG_FREQUENCY);
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
                durationBar.getX().getBaseVal().setValue(index * barPairWidth + barWidth);
                durationBar.getAnimatedWidth().getBaseVal().setValue(barWidth);
                double avgDuration = perfmonStatistic.getAvgDuration(index);
                if (Double.isFinite(avgDuration) && !Double.isNaN(avgDuration)) {
                    float heightValue = (float) (HEIGHT / 2.0 * avgDuration / MAX_AVG_DURATION);
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
            y += HEIGHT / 2;
        }
    }

    @Override
    public void onClose() {

    }
}
