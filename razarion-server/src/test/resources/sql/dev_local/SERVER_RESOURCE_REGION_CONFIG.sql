/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SERVER_RESOURCE_REGION_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `minDistanceToItems` double NOT NULL,
  `region_id` int(11) DEFAULT NULL,
  `resourceItemType_id` int(11) DEFAULT NULL,
  `serverGameEngineId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrwr42mqa70kps709cr58h2iir` (`region_id`),
  KEY `FKms0li4pxnifye8592vt0yksum` (`resourceItemType_id`),
  KEY `FKm41ytluw6qxyiba2ft8k7p11d` (`serverGameEngineId`),
  CONSTRAINT `FKm41ytluw6qxyiba2ft8k7p11d` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FKms0li4pxnifye8592vt0yksum` FOREIGN KEY (`resourceItemType_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrwr42mqa70kps709cr58h2iir` FOREIGN KEY (`region_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

