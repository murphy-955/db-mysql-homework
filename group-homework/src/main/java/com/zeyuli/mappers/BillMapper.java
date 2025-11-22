package com.zeyuli.mappers;


import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import com.zeyuli.pojo.vo.AddBillListVo;
import com.zeyuli.pojo.vo.AddBillVo;
import com.zeyuli.pojo.vo.ModifyBillVo;
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

    int deleteBill(@NotNull(message = "账单ID不能为空") Long id,
                   String userId);

    List<GetBillListBo> getDeleteBillList(@Param("page") int page,
                                          @Param("limit") int limit,
                                          @Param("userId") String userId);

    BillPo recoverBill(@Param("id") Long id,
                       @Param("userId") String userId);

    BillPo modifyBill(@Param("vo") ModifyBillVo vo,
                      @Param("userId") String userId);

    List<BillPo> addBillList(@Param("vo") AddBillListVo vo,
                       @Param("userId") String userId);

    int addAccountBalance(String userId,
                          @NotNull(message = "金额不能为空") Double amount);

    int modifyAccountBalance(String userId, @NotNull(message = "金额不能为空") Double amount);
}
