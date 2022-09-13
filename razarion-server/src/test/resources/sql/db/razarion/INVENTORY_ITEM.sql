/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `INVENTORY_ITEM` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baseItemTypeCount` int(11) NOT NULL,
  `crystalCost` int(11) DEFAULT NULL,
  `internalName` varchar(255) DEFAULT NULL,
  `itemFreeRange` double NOT NULL,
  `razarion` int(11) DEFAULT NULL,
  `baseItemType_id` int(11) DEFAULT NULL,
  `i18nName_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq1ayyaai4mk3dd8gsdcfpp1sk` (`baseItemType_id`),
  KEY `FKr30cctgb52isr7jyu1u6wsiif` (`i18nName_id`),
  KEY `FK70rj6a4eg5uq9s0r0i8rg10yr` (`image_id`),
  CONSTRAINT `FK70rj6a4eg5uq9s0r0i8rg10yr` FOREIGN KEY (`image_id`) REFERENCES `IMAGE_LIBRARY` (`id`),
  CONSTRAINT `FKq1ayyaai4mk3dd8gsdcfpp1sk` FOREIGN KEY (`baseItemType_id`) REFERENCES `BASE_ITEM_TYPE` (`id`),
  CONSTRAINT `FKr30cctgb52isr7jyu1u6wsiif` FOREIGN KEY (`i18nName_id`) REFERENCES `I18N_BUNDLE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

DELETE FROM `INVENTORY_ITEM`;
/*!40000 ALTER TABLE `INVENTORY_ITEM` DISABLE KEYS */;
INSERT INTO `INVENTORY_ITEM` (`id`, `baseItemTypeCount`, `crystalCost`, `internalName`, `itemFreeRange`, `razarion`, `baseItemType_id`, `i18nName_id`, `image_id`) VALUES
	(1, 3, NULL, 'Tutorial 3 Vipers', 2, NULL, 3, 56, 36),
	(2, 2, NULL, 'Noob 2 Vipers', 2, NULL, 3, 97, NULL);
/*!40000 ALTER TABLE `INVENTORY_ITEM` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
