
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

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `travel_reimbursement` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `travel_reimbursement`;
DROP TABLE IF EXISTS `fk_reim_allocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_allocation` (
  `id` varchar(32) NOT NULL COMMENT '分摊记录主键ID',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID',
  `company_id` varchar(32) NOT NULL COMMENT '费用归属公司ID',
  `company_no` varchar(50) NOT NULL COMMENT '公司编号',
  `company_name` varchar(200) NOT NULL COMMENT '公司名称',
  `project_id` varchar(32) NOT NULL COMMENT '项目ID',
  `project_no` varchar(50) NOT NULL COMMENT '项目编号',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `allocation_ratio` decimal(8,4) NOT NULL DEFAULT '0.0000' COMMENT '分摊比例，取值0至1',
  `allocation_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '分摊金额',
  `sort_no` int NOT NULL DEFAULT '1' COMMENT '排序号',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_allocation_reim_id` (`reim_id`),
  KEY `idx_allocation_project` (`project_id`),
  CONSTRAINT `fk_allocation_reim_id` FOREIGN KEY (`reim_id`) REFERENCES `fk_reim_main` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用分摊表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_allowance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_allowance` (
  `id` varchar(32) NOT NULL COMMENT '补助信息主键ID',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID',
  `trip_id` varchar(32) NOT NULL COMMENT '关联行程ID',
  `traveler_id` varchar(32) NOT NULL COMMENT '出行人ID',
  `traveler_name` varchar(100) NOT NULL COMMENT '出行人姓名',
  `start_date` date NOT NULL COMMENT '出差开始日期',
  `end_date` date NOT NULL COMMENT '出差结束日期',
  `allowance_days` int NOT NULL DEFAULT '0' COMMENT '补助天数',
  `departure_city_name` varchar(100) NOT NULL COMMENT '出发城市',
  `allowance_city_no` varchar(50) NOT NULL COMMENT '补助城市编码',
  `allowance_city_name` varchar(100) NOT NULL COMMENT '补助城市名称',
  `city_type` varchar(10) NOT NULL COMMENT '城市类型',
  `standard_total_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '标准补助总金额',
  `apply_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '申请金额',
  `allowance_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '实际补助金额',
  `sort_no` int NOT NULL DEFAULT '1' COMMENT '排序号',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_allowance_trip_id` (`trip_id`),
  KEY `idx_allowance_reim_id` (`reim_id`),
  CONSTRAINT `fk_allowance_reim_id` FOREIGN KEY (`reim_id`) REFERENCES `fk_reim_main` (`id`),
  CONSTRAINT `fk_allowance_trip_id` FOREIGN KEY (`trip_id`) REFERENCES `fk_reim_trip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='补助信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_allowance_calendar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_allowance_calendar` (
  `id` varchar(32) NOT NULL COMMENT '日历明细主键ID',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID',
  `allowance_id` varchar(32) NOT NULL COMMENT '补助信息ID',
  `trip_id` varchar(32) NOT NULL COMMENT '行程ID',
  `allowance_date` date NOT NULL COMMENT '补助日期',
  `weekday_no` tinyint NOT NULL COMMENT '星期序号，1至7',
  `meal_selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否选择餐费补助',
  `meal_standard_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '餐费标准金额',
  `meal_actual_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '餐费实际金额',
  `traffic_selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否选择交通补助',
  `traffic_standard_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '交通补助标准金额',
  `traffic_actual_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '交通补助实际金额',
  `communication_selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否选择通讯补助',
  `communication_standard_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '通讯补助标准金额',
  `communication_actual_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '通讯补助实际金额',
  `daily_standard_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '当日标准金额合计',
  `daily_actual_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '当日实际金额合计',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_allowance_date` (`allowance_id`,`allowance_date`),
  KEY `idx_calendar_allowance_id` (`allowance_id`),
  KEY `fk_calendar_reim_id` (`reim_id`),
  KEY `fk_calendar_trip_id` (`trip_id`),
  CONSTRAINT `fk_calendar_allowance_id` FOREIGN KEY (`allowance_id`) REFERENCES `fk_reim_allowance` (`id`),
  CONSTRAINT `fk_calendar_reim_id` FOREIGN KEY (`reim_id`) REFERENCES `fk_reim_main` (`id`),
  CONSTRAINT `fk_calendar_trip_id` FOREIGN KEY (`trip_id`) REFERENCES `fk_reim_trip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='补助日历明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_business_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_business_type` (
  `id` varchar(32) NOT NULL COMMENT '业务类型ID',
  `business_type_no` varchar(50) NOT NULL COMMENT '业务类型编号',
  `business_type_name` varchar(200) NOT NULL COMMENT '业务类型名称',
  `parent_id` varchar(32) NOT NULL DEFAULT 'none' COMMENT '父级业务类型ID',
  `has_children` tinyint NOT NULL DEFAULT '0' COMMENT '是否有下级节点',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_type_no` (`business_type_no`),
  KEY `idx_business_type_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_city`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_city` (
  `city_no` varchar(50) NOT NULL COMMENT '城市编码',
  `city_name` varchar(100) NOT NULL COMMENT '城市名称',
  `city_type` varchar(10) NOT NULL COMMENT '城市类型：1一线，2二线，3三线',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`city_no`),
  KEY `idx_city_type` (`city_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='城市字典表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_company` (
  `id` varchar(32) NOT NULL COMMENT '费用归属公司ID',
  `company_no` varchar(50) NOT NULL COMMENT '公司编号',
  `company_name` varchar(200) NOT NULL COMMENT '公司名称',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_no` (`company_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用归属公司字典表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_department` (
  `id` varchar(32) NOT NULL COMMENT '部门ID',
  `department_no` varchar(50) NOT NULL COMMENT '部门编号',
  `department_name` varchar(200) NOT NULL COMMENT '部门名称',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_no` (`department_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报销部门字典表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_employee` (
  `id` varchar(32) NOT NULL COMMENT '员工ID',
  `employee_no` varchar(50) NOT NULL COMMENT '员工工号',
  `employee_name` varchar(100) NOT NULL COMMENT '员工姓名',
  `department_id` varchar(32) DEFAULT NULL COMMENT '部门ID',
  `department_no` varchar(50) DEFAULT NULL COMMENT '部门编号',
  `department_name` varchar(200) DEFAULT NULL COMMENT '部门名称',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_employee_department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工字典表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_main`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_main` (
  `id` varchar(32) NOT NULL COMMENT '报销单主键ID',
  `reim_no` varchar(50) NOT NULL COMMENT '报销单编号',
  `document_date` date NOT NULL COMMENT '单据日期',
  `reim_title` varchar(500) NOT NULL COMMENT '报销标题',
  `travel_reason` varchar(500) NOT NULL COMMENT '出差事由',
  `reim_user_id` varchar(32) NOT NULL COMMENT '报销人ID',
  `reim_user_no` varchar(50) NOT NULL COMMENT '报销人工号',
  `reim_user_name` varchar(100) NOT NULL COMMENT '报销人姓名',
  `reim_dept_id` varchar(32) NOT NULL COMMENT '报销部门ID',
  `reim_dept_no` varchar(50) NOT NULL COMMENT '报销部门编号',
  `reim_dept_name` varchar(200) NOT NULL COMMENT '报销部门名称',
  `company_id` varchar(32) NOT NULL COMMENT '费用归属公司ID',
  `company_no` varchar(50) NOT NULL COMMENT '费用归属公司编号',
  `company_name` varchar(200) NOT NULL COMMENT '费用归属公司名称',
  `business_type_id` varchar(32) NOT NULL COMMENT '业务类型ID',
  `business_type_no` varchar(50) NOT NULL COMMENT '业务类型编号',
  `business_type_name` varchar(200) NOT NULL COMMENT '业务类型名称',
  `meal_allowance_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '餐费补助金额',
  `traffic_allowance_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '交通补助金额',
  `communication_allowance_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '通讯补助金额',
  `total_allowance_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '补助总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '单据状态：0草稿，1审批中，2已作废',
  `submit_user_id` varchar(32) DEFAULT NULL COMMENT '提交人ID',
  `submit_user_name` varchar(100) DEFAULT NULL COMMENT '提交人姓名',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `void_user_id` varchar(32) DEFAULT NULL COMMENT '作废人ID',
  `void_user_name` varchar(100) DEFAULT NULL COMMENT '作废人姓名',
  `void_time` datetime DEFAULT NULL COMMENT '作废时间',
  `void_reason` varchar(500) DEFAULT NULL COMMENT '作废原因',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `version` int NOT NULL DEFAULT '0' COMMENT '版本号',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_user_id` varchar(32) NOT NULL COMMENT '创建人ID',
  `create_user_name` varchar(100) NOT NULL COMMENT '创建人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(32) DEFAULT NULL COMMENT '修改人ID',
  `update_user_name` varchar(100) DEFAULT NULL COMMENT '修改人姓名',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reim_no` (`reim_no`),
  KEY `idx_reim_user` (`reim_user_id`),
  KEY `idx_reim_status` (`status`),
  KEY `idx_reim_create_time` (`create_time`),
  KEY `idx_reim_creator_deleted` (`create_user_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='差旅报销单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_operation_log` (
  `id` varchar(32) NOT NULL COMMENT '日志主键ID',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID',
  `reim_no` varchar(50) NOT NULL COMMENT '报销单编号',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) NOT NULL COMMENT '操作人姓名',
  `operation_result` varchar(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果',
  `operation_content` varchar(1000) DEFAULT NULL COMMENT '操作内容',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `request_id` varchar(100) DEFAULT NULL COMMENT '请求链路标识',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '客户端IP地址',
  PRIMARY KEY (`id`),
  KEY `idx_log_reim_id` (`reim_id`),
  KEY `idx_log_operation_time` (`operation_time`),
  CONSTRAINT `fk_log_reim_id` FOREIGN KEY (`reim_id`) REFERENCES `fk_reim_main` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报销单操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_project` (
  `id` varchar(32) NOT NULL COMMENT '项目ID',
  `project_no` varchar(50) NOT NULL COMMENT '项目编号',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_no` (`project_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目字典表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fk_reim_trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fk_reim_trip` (
  `id` varchar(32) NOT NULL COMMENT '行程主键ID',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID',
  `traveler_id` varchar(32) NOT NULL COMMENT '出行人ID',
  `traveler_no` varchar(50) NOT NULL COMMENT '出行人工号',
  `traveler_name` varchar(100) NOT NULL COMMENT '出行人姓名',
  `departure_city_no` varchar(50) NOT NULL COMMENT '出发城市编码',
  `departure_city_name` varchar(100) NOT NULL COMMENT '出发城市名称',
  `arrival_city_no` varchar(50) NOT NULL COMMENT '到达城市编码',
  `arrival_city_name` varchar(100) NOT NULL COMMENT '到达城市名称',
  `arrival_city_type` varchar(10) NOT NULL COMMENT '到达城市类型',
  `departure_date` date NOT NULL COMMENT '出发日期',
  `arrival_date` date NOT NULL COMMENT '到达日期',
  `trip_description` varchar(500) NOT NULL COMMENT '行程说明',
  `sort_no` int NOT NULL DEFAULT '1' COMMENT '行程排序号',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_trip_reim_id` (`reim_id`),
  KEY `idx_trip_traveler_date` (`traveler_id`,`departure_date`,`arrival_date`),
  CONSTRAINT `fk_trip_reim_id` FOREIGN KEY (`reim_id`) REFERENCES `fk_reim_main` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='补录行程表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

