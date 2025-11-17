package com.zeyuli.service;


import com.zeyuli.pojo.vo.AddBillVo;
import com.zeyuli.pojo.vo.UserGetBillDetailVo;
import com.zeyuli.pojo.vo.UserQueryBillVo;

import java.time.LocalDate;
import java.util.Map;

/**
 * 账单服务接口
 *
 * @author 李泽聿
 * @since 2025-11-14 14:25
 */

public interface BillService {
    Map<String, Object> addBill(AddBillVo vo);

    Map<String, Object> getBillList(UserQueryBillVo vo, int page, int limit, Long lastId, LocalDate lastDate);

    Map<String, Object> getBillDetail(UserGetBillDetailVo vo);
}
