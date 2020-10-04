/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOT_ATTACK_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `actorItemType_id` int(11) DEFAULT NULL,
  `targetItemType_id` int(11) DEFAULT NULL,
  `targetSelection_id` int(11) DEFAULT NULL,
  `botAttackCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK41qgg36def0x3j8xjg61tl9ly` (`actorItemType_id`),
  KEY `FK9q1onf1n6vq18ep096s80u0vm` (`targetItemType_id`),
  KEY `FK6lfeos35sq8euepu1nqeoijqq` (`targetSelection_id`),
  KEY `FKrgg9j7lq38k61hkwuy73wxght` (`botAttackCommandEntities_id`),
  CONSTRAINT `FK41qgg36def0x3j8xjg61tl9ly` FOREIGN KEY (`actorItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FK6lfeos35sq8euepu1nqeoijqq` FOREIGN KEY (`targetSelection_id`) REFERENCES `PLACE_CONFIG` (`id`),
  CONSTRAINT `FK9q1onf1n6vq18ep096s80u0vm` FOREIGN KEY (`targetItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKrgg9j7lq38k61hkwuy73wxght` FOREIGN KEY (`botAttackCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

