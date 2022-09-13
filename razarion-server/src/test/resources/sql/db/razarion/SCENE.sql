/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `SCENE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `duration` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `removeLoadingCover` bit(1) DEFAULT NULL,
  `scrollUiQuestCrystal` int(11) DEFAULT NULL,
  `scrollUiQuestI18nHidePassedDialog` bit(1) DEFAULT NULL,
  `scrollUiQuestRazarion` int(11) DEFAULT NULL,
  `scrollUiQuestTargetRectangleEndX` double DEFAULT NULL,
  `scrollUiQuestTargetRectangleEndY` double DEFAULT NULL,
  `scrollUiQuestTargetRectangleStartX` double DEFAULT NULL,
  `scrollUiQuestTargetRectangleStartY` double DEFAULT NULL,
  `scrollUiQuestXp` int(11) DEFAULT NULL,
  `suppressSell` bit(1) DEFAULT NULL,
  `viewFieldBottomWidth` double DEFAULT NULL,
  `viewFieldCameraLocked` bit(1) DEFAULT NULL,
  `viewFieldFromPositionX` double DEFAULT NULL,
  `viewFieldFromPositionY` double DEFAULT NULL,
  `viewFieldSpeed` double DEFAULT NULL,
  `viewFieldToPositionX` double DEFAULT NULL,
  `viewFieldToPositionY` double DEFAULT NULL,
  `wait4LevelUpDialog` bit(1) DEFAULT NULL,
  `wait4QuestPassedDialog` bit(1) DEFAULT NULL,
  `waitForBaseLostDialog` bit(1) DEFAULT NULL,
  `gameTipConfigEntity_id` int(11) DEFAULT NULL,
  `i18nIntroText_id` int(11) DEFAULT NULL,
  `questConfig_id` int(11) DEFAULT NULL,
  `scrollUiQuestI18nDescription_id` int(11) DEFAULT NULL,
  `scrollUiQuestI18nPassedMessage_id` int(11) DEFAULT NULL,
  `scrollUiQuestI18nTitle_id` int(11) DEFAULT NULL,
  `startPointPlacerEntity_id` int(11) DEFAULT NULL,
  `gameUiContextEntityId` int(11) NOT NULL,
  `orderColumn` int(11) DEFAULT NULL,
  `waitForBaseCreated` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3qbn108jatajcx7n3nh1wujwy` (`gameTipConfigEntity_id`),
  KEY `FKmgc4mkbvugjb4pqdaqqhmsjx9` (`i18nIntroText_id`),
  KEY `FKpyv03l0m8msuuohy94f40416s` (`questConfig_id`),
  KEY `FKgiqefa1ka4nmnf94bv0wvaw1e` (`scrollUiQuestI18nDescription_id`),
  KEY `FK5mvo838o44jb4cgcjbl1darg` (`scrollUiQuestI18nPassedMessage_id`),
  KEY `FKqpw9fls2x66i7v80ch6xiv7qq` (`scrollUiQuestI18nTitle_id`),
  KEY `FKt94pq452h8y4a7b8g26y8rfk8` (`startPointPlacerEntity_id`),
  KEY `FK76m0dalyevvu5nk2adwr7unmf` (`gameUiContextEntityId`),
  CONSTRAINT `FK3qbn108jatajcx7n3nh1wujwy` FOREIGN KEY (`gameTipConfigEntity_id`) REFERENCES `SCENE_TIP_CONFIG` (`id`),
  CONSTRAINT `FK5mvo838o44jb4cgcjbl1darg` FOREIGN KEY (`scrollUiQuestI18nPassedMessage_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FK76m0dalyevvu5nk2adwr7unmf` FOREIGN KEY (`gameUiContextEntityId`) REFERENCES `GAME_UI_CONTEXT` (`id`),
  CONSTRAINT `FKgiqefa1ka4nmnf94bv0wvaw1e` FOREIGN KEY (`scrollUiQuestI18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKmgc4mkbvugjb4pqdaqqhmsjx9` FOREIGN KEY (`i18nIntroText_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKpyv03l0m8msuuohy94f40416s` FOREIGN KEY (`questConfig_id`) REFERENCES `QUEST` (`id`),
  CONSTRAINT `FKqpw9fls2x66i7v80ch6xiv7qq` FOREIGN KEY (`scrollUiQuestI18nTitle_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKt94pq452h8y4a7b8g26y8rfk8` FOREIGN KEY (`startPointPlacerEntity_id`) REFERENCES `SCENE_START_POINT_PLACER` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=184 DEFAULT CHARSET=utf8mb4;

DELETE FROM `SCENE`;
/*!40000 ALTER TABLE `SCENE` DISABLE KEYS */;
INSERT INTO `SCENE` (`id`, `duration`, `internalName`, `removeLoadingCover`, `scrollUiQuestCrystal`, `scrollUiQuestI18nHidePassedDialog`, `scrollUiQuestRazarion`, `scrollUiQuestTargetRectangleEndX`, `scrollUiQuestTargetRectangleEndY`, `scrollUiQuestTargetRectangleStartX`, `scrollUiQuestTargetRectangleStartY`, `scrollUiQuestXp`, `suppressSell`, `viewFieldBottomWidth`, `viewFieldCameraLocked`, `viewFieldFromPositionX`, `viewFieldFromPositionY`, `viewFieldSpeed`, `viewFieldToPositionX`, `viewFieldToPositionY`, `wait4LevelUpDialog`, `wait4QuestPassedDialog`, `waitForBaseLostDialog`, `gameTipConfigEntity_id`, `i18nIntroText_id`, `questConfig_id`, `scrollUiQuestI18nDescription_id`, `scrollUiQuestI18nPassedMessage_id`, `scrollUiQuestI18nTitle_id`, `startPointPlacerEntity_id`, `gameUiContextEntityId`, `orderColumn`, `waitForBaseCreated`) VALUES
	(183, NULL, NULL, b'1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, b'0', NULL, NULL, NULL, 220, 360, NULL, b'1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 91, 0, NULL);
/*!40000 ALTER TABLE `SCENE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
