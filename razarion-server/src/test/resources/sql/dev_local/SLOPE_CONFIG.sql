/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SLOPE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coastDelimiterLineGameEngine` double NOT NULL,
  `horizontalSpace` double NOT NULL,
  `innerLineGameEngine` double NOT NULL,
  `innerSplattingBlur` double DEFAULT NULL,
  `innerSplattingImpact` double DEFAULT NULL,
  `innerSplattingOffset` double DEFAULT NULL,
  `innerSplattingScale` double DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `interpolateNorm` bit(1) NOT NULL,
  `materialBumpMapDepth` double DEFAULT NULL,
  `materialScale` double DEFAULT NULL,
  `materialShininess` double DEFAULT NULL,
  `materialSpecularStrength` double DEFAULT NULL,
  `outerLineGameEngine` double NOT NULL,
  `outerSplattingBlur` double DEFAULT NULL,
  `outerSplattingImpact` double DEFAULT NULL,
  `outerSplattingOffset` double DEFAULT NULL,
  `outerSplattingScale` double DEFAULT NULL,
  `shallowWaterDistortionStrength` double DEFAULT NULL,
  `shallowWaterDurationSeconds` double DEFAULT NULL,
  `shallowWaterScale` double DEFAULT NULL,
  `groundConfig_id` int(11) DEFAULT NULL,
  `innerSplattingTextureId` int(11) DEFAULT NULL,
  `materialBumpMapId` int(11) DEFAULT NULL,
  `materialTextureId` int(11) DEFAULT NULL,
  `outerSplattingTextureId` int(11) DEFAULT NULL,
  `shallowWaterDistortionId` int(11) DEFAULT NULL,
  `shallowWaterStencilId` int(11) DEFAULT NULL,
  `shallowWaterTextureId` int(11) DEFAULT NULL,
  `waterConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm6v85hb3xsj0d7o7dcsyutdtu` (`groundConfig_id`),
  KEY `FKq4ko6usrulwximbtoh9sfua6m` (`innerSplattingTextureId`),
  KEY `FKd0t0cu236j6ew5sub5qa5vbld` (`materialBumpMapId`),
  KEY `FKfmabq4sou5w8whtsyshp15jwe` (`materialTextureId`),
  KEY `FKkr6ugilc4ilmpx9k4niq0v5h3` (`outerSplattingTextureId`),
  KEY `FKikwl0enwj0gso8unw0ijr9xh7` (`shallowWaterDistortionId`),
  KEY `FK37ttmo5xct34jlqhc9wynq6h5` (`shallowWaterStencilId`),
  KEY `FKkys6s1fy27hepboitgejhub89` (`shallowWaterTextureId`),
  KEY `FKemea7ct22uhupjb877kxqpud9` (`waterConfig_id`),
  CONSTRAINT `FK37ttmo5xct34jlqhc9wynq6h5` FOREIGN KEY (`shallowWaterStencilId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKd0t0cu236j6ew5sub5qa5vbld` FOREIGN KEY (`materialBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKemea7ct22uhupjb877kxqpud9` FOREIGN KEY (`waterConfig_id`) REFERENCES `WATER_CONFIG` (`id`),
  CONSTRAINT `FKfmabq4sou5w8whtsyshp15jwe` FOREIGN KEY (`materialTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKikwl0enwj0gso8unw0ijr9xh7` FOREIGN KEY (`shallowWaterDistortionId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKkr6ugilc4ilmpx9k4niq0v5h3` FOREIGN KEY (`outerSplattingTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKkys6s1fy27hepboitgejhub89` FOREIGN KEY (`shallowWaterTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKm6v85hb3xsj0d7o7dcsyutdtu` FOREIGN KEY (`groundConfig_id`) REFERENCES `GROUND_CONFIG` (`id`),
  CONSTRAINT `FKq4ko6usrulwximbtoh9sfua6m` FOREIGN KEY (`innerSplattingTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

