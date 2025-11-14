SELECT *
FROM bill_sys.bill;

UPDATE bill_sys.bill
SET bill_amount = 1000
WHERE bill_id = 4;

CREATE TABLE IF NOT EXISTS bill_sys.user
(
    id       INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id  CHAR(40)     NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    whether_it_is_disabled_or_not BOOLEAN NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;