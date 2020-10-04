/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin` bit(1) NOT NULL,
  `crystals` int(11) NOT NULL,
  `email` varchar(190) DEFAULT NULL,
  `facebookUserId` varchar(190) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `passwordHash` varchar(190) DEFAULT NULL,
  `registerDate` datetime(3) DEFAULT NULL,
  `verificationDoneDate` datetime(3) DEFAULT NULL,
  `verificationId` varchar(190) DEFAULT NULL,
  `verificationStartedDate` datetime(3) DEFAULT NULL,
  `verificationTimedOutDate` datetime(3) DEFAULT NULL,
  `xp` int(11) NOT NULL,
  `activeQuest_id` int(11) DEFAULT NULL,
  `level_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_g6y5gdrvhgx350bvnhhok8us2` (`name`),
  KEY `IDXoj5g1ob8tb6gn928mukpbqat1` (`facebookUserId`),
  KEY `FKtr8la4tg31fj84o5q5wepu6ai` (`activeQuest_id`),
  KEY `FKas5w8de0ic1qgeo8edbs89ffy` (`level_id`),
  CONSTRAINT `FKas5w8de0ic1qgeo8edbs89ffy` FOREIGN KEY (`level_id`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FKtr8la4tg31fj84o5q5wepu6ai` FOREIGN KEY (`activeQuest_id`) REFERENCES `QUEST` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=503 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

