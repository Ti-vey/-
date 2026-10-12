package com.warehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 仓库库存管理系统启动类。
 *
 * @author 毕业设计
 */
@SpringBootApplication
@MapperScan("com.warehouse.mapper")
public class WarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
        System.out.println("仓库库存管理系统后端启动成功：http://localhost:8080");
    }
}
