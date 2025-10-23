# 第一关

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## retrieving the Name and Salary ##########

SELECT Name, Salary
FROM tb_emp;

########## retrieving all the table ##########
SELECT *
FROM tb_emp;

########## End ##########


# ======================================================================================================================

# 第二关

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## retrieving the Name and Salary with IN statement ##########
SELECT Name, Salary
FROM tb_emp
WHERE id NOT IN (1);


########## End ##########

# ======================================================================================================================

# 第三关

# 请你查询当字段Salary范围在3000~5000时，字段Name和Salary的内容。

USE Company;

#请在此处添加实现代码
########## Begin ##########

########## retrieving the Name and Salary with BETWEEN AND statement ##########
SELECT Name,Salary
FROM tb_emp
WHERE salary BETWEEN 3000 AND 5000;


########## End ##########
