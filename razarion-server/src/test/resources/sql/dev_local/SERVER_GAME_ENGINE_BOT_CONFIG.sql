/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SERVER_GAME_ENGINE_BOT_CONFIG` (
  `serverGameEngineId` int(11) NOT NULL,
  `botConfigId` int(11) NOT NULL,
  UNIQUE KEY `UK_9w8m9rvbobngldmp0p3hdxgtl` (`botConfigId`),
  KEY `FKh6pao5d5ikcs0h79x6gwx8tfj` (`serverGameEngineId`),
  CONSTRAINT `FKh6pao5d5ikcs0h79x6gwx8tfj` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`),
  CONSTRAINT `FKrlpv849ke6l8gyvfsm5f8ya9g` FOREIGN KEY (`botConfigId`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

