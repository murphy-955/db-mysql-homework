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

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT id, record_enum, amount, date
FROM bill_sys.user_bill
WHERE user_id = '202511271319ac58bdcc4b8681e3e39474d26aff'
  AND date BETWEEN '2025-10-30' AND '2025-12-01'
ORDER BY date DESC;

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, COSTS)
SELECT *
FROM bill_sys.user_bill
WHERE id = '4830093'
  AND deleted = FALSE;

DROP TABLE IF EXISTS bill_sys."user_bill";


-- 调试