/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOT_CONFIG_ENRAGEMENT_STATE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `enrageUpKills` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `botConfig` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8cnda4p93imbirpidwrfoq27n` (`botConfig`),
  CONSTRAINT `FK8cnda4p93imbirpidwrfoq27n` FOREIGN KEY (`botConfig`) REFERENCES `BOT_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

