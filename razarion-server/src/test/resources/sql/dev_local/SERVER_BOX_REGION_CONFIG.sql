/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SERVER_BOX_REGION_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `maxInterval` int(11) NOT NULL,
  `minDistanceToItems` double NOT NULL,
  `minInterval` int(11) NOT NULL,
  `boxItemTypeId_id` int(11) DEFAULT NULL,
  `region_id` int(11) DEFAULT NULL,
  `serverGameEngineId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8i0nxbtvhyiksqd3m90j8sble` (`boxItemTypeId_id`),
  KEY `FKcq7owvwvc3cvnd2dy4b134ed2` (`region_id`),
  KEY `FK4q31xi3o8ty49njgjdolpc5rj` (`serverGameEngineId`),
  CONSTRAINT `FK4q31xi3o8ty49njgjdolpc5rj` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FK8i0nxbtvhyiksqd3m90j8sble` FOREIGN KEY (`boxItemTypeId_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKcq7owvwvc3cvnd2dy4b134ed2` FOREIGN KEY (`region_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

