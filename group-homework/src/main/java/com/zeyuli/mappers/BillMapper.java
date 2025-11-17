package com.zeyuli.mappers;


import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import com.zeyuli.pojo.vo.AddBillVo;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单业务层
 *
 * @author 李泽聿
 * @since 2025-11-15 14:36
 */
@Mapper
public interface BillMapper {
    BillPo addBill(@Param("vo") AddBillVo vo,
                   @Param("userId") String userId);

    List<GetBillListBo> getBillList(@Param("limit") int limit,
                                    @Param("page") int page,
                                    @Param("userId") String userId,
                                    @Param("lastId") Long lastId,
                                    @Param("lastDate") LocalDate lastDate
                                    );

    BillPo getBillDetail(@Param("id") Long id,
                         @Param("userId") String userId);
}
