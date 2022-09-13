/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `LEVEL_UNLOCK` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemTypeCount` int(11) NOT NULL,
  `crystalCost` int(11) NOT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `i18nDescription_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `thumbnail_id` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbblsnanqjw6bv8ahra4o3epso` (`baseItemType_id`),
  KEY `FKq7py43ekb6hfgpx9jyhhk9854` (`i18nDescription_id`),
  KEY `FKkduxofk4bacta0r0fbn0sluov` (`i18nName_id`),
  KEY `FK8o89uccfj6dfo3jm0ucb2b2q8` (`thumbnail_id`),
  KEY `FK16dswxni52x0ya7bvjhtif7td` (`level`),
  CONSTRAINT `FK16dswxni52x0ya7bvjhtif7td` FOREIGN KEY (`level`) REFERENCES `LEVEL` (`id`),
  CONSTRAINT `FK8o89uccfj6dfo3jm0ucb2b2q8` FOREIGN KEY (`thumbnail_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKbblsnanqjw6bv8ahra4o3epso` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKkduxofk4bacta0r0fbn0sluov` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`),
  CONSTRAINT `FKq7py43ekb6hfgpx9jyhhk9854` FOREIGN KEY (`i18nDescription_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `LEVEL_UNLOCK`;
/*!40000 ALTER TABLE `LEVEL_UNLOCK` DISABLE KEYS */;
/*!40000 ALTER TABLE `LEVEL_UNLOCK` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
