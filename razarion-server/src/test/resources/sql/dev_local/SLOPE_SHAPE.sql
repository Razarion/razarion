/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SLOPE_SHAPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `slopeFactor` double NOT NULL,
  `shape_id` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKla78vv4yhnh8nh0hn60yj5ite` (`shape_id`),
  CONSTRAINT `FKla78vv4yhnh8nh0hn60yj5ite` FOREIGN KEY (`shape_id`) REFERENCES `SLOPE_CONFIG` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=322 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

