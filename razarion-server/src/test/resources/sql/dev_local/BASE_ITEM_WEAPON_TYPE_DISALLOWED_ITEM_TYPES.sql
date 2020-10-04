/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES` (
  `weapon` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_amlxea599glsjiee8ukxv7rj9` (`baseItemType`),
  KEY `FKslr6g6v2qqyxtt9dmc09ftrar` (`weapon`),
  CONSTRAINT `FK7jeslr1x745ry5ybx4agf1kte` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKslr6g6v2qqyxtt9dmc09ftrar` FOREIGN KEY (`weapon`) REFERENCES `BASE_ITEM_WEAPON_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

