/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_BOT_REMOVE_OWN_ITEMS_COMMAND` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botAuxiliaryIdId` int(11) DEFAULT NULL,
  `baseItemType2Remove_id` int(11) DEFAULT NULL,
  `botRemoveOwnItemCommandEntities_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd5rbur518hugollv8v21jnj1o` (`baseItemType2Remove_id`),
  KEY `FKhdrkr0eixuks1uoe9t9c949j3` (`botRemoveOwnItemCommandEntities_id`),
  CONSTRAINT `FKd5rbur518hugollv8v21jnj1o` FOREIGN KEY (`baseItemType2Remove_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKhdrkr0eixuks1uoe9t9c949j3` FOREIGN KEY (`botRemoveOwnItemCommandEntities_id`) REFERENCES `SCENE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

