package com.btxtech.server.model.engine;

import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BOT_CONFIG_GROUND_BOX_SLOPES")
public class BotGroundSlopeBoxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private double xPos;
    private double yPos;
    private double height;
    private double yRot;
    private double zRot;

    public BotGroundSlopeBox toBotGroundSlopeBox() {
        BotGroundSlopeBox botGroundSlopeBox = new BotGroundSlopeBox();
        botGroundSlopeBox.xPos = xPos;
        botGroundSlopeBox.yPos = yPos;
        botGroundSlopeBox.height = height;
        botGroundSlopeBox.yRot = yRot;
        botGroundSlopeBox.zRot = zRot;
        return botGroundSlopeBox;
    }

    public BotGroundSlopeBoxEntity fromBotGroundSlopeBox(BotGroundSlopeBox botGroundSlopeBox) {
        xPos = botGroundSlopeBox.xPos;
        yPos = botGroundSlopeBox.yPos;
        height = botGroundSlopeBox.height;
        yRot = botGroundSlopeBox.yRot;
        zRot = botGroundSlopeBox.zRot;
        return this;
    }

}
