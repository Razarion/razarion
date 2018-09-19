package com.btxtech.playback;

import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.MathHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Beat
 * on 18.09.2018.
 */
public class PlaybackService {
    private List<DebugHelperStatic.TickData> masterTickDatas;
    private List<DebugHelperStatic.TickData> slaveTickDatas;
    private int minMasterTickNumber;
    private int maxMasterTickNumber;
    private int minSlaveTickNumber;
    private int maxSlaveTickNumber;
    private int minTickNumber;
    private int maxTickNumber;
    private int currentTickNumber = -1;
    private DebugHelperStatic.TickData currentMasterTickData;
    private DebugHelperStatic.TickData currentSlaveTickData;
    private Runnable updateListener;

    public PlaybackService(Runnable updateListener) {
        this.updateListener = updateListener;
        try {
            masterTickDatas = readTickData(new File(DebugHelperStatic.TICK_DATA_MASTER));
            slaveTickDatas = readTickData(new File(DebugHelperStatic.TICK_DATA_SLAVE));
            minMasterTickNumber = masterTickDatas.stream().min(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            maxMasterTickNumber = masterTickDatas.stream().max(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            minSlaveTickNumber = slaveTickDatas.stream().min(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            maxSlaveTickNumber = slaveTickDatas.stream().max(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            minTickNumber = Math.max(minMasterTickNumber, minSlaveTickNumber);
            maxTickNumber = Math.min(maxMasterTickNumber, maxSlaveTickNumber);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private List<DebugHelperStatic.TickData> readTickData(File file) throws IOException {
        return new ObjectMapper().readValue(file, new TypeReference<List<DebugHelperStatic.TickData>>() {
        });
    }

    public int getMasterTickCount() {
        return masterTickDatas.size();
    }

    public int getSlaveTickCount() {
        return slaveTickDatas.size();
    }

    public void nextTick() {
        if (currentTickNumber == -1) {
            currentTickNumber = maxTickNumber;
        } else {
            currentTickNumber++;
        }
        setupCurrentTick();
        notifyUpdateListener();
    }

    public void prefTick() {
        currentTickNumber--;
        setupCurrentTick();
        notifyUpdateListener();
    }

    public void setTick(int tickNumber) {
        currentTickNumber = tickNumber;
        setupCurrentTick();
        notifyUpdateListener();
    }

    private void setupCurrentTick() {
        currentTickNumber = MathHelper.clamp(currentTickNumber, minTickNumber, maxTickNumber);
        currentMasterTickData = masterTickDatas.get(currentTickNumber - minMasterTickNumber);
        currentSlaveTickData = slaveTickDatas.get(currentTickNumber - minSlaveTickNumber);
    }

    private void notifyUpdateListener() {
        if (updateListener != null) {
            updateListener.run();
        }
    }

    public int getCurrentTickNumber() {
        return currentTickNumber;
    }

    public DebugHelperStatic.TickData getCurrentMasterTickData() {
        return currentMasterTickData;
    }

    public DebugHelperStatic.TickData getCurrentSlaveTickData() {
        return currentSlaveTickData;
    }
}
