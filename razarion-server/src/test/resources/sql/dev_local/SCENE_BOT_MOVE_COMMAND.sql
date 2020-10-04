/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOT_MOVE_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `targetPositionX` double DEFAULT NULL,
  `targetPositionY` double DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `botMoveCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhh30m3n5y5yy51iiodrfjgxj5` (`baseItemType_id`),
  KEY `FKo130arf1f906t656xu20lrd3l` (`botMoveCommandEntities_id`),
  CONSTRAINT `FKhh30m3n5y5yy51iiodrfjgxj5` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKo130arf1f906t656xu20lrd3l` FOREIGN KEY (`botMoveCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

