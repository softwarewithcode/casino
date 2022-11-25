/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE IF NOT EXISTS `casino` /*!40100 DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci */;
USE `casino`;

CREATE TABLE IF NOT EXISTS `account` (
  `id` uuid NOT NULL,
  `balance` decimal(20,6) unsigned NOT NULL DEFAULT 0.000000,
  `owner_id` uuid NOT NULL,
  `currency` enum('FN_COIN') NOT NULL DEFAULT 'FN_COIN',
  `created` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `status` enum('AVAILABLE','FROZEN','CLOSED') DEFAULT NULL,
  `name` varchar(30) DEFAULT NULL,
  `closed` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_account_user` (`owner_id`) USING BTREE,
  CONSTRAINT `FK_account_user` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT IGNORE INTO `account` (`id`, `balance`, `owner_id`, `currency`, `created`, `status`, `name`, `closed`) VALUES
	('049a3015-7948-4ff0-a724-050c6c139f22', 1000000.000000, 'c3560c91-6274-4584-ad02-8941ac04a8b8', 'FN_COIN', '2022-11-25 10:22:04', 'AVAILABLE', 'bank', NULL);

CREATE TABLE IF NOT EXISTS `game` (
  `id` int(11) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT IGNORE INTO `game` (`id`, `name`) VALUES
	(1, 'blackjack');

CREATE TABLE IF NOT EXISTS `language` (
  `id` int(11) unsigned NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT IGNORE INTO `language` (`id`, `name`) VALUES
	(1, 'english');

CREATE TABLE IF NOT EXISTS `table` (
  `id` uuid NOT NULL,
  `name` varchar(35) NOT NULL,
  `status` enum('GATHERING','OPEN','CLOSED','CLOSING','BREAK') NOT NULL,
  `type` enum('PUBLIC','PRIVATE','PASSWORD','VIP') NOT NULL,
  `created` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `closed` timestamp NULL DEFAULT NULL,
  `min_players` int(11) unsigned NOT NULL DEFAULT 1,
  `max_players` int(11) unsigned NOT NULL DEFAULT 7,
  `min_bet` decimal(20,6) unsigned NOT NULL,
  `max_bet` decimal(20,6) unsigned NOT NULL,
  `language` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FK_table_language` (`language`),
  CONSTRAINT `FK_table_language` FOREIGN KEY (`language`) REFERENCES `language` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;


CREATE TABLE IF NOT EXISTS `table_player` (
  `id` int(11) NOT NULL,
  `table_id` uuid NOT NULL,
  `player_id` uuid NOT NULL,
  `joined` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `left` timestamp NOT NULL DEFAULT current_timestamp(),
  `initial_balance` decimal(20,6) DEFAULT NULL,
  `left_balance` decimal(20,6) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;


CREATE TABLE IF NOT EXISTS `transaction` (
  `id` uuid NOT NULL,
  `type` enum('DAILY_RELOAD','BANK_DEPOSIT','BANK_WITHDRAW') NOT NULL,
  `from_account` uuid NOT NULL,
  `to_account` uuid NOT NULL,
  `amount` decimal(20,6) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `datetime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_transaction_account` (`from_account`) USING BTREE,
  KEY `FK_transaction_account_2` (`to_account`) USING BTREE,
  CONSTRAINT `FK_from_account` FOREIGN KEY (`from_account`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_to_account` FOREIGN KEY (`to_account`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;


CREATE TABLE IF NOT EXISTS `user` (
  `id` uuid NOT NULL,
  `name` varchar(15) NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `salt` varchar(50) NOT NULL,
  `created` datetime NOT NULL DEFAULT current_timestamp(),
  `status` enum('ACTIVE','INACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
  `email` varchar(130) DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `language` int(11) unsigned NOT NULL DEFAULT 1,
  `type` enum('INTERNAL','EXTERNAL_STANDARD','EXTERNAL_GOLD') NOT NULL DEFAULT 'EXTERNAL_STANDARD',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `unique_username` (`name`),
  UNIQUE KEY `unique_email` (`email`),
  KEY `FK_user_language` (`language`),
  CONSTRAINT `FK_user_language` FOREIGN KEY (`language`) REFERENCES `language` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

INSERT IGNORE INTO `user` (`id`, `name`, `password`, `salt`, `created`, `status`, `email`, `last_login`, `language`, `type`) VALUES
	('c3560c91-6274-4584-ad02-8941ac04a8b8', 'bank', 'todo', 'todo', '2022-11-25 10:35:40', 'BLOCKED', NULL, NULL, 1, 'INTERNAL');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
