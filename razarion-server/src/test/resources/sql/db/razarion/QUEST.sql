/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `QUEST` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `QUEST`;
/*!40000 ALTER TABLE `QUEST` DISABLE KEYS */;
/*!40000 ALTER TABLE `QUEST` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
