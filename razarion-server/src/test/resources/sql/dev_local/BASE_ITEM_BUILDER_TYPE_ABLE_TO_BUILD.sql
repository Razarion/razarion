/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD` (
  `builder` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_iilyj6hka733l0r6fe21q3imu` (`baseItemType`),
  KEY `FK28k6bigopln9kqkqspxgf45wo` (`builder`),
  CONSTRAINT `FK28k6bigopln9kqkqspxgf45wo` FOREIGN KEY (`builder`) REFERENCES `BASE_ITEM_BUILDER_TYPE` (`id`),
  CONSTRAINT `FKt42sflt3ub3tu41rcqgysx4h5` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

