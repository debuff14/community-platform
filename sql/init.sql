-- =====================================================================
-- 云上家园——社区物业一体化服务平台 建表脚本
-- 数据库：community_platform  字符集：utf8mb4
-- 说明：状态类字段统一用数字编码；金额用 decimal；工单表、账单表的
--       用户 id 和状态字段建有索引。
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `community_platform`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `community_platform`;

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
  `password`    VARCHAR(100) NOT NULL                COMMENT '密码(BCrypt加密)',
  `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `building`    VARCHAR(20)  DEFAULT NULL            COMMENT '楼栋号',
  `room_no`     VARCHAR(20)  DEFAULT NULL            COMMENT '房号',
  `role`        TINYINT      NOT NULL DEFAULT 0      COMMENT '角色 0业主 1管理员',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------------------------
-- 2. 公告表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`       VARCHAR(100) NOT NULL                COMMENT '标题',
  `content`     TEXT         NOT NULL                COMMENT '内容',
  `is_top`      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否置顶 0否 1是',
  `admin_id`    BIGINT       DEFAULT NULL            COMMENT '发布管理员ID',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_is_top_create_time` (`is_top`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ---------------------------------------------------------------------
-- 3. 账单表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `bill`;
CREATE TABLE `bill` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `user_id`     BIGINT        NOT NULL                COMMENT '业主ID',
  `fee_type`    TINYINT       NOT NULL                COMMENT '费用类型 1物业费 2停车费 3水电费',
  `amount`      DECIMAL(10,2) NOT NULL                COMMENT '金额',
  `month`       VARCHAR(7)    NOT NULL                COMMENT '所属月份 yyyy-MM',
  `status`      TINYINT       NOT NULL DEFAULT 0      COMMENT '状态 0未支付 1已支付',
  `pay_time`    DATETIME      DEFAULT NULL            COMMENT '支付时间',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_month` (`month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- ---------------------------------------------------------------------
-- 4. 支付流水表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `pay_no`      VARCHAR(32)   NOT NULL                COMMENT '支付流水号 PAY+日期+6位随机数',
  `bill_id`     BIGINT        NOT NULL                COMMENT '账单ID',
  `user_id`     BIGINT        NOT NULL                COMMENT '业主ID',
  `amount`      DECIMAL(10,2) NOT NULL                COMMENT '支付金额',
  `pay_time`    DATETIME      NOT NULL                COMMENT '支付时间',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_bill` (`bill_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- ---------------------------------------------------------------------
-- 5. 报修工单表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `repair_order`;
CREATE TABLE `repair_order` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `user_id`     BIGINT       NOT NULL                COMMENT '业主ID',
  `type`        TINYINT      NOT NULL DEFAULT 0      COMMENT '类型 0其他 1水电气 2门窗 3电梯',
  `description` VARCHAR(500) NOT NULL                COMMENT '问题描述',
  `urgency`     TINYINT      NOT NULL DEFAULT 0      COMMENT '紧急程度 0普通 1紧急',
  `images`      VARCHAR(500) DEFAULT NULL            COMMENT '现场照片地址,逗号分隔',
  `status`      TINYINT      NOT NULL DEFAULT 0      COMMENT '状态 0已提交 1已接单 2处理中 3已完成 4已评价 5已取消',
  `result`      VARCHAR(500) DEFAULT NULL            COMMENT '处理结果说明',
  `accept_time` DATETIME     DEFAULT NULL            COMMENT '接单时间',
  `finish_time` DATETIME     DEFAULT NULL            COMMENT '完成时间',
  `cancel_time` DATETIME     DEFAULT NULL            COMMENT '取消时间',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修工单表';

-- ---------------------------------------------------------------------
-- 6. 工单评价表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `repair_comment`;
CREATE TABLE `repair_comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id`    BIGINT       NOT NULL                COMMENT '工单ID',
  `user_id`     BIGINT       NOT NULL                COMMENT '业主ID',
  `star`        TINYINT      NOT NULL DEFAULT 5      COMMENT '星级 1-5',
  `content`     VARCHAR(500) DEFAULT NULL            COMMENT '评语',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单评价表';

-- ---------------------------------------------------------------------
-- 7. 二手商品表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `seller_id`     BIGINT        NOT NULL                COMMENT '卖家ID',
  `name`          VARCHAR(100)  NOT NULL                COMMENT '商品名称',
  `description`   VARCHAR(1000) DEFAULT NULL            COMMENT '商品描述',
  `price`         DECIMAL(10,2) NOT NULL                COMMENT '价格',
  `category`      TINYINT       NOT NULL DEFAULT 0      COMMENT '分类 0其他 1图书 2家电 3生活用品',
  `image`         VARCHAR(255)  DEFAULT NULL            COMMENT '商品图地址',
  `status`        TINYINT       NOT NULL DEFAULT 0      COMMENT '状态 0待审核 1在售 2已下架 3已驳回',
  `reject_reason` VARCHAR(255)  DEFAULT NULL            COMMENT '驳回原因',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品表';

-- ---------------------------------------------------------------------
-- 8. 访客登记表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `visitor`;
CREATE TABLE `visitor` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '访客登记ID',
  `user_id`      BIGINT       NOT NULL                COMMENT '业主ID',
  `visitor_name` VARCHAR(50)  NOT NULL                COMMENT '访客姓名',
  `car_no`       VARCHAR(20)  DEFAULT NULL            COMMENT '车牌号',
  `visit_date`   DATE         NOT NULL                COMMENT '来访日期',
  `time_start`   TIME         DEFAULT NULL            COMMENT '预计开始时间',
  `time_end`     TIME         DEFAULT NULL            COMMENT '预计结束时间',
  `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '状态 0待到访 1已入场 2已离场 3已取消',
  `enter_time`   DATETIME     DEFAULT NULL            COMMENT '入场时间',
  `leave_time`   DATETIME     DEFAULT NULL            COMMENT '离场时间',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客登记表';

-- ---------------------------------------------------------------------
-- 9. AI 聊天记录表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id`     BIGINT      NOT NULL                COMMENT '业主ID',
  `role`        VARCHAR(20) NOT NULL                COMMENT '角色 user业主 assistant客服',
  `content`     TEXT        NOT NULL                COMMENT '消息内容',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天记录表';

-- ---------------------------------------------------------------------
-- 10. 站内消息通知表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id`     BIGINT       NOT NULL                COMMENT '接收业主ID',
  `content`     VARCHAR(255) NOT NULL                COMMENT '消息内容',
  `type`        TINYINT      NOT NULL DEFAULT 1      COMMENT '类型 1工单 2账单 3系统',
  `related_id`  BIGINT       DEFAULT NULL            COMMENT '关联业务ID(如工单ID)',
  `is_read`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已读 0未读 1已读',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息通知表';

-- ---------------------------------------------------------------------
-- 11. AI 知识库表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `title`       VARCHAR(100) NOT NULL                COMMENT '标题',
  `content`     TEXT         NOT NULL                COMMENT '内容',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库表';

-- ---------------------------------------------------------------------
-- 12. 操作日志表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`     BIGINT        DEFAULT NULL            COMMENT '操作人ID',
  `username`    VARCHAR(50)   DEFAULT NULL            COMMENT '操作人用户名',
  `operation`   VARCHAR(100)  NOT NULL                COMMENT '操作描述(接口名)',
  `params`      VARCHAR(1000) DEFAULT NULL            COMMENT '参数摘要',
  `result`      VARCHAR(500)  DEFAULT NULL            COMMENT '执行结果',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================================
-- 预置数据
-- 说明：管理员账号不提供注册入口，由本脚本预置；
--       BCrypt 密文对应的明文密码均为 123456。
-- =====================================================================

INSERT INTO `user` (`username`, `password`, `phone`, `building`, `room_no`, `role`) VALUES
('admin',    '$2a$10$egs16MNW5UD89shW91gS7.SNAnISihSfHndnfqpj3DhcW2Jfq/sja', '13800000000', NULL,  NULL,  1),
('zhangsan', '$2a$10$egs16MNW5UD89shW91gS7.SNAnISihSfHndnfqpj3DhcW2Jfq/sja', '13800000001', '3栋', '502', 0),
('lisi',     '$2a$10$egs16MNW5UD89shW91gS7.SNAnISihSfHndnfqpj3DhcW2Jfq/sja', '13800000002', '3栋', '501', 0);

INSERT INTO `notice` (`title`, `content`, `is_top`, `admin_id`) VALUES
('9月电梯维护通知', '各位业主：9月15日 9:00-12:00 将对1-3栋电梯进行年度维护，期间电梯暂停使用，请提前安排出行。给您带来不便，敬请谅解。', 1, 1),
('小区中秋游园活动通知', '中秋佳节将至，物业将于9月20日18:00在中心广场举办游园活动，现场有猜灯谜、DIY月饼等环节，欢迎各位业主携家人参加。', 0, 1);
