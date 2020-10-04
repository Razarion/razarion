/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_TURRET_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angleVelocity` double NOT NULL,
  `muzzlePositionX` double DEFAULT NULL,
  `muzzlePositionY` double DEFAULT NULL,
  `muzzlePositionZ` double DEFAULT NULL,
  `shape3dMaterialId` varchar(255) DEFAULT NULL,
  `torrentCenterX` double DEFAULT NULL,
  `torrentCenterY` double DEFAULT NULL,
  `torrentCenterZ` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

