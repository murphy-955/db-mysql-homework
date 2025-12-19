CREATE TABLE IF NOT EXISTS bill_sys."user"
(
    id                            SERIAL PRIMARY KEY,
    user_id                       CHAR(40)     NOT NULL,
    username                      VARCHAR(100) NOT NULL,
    password                      VARCHAR(100) NOT NULL,
    whether_it_is_disabled_or_not BOOLEAN      NOT NULL DEFAULT FALSE
);
-- 为user表添加索引
CREATE INDEX idx_user_user_id ON bill_sys."user" (user_id);
CREATE INDEX idx_user_username ON bill_sys."user" (username);

SELECT *
FROM bill_sys."user"
WHERE username = 'user3';

-- 清空user表
TRUNCATE TABLE bill_sys."user";
DROP TABLE IF EXISTS bill_sys."user";


CREATE TABLE IF NOT EXISTS bill_sys."user_account"
(
    id          SERIAL PRIMARY KEY,
    user_id     CHAR(40)       NOT NULL,
    account     VARCHAR(20)    NOT NULL,
    balance     NUMERIC(20, 2) NOT NULL DEFAULT 0,
    description VARCHAR(50)
);

-- 为user_account表添加索引
CREATE INDEX idx_user_account_user_id ON bill_sys."user_account" (user_id);
CREATE INDEX idx_user_account_user_id_account ON bill_sys."user_account" (user_id, account);

-- 清空user_account表
TRUNCATE TABLE bill_sys."user_account";
DROP TABLE IF EXISTS bill_sys."user_account";



CREATE TABLE IF NOT EXISTS bill_sys."user_bill"
(
    id          SERIAL PRIMARY KEY,
    user_id     CHAR(40)       NOT NULL,
    record_enum VARCHAR(20)    NOT NULL,
    amount      NUMERIC(20, 2) NOT NULL,
    account     VARCHAR(20)    NOT NULL,
    type        VARCHAR(15)    NOT NULL,
    date        DATE           NOT NULL,
    remarks     VARCHAR(100),
    deleted     BOOLEAN        NOT NULL DEFAULT FALSE
);

-- 为user_bill表添加优化索引
-- 所有索引都以user_id和deleted为前缀，因为几乎所有查询都包含这两个条件
-- 1. 支持按user_id+deleted+date范围查询，以及分页查询
CREATE INDEX idx_bill_user_deleted_date_id ON bill_sys."user_bill" (user_id, deleted, date DESC, id DESC);
-- 2. 支持按user_id+deleted+account查询
CREATE INDEX idx_bill_user_deleted_account_date ON bill_sys."user_bill" (user_id, deleted, account, date DESC, id DESC);
-- 3. 支持按user_id+deleted+record_enum查询
CREATE INDEX idx_bill_user_deleted_record_enum_date ON bill_sys."user_bill" (user_id, deleted, record_enum, date DESC, id DESC);
-- 4. 支持按user_id+deleted+amount范围查询
CREATE INDEX idx_bill_user_deleted_amount_date ON bill_sys."user_bill" (user_id, deleted, amount, date DESC, id DESC);
-- 5. 支持按id查询
CREATE INDEX idx_bill_id_deleted ON bill_sys."user_bill" (id, deleted);

DROP INDEX IF EXISTS bill_sys.idx_bill_user_deleted_date_id;
CREATE INDEX idx_bill_user_date_id_filtered
    ON bill_sys."user_bill" (user_id, date DESC, id DESC)
    WHERE deleted = FALSE;

SELECT count(*)
FROM bill_sys.user_bill;

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT *
FROM bill_sys.user_bill
WHERE date BETWEEN '2025-10-30' AND '2025-12-01'
ORDER BY date DESC;

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT *
FROM bill_sys.user_bill
WHERE id = '4830093'
  AND deleted = FALSE;

DROP TABLE IF EXISTS bill_sys."user_bill";


-- 调试
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT id, record_enum, amount, date
FROM bill_sys.user_bill
WHERE user_id = '20251219940efb608eb44bd7a7301b6ca36d5a67'
  AND deleted = FALSE
ORDER BY date DESC, id DESC
LIMIT 10 OFFSET 8;

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT id, record_enum, amount, date
FROM bill_sys.user_bill
WHERE user_id = '20251219940efb608eb44bd7a7301b6ca36d5a67'
  AND deleted = FALSE
  AND amount >= 1000
  AND amount <= 10000
ORDER BY date DESC, id DESC
LIMIT 10 OFFSET 10 * (10 - 1);

SELECT id, record_enum, amount, date
FROM bill_sys.user_bill
WHERE user_id = '20251219940efb608eb44bd7a7301b6ca36d5a67'
  AND deleted = FALSE
  AND type = 'FOOD'
