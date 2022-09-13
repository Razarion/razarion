/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `BOX_ITEM_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fixVerticalNorm` bit(1) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `radius` double NOT NULL,
  `terrainType` varchar(255) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `shape3DId_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrq0demtetxtet71d7hfcjp4lb` (`i18nDescription_id`),
  KEY `FKdjertib6r3nx0t006wmbvv5w1` (`i18nName_id`),
  KEY `FK9wtm3xgkwr25ttr1pubebcdc3` (`shape3DId_id`),
  KEY `FKg12jahlu2kis8r2hfswjqykfe` (`thumbnail_id`),
  CONSTRAINT `FK9wtm3xgkwr25ttr1pubebcdc3` FOREIGN KEY (`shape3DId_id`) REFERENCES `COLLADA` (`id`),
  CONSTRAINT `FKdjertib6r3nx0t006wmbvv5w1` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKg12jahlu2kis8r2hfswjqykfe` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKrq0demtetxtet71d7hfcjp4lb` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

DELETE FROM `BOX_ITEM_TYPE`;
/*!40000 ALTER TABLE `BOX_ITEM_TYPE` DISABLE KEYS */;
INSERT INTO `BOX_ITEM_TYPE` (`id`, `fixVerticalNorm`, `internalName`, `radius`, `terrainType`, `ttl`, `i18nDescription_id`, `i18nName_id`, `shape3DId_id`, `thumbnail_id`) VALUES
	(1, b'0', 'Tutorial box', 2, 'LAND', NULL, 52, 51, 7, 79),
	(2, b'0', 'Noob Land 1 Crystal', 21, 'LAND', 300, 95, 96, 7, 79),
	(3, b'0', 'Noob Land 2 Vipers', 2, 'LAND', 240, 98, 99, 7, 79);
/*!40000 ALTER TABLE `BOX_ITEM_TYPE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
