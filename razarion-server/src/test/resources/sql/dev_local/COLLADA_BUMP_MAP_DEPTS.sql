/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COLLADA_BUMP_MAP_DEPTS` (
  `ColladaEntity_id` int(11) NOT NULL,
  `bumpMapDepts` double DEFAULT NULL,
  `bumpMapDepts_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`bumpMapDepts_KEY`),
  CONSTRAINT `FKftq510987a3aad2wjcb76f34w` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

