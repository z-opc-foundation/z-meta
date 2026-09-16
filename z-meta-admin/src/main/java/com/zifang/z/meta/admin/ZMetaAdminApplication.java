package com.zifang.z.meta.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * z-meta 管理后台启动类
 */
@SpringBootApplication(scanBasePackages = {"com.zifang.z.meta"})
public class ZMetaAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZMetaAdminApplication.class, args);
    }
}