/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SERVER_GAME_ENGINE_BOT_SCENE_CONFIG` (
  `serverGameEngineId` int(11) NOT NULL,
  `botSceneConfigId` int(11) NOT NULL,
  UNIQUE KEY `UK_rusfme732vodwo2j02s27oxhp` (`botSceneConfigId`),
  KEY `FK80dfty4j4too59uh1t4axhmt9` (`serverGameEngineId`),
  CONSTRAINT `FK2bm7mr807eoedkfin3pf4mamb` FOREIGN KEY (`botSceneConfigId`) REFERENCES `BOT_SCENE_CONFIG` (`id`),
  CONSTRAINT `FK80dfty4j4too59uh1t4axhmt9` FOREIGN KEY (`serverGameEngineId`) REFERENCES `SERVER_GAME_ENGINE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

