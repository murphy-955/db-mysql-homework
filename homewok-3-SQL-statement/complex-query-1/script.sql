# 第一关

# 给定一张tb_Salary表，如下所示，有 m = 男性 和 f = 女性的值。交换所有的 f 和 m 值（例如，将所有 f 值更改为 m，反之亦然）。
# 要求只使用一句更新update语句，且不允许含有任何select语句完成任务。
#请在此添加实现代码

########## Begin ##########
UPDATE tb_Salary
SET sex = IF(sex = 'm', 'f', IF(sex = 'f', 'm', sex));
########## End ##########

# ======================================================================================================================
# 第二关

#请在此添加实现代码
#     现在小美想改变相邻俩学生的座位（若学生人数为奇数，则无需改变最后一位同学的座位），现在需要你编写SQL输出小美想要的的结果。

########## Begin ##########
UPDATE tb_Seat s1
    JOIN tb_Seat s2 ON (
        (s1.id % 2 = 1 AND s2.id = s1.id + 1) OR
        (s1.id % 2 = 0 AND s2.id = s1.id - 1)
        )
SET s1.name = s2.name,
    s2.name = s1.name;

SELECT *
FROM tb_Seat;

########## End ##########

# ======================================================================================================================

# 第三关

#请在此添加实现代码

# 情况一：平分后的下一个名次是下一个连续的整数值。换句话说，名次之间不应该有“间隔”。例：1、1、2、3、4、4。

# 情况二：排名是非连续的。例：1、1、1、4、4、6。

########## Begin ##########
-- 情况一：连续的排名（没有间隔）
SELECT s1.score,
       (SELECT COUNT(DISTINCT s2.score)
        FROM score s2
        WHERE s2.score >= s1.score) as Rank
FROM score s1
ORDER BY Rank, s1.id;

-- 情况二：非连续的排名（有间隔）
SELECT Score,
       (SELECT COUNT(DISTINCT s2.Score) FROM score s2 WHERE s2.Score > s1.Score) + 1 as `Rank`
FROM score s1
ORDER BY `Rank`, Score DESC;

########## End ##########