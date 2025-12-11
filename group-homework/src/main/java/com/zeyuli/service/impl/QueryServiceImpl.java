package com.zeyuli.service.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.enm.RecordEnum;
import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.enm.UsageEnum;
import com.zeyuli.mappers.BillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import com.zeyuli.pojo.vo.GetReportVo;
import com.zeyuli.service.QueryService;
import com.zeyuli.strategy.BillQueryStrategy;
import com.zeyuli.strategy.BillQueryStrategyFactory;
import com.zeyuli.util.JwtUtil;
import com.zeyuli.util.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询接口实现
 *
 * @author 李泽聿
 * @since 2025-11-17 21:32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {
    private final BillMapper billMapper;
    private final JwtUtil jwtUtil;

    /**
     * 获取查询方式
     *
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 21:33
     */
    @Override
    public Map<String, Object> getSearchType() {
        Map<String, Object> res = new HashMap<>();
        for (QueryBillListTypeEnum queryBillListTypeEnum : QueryBillListTypeEnum.values()) {
            res.put(String.valueOf(queryBillListTypeEnum), queryBillListTypeEnum.getDescription());
        }
        return Response.success(StatusCodeEnum.SUCCESS, res);
    }

    /**
     * 查询账单列表,包含
     *
     * @param vo 包含了查询的日期范围，见 {@link GetBillListOrderBySpecificMethodVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-18 08:29
     */
    @Override
    public Map<String, Object> getBillList(GetBillListOrderBySpecificMethodVo vo, QueryBillListTypeEnum searchType) {
        BillQueryStrategy billQueryStrategy = BillQueryStrategyFactory.getBillQueryStrategy(searchType.name());
        List<GetBillListBo> getBillListBos = billQueryStrategy.queryBillList(vo);
        if (getBillListBos == null) {
            return Response.error(StatusCodeEnum.GET_DATA_FAILED, null);
        }
        if (getBillListBos.isEmpty()) {
            return Response.error(StatusCodeEnum.GET_DATA_FAILED, null);
        }

        return Response.success(StatusCodeEnum.SUCCESS, getBillListBos);
    }

    /**
     * 获取日期区间的统计报表
     *
     * @param vo 包含了查询的日期范围，见 {@link GetReportVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-12-09 10:14
     */
    @Override
    @CheckUserToken
    public Map<String, Object> getReport(GetReportVo vo) {
        String id = jwtUtil.getUserInfo(vo.getToken())[0];
        List<BillPo> userBillList = billMapper.getReport(vo.getStartDate(), vo.getEndDate(), id);
        // 总收入
        BigDecimal generalIncome = BigDecimal.ZERO;
        // 总支出
        BigDecimal generalExpenditure = BigDecimal.ZERO;
        List<Map<String, Object>> data = initReport();
        for (BillPo billPo : userBillList) {
            if (billPo.getRecordEnum() == RecordEnum.INCOME) {
                generalIncome = generalIncome.add(billPo.getAmount());
            }
            // todo 待完善
            if (billPo.getRecordEnum() == RecordEnum.EXPENDITURE) {
                generalExpenditure = generalExpenditure.add(billPo.getAmount());
            }
            setReport(data, billPo.getRecordEnum(), billPo.getAmount(), billPo.getType());
        }
        Map<String, Object> res = new HashMap<>();
        res.put("statusCode", 200);
        res.put("message", "成功");
        res.put("generalIncome", generalIncome);
        res.put("generalExpenditure", generalExpenditure);
        res.put("generalBalance", generalIncome.subtract(generalExpenditure));
        countGeneralBalance(data);
        res.put("data", data);
        return res;

    }

    /**
     * 设置报表数据（简化版）
     *
     * @param data   返回给前端的数据，确保Map中"income"和"expenditure"是BigDecimal类型
     * @param enm    收支类型
     * @param amount 金额
     * @param type   分类
     */
    public void setReport(List<Map<String, Object>> data, RecordEnum enm, BigDecimal amount, UsageEnum type) {
        for (Map<String, Object> billData : data) {
            // 使用 equals 比较字符串，避免空指针
            if (type.name().equals(billData.get("type"))) {
                if (enm == RecordEnum.INCOME) {
                    BigDecimal currentIncome = (BigDecimal) billData.getOrDefault("income", BigDecimal.ZERO);
                    billData.put("income", currentIncome.add(amount));
                    log.info("更新收入 - 类型: {}, 当前: {}, 增加: {}, 新值: {}",
                            type.name(), currentIncome, amount, currentIncome.add(amount));
                } else if (enm == RecordEnum.EXPENDITURE) {
                    BigDecimal currentExpenditure = (BigDecimal) billData.getOrDefault("expenditure", BigDecimal.ZERO);
                    billData.put("expenditure", currentExpenditure.add(amount));
                    log.info("更新支出 - 类型: {}, 当前: {}, 增加: {}, 新值: {}",
                            type.name(), currentExpenditure, amount, currentExpenditure.add(amount));
                }
                // 找到匹配项后可以提前退出循环
                break;
            }
        }
    }

    /**
     * 初始化报表数据
     *
     * @return : java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author : 李泽聿
     * @since : 2025-12-09 10:57
     */
    public List<Map<String, Object>> initReport() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (UsageEnum usageEnum : UsageEnum.values()) {
            Map<String, Object> billData = new HashMap<>();
            billData.put("type", usageEnum.name());
            billData.put("income", BigDecimal.ZERO);
            billData.put("expenditure", BigDecimal.ZERO);
            data.add(billData);
        }
        return data;
    }

    public void countGeneralBalance(List<Map<String, Object>> data) {
        for (Map<String, Object> billData : data) {
            BigDecimal income = (BigDecimal) billData.get("income");
            BigDecimal expenditure = (BigDecimal) billData.get("expenditure");
            billData.put("balance", income.subtract(expenditure));
        }
    }
}
