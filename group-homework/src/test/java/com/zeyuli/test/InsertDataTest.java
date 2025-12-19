package com.zeyuli.test;


import com.zeyuli.enm.RecordEnum;
import com.zeyuli.enm.UsageEnum;
import com.zeyuli.mappers.UserMapper;
import com.zeyuli.pojo.po.UserPo;
import com.zeyuli.pojo.vo.AddBillListVo;
import com.zeyuli.pojo.vo.AddBillVo;
import com.zeyuli.service.BillService;
import com.zeyuli.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 批量插入数据测试类
 * 用于测试大规模数据插入性能
 *
 * @author 李泽聿
 * @since 2025-11-22 15:05
 */
@Slf4j
@SpringBootTest
public class InsertDataTest {
    @Autowired
    private BillService billService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final Random random = new Random();

    // 延迟初始化用户列表，避免NullPointerException
    private List<UserPo> userList;

    // 批处理大小，每批插入的记录数
    private static final int BATCH_SIZE = 2000;
    // 总插入记录数
    private static final int TOTAL_RECORDS = 5000000;

    @Test
    public void testInsertBatchData() {
        // 在测试方法开始时初始化用户列表，确保userMapper已完成注入
        userList = userMapper.selectAllUsers();
        log.info("开始插入{}条记录", TOTAL_RECORDS);
        long startTime = System.currentTimeMillis();

        // 分批处理，避免内存溢出
        for (int batch = 0; batch < TOTAL_RECORDS; batch += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, TOTAL_RECORDS - batch);
            log.info("开始处理第{}批，共{}条记录", (batch / BATCH_SIZE) + 1, currentBatchSize);

            long batchStartTime = System.currentTimeMillis();


            List<AddBillVo> billList = new ArrayList<>(currentBatchSize);
            // 处理当前批次
            for (int i = 0; i < currentBatchSize; i++) {
                // 从userList中随机选择一个用户
                UserPo user = userList.get(random.nextInt(userList.size()));
                String userId = user.getUserId();

                // 创建随机账单数据，关联到选定的用户
                AddBillVo addBill = createRandomBillVo(userId);

                String token = jwtUtil.createToken(userId, user.getUsername(), user.getPassword());
                addBill.setToken(token);
                // 执行插入操作
                billList.add(addBill);
            }
            AddBillListVo addBillListVo = new AddBillListVo();
            addBillListVo.setBillList(billList);
            if (!billList.isEmpty()) {
                addBillListVo.setToken(billList.getFirst().getToken());
            }

            // 执行批量插入操作
            billService.addBillList(addBillListVo);

            long batchEndTime = System.currentTimeMillis();
            long batchDuration = batchEndTime - batchStartTime;
            log.info("第{}批处理完成，耗时{}ms，平均每秒插入{}条",
                    (batch / BATCH_SIZE) + 1,
                    batchDuration,
                    (int) ((currentBatchSize * 1000.0) / batchDuration));
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        double seconds = totalDuration / 1000.0;

        log.info("所有{}条记录插入完成，总耗时{}秒，平均每秒插入{}条",
                TOTAL_RECORDS,
                String.format("%.2f", seconds),
                (int) (TOTAL_RECORDS / seconds));
    }

    /**
     * 创建随机的账单数据
     */
    private AddBillVo createRandomBillVo(String userId) {
        AddBillVo vo = new AddBillVo();

        // 设置用户ID
        vo.setUserId(userId);

        // 随机选择记录类型
        RecordEnum[] recordTypes = RecordEnum.values();
        vo.setRecordEnum(recordTypes[random.nextInt(recordTypes.length)]);

        // 根据记录类型生成合理的随机金额
        double amount;
        if (vo.getRecordEnum() == RecordEnum.INCOME) {
            // 收入金额范围更大 (100-50000)
            amount = 100 + random.nextDouble() * 49900;
        } else {
            // 支出金额范围 (0.01-10000)
            amount = 0.01 + random.nextDouble() * 9999.99;
        }
        vo.setAmount(Math.round(amount * 100) / 100.0); // 保留两位小数

        // 使用InsertUserAndUserAccountTest中定义的真实账户
        String[] accounts = {
                "123456789012345601", "123456789012345602", "123456789012345603",
                "123456789012345604", "123456789012345605", "123456789012345606",
                "123456789012345607", "123456789012345608"
        };

        // 为用户分配固定的账户集合，确保数据一致性
        // 每个用户有1-8个随机账户
        int userAccountCount = (Math.abs(userId.hashCode()) % 8) + 1;
        int accountIndex = Math.abs((userId.hashCode() + i) % userAccountCount);
        vo.setAccount(accounts[accountIndex]);

        // 递增计数器，确保同一用户的账户选择有变化
        i++;

        // 随机类型，但根据记录类型进行一些合理的关联
        UsageEnum[] usageTypes = UsageEnum.values();
        // 根据记录类型选择合理的用途类型
        if (vo.getRecordEnum() == RecordEnum.INCOME) {
            // 收入记录更适合使用特定类型
            UsageEnum[] incomeTypes = {UsageEnum.FINANCE, UsageEnum.OTHER}; // 假设这些类型适合收入
            vo.setType(incomeTypes[random.nextInt(incomeTypes.length)]);
        } else {
            // 支出记录可以使用所有类型
            vo.setType(usageTypes[random.nextInt(usageTypes.length)]);
        }

        // 生成过去一年内的随机日期
        LocalDate startDate = LocalDate.now().minus(1, ChronoUnit.YEARS);
        long daysBetween = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        LocalDate randomDate = startDate.plusDays(random.nextInt((int) daysBetween + 1));
        vo.setDate(randomDate);

        // 根据账单类型生成有语义的中文备注，增加用户信息使备注更真实
        if (random.nextBoolean()) {
            String randomRemarks = generateSemanticRemark(vo.getRecordEnum(), vo.getType(), vo.getAccount(), userId);
            vo.setRemarks(
                    Optional.ofNullable(randomRemarks)
                            .map(s -> s.length() > 50 ? s.substring(0, 50) : s)
                            .orElse(null)
            );
        }

        return vo;
    }

