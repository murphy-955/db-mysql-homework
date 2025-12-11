package com.zeyuli.schedule;


import com.zeyuli.enm.RecordEnum;
import com.zeyuli.mappers.BillMapper;
import com.zeyuli.mappers.UserMapper;
import com.zeyuli.pojo.po.UserAccountPo;
import com.zeyuli.pojo.po.UserPo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 兜底策略，每天凌晨2点计算账单余额
 *
 * @author 李泽聿
 * @since 2025-12-11 08:52
 */
@EnableAsync
@Component
@RequiredArgsConstructor
public class RecalculationBillSchedule {
    private final BillMapper billMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每天凌晨2点计算账单余额
     *
     * @author : 李泽聿
     * @since : 2025-12-11 08:56
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void recalculationBill() {
        List<UserPo> userList = userMapper.selectAllUsers();
        BigDecimal balance;
        for (UserPo user : userList) {
            List<UserAccountPo> accountList = userMapper.selectAccountInfo(user.getUserId());
            for (UserAccountPo account : accountList) {
                BigDecimal totalIncome = billMapper.selectTotalAmountByAccountAndType(
                        user.getUserId(),
                        account.getAccount(),
                        RecordEnum.INCOME.name()  // 收入类型
                );
                BigDecimal totalExpense = billMapper.selectTotalAmountByAccountAndType(
                        user.getUserId(),
                        account.getAccount(),
                        RecordEnum.EXPENDITURE.name()  // 支出类型
                );

                balance = totalIncome.subtract(totalExpense);
                account.setBalance(balance.toString());
                userMapper.modifyAccountBalance(user.getUserId(), balance);

                // 更新redis和本地缓存
                String key = "user:" + user.getUserId().substring(0, 16) + "account:" + account.getAccount();
                redisTemplate.opsForValue().set(key, balance);
            }
        }
    }
}
