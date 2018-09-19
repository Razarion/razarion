package com.btxtech.playback;

import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import javax.inject.Singleton;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * on 07.08.2018.
 */
@Singleton
public class PlaybackGuiController implements Initializable {
    private static SimpleDateFormat MILLIS_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    @FXML
    private AnchorPane anchorPanel;
    @FXML
    private Canvas canvas;
    @FXML
    private Slider zoomSlider;
    @FXML
    private TextField scaleField;
    @FXML
    private TextField mouseLabel;
    @FXML
    private Label masterTickCountLabel;
    @FXML
    private Label slaveTickCountLabel;
    @FXML
    private CheckBox showMasterCheck;
    @FXML
    private CheckBox showSlaveCheck;
    @FXML
    private TextField tickNumberField;
    @FXML
    private Label masterDateLabel;
    @FXML
    private Label slaveDateLabel;
    @FXML
    private Label masterTickNumberLabel;
    @FXML
    private Label slaveTickNumberLabel;
    private PlaybackGuiRenderer playbackGuiRenderer;
    private PlaybackService playbackService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playbackGuiRenderer = new PlaybackGuiRenderer(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            runRender();
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            runRender();
        });
        scaleField.setText(String.format("%.2f", playbackGuiRenderer.getScale()));
        zoomSlider.setValue(playbackGuiRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));
        playbackService = new PlaybackService(this::onPlaybackChanged);
        masterTickCountLabel.setText(Integer.toString(playbackService.getMasterTickCount()));
        slaveTickCountLabel.setText(Integer.toString(playbackService.getSlaveTickCount()));
        addRenderListener(showMasterCheck);
        addRenderListener(showSlaveCheck);
    }

    private void addRenderListener(CheckBox checkBox) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> runRender());
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        playbackGuiRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", playbackGuiRenderer.getScale()));
        runRender();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (playbackGuiRenderer.shifting(event)) {
            runRender();
        }
    }

    public void onMouseReleased() {
        playbackGuiRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition mousePosition = playbackGuiRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", mousePosition.getX(), mousePosition.getY()));
    }

    public void onPrefTickButtonClicked() {
        playbackService.prefTick();
    }

    public void onNextTickButtonClicked(ActionEvent actionEvent) {
        playbackService.nextTick();
    }

    public void onNumberTickFieldChanged(ActionEvent actionEvent) {
        try {
            if (tickNumberField.getText().trim().isEmpty()) {
                playbackService.setTick(0);
            } else {
                playbackService.setTick(Integer.parseInt(tickNumberField.getText()));
            }
        } catch (NumberFormatException t) {
            tickNumberField.setText(Integer.toString(playbackService.getCurrentTickNumber()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void onPlaybackChanged() {
        tickNumberField.setText(Integer.toString(playbackService.getCurrentTickNumber()));
        masterDateLabel.setText(MILLIS_DATE_FORMAT.format(playbackService.getCurrentMasterTickData().getDate()));
        slaveDateLabel.setText(MILLIS_DATE_FORMAT.format(playbackService.getCurrentSlaveTickData().getDate()));
        masterTickNumberLabel.setText(Integer.toString((int) playbackService.getCurrentMasterTickData().getTickCount()));
        slaveTickNumberLabel.setText(Integer.toString((int) playbackService.getCurrentSlaveTickData().getTickCount()));
        runRender();
    }

    private void runRender() {
        playbackGuiRenderer.render(showMasterCheck.isSelected() ? playbackService.getCurrentMasterTickData() : null,
                showSlaveCheck.isSelected() ? playbackService.getCurrentSlaveTickData() : null);
    }
}
