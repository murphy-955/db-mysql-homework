CREATE TABLE IF NOT EXISTS bill_sys."user"
(
    id       SERIAL PRIMARY KEY,
    user_id  CHAR(40)     NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    balance  NUMERIC(20, 2) NOT NULL DEFAULT 0,
    whether_it_is_disabled_or_not BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_user_user_id ON bill_sys."user"(user_id);


CREATE TABLE IF NOT EXISTS bill_sys."user_account"
(
    id       SERIAL PRIMARY KEY,
    user_id  CHAR(40)     NOT NULL,
    account VARCHAR(20) NOT NULL ,
    balance  NUMERIC(20, 2) NOT NULL DEFAULT 0,
    description VARCHAR(50)
);

CREATE INDEX idx_user_account_user_id ON bill_sys."user_account"(user_id);

CREATE TABLE IF NOT EXISTS bill_sys."user_bill"
(
    id       SERIAL PRIMARY KEY,
    user_id  CHAR(40)     NOT NULL,
    record_enum VARCHAR(10) NOT NULL,
    amount  NUMERIC(20, 2) NOT NULL,
    account VARCHAR(20) NOT NULL,
    type VARCHAR(15) NOT NULL ,
    date DATE NOT NULL,
    remarks VARCHAR(50) ,
    deleted  BOOLEAN NOT NULL DEFAULT FALSE
);

DROP TABLE IF EXISTS bill_sys."user_bill";

CREATE INDEX idx_bill_user_id_date ON bill_sys."user_bill"(user_id, date);
CREATE INDEX idx_bill_date ON bill_sys."user_bill"(date);
CREATE INDEX idx_bill_account ON bill_sys."user_bill"(account);
CREATE INDEX idx_bill_user_date_type ON bill_sys."user_bill"(user_id, date, type);