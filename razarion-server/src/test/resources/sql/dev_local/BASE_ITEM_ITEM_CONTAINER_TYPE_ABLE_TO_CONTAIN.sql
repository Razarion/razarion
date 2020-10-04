/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN` (
  `container` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_bdxnra184ggd8if0dqio7px22` (`baseItemType`),
  KEY `FK2u6acl8c6po7hq1ejtifgrlm1` (`container`),
  CONSTRAINT `FK2u6acl8c6po7hq1ejtifgrlm1` FOREIGN KEY (`container`) REFERENCES `BASE_ITEM_ITEM_CONTAINER_TYPE` (`id`),
  CONSTRAINT `FKgmiiow8xi819knlixvlf29we5` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

