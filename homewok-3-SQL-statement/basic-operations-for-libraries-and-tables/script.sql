# 第一关
# 把数据表tb_emp改名为jd_emp；

# 查看该数据库下数据表的列表；

# 查看数据表jd_emp的基本结构。

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## modify the table name ##########
RENAME TABLE tb_emp TO jd_emp;


########## show tables in this database ##########
SHOW TABLES;


########## describe the table ##########
DESCRIBE jd_emp;


# ======================================================================================================================

# 第二关
# 把数据表tb_emp的字段Id改名为prod_id，数据类型不变；

# 把数据表tb_emp字段Name的数据类型改为varchar(30)。

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## change the column name ##########
ALTER TABLE tb_emp
    CHANGE Id prod_id INT(11);


########## change the data type of column ##########
ALTER TABLE tb_emp
    MODIFY Name VARCHAR(30);


########## End ##########

DESCRIBE tb_emp;

# ======================================================================================================================
# 第三关

# 在数据表tb_emp的Name字段后添加字段Country，数据格式为varchar(20)；

# 删除数据表tb_emp中的字段Salary。

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## add the column ##########
ALTER TABLE tb_emp
    ADD Country VARCHAR(20) AFTER Name;


########## delete the column ##########
ALTER TABLE tb_emp
    DROP Salary;

########## End ##########

DESCRIBE tb_emp;

# ======================================================================================================================
# 第四关

# 将数据表tb_emp的Name字段移至第一列，数据格式不变；

# 将DeptId字段移至Salary字段的后边，数据格式不变。

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## modify the column to top ##########
ALTER TABLE tb_emp
    MODIFY Name VARCHAR(25) FIRST;


########## modify the column to the rear of another column ##########
ALTER TABLE tb_emp
    MODIFY DeptId INT(11) AFTER Salary;

########## End ##########

DESCRIBE tb_emp;

# ======================================================================================================================
# 第四关

# 删除数据表tb_emp的外键约束emp_dept。

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## delete the foreign key ##########
ALTER TABLE tb_emp DROP FOREIGN KEY emp_dept;


########## End ##########
SHOW CREATE TABLE tb_emp \G;