ORDER BY date DESC, id DESC
LIMIT 10 OFFSET 10 * (10 - 1);

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT id, record_enum, amount, date
FROM bill_sys.user_bill
WHERE user_id = '20251219940efb608eb44bd7a7301b6ca36d5a67'
  AND deleted = FALSE
  AND amount >= 100
  AND amount <= 10000
ORDER BY date DESC, id DESC
LIMIT 10 OFFSET 10 * (2 - 1);


-- 实验一

-- 0. 可选：单独建库并切换
-- CREATE DATABASE sales_db;
-- \c sales_db

-- 1. 产品表
CREATE TABLE IF NOT EXISTS bill_sys."products"
(
    prod_id         SERIAL PRIMARY KEY,
    prod_grp_id     INT NOT NULL,
    prod_remain_cnt INT NOT NULL CHECK (prod_remain_cnt >= 0),
    prod_review     TEXT,
    create_time     TIMESTAMP DEFAULT NOW(),
    update_time     TIMESTAMP DEFAULT NOW()
);

DROP TABLE IF EXISTS bill_sys.products;

-- 2. 用户表
CREATE TABLE bill_sys."pro_users"
(
    user_id   SERIAL PRIMARY KEY,
    user_name VARCHAR(50)  NOT NULL,
    user_addr VARCHAR(200) NOT NULL
);

-- 3. 支付/订单表
CREATE TABLE bill_sys.payments
(
    pay_id      SERIAL PRIMARY KEY,
    prod_id     INT NOT NULL REFERENCES bill_sys.products (prod_id) ON DELETE CASCADE,
    user_id     INT NOT NULL REFERENCES bill_sys.pro_users (user_id) ON DELETE CASCADE,
    prod_serial VARCHAR(100),
    pay_time    TIMESTAMP DEFAULT NOW()
);
-- 4. 运输/物流表
CREATE TABLE bill_sys."transports"
(
    trans_id          SERIAL PRIMARY KEY,
    trans_prod_id     INT            NOT NULL REFERENCES bill_sys.products (prod_id) ON DELETE CASCADE,
    trans_user_id     INT            NOT NULL REFERENCES bill_sys.pro_users (user_id) ON DELETE CASCADE,
    trans_payment     NUMERIC(10, 2) NOT NULL CHECK (trans_payment >= 0),
    trans_dev_varchar VARCHAR(100),
    trans_note        TEXT,
    create_time       TIMESTAMP DEFAULT NOW()
);

-- ============== 模拟数据 ==============

-- 产品
INSERT INTO bill_sys.products(prod_grp_id, prod_remain_cnt, prod_review)
VALUES (1, 100, 'OpenGauss 入门书籍'),
       (1, 50, 'OpenGauss 官方文档'),
       (2, 200, '企业级数据库解决方案'),
       (3, 0, '已售罄的纪念 T 恤'),
       (2, 80, '数据库性能调优指南');

-- 用户
INSERT INTO bill_sys.pro_users(user_name, user_addr)
VALUES ('murphy-955', '南京市秦淮区'),
       ('朴国昌', '福州市福州大学'),
       ('王五', '深圳市南山区'),
       ('赵六', '广州市天河区'),
       ('钱七', '杭州市西湖区');

-- 支付订单
INSERT INTO bill_sys.payments(prod_id, user_id, prod_serial)
VALUES (1, 1, 'PG-BK-00001'),
       (2, 2, 'PG-DO-00002'),
       (3, 3, 'PG-ES-00003'),
       (5, 4, 'PG-PG-00005'),
       (1, 5, 'PG-BK-00006');

-- 物流
INSERT INTO bill_sys.transports(trans_prod_id, trans_user_id, trans_payment, trans_dev_varchar, trans_note)
VALUES (1, 1, 12.00, '顺丰速运', '已发货'),
       (2, 2, 10.50, '京东物流', '运输中'),
       (5, 4, 8.00, '圆通速递', '已签收'),
       (3, 3, 15.00, 'EMS', '清关中'),
       (1, 5, 12.00, '顺丰速运', '已发货');

-- 查询

SELECT prod_id,
       prod_remain_cnt AS stock,
       prod_review
FROM bill_sys.products
WHERE prod_remain_cnt < 50
ORDER BY prod_id;

SELECT u.user_name,
       py.pay_id,
       pr.prod_review AS prod_name,
       py.pay_time
FROM bill_sys.pro_users u
         JOIN bill_sys.payments py ON py.user_id = u.user_id
         JOIN bill_sys.products pr ON pr.prod_id = py.prod_id
WHERE u.user_name = '朴国昌'
ORDER BY py.pay_time DESC
LIMIT 5;

DROP TABLE IF EXISTS bill_sys.products CASCADE;
DROP TABLE IF EXISTS bill_sys.payments CASCADE;
DROP TABLE IF EXISTS bill_sys.transports CASCADE;
DROP TABLE IF EXISTS bill_sys.users CASCADE;