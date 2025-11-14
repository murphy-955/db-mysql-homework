package com.zeyuli.test;

import com.zaxxer.hikari.HikariDataSource;
import com.zeyuli.mappers.AdminMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试数据库 链接
 *
 * @author 李泽聿
 * @since 2025-11-13 17:27
 */

@SpringBootTest
public class TestDbConnect {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private AdminMapper mapper;

    @Test
    public void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("数据库连接成功!");
            // 测试查询
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT current_user")) {
                if (rs.next()) {
                    System.out.println("当前用户: " + rs.getString(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleConnection() {
        try {
            // 手动创建数据源
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:opengauss://139.9.131.107:26000/mydb");
            dataSource.setUsername("zeyu");
            dataSource.setPassword("102301237lzy.");
            dataSource.setDriverClassName("org.opengauss.Driver");

            System.out.println("数据源配置:");
            System.out.println("URL: " + dataSource.getJdbcUrl());
            System.out.println("用户名: " + dataSource.getUsername());

            try (Connection conn = dataSource.getConnection()) {
                System.out.println("✅ 手动数据源连接成功!");
            }
        } catch (SQLException e) {
            System.err.println("❌ 手动数据源连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testSqlSentence() {
//        com.zeyuli.pojo.Test test = mapper.selectAll();
//        System.out.println(test);
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        System.out.println(date);
    }
}
