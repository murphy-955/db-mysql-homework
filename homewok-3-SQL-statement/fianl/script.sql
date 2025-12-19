DROP DATABASE IF EXISTS db_homework;

CREATE DATABASE IF NOT EXISTS db_homework;

USE db_homework;

-- 创建用户角色的枚举类型，规范用户身份
CREATE TYPE user_role_enum AS ENUM ('student', 'teacher', 'admin');

-- 1.1 学校表
CREATE TABLE sys_school
(
    school_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '学校ID，主键，自动递增',
    school_name VARCHAR(255) NOT NULL COMMENT '学校名称',
    is_active   TINYINT(1) DEFAULT 1 COMMENT '是否启用，1-启用，0-禁用'
);

-- 1.2 用户表：
CREATE TABLE sys_user
(
    user_id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键，自动递增',
    school_id          INT          NOT NULL COMMENT '所属学校ID，外键引用sys_school(school_id)',
    username           VARCHAR(100) NOT NULL UNIQUE COMMENT '登录账号，唯一',
    password_hash      VARCHAR(255) NOT NULL COMMENT '加密密码',
    role               ENUM('student','teacher','admin') NOT NULL COMMENT '用户角色：student-学生，teacher-教师，admin-管理员',
    real_name          VARCHAR(100) COMMENT '真实姓名',
    student_teacher_no VARCHAR(50) COMMENT '学号/工号',
    college_major      VARCHAR(100) COMMENT '学院/专业',
    phone              VARCHAR(20) COMMENT '联系电话',
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    status             TINYINT(1) DEFAULT 1 CHECK (status in (0,1)) COMMENT '用户状态：1-正常，0-禁用',

    CONSTRAINT fk_user_school
        FOREIGN KEY (school_id)
            REFERENCES sys_school (school_id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
);

SELECT * FROM sys_school JOIN sys_user su on sys_school.school_id = su.school_id;

-- 2.1 学期表
CREATE TABLE edu_semester
(
    semester_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '学期ID，主键，自动递增',
    semester_name VARCHAR(100) NOT NULL COMMENT '学期名称（如 "2024-2025第一学期"）',
    start_date    DATE         NOT NULL COMMENT '学期开始日期',
    end_date      DATE         NOT NULL COMMENT '学期结束日期'
);

-- 2.2 课程模板表
CREATE TABLE edu_course
(
    course_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID，主键，自动递增',
    course_name       VARCHAR(200) NOT NULL COMMENT '课程名称',
    intro_content     TEXT COMMENT '课程介绍内容',
    task_requirements TEXT COMMENT '实践任务总体要求',
    is_public         TINYINT(1) DEFAULT 0 COMMENT '是否允许外校使用'
);

-- 2.3 教学班级表
CREATE TABLE edu_class
(
    class_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班级ID',
    course_id   INT          NOT NULL COMMENT '关联课程ID',
    semester_id INT          NOT NULL COMMENT '关联学期ID',
    teacher_id  BIGINT       NOT NULL COMMENT '任课教师ID',
    class_name  VARCHAR(100) NOT NULL COMMENT '班级名称',
    invite_code VARCHAR(50) COMMENT '选课邀请码）',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开课时间',

    CONSTRAINT fk_class_course   FOREIGN KEY (course_id)   REFERENCES edu_course(course_id)   ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_class_semester FOREIGN KEY (semester_id) REFERENCES edu_semester(semester_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_class_teacher  FOREIGN KEY (teacher_id)  REFERENCES sys_user(user_id)        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_class_invite_code (invite_code)
);

-- 2.4 选课关联表
CREATE TABLE edu_enrollment
(
    enrollment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选课记录ID',
    class_id   BIGINT NOT NULL COMMENT '班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    join_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',

    CONSTRAINT fk_enroll_class   FOREIGN KEY (class_id)   REFERENCES edu_class(class_id) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES sys_user(user_id)   ON DELETE CASCADE,
    UNIQUE KEY uk_enroll (class_id, student_id)   -- 防止重复选课
);

-- 3.1 仿真学习记录
CREATE TABLE task_unity_record
(
    record_id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    student_id       BIGINT NOT NULL COMMENT '学生ID',
    class_id         BIGINT NOT NULL COMMENT '班级ID',
    learn_duration   INT     DEFAULT 0 COMMENT '学习时长（秒）',
    completion_status TINYINT(1) DEFAULT 0 COMMENT '是否完成仿真学习',
    last_access_time TIMESTAMP NULL COMMENT '最后访问时间',

    CONSTRAINT fk_ur_student FOREIGN KEY (student_id) REFERENCES sys_user(user_id)   ON DELETE CASCADE,
    CONSTRAINT fk_ur_class   FOREIGN KEY (class_id)   REFERENCES edu_class(class_id) ON DELETE CASCADE,
    INDEX idx_ur_student (student_id),
    INDEX idx_ur_class   (class_id)
);

-- 3.2 题库表
CREATE TABLE task_quiz_question
(
    question_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    course_id      INT  NOT NULL COMMENT '归属课程ID',
    question_text  TEXT NOT NULL COMMENT '题目内容',
    options_json   JSON NOT NULL COMMENT '选项内容）',
    correct_answer CHAR(1) CHECK (correct_answer IN ('A','B','C','D')) COMMENT '正确选项',
    score_value    INT DEFAULT 5 COMMENT '分值（默认5分）',

    CONSTRAINT fk_qq_course FOREIGN KEY (course_id) REFERENCES edu_course(course_id) ON DELETE CASCADE,
    INDEX idx_qq_course (course_id)
);

-- 3.3 测试成绩表
CREATE TABLE task_quiz_result
(
    result_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成绩ID',
    student_id    BIGINT NOT NULL COMMENT '学生ID',
    class_id      BIGINT NOT NULL COMMENT '班级ID',
    total_score   INT CHECK (total_score BETWEEN 0 AND 100) COMMENT '总分',
    is_passed     TINYINT(1) DEFAULT 0 COMMENT '是否及格',
    attempt_count INT       DEFAULT 1 COMMENT '尝试次数',
    submit_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',

    CONSTRAINT fk_qr_student FOREIGN KEY (student_id) REFERENCES sys_user(user_id)   ON DELETE CASCADE,
    CONSTRAINT fk_qr_class   FOREIGN KEY (class_id)   REFERENCES edu_class(class_id) ON DELETE CASCADE,
    INDEX idx_qr_student (student_id),
    INDEX idx_qr_class   (class_id)
);

-- 4.1 家乡基地投稿
CREATE TABLE task_hometown_submission
(
    submission_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提交ID',
    student_id        BIGINT       NOT NULL COMMENT '学生ID',
    class_id          BIGINT       NOT NULL COMMENT '班级ID',
    base_name         VARCHAR(200) NOT NULL COMMENT '基地名称',
    location          VARCHAR(200) COMMENT '地理位置/家乡',
    description       TEXT COMMENT '介绍资料文本',
    attachment_url    VARCHAR(500) COMMENT '图片或视频附件地址',
    submit_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    teacher_feedback  TEXT COMMENT '教师评语（可选）',

    CONSTRAINT fk_hts_student FOREIGN KEY (student_id) REFERENCES sys_user(user_id)   ON DELETE CASCADE,
    CONSTRAINT fk_hts_class   FOREIGN KEY (class_id)   REFERENCES edu_class(class_id) ON DELETE CASCADE,
    INDEX idx_hts_student (student_id),
    INDEX idx_hts_class   (class_id)
);

-- 4.2 讨论区帖子
CREATE TABLE forum_topic
(
    topic_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    class_id   BIGINT       NOT NULL COMMENT '归属班级ID',
    user_id    BIGINT       NOT NULL COMMENT '发帖人ID',
    title      VARCHAR(200) NOT NULL COMMENT '标题',
    content    TEXT COMMENT '帖子内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',

    CONSTRAINT fk_ft_class FOREIGN KEY (class_id) REFERENCES edu_class(class_id) ON DELETE CASCADE,
    CONSTRAINT fk_ft_user  FOREIGN KEY (user_id)  REFERENCES sys_user(user_id)  ON DELETE CASCADE,
    INDEX idx_ft_class (class_id),
    INDEX idx_ft_user  (user_id)
);

-- 4.3 讨论区评论
CREATE TABLE forum_comment
(
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    topic_id   BIGINT NOT NULL COMMENT '关联帖子ID',
    user_id    BIGINT NOT NULL COMMENT '评论人ID',
    content    TEXT COMMENT '评论内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',

    CONSTRAINT fk_fc_topic FOREIGN KEY (topic_id) REFERENCES forum_topic(topic_id) ON DELETE CASCADE,
    CONSTRAINT fk_fc_user  FOREIGN KEY (user_id)  REFERENCES sys_user(user_id)    ON DELETE CASCADE,
    INDEX idx_fc_topic (topic_id),
    INDEX idx_fc_user  (user_id)
);

CREATE OR REPLACE VIEW view_teacher_task1_summary AS
SELECT e.class_id,
       u.student_teacher_no,
       u.real_name,
       COALESCE(tqr.total_score, 0)           as total_score, -- 空值处理为0
       COALESCE(tqr.is_passed, FALSE)         as is_passed,
       COALESCE(tur.learn_duration, 0)        as learn_duration,
       COALESCE(tur.completion_status, FALSE) AS sim_completed
FROM edu_enrollment e
         JOIN
     sys_user u ON e.student_id = u.user_id
         LEFT JOIN
     task_quiz_result tqr ON e.student_id = tqr.student_id AND e.class_id = tqr.class_id
         LEFT JOIN
     task_unity_record tur ON e.student_id = tur.student_id AND e.class_id = tur.class_id;

/* 2 触发器：MySQL 写法 */
/* 插入前触发器 */
DELIMITER $$
CREATE TRIGGER trg_check_quiz_pass_ins
    BEFORE INSERT ON task_quiz_result
    FOR EACH ROW
BEGIN
    IF NEW.total_score >= 70 THEN
        SET NEW.is_passed = 1;
    ELSE
        SET NEW.is_passed = 0;
    END IF;
END$$
DELIMITER ;

/* 更新前触发器 */
DELIMITER $$
CREATE TRIGGER trg_check_quiz_pass_upd
    BEFORE UPDATE ON task_quiz_result
    FOR EACH ROW
BEGIN
    IF NEW.total_score >= 70 THEN
        SET NEW.is_passed = 1;
    ELSE
        SET NEW.is_passed = 0;
    END IF;
END$$
DELIMITER ;

/* 3 存储过程：生成班级并返回邀请码 */
DELIMITER $$
CREATE PROCEDURE sp_create_class_instance(
    IN  p_teacher_id   BIGINT,
    IN  p_course_id    INT,
    IN  p_semester_id  INT,
    IN  p_class_name   VARCHAR(100),
    OUT p_invite_code  VARCHAR(20)
)
BEGIN
    /* 生成 8 位大写邀请码 */
    SET p_invite_code = UPPER(LEFT(MD5(CONCAT(p_teacher_id, p_course_id,
                                              p_semester_id, NOW(3),
                                              p_class_name)), 8));
    INSERT INTO edu_class (course_id, semester_id, teacher_id, class_name, invite_code)
    VALUES (p_course_id, p_semester_id, p_teacher_id, p_class_name, p_invite_code);
END$$
DELIMITER ;

/* 4 行级安全：MySQL 8.0 不支持 RLS，用视图+权限或应用侧过滤代替 */
/* 这里仅给出等价过滤条件，供创建安全视图使用 */
-- 例：
-- CREATE VIEW v_edu_class AS
-- SELECT * FROM edu_class
-- WHERE teacher_id = fn_get_user_id()   -- 自定义函数返回当前登录用户 ID
--    OR class_id IN (SELECT class_id FROM edu_enrollment WHERE student_id = fn_get_user_id());

/* 5 GIN 索引 → MySQL 8.0 的 JSON 索引 */
/* 假设 options_json 是 JSON 类型，建函数索引 */
ALTER TABLE task_quiz_question
    ADD INDEX idx_option_a (
        ( CAST(options_json->>'$.optionA' AS CHAR(200)) )
        );

/* 6 加密扩展：MySQL 自带，无需 CREATE EXTENSION */
/* 插入示例：使用 bcrypt 需下载 plugin（8.0 后可用 caching_sha2_password 或 SHA2+salt）*/
-- 例：INSERT INTO sys_user(password_hash)
--     VALUES(CONCAT('$2a$10$', SHA2(RAND(),512)));   -- 仅示意，真正 bcrypt 需外部库