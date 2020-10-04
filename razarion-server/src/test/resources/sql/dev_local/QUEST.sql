/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUEST` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystal` int(11) NOT NULL,
  `hidePassedDialog` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `razarion` int(11) NOT NULL,
  `xp` int(11) NOT NULL,
  `conditionConfigEntity_id` int(11) DEFAULT NULL,
  `description_id` int(11) DEFAULT NULL,
  `passedMessage_id` int(11) DEFAULT NULL,
  `title_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg8ekfv29livdwri9llb7p620p` (`conditionConfigEntity_id`),
  KEY `FK8lv5qeunaw77hqlwfticg7w55` (`description_id`),
  KEY `FKmllgre0tlj3i2od98hfmfj9ry` (`passedMessage_id`),
  KEY `FKmbk4qf213aih41banidutli49` (`title_id`),
  CONSTRAINT `FK8lv5qeunaw77hqlwfticg7w55` FOREIGN KEY (`description_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKg8ekfv29livdwri9llb7p620p` FOREIGN KEY (`conditionConfigEntity_id`) REFERENCES `QUEST_CONDITION` (`id`),
  CONSTRAINT `FKmbk4qf213aih41banidutli49` FOREIGN KEY (`title_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKmllgre0tlj3i2od98hfmfj9ry` FOREIGN KEY (`passedMessage_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

