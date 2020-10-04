/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUEST_COMPARISON_BOT` (
  `comparisonConfig` int(11) NOT NULL,
  `botConfig` int(11) NOT NULL,
  KEY `FKgsivij4a6t882dkggs8ubbgay` (`botConfig`),
  KEY `FKk49fh7nqm3yx2jbu9088qh4pn` (`comparisonConfig`),
  CONSTRAINT `FKgsivij4a6t882dkggs8ubbgay` FOREIGN KEY (`botConfig`) REFERENCES `BOT_CONFIG` (`id`),
  CONSTRAINT `FKk49fh7nqm3yx2jbu9088qh4pn` FOREIGN KEY (`comparisonConfig`) REFERENCES `QUEST_COMPARISON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

