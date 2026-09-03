-- MySQL dump 10.13  Distrib 8.0.30, for Linux (x86_64)
--
-- Host: localhost    Database: pos
-- ------------------------------------------------------
-- Server version	8.0.30-0ubuntu0.20.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `branch_entity`
--

DROP TABLE IF EXISTS `branch_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch_entity` (
  `id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `close_time` time DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `open_time` time DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `workdays` json DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  `user_entity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKossac2c91w2erq12ae0rewvp4` (`user_entity_id`),
  KEY `FK7ubcwmuad0uytisa1iupum4wr` (`store_entity_id`),
  CONSTRAINT `FK7ubcwmuad0uytisa1iupum4wr` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`),
  CONSTRAINT `FKhl42x8vi857c5r0kp1iek52wn` FOREIGN KEY (`user_entity_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branch_entity`
--

LOCK TABLES `branch_entity` WRITE;
/*!40000 ALTER TABLE `branch_entity` DISABLE KEYS */;
INSERT INTO `branch_entity` VALUES (154,'near Sofia Ring mall',NULL,'2026-08-28 14:12:33.058311',NULL,'Renko shopping mall',NULL,'123451241',NULL,NULL,52,NULL);
/*!40000 ALTER TABLE `branch_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `branch_entity_seq`
--

DROP TABLE IF EXISTS `branch_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branch_entity_seq`
--

LOCK TABLES `branch_entity_seq` WRITE;
/*!40000 ALTER TABLE `branch_entity_seq` DISABLE KEYS */;
INSERT INTO `branch_entity_seq` VALUES (351);
/*!40000 ALTER TABLE `branch_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `branch_entity_workdays`
--

DROP TABLE IF EXISTS `branch_entity_workdays`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch_entity_workdays` (
  `branch_entity_id` bigint NOT NULL,
  `workdays` varchar(255) DEFAULT NULL,
  KEY `FKfcjm3o60v2p6fc5t2dcicr2mp` (`branch_entity_id`),
  CONSTRAINT `FKfcjm3o60v2p6fc5t2dcicr2mp` FOREIGN KEY (`branch_entity_id`) REFERENCES `branch_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branch_entity_workdays`
--

LOCK TABLES `branch_entity_workdays` WRITE;
/*!40000 ALTER TABLE `branch_entity_workdays` DISABLE KEYS */;
INSERT INTO `branch_entity_workdays` VALUES (154,'Mon'),(154,'Tue'),(154,'Wed'),(154,'Thu'),(154,'Fri');
/*!40000 ALTER TABLE `branch_entity_workdays` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_entity`
--

DROP TABLE IF EXISTS `category_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_entity` (
  `id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk4fp8eel5eucglui2jh8ndfw9` (`store_entity_id`),
  CONSTRAINT `FKk4fp8eel5eucglui2jh8ndfw9` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category_entity`
--

LOCK TABLES `category_entity` WRITE;
/*!40000 ALTER TABLE `category_entity` DISABLE KEYS */;
INSERT INTO `category_entity` VALUES (1,'pants',52),(2,'shirt',52);
/*!40000 ALTER TABLE `category_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_entity_seq`
--

DROP TABLE IF EXISTS `category_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category_entity_seq`
--

LOCK TABLES `category_entity_seq` WRITE;
/*!40000 ALTER TABLE `category_entity_seq` DISABLE KEYS */;
INSERT INTO `category_entity_seq` VALUES (101);
/*!40000 ALTER TABLE `category_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_entity`
--

DROP TABLE IF EXISTS `customer_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_entity` (
  `id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj7gduirkjghrclg8wqjbikjuv` (`store_entity_id`),
  CONSTRAINT `FKj7gduirkjghrclg8wqjbikjuv` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_entity`
--

LOCK TABLES `customer_entity` WRITE;
/*!40000 ALTER TABLE `customer_entity` DISABLE KEYS */;
INSERT INTO `customer_entity` VALUES (2,'2026-08-31 13:55:37.980284','customer@gmail.com','Customer Customerov','12354567',NULL,NULL),(3,'2026-08-31 13:55:52.171904','pablo@gmail.com','Pablo Pablov','11223344555',NULL,NULL);
/*!40000 ALTER TABLE `customer_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_entity_seq`
--

DROP TABLE IF EXISTS `customer_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_entity_seq`
--

LOCK TABLES `customer_entity_seq` WRITE;
/*!40000 ALTER TABLE `customer_entity_seq` DISABLE KEYS */;
INSERT INTO `customer_entity_seq` VALUES (101);
/*!40000 ALTER TABLE `customer_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_entity`
--

DROP TABLE IF EXISTS `inventory_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_entity` (
  `id` bigint NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `low_stock_threshold` int NOT NULL,
  `quantity` int NOT NULL,
  `product_entity_id` bigint DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_store_product` (`store_entity_id`,`product_entity_id`),
  KEY `FKg5otmjp8wru3r5h8y8xq76sj1` (`product_entity_id`),
  CONSTRAINT `FKdqpob2p4oangbgh8w4t60yf05` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`),
  CONSTRAINT `FKg5otmjp8wru3r5h8y8xq76sj1` FOREIGN KEY (`product_entity_id`) REFERENCES `product_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_entity`
--

LOCK TABLES `inventory_entity` WRITE;
/*!40000 ALTER TABLE `inventory_entity` DISABLE KEYS */;
INSERT INTO `inventory_entity` VALUES (52,'2026-08-31 10:38:19.340443',11,20,1,52),(153,'2026-08-31 11:07:51.969727',10,13,53,52);
/*!40000 ALTER TABLE `inventory_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_entity_seq`
--

DROP TABLE IF EXISTS `inventory_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_entity_seq`
--

LOCK TABLES `inventory_entity_seq` WRITE;
/*!40000 ALTER TABLE `inventory_entity_seq` DISABLE KEYS */;
INSERT INTO `inventory_entity_seq` VALUES (251);
/*!40000 ALTER TABLE `inventory_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_entity`
--

DROP TABLE IF EXISTS `product_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_entity` (
  `id` bigint NOT NULL,
  `brand` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `max_retail_price` double DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `selling_price` double DEFAULT NULL,
  `sku` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `category_entity_id` bigint DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7f0t2uqbqx9j8l5a2bo6m0cnw` (`sku`),
  KEY `FKkhllkj99qwr2gp47i94nqo7tj` (`category_entity_id`),
  KEY `FK5gjlmfaa1smdo51f9tfbqmx8s` (`store_entity_id`),
  CONSTRAINT `FK5gjlmfaa1smdo51f9tfbqmx8s` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`),
  CONSTRAINT `FKkhllkj99qwr2gp47i94nqo7tj` FOREIGN KEY (`category_entity_id`) REFERENCES `category_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_entity`
--

LOCK TABLES `product_entity` WRITE;
/*!40000 ALTER TABLE `product_entity` DISABLE KEYS */;
INSERT INTO `product_entity` VALUES (1,'H&M','2026-08-27 15:03:03.087519','Comfortable blue cotton shirt for men. Ideal for casual and semi-formal occasions.',NULL,NULL,'Men\'s Cotton Casual Shirt',899,'SHRT-M-COTTON-BLU-2025','2026-08-28 10:24:29.785826',NULL,52,52),(53,'Zara','2026-08-31 11:02:53.685013','Comfortable blue cotton shirt for men. Ideal for casual and semi-formal occasions.',NULL,NULL,'Men\'s Cotton Casual Pants',899,'SHRT-M-COTTON-BLU-2024',NULL,NULL,52,52);
/*!40000 ALTER TABLE `product_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_entity_seq`
--

DROP TABLE IF EXISTS `product_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_entity_seq`
--

LOCK TABLES `product_entity_seq` WRITE;
/*!40000 ALTER TABLE `product_entity_seq` DISABLE KEYS */;
INSERT INTO `product_entity_seq` VALUES (151);
/*!40000 ALTER TABLE `product_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_entity`
--

DROP TABLE IF EXISTS `store_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_entity` (
  `id` bigint NOT NULL,
  `brand_name` varchar(255) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `store_type` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_admin_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_store_admin_id` (`store_admin_id`),
  CONSTRAINT `fk_store_admin` FOREIGN KEY (`store_admin_id`) REFERENCES `user` (`id`),
  CONSTRAINT `store_entity_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_entity`
--

LOCK TABLES `store_entity` WRITE;
/*!40000 ALTER TABLE `store_entity` DISABLE KEYS */;
INSERT INTO `store_entity` VALUES (52,'renko shopping',NULL,NULL,NULL,'2026-08-26 11:05:45.534310','A cool store for cool things',1,'ecommerce store','2026-08-26 11:15:05.553593',152);
/*!40000 ALTER TABLE `store_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_entity_seq`
--

DROP TABLE IF EXISTS `store_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_entity_seq`
--

LOCK TABLES `store_entity_seq` WRITE;
/*!40000 ALTER TABLE `store_entity_seq` DISABLE KEYS */;
INSERT INTO `store_entity_seq` VALUES (351);
/*!40000 ALTER TABLE `store_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `role` tinyint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`),
  KEY `FKm1n7cl3jdown9mn7pqlo9g6cd` (`store_entity_id`),
  CONSTRAINT `FKm1n7cl3jdown9mn7pqlo9g6cd` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`),
  CONSTRAINT `user_chk_1` CHECK ((`role` between 0 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,NULL,'testingemail@gmail.com','Full Name Test','2026-08-10 13:08:18.326953','$2a$10$dwekDN4bvHkiHIqmjEInWOo3MzMF4V7tnK9HqYpEMJkLdNnOJkGjS','01234567889',4,'2026-08-10 10:59:07.286578',NULL),(2,NULL,'testingemail2@gmail.com','Full Name Test',NULL,'$2a$10$c4I5Nks6FbM1ONUeI6TFVOHmYdf4k.mdo58J141McVOP4mhzZbho2','01234567889',4,'2026-08-10 13:04:12.122761',NULL),(52,NULL,'pablopablitov@gmail.com','Pablo Pablito',NULL,'$2a$10$vI8aWBnW3fID.ZQ4/ZO1G.q1lR.y8C4N8Hqg8vL51l5B11J6G1mOi',NULL,4,'2026-08-24 14:45:14.434199',NULL),(102,NULL,'fernando@gmail.com','Fernando Sucre','2026-08-25 10:55:21.337956','$2a$10$z3gf9G6qVmPCXzUVlBi13uonxM3NmrWb9O0d8u8u.bdrhr/Aeja5O',NULL,4,'2026-08-24 16:03:06.933080',NULL),(152,NULL,'admin@gmail.com','Admin Admin','2026-08-31 10:33:40.180333','$2a$10$ni0T.7tshYpdT5uPo/.bjO6WWGRwSM42oA.5U6yP13cH0zh6LgSBW',NULL,1,'2026-08-26 10:21:59.083937',NULL),(252,NULL,'employee@gmail.com','Employee Employyov',NULL,'$2a$10$3C0H0HKc2x1BHh98M.Z1Z.L85YE27LLm88zgrb6M5w9wNkDKyTI2m',NULL,4,NULL,52),(253,NULL,'branch@gmail.com','Branch Branchov',NULL,'$2a$10$ZqGaWcQSNOs3E56lFa0.j.nFN1RzsHZM7f91GEvYNLcZWfJyA9KKK',NULL,3,NULL,52);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_entity`
--

DROP TABLE IF EXISTS `user_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_entity` (
  `id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `role` tinyint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_entity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4xad1enskw4j1t2866f7sodrx` (`email`),
  KEY `FK8v728h5lw5heib0lxv8ukhtdx` (`store_entity_id`),
  CONSTRAINT `FK8v728h5lw5heib0lxv8ukhtdx` FOREIGN KEY (`store_entity_id`) REFERENCES `store_entity` (`id`),
  CONSTRAINT `user_entity_chk_1` CHECK ((`role` between 0 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_entity`
--

LOCK TABLES `user_entity` WRITE;
/*!40000 ALTER TABLE `user_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_entity_seq`
--

DROP TABLE IF EXISTS `user_entity_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_entity_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_entity_seq`
--

LOCK TABLES `user_entity_seq` WRITE;
/*!40000 ALTER TABLE `user_entity_seq` DISABLE KEYS */;
INSERT INTO `user_entity_seq` VALUES (1);
/*!40000 ALTER TABLE `user_entity_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_seq`
--

DROP TABLE IF EXISTS `user_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_seq`
--

LOCK TABLES `user_seq` WRITE;
/*!40000 ALTER TABLE `user_seq` DISABLE KEYS */;
INSERT INTO `user_seq` VALUES (351);
/*!40000 ALTER TABLE `user_seq` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 14:06:51
