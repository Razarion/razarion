/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PROPERTY` (
  `propertyKey` varchar(190) NOT NULL,
  `colorAValue` double DEFAULT NULL,
  `colorBValue` double DEFAULT NULL,
  `colorGValue` double DEFAULT NULL,
  `colorRValue` double DEFAULT NULL,
  `doubleValue` double DEFAULT NULL,
  `intValue` int(11) DEFAULT NULL,
  `audio_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`propertyKey`),
  KEY `FKgv62h5ai81d9fld7h96s4yw5k` (`audio_id`),
  KEY `FK3v2abxg87erc7jc7gm22ap9ui` (`image_id`),
  KEY `FKa5a5hlgo2c1ycdku94ya9vo79` (`shape3DId_id`),
  CONSTRAINT `FK3v2abxg87erc7jc7gm22ap9ui` FOREIGN KEY (`image_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKa5a5hlgo2c1ycdku94ya9vo79` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKgv62h5ai81d9fld7h96s4yw5k` FOREIGN KEY (`audio_id`) REFERENCES `AUDIO_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

