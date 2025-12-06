package com.zeyuli.test;

import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.pojo.vo.InitAccountInfoVo;
import com.zeyuli.pojo.vo.UserRegisterVo;
import com.zeyuli.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 生成测试用户和账户的测试类
 *
 * @author 李泽聿
 * @since 2025-11-22 15:57
 */
@Slf4j
@SpringBootTest
public class InsertUserAndUserAccountTest {
    @Autowired
    private UserService userService;

    private static final String pwd = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    // 账户和对应描述的常量定义
    private static final Map<String, String> ACCOUNT_INFO_MAP = Map.of(
            "123456789012345601", "中国建设银行",
            "123456789012345602", "中国工商银行",
            "123456789012345603", "中国农业银行",
            "123456789012345604", "中国银行",
            "123456789012345605", "公司账户",
            "123456789012345606", "福州大学公务卡",
            "123456789012345607", "业务卡",
            "123456789012345608", "南京银行"
    );
    
    // 账户列表
    private final List<String> accounts = new ArrayList<>(ACCOUNT_INFO_MAP.keySet());

    /**
     * 构造函数确保账户列表已初始化
     */
    public InsertUserAndUserAccountTest() {
        // 账户列表已通过初始化器从ACCOUNT_INFO_MAP获取
        log.info("初始化测试类，加载了 {} 个账户信息", accounts.size());
    }

    /**
     * 生成user1-user100并随机分配1-8个账户
     */
    @Test
    public void testGenerateUsersAndAccounts() {
        log.info("开始生成用户和账户数据...");
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        
        // 生成user1到user100
        for (int i = 1; i <= 100; i++) {
            String username = "user" + i;
//            String userId = generateUserId();
            
            try {
                // 注册用户
                UserRegisterVo userRegisterVo = new UserRegisterVo();
                userRegisterVo.setUsername(username);
                userRegisterVo.setPassword(pwd);
                Map<String ,Object> registerResult = userService.register(userRegisterVo);
                
                if (registerResult.get("statusCode").equals(StatusCodeEnum.SUCCESS.getStatusCode())) {
                    // 为用户随机分配1-8个账户
                    Map<String, Object> userIdMap = (Map<String, Object>) registerResult.get("data");
                    String token = (String) userIdMap.get("token");
                    assignRandomAccountsToUser(token);
                    successCount++;
                    
                    // 每10个用户输出一次进度
                    if (i % 10 == 0) {
                        log.info("已生成 {} 个用户...", i);
                    }
                } else {
                    log.error("用户 {} 注册失败", username);
                }
            } catch (Exception e) {
                log.error("处理用户 {} 时发生异常: {}", username, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        log.info("数据生成完成！成功生成 {} 个用户，耗时 {} 毫秒", 
                successCount, (endTime - startTime));
    }
    
    /**
     * 生成用户ID
     * 使用当前日期和UUID组合生成唯一ID
     */
    private String generateUserId() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return date + uuid;
    }
    
    /**
     * 为用户随机分配1-8个账户
     */
    private void assignRandomAccountsToUser(String token) {
        Random random = new Random();
        // 随机选择1-8个账户
        int accountCount = random.nextInt(8) + 1;
        
        // 使用Collections.shuffle更高效地随机选择不重复账户
        List<String> shuffledAccounts = new ArrayList<>(accounts);
        Collections.shuffle(shuffledAccounts, random);
        Set<String> selectedAccounts = new HashSet<>(shuffledAccounts.subList(0, accountCount));
        
        // 为每个选中的账户初始化账户信息
        for (String account : selectedAccounts) {
            // 生成随机的初始余额（-10000到100000之间）
            double randomBalance = -10000 + (100000 - (-10000)) * random.nextDouble();
            // 保留两位小数
            randomBalance = Math.round(randomBalance * 100) / 100.0;
            
            String description = ACCOUNT_INFO_MAP.get(account);
            
            try {
                // 初始化账户信息
                InitAccountInfoVo initAccountInfoVo = new InitAccountInfoVo();

                initAccountInfoVo.setToken(token);
                initAccountInfoVo.setAccount(account);
                initAccountInfoVo.setBalance(randomBalance);
                initAccountInfoVo.setDescription(description);

                Map<String, Object> res = userService.initAccountInfo(initAccountInfoVo);


                if (!res.get("statusCode").equals(StatusCodeEnum.SUCCESS.getStatusCode())) {
                    log.warn("用户 {} 账户 {} 初始化失败", token, account);
                }
            } catch (Exception e) {
                log.error("初始化用户 {} 账户 {} 时发生异常: {}", token, account, e.getMessage());
            }
        }
    }
}
