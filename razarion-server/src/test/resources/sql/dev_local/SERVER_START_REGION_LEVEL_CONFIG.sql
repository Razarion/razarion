/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SERVER_START_REGION_LEVEL_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `minimalLevel_id` int(11) DEFAULT NULL,
  `serverGameEngineId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK88fp044cjqk0qx3ly7mg8fd8v` (`minimalLevel_id`),
  KEY `FKnv57ds9hqity6ikfkwgighxvl` (`serverGameEngineId`),
  CONSTRAINT `FK88fp044cjqk0qx3ly7mg8fd8v` FOREIGN KEY (`minimalLevel_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKnv57ds9hqity6ikfkwgighxvl` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

