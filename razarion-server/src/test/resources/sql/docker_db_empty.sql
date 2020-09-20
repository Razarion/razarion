/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 DROP DATABASE IF EXISTS `razarion`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `razarion` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `razarion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUDIO_LIBRARY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` longblob DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_CONSUMER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wattage` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_FACTORY_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `progress` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_GENERATOR_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wattage` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_HOUSE_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_ITEM_CONTAINER_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `itemRange` double NOT NULL,
  `maxCount` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_SPECIAL_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `miniTerrain` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BASE_ITEM_TURRET_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angleVelocity` double NOT NULL,
  `muzzlePositionX` double DEFAULT NULL,
  `muzzlePositionY` double DEFAULT NULL,
  `muzzlePositionZ` double DEFAULT NULL,
  `shape3dMaterialId` varchar(255) DEFAULT NULL,
  `torrentCenterX` double DEFAULT NULL,
  `torrentCenterY` double DEFAULT NULL,
  `torrentCenterZ` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BOT_SCENE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COLLADA` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `colladaString` longtext DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COLLADA_ALPHA_TO_COVERAGE` (
  `ColladaEntity_id` int(11) NOT NULL,
  `alphaToCoverages` double DEFAULT NULL,
  `alphaToCoverages_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`alphaToCoverages_KEY`),
  CONSTRAINT `FKixsen372t78ih6v56jf9l2qih` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COLLADA_ANIMATIONS` (
  `ColladaEntity_id` int(11) NOT NULL,
  `animations` varchar(255) DEFAULT NULL,
  `animations_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`animations_KEY`),
  CONSTRAINT `FKtk7g7rbkh3n2wrl77bafo4bhd` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COLLADA_CHARACTER_REPRESENTING` (
  `ColladaEntity_id` int(11) NOT NULL,
  `characterRepresentings` bit(1) DEFAULT NULL,
  `characterRepresentings_KEY` varchar(180) NOT NULL,
  PRIMARY KEY (`ColladaEntity_id`,`characterRepresentings_KEY`),
  CONSTRAINT `FK3nx9shdy3yrdpk6w8kmhdtfot` FOREIGN KEY (`ColladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEBUG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `debugMessage` longtext DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `system` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FB_MARKETING_CLICK_TRACKER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adId` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FB_MARKETING_CURRENT_AD` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adId` bigint(20) NOT NULL,
  `adSetId` bigint(20) NOT NULL,
  `body` varchar(255) DEFAULT NULL,
  `campaignId` bigint(20) NOT NULL,
  `custom` varchar(255) DEFAULT NULL,
  `dailyBudget` double DEFAULT NULL,
  `dateStart` datetime(3) DEFAULT NULL,
  `dateStop` datetime(3) DEFAULT NULL,
  `facebookPositions` varchar(255) DEFAULT NULL,
  `imageHash` varchar(255) DEFAULT NULL,
  `lifeTime` bit(1) NOT NULL,
  `lifeTimeBudget` double DEFAULT NULL,
  `scheduleTimeEnd` datetime(3) DEFAULT NULL,
  `scheduleTimeStart` datetime(3) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `urlTagParam` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FB_MARKETING_CURRENT_AD_INTEREST` (
  `currentAdEntityId` int(11) NOT NULL,
  `audienceSize` bigint(20) DEFAULT NULL,
  `fbId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  KEY `FKnjtju67voai2g6udj66ueo9v0` (`currentAdEntityId`),
  CONSTRAINT `FKnjtju67voai2g6udj66ueo9v0` FOREIGN KEY (`currentAdEntityId`) REFERENCES `FB_MARKETING_CURRENT_AD` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FB_MARKETING_HISTORY_AD` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adId` bigint(20) NOT NULL,
  `adSetId` bigint(20) NOT NULL,
  `body` varchar(255) DEFAULT NULL,
  `campaignId` bigint(20) NOT NULL,
  `clicks` int(11) NOT NULL,
  `custom` varchar(255) DEFAULT NULL,
  `dailyBudget` double DEFAULT NULL,
  `dateStart` datetime(3) DEFAULT NULL,
  `dateStop` datetime(3) DEFAULT NULL,
  `facebookPositions` varchar(255) DEFAULT NULL,
  `imageHash` varchar(255) DEFAULT NULL,
  `impressions` int(11) NOT NULL,
  `lifeTime` bit(1) NOT NULL,
  `lifeTimeBudget` double DEFAULT NULL,
  `scheduleTimeEnd` datetime(3) DEFAULT NULL,
  `scheduleTimeStart` datetime(3) DEFAULT NULL,
  `spent` double NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `urlTagParam` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FB_MARKETING_HISTORY_AD_INTEREST` (
  `historyAdEntityId` int(11) NOT NULL,
  `audienceSize` bigint(20) DEFAULT NULL,
  `fbId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  KEY `FKad1jynmyqvb71p3w5ftjsbaxa` (`historyAdEntityId`),
  CONSTRAINT `FKad1jynmyqvb71p3w5ftjsbaxa` FOREIGN KEY (`historyAdEntityId`) REFERENCES `FB_MARKETING_HISTORY_AD` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_BOT_SCENE_INDICATOR` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `botSceneId` int(11) DEFAULT NULL,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `newBotSceneConflictConfigId` int(11) DEFAULT NULL,
  `oldBotSceneConflictConfigId` int(11) DEFAULT NULL,
  `raise` bit(1) NOT NULL,
  `step` int(11) DEFAULT NULL,
  `stepCount` int(11) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_FORGOT_PASSWORDY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `forgotPasswordEntityId` int(11) NOT NULL,
  `humanPlayerId` int(11) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_INVENTORY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystals` int(11) DEFAULT NULL,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `inventoryItemId` int(11) DEFAULT NULL,
  `inventoryItemName` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_LEVEL` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `levelId` int(11) NOT NULL,
  `levelNumber` int(11) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_QUEST` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `questId` int(11) NOT NULL,
  `questInternalName` varchar(255) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_UNLOCKED` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `crystals` int(11) DEFAULT NULL,
  `humanPlayerIdEntityId` int(11) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `unlockEntityId` int(11) DEFAULT NULL,
  `unlockEntityName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HISTORY_USER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `loggedIn` datetime(3) DEFAULT NULL,
  `loggedOut` datetime(3) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HUMAN_PLAYER_ENTITY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `I18N_BUNDLE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `I18N_BUNDLE_STRING` (
  `bundle` int(11) NOT NULL,
  `i18nString` varchar(10000) DEFAULT NULL,
  `locale` varchar(180) NOT NULL,
  PRIMARY KEY (`bundle`,`locale`),
  CONSTRAINT `FKpveq2u612sld12e8xax55qlgm` FOREIGN KEY (`bundle`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IMAGE_LIBRARY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` longblob DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LEVEL` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `number` int(11) NOT NULL,
  `xp2LevelUp` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLACE_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `radius` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLACE_CONFIG_POSITION_POLYGON` (
  `OWNER_ID` int(11) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `orderColumn` int(11) NOT NULL,
  PRIMARY KEY (`OWNER_ID`,`orderColumn`),
  CONSTRAINT `FKmmbtyhe3hwrjo2djh1jnlfotc` FOREIGN KEY (`OWNER_ID`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PROPERTY` (
  `propertyKey` varchar(190) NOT NULL,
  `colorAValue` double DEFAULT NULL,
  `colorBValue` double DEFAULT NULL,
  `colorGValue` double DEFAULT NULL,
  `colorRValue` double DEFAULT NULL,
  `doubleValue` double DEFAULT NULL,
  `intValue` int(11) DEFAULT NULL,
  `audio_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`propertyKey`),
  KEY `FKgv62h5ai81d9fld7h96s4yw5k` (`audio_id`),
  KEY `FK3v2abxg87erc7jc7gm22ap9ui` (`image_id`),
  KEY `FKa5a5hlgo2c1ycdku94ya9vo79` (`shape3DId_id`),
  CONSTRAINT `FK3v2abxg87erc7jc7gm22ap9ui` FOREIGN KEY (`image_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKa5a5hlgo2c1ycdku94ya9vo79` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKgv62h5ai81d9fld7h96s4yw5k` FOREIGN KEY (`audio_id`) REFERENCES `AUDIO_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUEST_COMPARISON` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) DEFAULT NULL,
  `time` int(11) DEFAULT NULL,
  `placeConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlgftwoj397x1478q83caeykv4` (`placeConfig_id`),
  CONSTRAINT `FKlgftwoj397x1478q83caeykv4` FOREIGN KEY (`placeConfig_id`) REFERENCES `PLACE_CONFIG` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUEST_CONDITION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `conditionTrigger` varchar(255) DEFAULT NULL,
  `comparisonConfig_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe3ke0d5alxod0vb59hbol2t5t` (`comparisonConfig_id`),
  CONSTRAINT `FKe3ke0d5alxod0vb59hbol2t5t` FOREIGN KEY (`comparisonConfig_id`) REFERENCES `QUEST_COMPARISON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` int(11) NOT NULL,
  `fixVerticalNorm` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKna2llalftwo2brjaevmocun4a` (`i18nDescription_id`),
  KEY `FK92mldtoam62qyvjd90f8rw7op` (`i18nName_id`),
  KEY `FKsxtu46oir6k4rqq8em4jcxc7j` (`shape3DId_id`),
  KEY `FKpaxta3ofhuj6op1ris60fnssi` (`thumbnail_id`),
  CONSTRAINT `FK92mldtoam62qyvjd90f8rw7op` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKna2llalftwo2brjaevmocun4a` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKpaxta3ofhuj6op1ris60fnssi` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKsxtu46oir6k4rqq8em4jcxc7j` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCENE_START_POINT_PLACER` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `enemyFreeRadius` double DEFAULT NULL,
  `suggestedPositionX` double DEFAULT NULL,
  `suggestedPositionY` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SLOPE_DRIVEWAY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `angle` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TERRAIN_OBJECT` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `colladaEntity_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd2dgp25xa48e7tpah4gc4jk00` (`colladaEntity_id`),
  CONSTRAINT `FKd2dgp25xa48e7tpah4gc4jk00` FOREIGN KEY (`colladaEntity_id`) REFERENCES `COLLADA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_CONNECTION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `humanPlayerId` int(11) NOT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_FRONTEND_NAVIGATION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  `url` varchar(190) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_GAME_UI_CONTROL` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientStartTime` datetime(3) DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXe1u611bhlj3y6j7lycxdvqp0p` (`sessionId`),
  KEY `IDXm23hwcquflqt4qknm1kdyi6g4` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_PAGE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `page` varchar(255) DEFAULT NULL,
  `params` longtext DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `uri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX8r3ssjnqi1ayq740sslge33bl` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_PERFMON` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientTimeStamp` datetime(3) DEFAULT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `perfmonEnum` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXjerhb27210brxagvh1igum3oa` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_PERFMON_ENTRY` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `avgDuration` double NOT NULL,
  `date` datetime(3) DEFAULT NULL,
  `frequency` double NOT NULL,
  `samples` int(11) NOT NULL,
  `perfmonStatisticEntity` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7j97gwblgwphwk5xxlo6atcvm` (`perfmonStatisticEntity`),
  CONSTRAINT `FK7j97gwblgwphwk5xxlo6atcvm` FOREIGN KEY (`perfmonStatisticEntity`) REFERENCES `TRACKER_PERFMON` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_SCENE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientStartTime` datetime(3) DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `timeStamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX9o1helnck9k2whan5epnp63mp` (`sessionId`),
  KEY `IDX2l3oymcscbqbf34mgmmqqito5` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_SESSION` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acceptLanguage` longtext DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `razarionCookie` varchar(190) DEFAULT NULL,
  `referer` longtext DEFAULT NULL,
  `remoteAddr` varchar(255) DEFAULT NULL,
  `remoteHost` varchar(255) DEFAULT NULL,
  `sessionId` varchar(190) NOT NULL,
  `timeStamp` datetime(3) NOT NULL,
  `userAgent` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXtmu9rd272j5lofhd2c8rh6fxh` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_STARTUP_TASK` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientStartTime` datetime(3) DEFAULT NULL,
  `duration` int(11) NOT NULL,
  `error` longtext DEFAULT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `startTime` datetime(3) NOT NULL,
  `taskEnum` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXmnp841oq89d322wfkc062dlf5` (`sessionId`),
  KEY `IDXd87lfgp8dxx91d6d0mvp4xri8` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_STARTUP_TERMINATED` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `successful` bit(1) NOT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  `totalTime` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXe0iw0eq6smtw9ttdsctibxdrk` (`sessionId`),
  KEY `IDXp06cndke1flthaiq26ruywo52` (`gameSessionUuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_TERRAIN_TILE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientTimeStamp` datetime(3) DEFAULT NULL,
  `gameSessionUuid` varchar(190) DEFAULT NULL,
  `generationTime` int(11) NOT NULL,
  `sessionId` varchar(190) NOT NULL,
  `terrainTileX` int(11) DEFAULT NULL,
  `terrainTileY` int(11) DEFAULT NULL,
  `timeStamp` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX2p1x3lnepf6trug4a2fo5f4cb` (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TRACKER_WINDOW_CLOSED` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clientTime` varchar(190) DEFAULT NULL,
  `eventString` varchar(190) DEFAULT NULL,
  `serverTime` datetime(3) DEFAULT NULL,
  `sessionId` varchar(190) DEFAULT NULL,
  `url` varchar(190) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WATER_CONFIG` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bumpDistortionScale` double NOT NULL,
  `bumpMapDepth` double NOT NULL,
  `distortionDurationSeconds` double NOT NULL,
  `distortionStrength` double NOT NULL,
  `fresnelDelta` double NOT NULL,
  `fresnelOffset` double NOT NULL,
  `groundLevel` double NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `reflectionScale` double NOT NULL,
  `shininess` double NOT NULL,
  `specularStrength` double NOT NULL,
  `transparency` double NOT NULL,
  `waterLevel` double NOT NULL,
  `bumpMap_id` int(11) DEFAULT NULL,
  `distortion_id` int(11) DEFAULT NULL,
  `reflection_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk8u5re1hk96pkj5mbohe9fue7` (`bumpMap_id`),
  KEY `FKky3w6oh8qfvnu81s0ghcmlg8a` (`distortion_id`),
  KEY `FKjxbl886qx1n0nyxth0uto4g0j` (`reflection_id`),
  CONSTRAINT `FKjxbl886qx1n0nyxth0uto4g0j` FOREIGN KEY (`reflection_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKk8u5re1hk96pkj5mbohe9fue7` FOREIGN KEY (`bumpMap_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKky3w6oh8qfvnu81s0ghcmlg8a` FOREIGN KEY (`distortion_id`) REFERENCES `IMAGE_LIBRARY` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

