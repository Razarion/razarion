/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOT_HARVEST_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `harvesterItemType_id` int(11) DEFAULT NULL,
  `resourceItemType_id` int(11) DEFAULT NULL,
  `resourceSelection_id` int(11) DEFAULT NULL,
  `botHarvestCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK52rh3hg6sc1g6xuhxqd6l0l0w` (`harvesterItemType_id`),
  KEY `FK9sc5qi93p4cby5b2hojcr90gh` (`resourceItemType_id`),
  KEY `FKs2dv4bd53e7r6vokmm96qh0x4` (`resourceSelection_id`),
  KEY `FK3mpduff9ibnr01pj8lnbnetm1` (`botHarvestCommandEntities_id`),
  CONSTRAINT `FK3mpduff9ibnr01pj8lnbnetm1` FOREIGN KEY (`botHarvestCommandEntities_id`) REFERENCES `SCENE` (`id`),
  CONSTRAINT `FK52rh3hg6sc1g6xuhxqd6l0l0w` FOREIGN KEY (`harvesterItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FK9sc5qi93p4cby5b2hojcr90gh` FOREIGN KEY (`resourceItemType_id`) REFERENCES `RESOURCE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKs2dv4bd53e7r6vokmm96qh0x4` FOREIGN KEY (`resourceSelection_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

