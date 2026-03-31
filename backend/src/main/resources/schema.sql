-- 酒店预订系统数据库初始化脚本
CREATE DATABASE IF NOT EXISTS hotel_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_booking;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 酒店表
DROP TABLE IF EXISTS `hotel`;
CREATE TABLE `hotel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '酒店ID',
    `name` VARCHAR(100) NOT NULL COMMENT '酒店名称',
    `address` VARCHAR(255) NOT NULL COMMENT '地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `description` TEXT COMMENT '描述',
    `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图',
    `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店表';

-- 房间表
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房间ID',
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `room_type` VARCHAR(50) NOT NULL COMMENT '房型',
    `name` VARCHAR(100) NOT NULL COMMENT '房间名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格/晚',
    `capacity` INT NOT NULL DEFAULT 2 COMMENT '容纳人数',
    `amenities` VARCHAR(500) DEFAULT NULL COMMENT '设施(JSON)',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片(JSON)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-不可用, 1-可用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- 预订表
DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预订ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `room_id` BIGINT NOT NULL COMMENT '房间ID',
    `check_in_date` DATE NOT NULL COMMENT '入住日期',
    `check_out_date` DATE NOT NULL COMMENT '离店日期',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待确认, 1-已确认, 2-已入住, 3-已完成, 4-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订表';

-- 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
    `operation` VARCHAR(50) DEFAULT NULL COMMENT '操作',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '方法',
    `params` TEXT COMMENT '参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `duration` INT DEFAULT NULL COMMENT '耗时(ms)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ===================== 初始化数据 =====================

-- 初始化用户数据 (密码均为: 123456)
-- BCrypt hash for "123456" - generated with cost factor 10
-- Format: $2a$[cost]$[22 char salt][31 char hash]
INSERT INTO `user` (`username`, `password`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13800000001', 'admin@hotel.com', 1, 1),
('manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13800000002', 'manager@hotel.com', 1, 1),
('zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13900000001', 'zhangsan@qq.com', 0, 1),
('lisi', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13900000002', 'lisi@qq.com', 0, 1),
('wangwu', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13900000003', 'wangwu@qq.com', 0, 1),
('zhaoliu', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13900000004', 'zhaoliu@qq.com', 0, 1),
('sunqi', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '13900000005', 'sunqi@qq.com', 0, 0);

-- 初始化酒店数据
INSERT INTO `hotel` (`name`, `address`, `phone`, `description`, `cover_image`, `rating`, `status`) VALUES
('上海外滩华尔道夫酒店', '上海市黄浦区中山东一路2号', '021-63229988', '坐落于外滩核心地段，尽享黄浦江与陆家嘴天际线美景，提供顶级奢华住宿体验', 'https://www.hilton.com.cn/file/images/20251005/20251005170302611F9T3VcS_thum_mid.jpg', 4.9, 1),
('北京王府井文华东方酒店', '北京市东城区王府井大街269号', '010-85098888', '位于王府井商业区核心，融合东方韵味与现代奢华，是商务休闲的理想之选', 'https://img1.selfimg.com.cn/uedvoguecms/2023/02/17/1676615594_l42fs0.jpg', 4.8, 1),
('杭州西湖国宾馆', '杭州市西湖区杨公堤18号', '0571-87979889', '依山傍水，坐拥西湖美景，园林式建筑风格，曾接待多国元首', 'https://b0.bdstatic.com/ugc/GvVArvh7s2EAvF76H-03Dg58160a1cf1b9737fbb9b8a7c2ecb004a.jpg', 4.9, 1),
('三亚亚特兰蒂斯酒店', '三亚市海棠区海棠北路36号', '0898-88988888', '海棠湾畔的海洋主题度假胜地，拥有水族馆、水上乐园等丰富娱乐设施', 'https://miaobi-lite.bj.bcebos.com/miaobi/5mao/b%27LV8xNzM0MTI4NTc3LjE3NTQ3OTRfMTczNDEyODU3Ny45NDQwMDk1%27/1.png', 4.7, 1),
('成都香格里拉大酒店', '成都市锦江区滨江东路9号', '028-88889999', '位于春熙路商圈，俯瞰锦江美景，感受天府之国的悠闲与繁华', 'https://t9.baidu.com/it/u=3581935902,587733281&fm=193', 4.6, 1),
('深圳瑞吉酒店', '深圳市罗湖区深南东路5016号', '0755-82088888', '地处深圳金融中心，提供管家式服务，尽显尊贵品味', 'https://gips1.baidu.com/it/u=3306895351,3881686558&fm=3074&app=3074&f=JPEG?w=1000&h=1101&type=normal&func=', 4.8, 1),
('苏州柏悦酒店', '苏州市工业园区苏州中心广场', '0512-62888888', '现代建筑与苏式园林完美融合，体验江南水乡的诗意生活', 'https://gips2.baidu.com/it/u=3414136604,3915170790&fm=3074&app=3074&f=JPEG', 4.5, 0);

-- 初始化房间数据
INSERT INTO `room` (`hotel_id`, `room_type`, `name`, `price`, `capacity`, `amenities`, `images`, `status`) VALUES
-- 上海外滩华尔道夫酒店
(1, '豪华房', '外滩景观豪华大床房', 2888.00, 2, '["免费WiFi","空调","55寸智能电视","独立卫浴","迷你吧","保险箱","浴袍拖鞋"]', '["https://picsum.photos/seed/room1/600/400"]', 1),
(1, '行政房', '江景行政套房', 4888.00, 2, '["免费WiFi","空调","65寸智能电视","独立卫浴","迷你吧","行政酒廊","管家服务"]', '["https://picsum.photos/seed/room2/600/400"]', 1),
(1, '总统套房', '外滩总统套房', 18888.00, 4, '["免费WiFi","空调","75寸智能电视","独立卫浴","迷你吧","私人管家","专属电梯","会客厅"]', '["https://picsum.photos/seed/room3/600/400"]', 1),
-- 北京王府井文华东方酒店
(2, '豪华房', '城景豪华双床房', 2288.00, 2, '["免费WiFi","空调","50寸智能电视","独立卫浴","迷你吧","茶具套装"]', '["https://picsum.photos/seed/room4/600/400"]', 1),
(2, '行政房', '行政大床房', 3588.00, 2, '["免费WiFi","空调","55寸智能电视","独立卫浴","迷你吧","行政酒廊","延迟退房"]', '["https://picsum.photos/seed/room5/600/400"]', 1),
-- 杭州西湖国宾馆
(3, '园景房', '园景标准间', 1688.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","茶具","园林景观"]', '["https://picsum.photos/seed/room6/600/400"]', 1),
(3, '湖景房', '西湖景观大床房', 2888.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","迷你吧","西湖景观阳台"]', '["https://picsum.photos/seed/room7/600/400"]', 1),
(3, '别墅', '独栋湖景别墅', 8888.00, 6, '["免费WiFi","空调","智能电视","独立卫浴","厨房","私人花园","管家服务"]', '["https://picsum.photos/seed/room8/600/400"]', 1),
-- 三亚亚特兰蒂斯酒店
(4, '海景房', '海景双床房', 1988.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","阳台海景","水族馆门票"]', '["https://picsum.photos/seed/room9/600/400"]', 1),
(4, '水底套房', '尼普顿水底套房', 9888.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","水族馆景观","私人管家","VIP通道"]', '["https://picsum.photos/seed/room10/600/400"]', 1),
-- 成都香格里拉大酒店
(5, '豪华房', '锦江景观豪华房', 1288.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","迷你吧","江景"]', '["https://picsum.photos/seed/room11/600/400"]', 1),
(5, '行政房', '行政江景套房', 2288.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","迷你吧","行政酒廊","健身房"]', '["https://picsum.photos/seed/room12/600/400"]', 1),
-- 深圳瑞吉酒店
(6, '豪华房', '城景豪华大床房', 1888.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","迷你吧","管家服务"]', '["https://picsum.photos/seed/room13/600/400"]', 1),
(6, '套房', '瑞吉套房', 5888.00, 3, '["免费WiFi","空调","智能电视","独立卫浴","迷你吧","管家服务","会客厅","书房"]', '["https://picsum.photos/seed/room14/600/400"]', 1),
-- 苏州柏悦酒店（下架酒店的房间）
(7, '园景房', '苏式园景房', 1588.00, 2, '["免费WiFi","空调","智能电视","独立卫浴","园林景观"]', '["https://picsum.photos/seed/room15/600/400"]', 0);


-- 初始化预订数据
INSERT INTO `booking` (`user_id`, `room_id`, `check_in_date`, `check_out_date`, `total_price`, `status`, `remark`, `created_at`) VALUES
(3, 1, '2026-03-01', '2026-03-03', 5776.00, 0, '需要高楼层房间', '2026-02-20 10:30:00'),
(3, 4, '2026-03-10', '2026-03-12', 4576.00, 1, '商务出差，需要发票', '2026-02-21 14:20:00'),
(4, 6, '2026-03-05', '2026-03-07', 3376.00, 2, '蜜月旅行', '2026-02-18 09:15:00'),
(4, 9, '2026-04-01', '2026-04-05', 7952.00, 1, '家庭度假，有两个小孩', '2026-02-22 16:45:00'),
(5, 2, '2026-03-15', '2026-03-17', 9776.00, 0, '公司年会', '2026-02-23 11:00:00'),
(5, 11, '2026-03-20', '2026-03-22', 2576.00, 3, '周末休闲', '2026-02-10 08:30:00'),
(6, 7, '2026-03-08', '2026-03-10', 5776.00, 4, '行程取消', '2026-02-15 13:20:00'),
(6, 13, '2026-04-10', '2026-04-12', 3776.00, 1, '出差深圳', '2026-02-24 17:30:00');

-- 初始化操作日志数据
INSERT INTO `operation_log` (`user_id`, `username`, `module`, `operation`, `method`, `params`, `ip`, `duration`, `created_at`) VALUES
(1, 'admin', '酒店管理', '新增酒店', 'com.hotel.controller.HotelController.create', '{"name":"上海外滩华尔道夫酒店"}', '192.168.1.100', 45, '2026-02-20 09:00:00'),
(1, 'admin', '酒店管理', '更新酒店', 'com.hotel.controller.HotelController.update', '{"id":1,"status":1}', '192.168.1.100', 32, '2026-02-20 09:05:00'),
(1, 'admin', '房间管理', '新增房间', 'com.hotel.controller.RoomController.create', '{"hotelId":1,"name":"外滩景观豪华大床房"}', '192.168.1.100', 28, '2026-02-20 09:10:00'),
(2, 'manager', '用户管理', '更新状态', 'com.hotel.controller.UserController.updateStatus', '{"id":7,"status":0}', '192.168.1.101', 15, '2026-02-21 10:30:00'),
(2, 'manager', '预订管理', '更新状态', 'com.hotel.controller.BookingController.updateStatus', '{"id":2,"status":1}', '192.168.1.101', 22, '2026-02-21 14:25:00'),
(3, 'zhangsan', '预订管理', '创建预订', 'com.hotel.controller.BookingController.create', '{"roomId":1,"checkInDate":"2026-03-01"}', '192.168.1.50', 56, '2026-02-20 10:30:00'),
(4, 'lisi', '预订管理', '创建预订', 'com.hotel.controller.BookingController.create', '{"roomId":6,"checkInDate":"2026-03-05"}', '192.168.1.51', 48, '2026-02-18 09:15:00'),
(6, 'zhaoliu', '预订管理', '取消预订', 'com.hotel.controller.BookingController.cancel', '{"id":7}', '192.168.1.53', 35, '2026-02-16 10:00:00');
