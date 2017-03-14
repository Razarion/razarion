package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.webglemulator.razarion.renderer.DevToolFloat32ArrayEmu;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class DevToolGameEngineControl extends GameEngineControl {
    @Inject
    private WorkerEmulator workerEmulator;

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    protected void sendToWorker(GameEngineControlPackage.Command command, Object... data) {
        workerEmulator.sendPackage(new GameEngineControlPackage(command, data));
    }

    void receivePackage(GameEngineControlPackage gameEngineControlPackage) {
        if (gameEngineControlPackage.getCommand() == GameEngineControlPackage.Command.INITIALIZED) {
            gameEngineControlPackage = setupInitializedpackage(gameEngineControlPackage);
        }
        dispatch(gameEngineControlPackage);
    }

    private GameEngineControlPackage setupInitializedpackage(GameEngineControlPackage input) {
        // Ground
        VertexList groundVertexList = (VertexList) input.getData(0);
        GroundUi groundUi = new GroundUi(groundVertexList.getVerticesCount(), new DevToolFloat32ArrayEmu().setBufferFromVertex(groundVertexList.getVertices()),
                new DevToolFloat32ArrayEmu().setBufferFromVertex(groundVertexList.getNormVertices()),
                new DevToolFloat32ArrayEmu().setBufferFromVertex(groundVertexList.getTangentVertices()),
                new DevToolFloat32ArrayEmu().setBufferDoubles(groundVertexList.getSplattings()));
        // Slopes
        Collection<Slope> slopes = (Collection<Slope>) input.getData(1);
        List<SlopeUi> slopeUis = new ArrayList<>();
        for (Slope slope : slopes) {
            Mesh mesh = slope.getMesh();
            slopeUis.add(new SlopeUi(slope.getSlopeSkeletonConfig().getId(), mesh.size(), new DevToolFloat32ArrayEmu().setBufferFromVertex(mesh.getVertices()),
                    new DevToolFloat32ArrayEmu().setBufferFromVertex(mesh.getNorms()), new DevToolFloat32ArrayEmu().setBufferFromVertex(mesh.getTangents()),
                    new DevToolFloat32ArrayEmu().setBufferFloats(mesh.getSplatting()), new DevToolFloat32ArrayEmu().setBufferFloats(mesh.getSlopeFactors())));
        }
        // Water
        Water water = (Water) input.getData(2);
        WaterUi waterUi = new WaterUi(water.getVertices().size(), new DevToolFloat32ArrayEmu().setBufferFromVertex(water.getVertices()), new DevToolFloat32ArrayEmu().setBufferFromVertex(water.getNorms()),
                new DevToolFloat32ArrayEmu().setBufferFromVertex(water.getTangents()), water.calculateAabb());

        return new GameEngineControlPackage(input.getCommand(), groundUi, slopeUis, waterUi);
    }

    @Override
    protected void onLoaded() {
        throw new UnsupportedOperationException();
    }
}
