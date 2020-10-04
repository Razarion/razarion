/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD` (
  `factory` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_s56cdvc3hq6y8o7r2gsrfu52c` (`baseItemType`),
  KEY `FKtc6hh437se4nqa2g9tmmg4na5` (`factory`),
  CONSTRAINT `FKlfyjj2uwgsatdw7ux1wds5t7x` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKtc6hh437se4nqa2g9tmmg4na5` FOREIGN KEY (`factory`) REFERENCES `BASE_ITEM_FACTORY_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

