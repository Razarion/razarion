/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GAME_UI_CONTEXT` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `detailedTracking` bit(1) NOT NULL,
  `gameEngineMode` varchar(255) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `minimalLevel_id` int(11) DEFAULT NULL,
  `planetEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg2h672edbiaseb0sabbdto23x` (`minimalLevel_id`),
  KEY `FKj9wevg184w0jdmabmqk8h93lg` (`planetEntity_id`),
  CONSTRAINT `FKg2h672edbiaseb0sabbdto23x` FOREIGN KEY (`minimalLevel_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKj9wevg184w0jdmabmqk8h93lg` FOREIGN KEY (`planetEntity_id`) REFERENCES `PLANET` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

