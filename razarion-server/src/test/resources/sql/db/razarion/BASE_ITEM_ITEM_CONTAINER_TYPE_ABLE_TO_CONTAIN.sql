/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN` (
  `container` int(11) NOT NULL,
  `baseItemType` int(11) NOT NULL,
  UNIQUE KEY `UK_bdxnra184ggd8if0dqio7px22` (`baseItemType`),
  KEY `FK2u6acl8c6po7hq1ejtifgrlm1` (`container`),
  CONSTRAINT `FK2u6acl8c6po7hq1ejtifgrlm1` FOREIGN KEY (`container`) REFERENCES `BASE_ITEM_ITEM_CONTAINER_TYPE` (`id`),
  CONSTRAINT `FKgmiiow8xi819knlixvlf29we5` FOREIGN KEY (`baseItemType`) REFERENCES `BASE_ITEM_TYPE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN`;
/*!40000 ALTER TABLE `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN` DISABLE KEYS */;
INSERT INTO `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN` (`container`, `baseItemType`) VALUES
	(1, 1);
/*!40000 ALTER TABLE `BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;