CREATE TABLE IF NOT EXISTS bill_sys."user"
(
    id                            SERIAL PRIMARY KEY,
    user_id                       CHAR(40)     NOT NULL,
    username                      VARCHAR(100) NOT NULL,
    password                      VARCHAR(100) NOT NULL,
    whether_it_is_disabled_or_not BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_user_user_id ON bill_sys."user" (user_id);

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

CREATE INDEX idx_user_account_user_id ON bill_sys."user_account" (user_id);

SELECT *
FROM bill_sys."user_account";
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

SELECT id, record_enum, amount, date
FROM bill_sys.user_bill
WHERE user_id = '202511271319ac58bdcc4b8681e3e39474d26aff'
  AND date BETWEEN '2025-11-30' AND '2025-12-01'
ORDER BY date DESC;


DROP TABLE IF EXISTS bill_sys."user_bill";

CREATE INDEX idx_bill_user_id_date ON bill_sys."user_bill" (user_id, date);
CREATE INDEX idx_bill_date ON bill_sys."user_bill" (date);
CREATE INDEX idx_bill_account ON bill_sys."user_bill" (account);
CREATE INDEX idx_bill_user_date_type ON bill_sys."user_bill" (user_id, date, type);

-- 调试