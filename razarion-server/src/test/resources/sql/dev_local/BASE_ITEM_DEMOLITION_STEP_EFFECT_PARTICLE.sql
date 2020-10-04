/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_DEMOLITION_STEP_EFFECT_PARTICLE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `particleConfigId_TMP` int(11) DEFAULT NULL,
  `positionX` double DEFAULT NULL,
  `positionY` double DEFAULT NULL,
  `positionZ` double DEFAULT NULL,
  `demolitionStepEffect` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5y84j4v1h2w5xe21gmu4vda0k` (`demolitionStepEffect`),
  CONSTRAINT `FK5y84j4v1h2w5xe21gmu4vda0k` FOREIGN KEY (`demolitionStepEffect`) REFERENCES `BASE_ITEM_DEMOLITION_STEP_EFFECT` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

