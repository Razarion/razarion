/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOT_KILL_HUMAN_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `dominanceFactor` int(11) NOT NULL,
  `attackerBaseItemType_id` int(11) DEFAULT NULL,
  `spawnPoint_id` int(11) DEFAULT NULL,
  `botKillHumanCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqdgvd8bc4gqifn5d46rmf5iy8` (`attackerBaseItemType_id`),
  KEY `FKdyqi8q4nrjqpcu4r9oiahw264` (`spawnPoint_id`),
  KEY `FKptfyx1wxu2fyxkg6nbj0xwec` (`botKillHumanCommandEntities_id`),
  CONSTRAINT `FKdyqi8q4nrjqpcu4r9oiahw264` FOREIGN KEY (`spawnPoint_id`) REFERENCES `PLACE_CONFIG` (`id`),
  CONSTRAINT `FKptfyx1wxu2fyxkg6nbj0xwec` FOREIGN KEY (`botKillHumanCommandEntities_id`) REFERENCES `SCENE` (`id`),
  CONSTRAINT `FKqdgvd8bc4gqifn5d46rmf5iy8` FOREIGN KEY (`attackerBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

