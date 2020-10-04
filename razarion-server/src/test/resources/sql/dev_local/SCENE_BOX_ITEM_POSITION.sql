/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOX_ITEM_POSITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `rotationZ` double NOT NULL,
  `boxItemType_id` int(11) DEFAULT NULL,
  `sceneId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1o5uesm507f5p1bu2km3ks769` (`boxItemType_id`),
  KEY `FK3yk9nif0gqotjwm5fmg4ep1ko` (`sceneId`),
  CONSTRAINT `FK1o5uesm507f5p1bu2km3ks769` FOREIGN KEY (`boxItemType_id`) REFERENCES `BOX_ITEM_TYPE` (`id`),
  CONSTRAINT `FK3yk9nif0gqotjwm5fmg4ep1ko` FOREIGN KEY (`sceneId`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

