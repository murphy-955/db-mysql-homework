# 第一关


# 在右侧编辑器补充代码，查询数据表中学生姓名以及对应的班级名称，将其对应的列名分别另命名为studentName和className。
USE School;

########## 查询数据表中学生姓名和对应的班级 ##########
#请在此处添加实现代码
########## Begin ##########
SELECT s.name AS studentName, c.name AS className
FROM tb_student s
         JOIN tb_class c ON s.class_id = c.id;


########## End ##########

# ======================================================================================================================

# 第二关

USE School;

########## 使用左外连接查询所有学生姓名和对应的班级 ##########

#请在此处添加实现代码
########## Begin ##########
SELECT s.name AS studentName, c.name AS className
FROM tb_student s
         LEFT JOIN tb_class c ON s.class_id = c.id;



########## End ##########

########## 使用右外连接查询所有学生姓名和对应的班级 ##########

#请在此处添加实现代码
########## Begin ##########
SELECT s.name AS studentName, c.name AS className
FROM tb_student s
         RIGHT JOIN tb_class c ON s.class_id = c.id;

########## End ##########

# ======================================================================================================================

# 第三关

USE School;

########## 查询所有班级里分数在90分以上的学生的姓名和学生的成绩以及学生所在的班级 ##########
#请在此处添加实现代码
########## Begin ##########
SELECT s.name AS studentName, s.score, c.name AS className
FROM tb_student s
         JOIN tb_class c ON s.class_id = c.id
WHERE score > 90;


########## End ##########


