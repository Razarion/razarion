/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WATER_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bumpDistortionScale` double NOT NULL,
  `bumpMapDepth` double NOT NULL,
  `distortionDurationSeconds` double NOT NULL,
  `distortionStrength` double NOT NULL,
  `fresnelDelta` double NOT NULL,
  `fresnelOffset` double NOT NULL,
  `groundLevel` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `reflectionScale` double NOT NULL,
  `shininess` double NOT NULL,
  `specularStrength` double NOT NULL,
  `transparency` double NOT NULL,
  `waterLevel` double NOT NULL,
  `bumpMap_id` int(11) DEFAULT NULL,
  `distortion_id` int(11) DEFAULT NULL,
  `reflection_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk8u5re1hk96pkj5mbohe9fue7` (`bumpMap_id`),
  KEY `FKky3w6oh8qfvnu81s0ghcmlg8a` (`distortion_id`),
  KEY `FKjxbl886qx1n0nyxth0uto4g0j` (`reflection_id`),
  CONSTRAINT `FKjxbl886qx1n0nyxth0uto4g0j` FOREIGN KEY (`reflection_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKk8u5re1hk96pkj5mbohe9fue7` FOREIGN KEY (`bumpMap_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKky3w6oh8qfvnu81s0ghcmlg8a` FOREIGN KEY (`distortion_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

