/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `RESOURCE_ITEM_TYPE` (
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

DELETE FROM `RESOURCE_ITEM_TYPE`;
/*!40000 ALTER TABLE `RESOURCE_ITEM_TYPE` DISABLE KEYS */;
INSERT INTO `RESOURCE_ITEM_TYPE` (`id`, `amount`, `fixVerticalNorm`, `internalName`, `radius`, `terrainType`, `i18nDescription_id`, `i18nName_id`, `shape3DId_id`, `thumbnail_id`) VALUES
	(1, 100000, b'1', 'Start razarion', 3, 'LAND', 55, 54, 4, 80),
	(2, 1000, b'1', 'Noob', 3, 'LAND', 225, 224, 4, 80);
/*!40000 ALTER TABLE `RESOURCE_ITEM_TYPE` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
