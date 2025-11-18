package com.zeyuli.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.zeyuli.pojo.vo.*;

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

    Map<String, Object> getBillDetail(UserOperateBillDetailVo vo);

    Map<String, Object> deleteBill(UserOperateBillDetailVo vo, int page, int limit) throws InterruptedException;

    Map<String, Object> getDeleteBillList(UserQueryBillVo vo, int page, int limit) throws JsonProcessingException;

    Map<String, Object> recoverBill(UserOperateBillDetailVo vo);

    Map<String, Object> modifyBill(ModifyBillVo vo, int page, int limit)throws InterruptedException;

    Map<String, Object> addBillList(AddBillListVo vo);

    Map<String, Object> getRecordType();
}
