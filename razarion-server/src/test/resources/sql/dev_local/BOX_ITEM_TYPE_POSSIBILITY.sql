/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOX_ITEM_TYPE_POSSIBILITY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystals` int(11) DEFAULT NULL,
  `possibility` double NOT NULL,
  `inventoryItem_id` int(11) DEFAULT NULL,
  `boxItemTypePossibilities_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo3e4s038xiegu5lp08ts2vbda` (`inventoryItem_id`),
  KEY `FK94mqsdrnx85779a7i1de17db6` (`boxItemTypePossibilities_id`),
  CONSTRAINT `FK94mqsdrnx85779a7i1de17db6` FOREIGN KEY (`boxItemTypePossibilities_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FKo3e4s038xiegu5lp08ts2vbda` FOREIGN KEY (`inventoryItem_id`) REFERENCES `INVENTORY_ITEM` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

