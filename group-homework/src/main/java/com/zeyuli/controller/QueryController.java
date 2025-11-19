package com.zeyuli.controller;


import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import com.zeyuli.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 查询控制层
 *
 * @author 李泽聿
 * @since 2025-11-17 21:15
 */
@RestController
@RequestMapping("api/query")
public class QueryController {
    @Autowired
    private QueryService queryService;

    @GetMapping("getSearchType")
    public Map<String, Object> getSearchType() {
        return queryService.getSearchType();
    }

    @PostMapping("getBillList")
    public Map<String, Object> getBillList(@RequestParam(value = "searchType",required = true,defaultValue = "UNDEFINED") QueryBillListTypeEnum searchType,
                                           @RequestBody GetBillListOrderBySpecificMethodVo vo) {
        return queryService.getBillList(vo, searchType);
    }
}

