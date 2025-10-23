# 第一关
CREATE DATABASE IF NOT EXISTS MyDb;

SHOW DATABASES;

# 第三关主键约束
# 在数据库MyDb中，创建两张表t_user1，t_user2，表结构如下，请为两张表分别创建主键约束，
# t_user1的主键为userId,
# t_user2的主键为联合主键，将字段name和phone作为t_user2的联合主键。
USE MyDb;

CREATE TABLE IF NOT EXISTS t_user1
(
    `userId`   INT(11) PRIMARY KEY COMMENT '用户ID',
    `name`     VARCHAR(32) COMMENT '姓名',
    `password` VARCHAR(11) COMMENT '密码',
    `phone`    VARCHAR(11) COMMENT '手机号',
    `email`    VARCHAR(32) COMMENT '邮箱'
) ENGINE = InnoDB,
  CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS t_user2
(
    `name`  VARCHAR(32) COMMENT '用户名',
    `phone` VARCHAR(11) COMMENT '手机号',
    `email` VARCHAR(32) COMMENT '邮箱',
    PRIMARY KEY (name, phone)
) ENGINE = InnoDB,
  CHARSET = utf8mb4;

# 第四关
# 创建两张表如下，给t_student表添加外键约束，外键为classId,外键名称为fk_stu_class1。

CREATE TABLE IF NOT EXISTS t_class
(
    `id`   INT(11) PRIMARY KEY COMMENT '班级ID',
    `name` VARCHAR(22) COMMENT '班级名称'
) ENGINE = InnoDB,
  CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS t_student
(
    `id`      INT PRIMARY KEY COMMENT '学生ID',
    `name`    VARCHAR(22) COMMENT '学生姓名',
    `classId` INT COMMENT '班级ID,外键',
    CONSTRAINT fk_stu_class1 FOREIGN KEY (id) REFERENCES t_class (id)
) ENGINE = InnoDB,
  CHARSET = utf8mb4;

# 第五关

CREATE TABLE IF NOT EXISTS t_user
(
    `id`       INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(32) NOT NULL UNIQUE,
    `sex`      VARCHAR(4) DEFAULT '男'
) ENGINE = InnoDB,
  CHARSET = utf8mb4;
