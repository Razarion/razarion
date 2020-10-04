/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_HARVESTER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animationOriginX` double DEFAULT NULL,
  `animationOriginY` double DEFAULT NULL,
  `animationOriginZ` double DEFAULT NULL,
  `harvestRange` int(11) NOT NULL,
  `progress` double NOT NULL,
  `animationShape3d_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKegmix24ey7e76un4erbrvfcqr` (`animationShape3d_id`),
  CONSTRAINT `FKegmix24ey7e76un4erbrvfcqr` FOREIGN KEY (`animationShape3d_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

