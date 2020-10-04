/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LEVEL_UNLOCK` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemTypeCount` int(11) NOT NULL,
  `crystalCost` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbblsnanqjw6bv8ahra4o3epso` (`baseItemType_id`),
  KEY `FKq7py43ekb6hfgpx9jyhhk9854` (`i18nDescription_id`),
  KEY `FKkduxofk4bacta0r0fbn0sluov` (`i18nName_id`),
  KEY `FK8o89uccfj6dfo3jm0ucb2b2q8` (`thumbnail_id`),
  KEY `FK16dswxni52x0ya7bvjhtif7td` (`level`),
  CONSTRAINT `FK16dswxni52x0ya7bvjhtif7td` FOREIGN KEY (`level`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FK8o89uccfj6dfo3jm0ucb2b2q8` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKbblsnanqjw6bv8ahra4o3epso` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKkduxofk4bacta0r0fbn0sluov` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKq7py43ekb6hfgpx9jyhhk9854` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

