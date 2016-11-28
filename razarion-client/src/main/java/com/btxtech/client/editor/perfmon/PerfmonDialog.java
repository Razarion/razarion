package com.btxtech.client.editor.perfmon;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.StatisticEntry;
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
import java.util.List;
import java.util.Map;

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
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        modalDialogPanel.addFooterButton("Refresh", this::drawBar);
    }

    @Override
    public void init(Void aVoid) {
        drawBar();
    }

    private void drawBar() {
        SVGSVGElement svg = (SVGSVGElement) svgElement;
        while (svg.hasChildNodes()) {
            svg.removeChild(svg.getFirstChild());
        }
        MapList<PerfmonEnum, StatisticEntry> statisticEntries = perfmonService.getStatisticEntries();

        int barPairWidth = WIDTH / PerfmonService.COUNT;
        int barWidth = barPairWidth / 2;
        int y = 0;
        for (Map.Entry<PerfmonEnum, List<StatisticEntry>> entry : statisticEntries.getMap().entrySet()) {
            // Text
            SVGTextElement descr = Browser.getDocument().createSVGTextElement();
            descr.setTextContent(entry.getKey().toString());
            descr.setAttribute("x", "10px");
            descr.setAttribute("y", y + 20 + "px");
            descr.getStyle().setProperty("fill", "rgb(142, 142, 243)");
            descr.getStyle().setFontSize(15, CSSStyleDeclaration.Unit.PX);
            svg.appendChild(descr);
            // Bars
            int x = 0;
            for (StatisticEntry statisticEntry : entry.getValue()) {
                // First bar is frequency
                SVGRectElement frequencyBar = Browser.getDocument().createSVGRectElement();
                frequencyBar.getX().getBaseVal().setValue(x);
                frequencyBar.getAnimatedWidth().getBaseVal().setValue(barWidth);
                if (Double.isFinite(statisticEntry.getFrequency()) && !Double.isNaN(statisticEntry.getFrequency())) {
                    float heightValue = (float) (HEIGHT / 2.0 * statisticEntry.getFrequency() / MAX_AVG_FREQUENCY);
                    frequencyBar.getY().getBaseVal().setValue(y + HEIGHT / 2 - heightValue);
                    frequencyBar.getAnimatedHeight().getBaseVal().setValue(heightValue);
                    SVGTitleElement tooltip = Browser.getDocument().createSVGTitleElement();
                    tooltip.setTextContent("[blue] Frequency:" + statisticEntry.getFrequency() + "hz");
                    frequencyBar.appendChild(tooltip);
                } else {
                    frequencyBar.getY().getBaseVal().setValue(y + HEIGHT / 2);
                    frequencyBar.getAnimatedHeight().getBaseVal().setValue(0);
                }
                frequencyBar.getStyle().setProperty("fill", "blue");
                svg.appendChild(frequencyBar);
                // First bar is avg duration
                SVGRectElement durationBar = Browser.getDocument().createSVGRectElement();
                durationBar.getX().getBaseVal().setValue(x + barWidth);
                durationBar.getAnimatedWidth().getBaseVal().setValue(barWidth);
                if (Double.isFinite(statisticEntry.getAvgDuration()) && !Double.isNaN(statisticEntry.getAvgDuration())) {
                    float heightValue = (float) (HEIGHT / 2.0 * statisticEntry.getAvgDuration() / MAX_AVG_DURATION);
                    durationBar.getY().getBaseVal().setValue(y + HEIGHT / 2 - heightValue);
                    durationBar.getAnimatedHeight().getBaseVal().setValue(heightValue);
                    SVGTitleElement tooltip = Browser.getDocument().createSVGTitleElement();
                    tooltip.setTextContent("[red] avg Duration:" + statisticEntry.getAvgDuration() + "s");
                    durationBar.appendChild(tooltip);
                } else {
                    durationBar.getY().getBaseVal().setValue(y + HEIGHT / 2);
                    durationBar.getAnimatedHeight().getBaseVal().setValue(0);
                }
                durationBar.getStyle().setProperty("fill", "red");
                svg.appendChild(durationBar);

                x += barPairWidth;
            }
            y += HEIGHT / 2;
        }
    }

    @Override
    public void onClose() {

    }
}
