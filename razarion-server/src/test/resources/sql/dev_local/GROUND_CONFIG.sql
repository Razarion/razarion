/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GROUND_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bottomBumpMapDepth` double DEFAULT NULL,
  `bottomScale` double DEFAULT NULL,
  `bottomShininess` double DEFAULT NULL,
  `bottomSpecularStrength` double DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `splattingBlur` double DEFAULT NULL,
  `splattingOffset` double DEFAULT NULL,
  `splattingScale1` double DEFAULT NULL,
  `splattingScale2` double DEFAULT NULL,
  `topBumpMapDepth` double DEFAULT NULL,
  `topScale` double DEFAULT NULL,
  `topShininess` double DEFAULT NULL,
  `topSpecularStrength` double DEFAULT NULL,
  `bottomBumpMapId` int(11) DEFAULT NULL,
  `bottomTextureId` int(11) DEFAULT NULL,
  `splattingTextureId` int(11) DEFAULT NULL,
  `topBumpMapId` int(11) DEFAULT NULL,
  `topTextureId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrwtceqklrjay1axmmlfj75di3` (`bottomBumpMapId`),
  KEY `FKnoefvdom880ef7ioae7tjwct8` (`bottomTextureId`),
  KEY `FK8vbr1544f1nkaxkkhk7vdf1ew` (`splattingTextureId`),
  KEY `FKkqjsikvnicb0qo3auqhlghq3a` (`topBumpMapId`),
  KEY `FKnrbnl1yvrst306eyg8mkpqt6i` (`topTextureId`),
  CONSTRAINT `FK8vbr1544f1nkaxkkhk7vdf1ew` FOREIGN KEY (`splattingTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKkqjsikvnicb0qo3auqhlghq3a` FOREIGN KEY (`topBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKnoefvdom880ef7ioae7tjwct8` FOREIGN KEY (`bottomTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKnrbnl1yvrst306eyg8mkpqt6i` FOREIGN KEY (`topTextureId`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKrwtceqklrjay1axmmlfj75di3` FOREIGN KEY (`bottomBumpMapId`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=255 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

