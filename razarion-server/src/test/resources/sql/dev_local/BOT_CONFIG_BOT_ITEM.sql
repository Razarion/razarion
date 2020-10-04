/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOT_CONFIG_BOT_ITEM` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angle` double NOT NULL,
  `count` int(11) NOT NULL,
  `createDirectly` bit(1) NOT NULL,
  `idleTtl` int(11) DEFAULT NULL,
  `moveRealmIfIdle` bit(1) NOT NULL,
  `noRebuild` bit(1) NOT NULL,
  `noSpawn` bit(1) NOT NULL,
  `rePopTime` int(11) DEFAULT NULL,
  `baseItemTypeEntity_id` int(11) DEFAULT NULL,
  `place_id` int(11) DEFAULT NULL,
  `botEnragementStateConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrmnmfvv7eqabotm7aik3kp0vo` (`baseItemTypeEntity_id`),
  KEY `FKo3anxwa9qjqljjau19r55097r` (`place_id`),
  KEY `FKka3ph3evw0vd8qvpafrc8u9q3` (`botEnragementStateConfig`),
  CONSTRAINT `FKka3ph3evw0vd8qvpafrc8u9q3` FOREIGN KEY (`botEnragementStateConfig`) REFERENCES `BOT_CONFIG_ENRAGEMENT_STATE_CONFIG` (`id`),
  CONSTRAINT `FKo3anxwa9qjqljjau19r55097r` FOREIGN KEY (`place_id`) REFERENCES `PLACE_CONFIG` (`id`),
  CONSTRAINT `FKrmnmfvv7eqabotm7aik3kp0vo` FOREIGN KEY (`baseItemTypeEntity_id`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

