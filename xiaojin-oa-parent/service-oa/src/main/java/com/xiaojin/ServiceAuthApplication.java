package com.xiaojin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author xiaojin
 * @Date 2023/7/13 10:38
 */

@SpringBootApplication
@ComponentScan(basePackages = {"com.xiaojin","com.atguigu"})
//@MapperScan("com.xiaojin.*.mapper")
public class ServiceAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAuthApplication.class,args);
    }
}
