/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOX_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fixVerticalNorm` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrq0demtetxtet71d7hfcjp4lb` (`i18nDescription_id`),
  KEY `FKdjertib6r3nx0t006wmbvv5w1` (`i18nName_id`),
  KEY `FK9wtm3xgkwr25ttr1pubebcdc3` (`shape3DId_id`),
  KEY `FKg12jahlu2kis8r2hfswjqykfe` (`thumbnail_id`),
  CONSTRAINT `FK9wtm3xgkwr25ttr1pubebcdc3` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKdjertib6r3nx0t006wmbvv5w1` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKg12jahlu2kis8r2hfswjqykfe` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKrq0demtetxtet71d7hfcjp4lb` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

