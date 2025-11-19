package com.zeyuli.strategy;


import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.expection.SearchTypeNotAllowedExpectation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取账单的静态工厂类
 *
 * @author 李泽聿
 * @since 2025-11-18 09:03
 */
@Component
public class BillQueryStrategyFactory implements InitializingBean {
    @Autowired
    private ApplicationContext applicationContext;

    private static final Map<String,BillQueryStrategy> map = new ConcurrentHashMap<>();

    /**
     * 初始化方法，将所有实现了BillQueryStrategy接口的Bean注册到map中
     *
     * @author : 李泽聿
     * @since : 2025-11-18 09:14
     */
    @Override
    public void afterPropertiesSet() {
        Map<String,BillQueryStrategy> beans = applicationContext.getBeansOfType(BillQueryStrategy.class);
        for  (BillQueryStrategy strategy : beans.values()) {
            map.put(strategy.getSearchType(), strategy);
        }
    }

     /**
      * 根据查询方式获取对应的策略实现类
      *
      * @author : 李泽聿
      * @since : 2025-11-18 09:14
      * @param searchType 查询方式，见 {@link QueryBillListTypeEnum}
      * @return : com.zeyuli.strategy.BillQueryStrategy
      */
    public static BillQueryStrategy getBillQueryStrategy(String searchType) {
        BillQueryStrategy strategy = map.get(searchType);
        if (strategy == null) {
            throw new SearchTypeNotAllowedExpectation("查找方式不被允许");
        }
        return strategy;
    }
}