    // 计数器，用于确保同一用户的账户选择有变化
    private int i = 0;

    /**
     * 生成有语义的中文备注
     * 根据账单类型、用途、账户和用户生成相关的备注信息
     */
    private String generateSemanticRemark(RecordEnum recordEnum, UsageEnum usageEnum, String account, String userId) {
        // 获取账户对应的银行名称
        String bankName = getBankNameByAccount(account);

        // 支出类备注
        if (recordEnum == RecordEnum.EXPENDITURE) {
            return switch (usageEnum) {
                case FOOD ->
                        randomSelect("在超市购买食品", "外卖点餐", "餐厅用餐", "买菜做饭", "购买零食", "水果生鲜采购",
                                bankName + "支付午餐", bankName + "支付晚餐", bankName + "购买早餐");
                case SHOPPING -> randomSelect("服装鞋帽购物", "日用品采购", "电子产品配件", "网上商城购物",
                        "超市大采购", "生日礼物购买", bankName + "支付购物费用");
                case TRANSPORTATION -> randomSelect("地铁出行", "公交卡充值", "出租车费用", "网约车费用",
                        "共享单车骑行", "加油站加油", bankName + "支付交通费用");
                case HOUSING -> randomSelect("房租支付", "水电费缴纳", "物业费缴纳", "宽带费支付",
                        "燃气费缴纳", "生活用品采购", bankName + "支付住房费用");
                case HEALTH -> randomSelect("药店买药", "医院挂号", "体检费用", "保健品购买",
                        "健身卡续费", "医疗费用支付", bankName + "支付医疗费用");
                case FINANCE -> randomSelect("理财产品购买", "股票投资", "基金申购", "保险续费",
                        "银行手续费", "信用卡还款", bankName + "支付金融费用");
                case CLOTHING -> randomSelect("购买新衣服", "鞋子换新", "配饰购买", "季节服装采购",
                        "内衣袜子购买", "儿童衣物", bankName + "支付服装费用");
                case ELECTRONIC -> randomSelect("手机配件购买", "电脑周边", "家用电器", "数码产品",
                        "耳机音箱", "智能设备", bankName + "支付电子产品");
                case FURNITURE -> randomSelect("家具添置", "家居装饰", "床上用品", "厨房用品",
                        "收纳整理", "灯具照明", bankName + "支付家居费用");
                case HOUSEHOLD -> randomSelect("日常用品", "清洁用品", "纸巾采购", "洗涤用品",
                        "厨房调料", "卫生间用品", bankName + "支付家庭用品");
                default -> randomSelect("其他支出", "杂项费用", "意外开支", "临时开销",
                        "人情往来", "礼金支出", bankName + "支付其他费用");
            };
        }
        // 收入类备注
        else {
            return randomSelect(userId + "的工资收入", userId + "的奖金发放", userId + "的投资收益",
                    userId + "的兼职收入", "退款到账", "报销款", "礼金收入",
                    bankName + "转账收入", "副业收入", "理财收益", "红包收入",
                    bankName + "收款");
        }
    }

    /**
     * 从多个选项中随机选择一个
     */
    private String randomSelect(String... options) {
        return options[random.nextInt(options.length)];
    }

    /**
     * 根据账户号获取对应的银行名称
     */
    private String getBankNameByAccount(String account) {
        return switch (account) {
            case "123456789012345601" -> "中国建设银行";
            case "123456789012345602" -> "中国工商银行";
            case "123456789012345603" -> "中国农业银行";
            case "123456789012345604" -> "中国银行";
            case "123456789012345605" -> "公司账户";
            case "123456789012345606" -> "福州大学公务卡";
            case "123456789012345607" -> "业务卡";
            case "123456789012345608" -> "南京银行";
            default -> "未知账户";
        };
    }
}
