/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TERRAIN_SLOPE_CORNER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `drivewayConfigEntity_id` int(11) DEFAULT NULL,
  `terrainSlopePositionId` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn3or3udtex818ureyxe80ps47` (`drivewayConfigEntity_id`),
  KEY `FKcge2ffcawpabcjmml8cjsgc7v` (`terrainSlopePositionId`),
  CONSTRAINT `FKcge2ffcawpabcjmml8cjsgc7v` FOREIGN KEY (`terrainSlopePositionId`) REFERENCES `TERRAIN_SLOPE_POSITION` (`id`),
  CONSTRAINT `FKn3or3udtex818ureyxe80ps47` FOREIGN KEY (`drivewayConfigEntity_id`) REFERENCES `SLOPE_DRIVEWAY` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

