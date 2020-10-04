/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TERRAIN_SLOPE_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inverted` bit(1) NOT NULL,
  `slopeConfigEntity_id` int(11) NOT NULL,
  `parentTerrainSlopePosition` int(11) DEFAULT NULL,
  `planet` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xqtrjsn0gheqg5og8e9s5mmt` (`slopeConfigEntity_id`),
  KEY `FKmhab0g7928t6vw57a79a17pn5` (`parentTerrainSlopePosition`),
  KEY `FK5ptx8wbuewvwsgwc7uom195h0` (`planet`),
  CONSTRAINT `FK4xqtrjsn0gheqg5og8e9s5mmt` FOREIGN KEY (`slopeConfigEntity_id`) REFERENCES `SLOPE_CONFIG` (`id`),
  CONSTRAINT `FK5ptx8wbuewvwsgwc7uom195h0` FOREIGN KEY (`planet`) REFERENCES `PLANET` (`id`),
  CONSTRAINT `FKmhab0g7928t6vw57a79a17pn5` FOREIGN KEY (`parentTerrainSlopePosition`) REFERENCES `TERRAIN_SLOPE_POSITION` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

