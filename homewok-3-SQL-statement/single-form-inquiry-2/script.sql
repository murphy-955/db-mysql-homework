# 第一关

# 请你查询所有Name以字母C为起始的员工的Name和Salary的内容

USE Company;

######### Begin #########
SELECT Name, Salary
FROM tb_emp
WHERE Name LIKE 'C%';

######### End #########

# ======================================================================================================================

# 第二关

# 使用关键字IS NULL返回数据表中字段DeptId为NULL的所有字段的内容，注意，返回的是指定行所有字段的内容；

# 使用关键字DISTINCT返回数据表中字段Name不重复的内容，注意，只需返回字段Name的内容。
USE Company;

######### Begin #########
SELECT *
FROM tb_emp
WHERE DeptId IS NULL;

######### End #########

######### Begin #########
SELECT DISTINCT Name
FROM tb_emp;

######### End #########

# ======================================================================================================================
# 第三关

# 使用关键字AND返回数据表中字段DeptId为301并且薪水大于3000的所有字段的内容，其中DeptId的倒数第二个字母为i的大写；

# 使用关键字IN返回数据表中字段DeptId为301和303的所有字段的内容。
USE Company;

######### Begin #########
SELECT *
FROM tb_emp
WHERE DeptId = 301 AND Salary > 3000;

######### End #########

######### Begin #########
SELECT *
FROM tb_emp
WHERE DeptId IN (301 ,303);

######### End #########

