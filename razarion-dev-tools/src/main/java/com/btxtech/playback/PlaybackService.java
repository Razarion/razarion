package com.btxtech.playback;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.MathHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 18.09.2018.
 */
public class PlaybackService {
    private Map<Long, DebugHelperStatic.TickData> masterTickDatas;
    private Map<Long, DebugHelperStatic.TickData> slaveTickDatas;
    private long minMasterTickNumber;
    private long maxMasterTickNumber;
    private long minSlaveTickNumber;
    private long maxSlaveTickNumber;
    private long minTickNumber;
    private long maxTickNumber;
    private long currentTickNumber = -1;
    private DebugHelperStatic.TickData currentMasterTickData;
    private DebugHelperStatic.TickData currentSlaveTickData;
    private Runnable updateListener;

    public PlaybackService(Runnable updateListener) {
        this.updateListener = updateListener;
        try {
            masterTickDatas = readTickData(new File(DebugHelperStatic.TICK_DATA_MASTER));
            slaveTickDatas = readTickData(new File(DebugHelperStatic.TICK_DATA_SLAVE));
            minMasterTickNumber = masterTickDatas.values().stream().min(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            maxMasterTickNumber = masterTickDatas.values().stream().max(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            minSlaveTickNumber = slaveTickDatas.values().stream().min(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            maxSlaveTickNumber = slaveTickDatas.values().stream().max(Comparator.comparingDouble(DebugHelperStatic.TickData::getTickCount)).map(tickData -> (int) tickData.getTickCount()).orElseThrow(Exception::new);
            minTickNumber = Math.max(minMasterTickNumber, minSlaveTickNumber);
            maxTickNumber = Math.min(maxMasterTickNumber, maxSlaveTickNumber);
            findTeleportation();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void findTeleportation() {
        for (long i = minTickNumber; i <= maxTickNumber; i++) {
            DebugHelperStatic.TickData master = masterTickDatas.get(i);
            DebugHelperStatic.TickData slave = slaveTickDatas.get(i);
            if (master != null && slave != null) {
                compareTickSyncBaseItems(master.getTickSyncBaseItems(), slave.getTickSyncBaseItems(), i);
            }
        }
    }

    private void compareTickSyncBaseItems(List<DebugHelperStatic.TickSyncBaseItem> master, List<DebugHelperStatic.TickSyncBaseItem> slave, long tickCount) {
        if (master.size() != slave.size()) {
            System.out.println("Tick count: " + tickCount + ". Different SyncBaseItem count");
            return;
        }
        master.forEach(masterItem -> {
            DebugHelperStatic.TickSyncBaseItem slaveItem = findTickSyncBaseItem(slave, masterItem.getId());
            if (slaveItem == null) {
                System.out.println("Tick count: " + tickCount + ". No slave item for master id: " + masterItem.getId());
                return;
            }
            double distance = slaveItem.getPosition().getDistance(masterItem.getPosition());
            double angle = MathHelper.getAngle(masterItem.getAngle(), slaveItem.getAngle());
            double vDistance = DecimalPosition.zeroIfNull(masterItem.getVelocity()).getDistance(DecimalPosition.zeroIfNull(slaveItem.getVelocity()));
            double pvDistance = DecimalPosition.zeroIfNull(masterItem.getPreferredVelocity()).getDistance(DecimalPosition.zeroIfNull(slaveItem.getPreferredVelocity()));
            boolean pathEquals = comparePath(masterItem.getPath(), slaveItem.getPath());
            if (distance > 0.001 || Math.toDegrees(angle) > 0.001 || vDistance > 0.001 || pvDistance > 0.001 || !pathEquals) {
                System.out.println("Tick count: " + tickCount + ". TELEPORTATION: " + masterItem.getId() + ". Distance: " + distance + ". vDistance: " + vDistance + ". pvDistance: " + pvDistance + ". Angle: " + Math.toDegrees(angle) + "Deg. Path equals: " + pathEquals);
            }
        });
    }

    private boolean comparePath(List<DecimalPosition> masterItemPath, List<DecimalPosition> slaveItemPath) {
        if (masterItemPath == null && slaveItemPath == null) {
            return true;
        } else if (masterItemPath != null && slaveItemPath == null) {
            return false;
        } else if (masterItemPath == null) {
            return false;
        }

        if (masterItemPath.size() != slaveItemPath.size()) {
            return false;
        }

        for (int i = 0; i < masterItemPath.size(); i++) {
            if (!masterItemPath.get(i).equals(slaveItemPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    private DebugHelperStatic.TickSyncBaseItem findTickSyncBaseItem(List<DebugHelperStatic.TickSyncBaseItem> slave, int id) {
        return slave.stream().filter(tickSyncBaseItem -> tickSyncBaseItem.getId() == id).findFirst().orElse(null);
    }

    private Map<Long, DebugHelperStatic.TickData> readTickData(File file) throws IOException {
        List<DebugHelperStatic.TickData> tickList = new ObjectMapper().readValue(file, new TypeReference<List<DebugHelperStatic.TickData>>() {
        });
        Map<Long, DebugHelperStatic.TickData> tickDatas = new HashMap<>();
        tickList.forEach(tickData -> tickDatas.put((long) tickData.getTickCount(), tickData));
        return tickDatas;
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
        currentMasterTickData = masterTickDatas.get(currentTickNumber);
        currentSlaveTickData = slaveTickDatas.get(currentTickNumber);
    }

    private void notifyUpdateListener() {
        if (updateListener != null) {
            updateListener.run();
        }
    }

    public long getCurrentTickNumber() {
        return currentTickNumber;
    }

    public DebugHelperStatic.TickData getCurrentMasterTickData() {
        return currentMasterTickData;
    }

    public DebugHelperStatic.TickData getCurrentSlaveTickData() {
        return currentSlaveTickData;
    }

    public String toMasterString() {
        return dumpItems(currentMasterTickData);
    }

    public String toSlaveString() {
        return dumpItems(currentSlaveTickData);
    }

    private String dumpItems(DebugHelperStatic.TickData tickData) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tick count: ");
        builder.append(tickData.getTickCount());
        builder.append("\nDate: ");
        builder.append(PlaybackGuiController.MILLIS_DATE_FORMAT.format(tickData.getDate()));
        for (DebugHelperStatic.TickSyncBaseItem tickSyncBaseItem : tickData.getTickSyncBaseItems()) {
            builder.append("\n");
            builder.append(tickSyncBaseItem.getId());
            builder.append(". Pos: ").append(tickSyncBaseItem.getPosition());
            builder.append(". Preferred v: ").append(tickSyncBaseItem.getPreferredVelocity());
            builder.append(". v: ").append(tickSyncBaseItem.getVelocity());
            builder.append(". Path: ").append(tickSyncBaseItem.getPath());
        }
        return builder.toString();
    }
}