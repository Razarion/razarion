/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLANET` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ambientA` double DEFAULT NULL,
  `ambientB` double DEFAULT NULL,
  `ambientG` double DEFAULT NULL,
  `ambientR` double DEFAULT NULL,
  `diffuseA` double DEFAULT NULL,
  `diffuseB` double DEFAULT NULL,
  `diffuseG` double DEFAULT NULL,
  `diffuseR` double DEFAULT NULL,
  `houseSpace` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `lightDirectionX` double DEFAULT NULL,
  `lightDirectionY` double DEFAULT NULL,
  `lightDirectionZ` double DEFAULT NULL,
  `miniMapImage` longblob DEFAULT NULL,
  `shadowAlpha` double NOT NULL,
  `width` double DEFAULT NULL,
  `height` double DEFAULT NULL,
  `startRazarion` int(11) NOT NULL,
  `groundConfig_id` int(11) DEFAULT NULL,
  `startBaseItemType_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqvdx3rd9pwbcpl3k3my1pdefw` (`groundConfig_id`),
  KEY `FKpal29tk0xwgxthg5jpc08tcbr` (`startBaseItemType_id`),
  CONSTRAINT `FKpal29tk0xwgxthg5jpc08tcbr` FOREIGN KEY (`startBaseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKqvdx3rd9pwbcpl3k3my1pdefw` FOREIGN KEY (`groundConfig_id`) REFERENCES `GROUND_CONFIG` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

