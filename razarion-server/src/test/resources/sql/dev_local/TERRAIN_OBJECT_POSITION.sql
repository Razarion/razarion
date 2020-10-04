/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TERRAIN_OBJECT_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `terrainObjectEntity_id` int(11) NOT NULL,
  `planet` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr6v0ku0sca2p8a2e0oiac4g8f` (`terrainObjectEntity_id`),
  KEY `FKay7f7hjllvmgww9j4e2l1tfqu` (`planet`),
  CONSTRAINT `FKay7f7hjllvmgww9j4e2l1tfqu` FOREIGN KEY (`planet`) REFERENCES `PLANET` (`id`),
  CONSTRAINT `FKr6v0ku0sca2p8a2e0oiac4g8f` FOREIGN KEY (`terrainObjectEntity_id`) REFERENCES `TERRAIN_OBJECT` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

