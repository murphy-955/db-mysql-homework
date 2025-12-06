package com.zeyuli.controller;

import com.zeyuli.pojo.vo.*;
import com.zeyuli.service.BillService;
import com.zeyuli.util.Response;
import com.zeyuli.enm.UsageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 账单控制层
 *
 * @author 李泽聿
 * @since 2025-11-14 14:23
 */
@RestController
@RequestMapping("api/bill")
public class BillController {
    @Autowired
    private BillService billService;

    @PostMapping("addBill")
    public Map<String, Object> addBill(@RequestBody AddBillVo vo) {
        return billService.addBill(vo);
    }

    @GetMapping("getRecordType")
    public Map<String, Object> getRecordType() {
        return billService.getRecordType();
    }

    @PostMapping("getBillList")
    public Map<String, Object> getBillList(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                           @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                           @RequestParam(value = "lastId", required = false, defaultValue = "0") Long lastId,
                                           @RequestParam(value = "lastDate", required = false) LocalDate lastDate,
                                           @RequestBody UserQueryBillVo vo) {
        if (lastDate == null) {
            lastDate = LocalDate.now();
        }
        return billService.getBillList(vo, page, limit, lastId, lastDate);
    }

    @PostMapping("getBillDetail")
    public Map<String, Object> getBillDetail(@RequestBody UserOperateBillDetailVo vo) {
        return billService.getBillDetail(vo);
    }

    @PostMapping("addBillList")
    public Map<String, Object> addBillList(@RequestBody AddBillListVo vo) {
        return billService.addBillList(vo);
    }

    @GetMapping("getTypeList")
    public Map<String, Object> getTypeList() {
        HashMap<String, Object> map = new HashMap<>();
        for (UsageEnum usageEnum : UsageEnum.values()) {
            map.put(usageEnum.name(), usageEnum.getDescription());
        }
        return Response.success(map);
    }
}
