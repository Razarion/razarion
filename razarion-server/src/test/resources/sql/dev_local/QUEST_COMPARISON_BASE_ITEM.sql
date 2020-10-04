/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUEST_COMPARISON_BASE_ITEM` (
  `ComparisonConfigEntity_id` int(11) NOT NULL,
  `typeCount` int(11) DEFAULT NULL,
  `baseItemTypeEntityId` int(11) NOT NULL,
  PRIMARY KEY (`ComparisonConfigEntity_id`,`baseItemTypeEntityId`),
  KEY `FKox69d9d2u7iegll4y4bua3uhn` (`baseItemTypeEntityId`),
  CONSTRAINT `FKox69d9d2u7iegll4y4bua3uhn` FOREIGN KEY (`baseItemTypeEntityId`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKqewsitlqloonk59cl0vlbyq6o` FOREIGN KEY (`ComparisonConfigEntity_id`) REFERENCES `QUEST_COMPARISON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

