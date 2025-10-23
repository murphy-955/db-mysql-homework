# 第一关

USE Company;

#请在此处添加实现代码
########## Begin ##########
#1.查询大于所有平均年龄的员工姓名与年龄
SELECT name, age
FROM tb_emp
WHERE age > (SELECT AVG(age) FROM tb_emp);

########## End ##########

# ======================================================================================================================

# 第二关

USE Company;
#请在此处添加实现代码
########## Begin ##########

#1.使用 ALL 关键字进行查询
# 查询薪资表中比Java最高工资高的所有员工职位名称和薪资；
SELECT position, salary
FROM tb_salary
WHERE salary > ALL (SELECT MAX(salary) FROM tb_salary WHERE position = 'Java');
#2.使用 ANY 关键字进行查询
# 查询薪资表中比Java最低工资高的所有员工职位名称和薪资；
SELECT position, salary
FROM tb_salary
WHERE salary > ANY (SELECT MIN(salary) FROM tb_salary WHERE position = 'Java');
#3.使用 IN 关键字进行查询
# 查询薪资表中职位为Java的所有员工职位名称和薪资。
SELECT position, salary
FROM tb_salary
WHERE position IN ('Java');

########## End ##########
