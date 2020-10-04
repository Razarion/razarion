/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_INVENTORY` (
  `user` int(11) NOT NULL,
  `inventory` int(11) NOT NULL,
  KEY `FK5aj7t60abpgnng973addr49em` (`inventory`),
  KEY `FKim3foqucksxlw5trj7rooqk4` (`user`),
  CONSTRAINT `FK5aj7t60abpgnng973addr49em` FOREIGN KEY (`inventory`) REFERENCES `INVENTORY_ITEM` (`id`),
  CONSTRAINT `FKim3foqucksxlw5trj7rooqk4` FOREIGN KEY (`user`) REFERENCES `USER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

