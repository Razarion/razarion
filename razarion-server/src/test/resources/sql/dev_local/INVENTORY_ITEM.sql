/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `INVENTORY_ITEM` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemTypeCount` int(11) NOT NULL,
  `crystalCost` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `itemFreeRange` double NOT NULL,
  `razarion` int(11) DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq1ayyaai4mk3dd8gsdcfpp1sk` (`baseItemType_id`),
  KEY `FKr30cctgb52isr7jyu1u6wsiif` (`i18nName_id`),
  KEY `FK70rj6a4eg5uq9s0r0i8rg10yr` (`image_id`),
  CONSTRAINT `FK70rj6a4eg5uq9s0r0i8rg10yr` FOREIGN KEY (`image_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKq1ayyaai4mk3dd8gsdcfpp1sk` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKr30cctgb52isr7jyu1u6wsiif` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

