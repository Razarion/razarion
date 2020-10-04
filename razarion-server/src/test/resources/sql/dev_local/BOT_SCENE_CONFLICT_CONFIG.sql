/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOT_SCENE_CONFLICT_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `enterDuration` int(11) NOT NULL,
  `enterKills` int(11) NOT NULL,
  `leaveNoKillDuration` int(11) NOT NULL,
  `maxDistance` double NOT NULL,
  `minDistance` double NOT NULL,
  `rePopMillis` int(11) DEFAULT NULL,
  `stopKills` int(11) DEFAULT NULL,
  `stopMillis` int(11) DEFAULT NULL,
  `botConfig_id` int(11) DEFAULT NULL,
  `targetBaseItemType_id` int(11) DEFAULT NULL,
  `botConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsa7ypi28v52meu8c3le2nwaom` (`botConfig_id`),
  KEY `FKlma4qi8pwcrif0ckup8rxrya7` (`targetBaseItemType_id`),
  KEY `FKeiobef7r4fmbjbxa1pghslhvm` (`botConfig`),
  CONSTRAINT `FKeiobef7r4fmbjbxa1pghslhvm` FOREIGN KEY (`botConfig`) REFERENCES `BOT_SCENE_CONFIG` (`id`),
  CONSTRAINT `FKlma4qi8pwcrif0ckup8rxrya7` FOREIGN KEY (`targetBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsa7ypi28v52meu8c3le2nwaom` FOREIGN KEY (`botConfig_id`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

