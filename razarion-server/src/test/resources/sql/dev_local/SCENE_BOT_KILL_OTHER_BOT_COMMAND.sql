/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOT_KILL_OTHER_BOT_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `dominanceFactor` int(11) NOT NULL,
  `targetBotAuxiliaryIdId` int(11) DEFAULT NULL,
  `attackerBaseItemType_id` int(11) DEFAULT NULL,
  `spawnPoint_id` int(11) DEFAULT NULL,
  `botKillOtherBotCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKckvfx8kt348yfb5d8txret8c1` (`attackerBaseItemType_id`),
  KEY `FKsl1rsgx936c34qyovbws0otae` (`spawnPoint_id`),
  KEY `FKbb3b9ari50ovnnpvjv3mevis4` (`botKillOtherBotCommandEntities_id`),
  CONSTRAINT `FKbb3b9ari50ovnnpvjv3mevis4` FOREIGN KEY (`botKillOtherBotCommandEntities_id`) REFERENCES `SCENE` (`id`),
  CONSTRAINT `FKckvfx8kt348yfb5d8txret8c1` FOREIGN KEY (`attackerBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKsl1rsgx936c34qyovbws0otae` FOREIGN KEY (`spawnPoint_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

