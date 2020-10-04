/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` int(11) NOT NULL,
  `fixVerticalNorm` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKna2llalftwo2brjaevmocun4a` (`i18nDescription_id`),
  KEY `FK92mldtoam62qyvjd90f8rw7op` (`i18nName_id`),
  KEY `FKsxtu46oir6k4rqq8em4jcxc7j` (`shape3DId_id`),
  KEY `FKpaxta3ofhuj6op1ris60fnssi` (`thumbnail_id`),
  CONSTRAINT `FK92mldtoam62qyvjd90f8rw7op` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKna2llalftwo2brjaevmocun4a` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKpaxta3ofhuj6op1ris60fnssi` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKsxtu46oir6k4rqq8em4jcxc7j` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

